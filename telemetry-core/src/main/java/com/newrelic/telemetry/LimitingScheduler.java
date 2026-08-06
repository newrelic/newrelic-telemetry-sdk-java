package com.newrelic.telemetry;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A scheduler that delegates to a ScheduledExecutorService while also enforcing a maximum upper
 * bound of the amount of work that is being done. For our purposes, the amount of work corresponds
 * to the number of telemetry items "in-flight" (being buffered) in memory.
 *
 * <p>A call to schedule() will include a work unit "size" that is accumulated, and if the max would
 * be exceeded then the work unit is rejected and a warning is logged.
 *
 * <p>This scheduler optionally supports periodic flushing to reduce buffer pressure when configured
 * with a flush interval and threshold.
 */
public class LimitingScheduler {

  private static final Logger logger = LoggerFactory.getLogger(LimitingScheduler.class);
  private final ScheduledExecutorService executor;
  private final int max;
  private final Semaphore semaphore;
  private final AtomicLong flushThreshold;
  private final AtomicLong flushIntervalMs;
  private volatile Runnable flushCallback;
  private volatile ScheduledFuture<?> flushTask;
  private volatile ScheduledExecutorService flushExecutor;

  public LimitingScheduler(ScheduledExecutorService executor, int max) {
    this(executor, max, 0, 0);
  }

  /**
   * Creates a LimitingScheduler with optional periodic flushing capabilities.
   *
   * @param executor the underlying ScheduledExecutorService
   * @param max the maximum number of telemetry items to buffer
   * @param flushThreshold threshold of buffer usage (as percentage 0-100) to trigger flush, 0
   *     disables
   * @param flushIntervalMs interval in milliseconds for periodic flush checks, 0 disables
   */
  public LimitingScheduler(
      ScheduledExecutorService executor, int max, int flushThreshold, long flushIntervalMs) {
    this.executor = executor;
    this.max = max;
    this.semaphore = new Semaphore(max);
    this.flushThreshold = new AtomicLong(Math.max(0, Math.min(100, flushThreshold)));
    this.flushIntervalMs = new AtomicLong(Math.max(0, flushIntervalMs));

    if (this.flushIntervalMs.get() > 0 && this.flushThreshold.get() > 0) {
      startPeriodicFlush();
    }
  }

  public boolean schedule(int size, Runnable command) {
    return schedule(size, command, 0, TimeUnit.MILLISECONDS);
  }

  public boolean schedule(int size, Runnable command, long delay, TimeUnit unit) {
    if (!semaphore.tryAcquire(size)) {
      logger.warn(
          "Refusing to schedule batch of size "
              + size
              + " (would put us over max size "
              + max
              + ", available = "
              + semaphore.availablePermits()
              + ")");
      logger.warn("DATA IS BEING LOST!");
      return false;
    }
    try {
      executor.schedule(
          () -> {
            try {
              command.run();
            } finally {
              semaphore.release(size);
            }
          },
          delay,
          unit);
      return true;
    } catch (RejectedExecutionException e) {
      logger.warn("Data is being lost, job could not be scheduled", e);
      semaphore.release(size);
      return false;
    }
  }

  public boolean isTerminated() {
    return executor.isTerminated();
  }

  public void shutdown() {
    stopPeriodicFlush();
    executor.shutdown();
  }

  public boolean awaitTermination(int shutdownSeconds, TimeUnit seconds)
      throws InterruptedException {
    return executor.awaitTermination(shutdownSeconds, seconds);
  }

  public void shutdownNow() {
    stopPeriodicFlush();
    executor.shutdownNow();
  }

  /**
   * Configures the flush callback that will be invoked when buffer usage exceeds the threshold.
   *
   * @param flushCallback the callback to invoke for flushing
   */
  public void setFlushCallback(Runnable flushCallback) {
    this.flushCallback = flushCallback;
  }

  /**
   * Updates the flush configuration. If both parameters are > 0, periodic flushing will be enabled.
   *
   * @param flushThreshold threshold of buffer usage (as percentage 0-100) to trigger flush, 0
   *     disables
   * @param flushIntervalMs interval in milliseconds for periodic flush checks, 0 disables
   */
  public void configureFlush(int flushThreshold, long flushIntervalMs) {
    this.flushThreshold.set(Math.max(0, Math.min(100, flushThreshold)));
    this.flushIntervalMs.set(Math.max(0, flushIntervalMs));

    stopPeriodicFlush();
    if (this.flushIntervalMs.get() > 0 && this.flushThreshold.get() > 0) {
      startPeriodicFlush();
    }
  }

  /**
   * Gets the current buffer usage percentage (0-100).
   *
   * @return buffer usage as percentage
   */
  public int getBufferUsagePercent() {
    return (int) ((double) (max - semaphore.availablePermits()) / max * 100);
  }

  /**
   * Gets the number of available permits in the buffer.
   *
   * @return available buffer capacity
   */
  public int getAvailableCapacity() {
    return semaphore.availablePermits();
  }

  private void startPeriodicFlush() {
    if (flushTask != null) {
      flushTask.cancel(false);
    }

    if (flushExecutor == null) {
      flushExecutor =
          Executors.newSingleThreadScheduledExecutor(
              r -> {
                Thread t = new Thread(r, "telemetry-flush-checker");
                t.setDaemon(true);
                return t;
              });
    }

    flushTask =
        flushExecutor.scheduleAtFixedRate(
            this::checkAndFlush,
            flushIntervalMs.get(),
            flushIntervalMs.get(),
            TimeUnit.MILLISECONDS);
  }

  private void stopPeriodicFlush() {
    if (flushTask != null) {
      flushTask.cancel(false);
      flushTask = null;
    }
    if (flushExecutor != null) {
      flushExecutor.shutdown();
      try {
        if (!flushExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
          flushExecutor.shutdownNow();
        }
      } catch (InterruptedException e) {
        flushExecutor.shutdownNow();
        Thread.currentThread().interrupt();
      }
      flushExecutor = null;
    }
  }

  private void checkAndFlush() {
    try {
      int usagePercent = getBufferUsagePercent();
      long threshold = flushThreshold.get();

      if (threshold > 0 && usagePercent >= threshold && flushCallback != null) {
        logger.debug(
            "Buffer usage at {}%, triggering flush (threshold: {}%)", usagePercent, threshold);
        flushCallback.run();
      }
    } catch (Exception e) {
      logger.warn("Error during periodic flush check", e);
    }
  }
}

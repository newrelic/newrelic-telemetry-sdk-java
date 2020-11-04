package com.newrelic.telemetry;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A scheduler that delegates to a ScheduledExecutorService while also enforcing a maximum upper
 * bound of the amount of work that is being done. For our purposes, the amount of work corresponds
 * to the number of telemetry items "in-flight" (being buffered) in memory.
 *
 * <p>A call to schedule() will include a work unit "size" that is accumulated, and if the max would
 * be exceeded then the work unit is rejected and a warning is logged.
 */
public class LimitingScheduler {

  private static final Logger logger = LoggerFactory.getLogger(LimitingScheduler.class);
  private final ScheduledExecutorService executor;
  private final int max;
  private final Semaphore semaphore;

  public LimitingScheduler(ScheduledExecutorService executor, int max) {
    this.executor = executor;
    this.max = max;
    this.semaphore = new Semaphore(max);
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
    executor.shutdown();
  }

  public boolean awaitTermination(int shutdownSeconds, TimeUnit seconds)
      throws InterruptedException {
    return executor.awaitTermination(shutdownSeconds, seconds);
  }

  public void shutdownNow() {
    executor.shutdownNow();
  }
}

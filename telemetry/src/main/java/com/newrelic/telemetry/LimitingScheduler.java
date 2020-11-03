package com.newrelic.telemetry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A scheduler that delegates to a ScheduledExecutorService while also enforcing
 * a maximum upper bound of the amount of work that is being done.
 * Each call to schedule() will include a work unit "size" that is accumulated
 * on each schedule and decremented when the job is finished.
 */
public class LimitingScheduler {

    private final static Logger logger = LoggerFactory.getLogger(LimitingScheduler.class);
    private final ScheduledExecutorService executor;
    Semaphore s = new Semaphore(1);
    private final int max;
    private final AtomicInteger current = new AtomicInteger(0);

    public LimitingScheduler(ScheduledExecutorService executor, int max) {
        this.executor = executor;
        this.max = max;
    }

    public boolean schedule(int size, Runnable command) {
        return schedule(size, command, 0, TimeUnit.MILLISECONDS);
    }

    public boolean schedule(int size, Runnable command, long delay, TimeUnit unit) {
        if (wouldExceedMax(size)) {
            return false;
        }
        executor.schedule(() -> {
            try {
                command.run();
            } finally {
                current.addAndGet(size * -1);
            }
        }, delay, unit);
        return true;
    }

    private boolean wouldExceedMax(int size) throws InterruptedException {
        // In the event that we do exceed the max, we don't want to prevent a separate thread
        // from potentially adding a smaller, acceptable batch, so we hold a semaphore very
        s.acquire();
        try{
            int newSize = current.addAndGet(size);
            if (newSize > max) {
                current.addAndGet(size * -1);
                logger.warn("Refusing to schedule batch of size " + size + " (would put us over max size)");
                logger.warn("DATA IS BEING LOST!");
                return true;
            }
        }
        finally {
            s.release();
        }
        return false;
    }

    public boolean isTerminated() {
        return executor.isTerminated();
    }

    public void shutdown() {
        executor.shutdown();
    }

    public boolean awaitTermination(int shutdownSeconds, TimeUnit seconds) throws InterruptedException {
        return executor.awaitTermination(shutdownSeconds, seconds);
    }

    public void shutdownNow() {
        executor.shutdownNow();
    }
}

package com.newrelic.telemetry;

public class Backoff {

    private final long maxBackoffTimeMs;
    private final long backoffFactorMs;
    private final int maxRetries;
    private int numRetries = 0;

    public Backoff(long maxBackoffTimeMs, long backoffFactorMs, int maxRetries) {
        this.maxBackoffTimeMs = maxBackoffTimeMs;
        this.backoffFactorMs = backoffFactorMs;
        this.maxRetries = maxRetries;
    }

    public static Backoff defaultBackoff() {
        return new Backoff(15000, 1000, 10);
    }

    /**
     * Computes the next wait time and does NOT actually sleep.  This increases
     * the tracked number of retries;
     *
     * @return the next amount of time to wait, or -1 if max retries exceeded.
     */
    public long nextWaitMs() {
        numRetries++;
        int n = numRetries - 1;

        if(n == 0){
            return 0;
        }
        if(n >= maxRetries){
            return -1;
        }
        return (long) Math.min(maxBackoffTimeMs, backoffFactorMs * Math.pow(2, n - 1));
    }

}

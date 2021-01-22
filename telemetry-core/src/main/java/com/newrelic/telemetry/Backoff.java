package com.newrelic.telemetry;

import java.util.concurrent.TimeUnit;

public class Backoff {

  private final long maxBackoffTimeMs;
  private final long backoffFactorMs;
  private final int maxRetries;
  private int numRetries = 0;

  private Backoff(Builder builder) {
    this.maxBackoffTimeMs = builder.maxBackoffTimeMs;
    this.backoffFactorMs = builder.backoffFactorMs;
    this.maxRetries = builder.maxRetries;
  }

  public static Backoff defaultBackoff() {
    return Backoff.builder()
        .maxBackoff(15, TimeUnit.SECONDS)
        .backoffFactor(1, TimeUnit.SECONDS)
        .maxRetries(10)
        .build();
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Computes the next wait time and does NOT actually sleep. This increases the tracked number of
   * retries;
   *
   * @return the next amount of time to wait, or -1 if max retries exceeded.
   */
  public long nextWaitMs() {
    numRetries++;
    int n = numRetries - 1;

    if (n == 0) {
      return 0;
    }
    if (n >= maxRetries) {
      return -1;
    }
    return (long) Math.min(maxBackoffTimeMs, backoffFactorMs * Math.pow(2, n - 1));
  }

  public static class Builder {

    private long maxBackoffTimeMs;
    private long backoffFactorMs;
    private int maxRetries;

    /**
     * The max time between retries
     *
     * @param backoff backoff time value
     * @param unit time to be used, e.g. SECONDS
     * @return Builder instance
     */
    public Builder maxBackoff(int backoff, TimeUnit unit) {
      this.maxBackoffTimeMs = unit.toMillis(backoff);
      return this;
    }

    /**
     * The base amount of time to start doubling from when backing off.
     *
     * @param backoffFactor backoff time value, to be doubled
     * @param unit time to be used, e.g. SECONDS
     * @return Builder instance
     */
    public Builder backoffFactor(int backoffFactor, TimeUnit unit) {
      this.backoffFactorMs = unit.toMillis(backoffFactor);
      return this;
    }

    /**
     * Define max of retries
     *
     * @param maxRetries max retries
     * @return Builder instance
     */
    public Builder maxRetries(int maxRetries) {
      this.maxRetries = maxRetries;
      return this;
    }

    /**
     * Create a Backoff instance
     *
     * @return a new instance
     */
    public Backoff build() {
      return new Backoff(this);
    }
  }
}

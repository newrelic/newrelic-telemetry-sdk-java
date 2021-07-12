/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.telemetry.logs;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Telemetry;
import com.newrelic.telemetry.util.IngestWarnings;
import com.newrelic.telemetry.util.Utils;
import java.util.Map;

/** A Log instance represents a single entry in a log. */
public class Log implements Telemetry {
  private final long timestamp; // in epoch ms
  private final String message;
  private final Attributes attributes;
  private final String serviceName; // service.name <- goes in attributes
  private final String level;
  private final Throwable throwable;

  private Log(LogBuilder builder, Throwable throwable) {
    this.timestamp = builder.timestamp;
    this.message = builder.message;
    this.attributes = builder.attributes;
    this.serviceName = builder.serviceName;
    this.level = builder.level;
    this.throwable = throwable;
  }

  /** @return The point in time (ms since UNIX epoch) that the log entry was created. */
  public long getTimestamp() {
    return timestamp;
  }

  /** @return The log line itself */
  public String getMessage() {
    return message;
  }

  /** @return Additional attributes associated with the log entry. */
  public Attributes getAttributes() {
    return attributes;
  }

  /** @return The name of the service which produced this log entry. */
  public String getServiceName() {
    return serviceName;
  }

  /** @return The log level (eg. INFO, DEBUG, etc) for the log entry. */
  public String getLevel() {
    return level;
  }

  /** @return Create a builder for building a new log entry. */
  public static LogBuilder builder() {
    return new LogBuilder();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Log)) {
      return false;
    }

    Log log = (Log) o;

    if (timestamp != log.timestamp) {
      return false;
    }
    if (message != null ? !message.equals(log.message) : log.message != null) {
      return false;
    }
    if (attributes != null ? !attributes.equals(log.attributes) : log.attributes != null) {
      return false;
    }
    if (serviceName != null ? !serviceName.equals(log.serviceName) : log.serviceName != null) {
      return false;
    }
    if (level != null ? !level.equals(log.level) : log.level != null) {
      return false;
    }
    return throwable != null ? throwable.equals(log.throwable) : log.throwable == null;
  }

  @Override
  public int hashCode() {
    int result = (int) (timestamp ^ (timestamp >>> 32));
    result = 31 * result + (message != null ? message.hashCode() : 0);
    result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
    result = 31 * result + (serviceName != null ? serviceName.hashCode() : 0);
    result = 31 * result + (level != null ? level.hashCode() : 0);
    result = 31 * result + (throwable != null ? throwable.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Log{"
        + "timestamp="
        + timestamp
        + ", message='"
        + message
        + '\''
        + ", attributes="
        + attributes
        + ", serviceName='"
        + serviceName
        + '\''
        + ", level='"
        + level
        + '\''
        + ", throwable="
        + throwable
        + '}';
  }

  public Throwable getThrowable() {
    return throwable;
  }

  /**
   * A class for holding the variables associated with a Log object and creating a new Log object
   * with those variables.
   */
  public static class LogBuilder {
    private long timestamp = System.currentTimeMillis();
    private String message;
    private Attributes attributes = new Attributes();
    private IngestWarnings ingestWarnings = new IngestWarnings();
    private String serviceName; // service.name <- goes in attributes
    private String level;
    private Throwable throwable;

    /**
     * @param timestamp The point in time (ms since UNIX epoch) that the log entry was created.
     * @return log builder instance
     */
    public LogBuilder timestamp(long timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    /**
     * @param message The log line itself.
     * @return log builder instance
     */
    public LogBuilder message(String message) {
      this.message = message;
      return this;
    }

    /**
     * @param attributes Additional attributes associated with the log entry.
     * @return log builder instance
     */
    public LogBuilder attributes(Attributes attributes) {
      Map<String, Object> attrs = attributes.asMap();
      ingestWarnings.raiseIngestWarnings(attrs, "Log");

      this.attributes = attributes;
      return this;
    }

    /**
     * @param serviceName The name of the service which produced this log entry.
     * @return log builder instance
     */
    public LogBuilder serviceName(String serviceName) {
      this.serviceName = serviceName;
      return this;
    }

    /**
     * @param logLevel The log level (eg. INFO, DEBUG, etc) for the log entry.
     * @return log builder instance
     */
    public LogBuilder level(String logLevel) {
      this.level = logLevel;
      return this;
    }

    /** @return Create the new {@link Log} entry. */
    public Log build() {

      return new Log(this, throwable);
    }

    /**
     * @param e Will assign a throwable to the log entry.
     * @return log builder instance
     */
    public LogBuilder throwable(Throwable e) {
      Utils.verifyNonNull(e);
      this.throwable = e;
      return this;
    }

    @Override
    public String toString() {
      return "LogBuilder{"
          + "timestamp="
          + timestamp
          + ", message='"
          + message
          + '\''
          + ", attributes="
          + attributes
          + ", serviceName='"
          + serviceName
          + '\''
          + ", level='"
          + level
          + '\''
          + ", throwable="
          + throwable
          + '}';
    }
  }
}

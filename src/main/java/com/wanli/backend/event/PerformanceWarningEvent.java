package com.wanli.backend.event;

public class PerformanceWarningEvent extends BaseEvent {
  private String operation;
  private long executionTime;
  private long threshold;

  public PerformanceWarningEvent(String operation, long executionTime, long threshold) {
    super();
    this.operation = operation;
    this.executionTime = executionTime;
    this.threshold = threshold;
  }

  public String getOperation() {
    return operation;
  }

  public long getExecutionTime() {
    return executionTime;
  }

  public long getThreshold() {
    return threshold;
  }
}

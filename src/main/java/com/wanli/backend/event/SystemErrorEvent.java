package com.wanli.backend.event;

public class SystemErrorEvent extends BaseEvent {
  private String errorType;
  private String errorMessage;
  private Exception exception;

  public SystemErrorEvent(String errorType, String errorMessage, Exception exception) {
    super();
    this.errorType = errorType;
    this.errorMessage = errorMessage;
    this.exception = exception;
  }

  public String getErrorType() {
    return errorType;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public Exception getException() {
    return exception;
  }
}

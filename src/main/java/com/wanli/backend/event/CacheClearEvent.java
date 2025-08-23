package com.wanli.backend.event;

public class CacheClearEvent extends BaseEvent {
  private String cacheType;
  private String cacheKey;
  private String reason;
  private String triggeredBy;

  public CacheClearEvent(String cacheType, String cacheKey, String reason, String triggeredBy) {
    super();
    this.cacheType = cacheType;
    this.cacheKey = cacheKey;
    this.reason = reason;
    this.triggeredBy = triggeredBy;
  }

  public String getCacheType() {
    return cacheType;
  }

  public String getCacheKey() {
    return cacheKey;
  }

  public String getReason() {
    return reason;
  }

  public String getTriggeredBy() {
    return triggeredBy;
  }
}

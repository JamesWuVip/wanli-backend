package com.wanli.backend.event;

import java.util.UUID;

public class SecurityEvent extends BaseEvent {
  private String eventType;
  private UUID userId;
  private String details;
  private String ipAddress;

  public SecurityEvent(String eventType, UUID userId, String details, String ipAddress) {
    super();
    this.eventType = eventType;
    this.userId = userId;
    this.details = details;
    this.ipAddress = ipAddress;
  }

  public String getEventType() {
    return eventType;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getDetails() {
    return details;
  }

  public String getIpAddress() {
    return ipAddress;
  }
}

package com.wanli.backend.event;

import java.time.LocalDateTime;
import java.util.UUID;

/** 基础事件类 所有业务事件的基类 */
public abstract class BaseEvent {

  private UUID eventId;
  private LocalDateTime timestamp;
  private String eventType;
  private UUID sourceId;
  private String source;

  protected BaseEvent() {
    this.eventType = this.getClass().getSimpleName();
    this.timestamp = LocalDateTime.now();
    this.eventId = UUID.randomUUID();
  }

  protected BaseEvent(UUID sourceId, String source) {
    this();
    this.sourceId = sourceId;
    this.source = source;
  }

  // Getters and Setters
  public UUID getEventId() {
    return eventId;
  }

  public void setEventId(UUID eventId) {
    this.eventId = eventId;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public UUID getSourceId() {
    return sourceId;
  }

  public void setSourceId(UUID sourceId) {
    this.sourceId = sourceId;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  @Override
  public String toString() {
    return String.format(
        "%s{eventId=%s, timestamp=%s, sourceId=%s, source='%s'}",
        eventType, eventId, timestamp, sourceId, source);
  }
}

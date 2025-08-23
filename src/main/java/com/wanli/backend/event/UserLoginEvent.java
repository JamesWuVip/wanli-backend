package com.wanli.backend.event;

import java.util.UUID;

/** 用户登录事件 */
public class UserLoginEvent extends BaseEvent {
  private final UUID userId;
  private final String username;
  private final String ipAddress;

  public UserLoginEvent(UUID userId, String username, String ipAddress) {
    super(userId, "AuthService");
    this.userId = userId;
    this.username = username;
    this.ipAddress = ipAddress;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getUsername() {
    return username;
  }

  public String getIpAddress() {
    return ipAddress;
  }
}

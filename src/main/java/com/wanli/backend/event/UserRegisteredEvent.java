package com.wanli.backend.event;

import java.util.UUID;

/** 用户注册事件 */
public class UserRegisteredEvent extends BaseEvent {
  private final UUID userId;
  private final String username;
  private final String email;

  public UserRegisteredEvent(UUID userId, String username, String email) {
    super(userId, "UserService");
    this.userId = userId;
    this.username = username;
    this.email = email;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getUsername() {
    return username;
  }

  public String getEmail() {
    return email;
  }
}

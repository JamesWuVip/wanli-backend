package com.wanli.backend.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;

/** 用户实体类 对应数据库设计文档中的users表 */
@Entity
@Table(name = "users", schema = "staging")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "UUID")
  private UUID id;

  @Column(name = "franchise_id", columnDefinition = "UUID")
  private UUID franchiseId;

  @Column(name = "username", length = 50, nullable = false, unique = true)
  private String username;

  @Column(name = "password", length = 255, nullable = false)
  private String password;

  @Column(name = "email", length = 100, nullable = false, unique = true)
  private String email;

  @Column(name = "role", length = 50, nullable = false)
  private String role;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  // 默认构造函数
  public User() {}

  // 构造函数
  public User(String username, String password, String email, String role) {
    this.username = username;
    this.password = password;
    this.email = email;
    this.role = role;
  }

  // Getters and Setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getFranchiseId() {
    return franchiseId;
  }

  public void setFranchiseId(UUID franchiseId) {
    this.franchiseId = franchiseId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public LocalDateTime getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(LocalDateTime deletedAt) {
    this.deletedAt = deletedAt;
  }

  // 便利方法：检查是否已删除
  public boolean isDeleted() {
    return deletedAt != null;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    User user = (User) obj;
    return id != null && id.equals(user.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "User{"
        + "id="
        + id
        + ", franchiseId="
        + franchiseId
        + ", username='"
        + username
        + "'"
        + ", email='"
        + email
        + "'"
        + ", role='"
        + role
        + "'"
        + ", createdAt="
        + createdAt
        + '}';
  }
}

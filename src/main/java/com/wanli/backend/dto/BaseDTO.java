package com.wanli.backend.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

/** 数据传输对象基类 提供通用的DTO字段和功能 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseDTO implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 实体ID */
  private UUID id;

  /** 创建时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;

  /** 更新时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updatedAt;

  /** 创建者ID */
  private UUID createdBy;

  /** 更新者ID */
  private UUID updatedBy;

  /** 版本号（用于乐观锁） */
  private Long version;

  /** 是否已删除 */
  private Boolean deleted;

  // 构造函数
  protected BaseDTO() {}

  protected BaseDTO(UUID id) {
    this.id = id;
  }

  // Getters and Setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
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

  public UUID getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(UUID createdBy) {
    this.createdBy = createdBy;
  }

  public UUID getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(UUID updatedBy) {
    this.updatedBy = updatedBy;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public Boolean getDeleted() {
    return deleted;
  }

  public void setDeleted(Boolean deleted) {
    this.deleted = deleted;
  }

  /** 检查是否为新实体（ID为空） */
  public boolean isNew() {
    return this.id == null;
  }

  /** 检查是否已删除 */
  public boolean isDeleted() {
    return Boolean.TRUE.equals(this.deleted);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    BaseDTO baseDTO = (BaseDTO) obj;
    return id != null && id.equals(baseDTO.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "{"
        + "id="
        + id
        + ", createdAt="
        + createdAt
        + ", updatedAt="
        + updatedAt
        + ", version="
        + version
        + ", deleted="
        + deleted
        + '}';
  }
}

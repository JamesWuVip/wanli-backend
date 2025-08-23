package com.wanli.backend.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** 查询条件基类 提供通用的查询参数和分页功能 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseQueryDTO implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 页码（从1开始） */
  @Min(value = 1, message = "页码必须大于0")
  private Integer page = 1;

  /** 每页大小 */
  @Min(value = 1, message = "每页大小必须大于0")
  @Max(value = 100, message = "每页大小不能超过100")
  private Integer size = 10;

  /** 排序字段 */
  private String sortBy;

  /** 排序方向（ASC/DESC） */
  private String sortDirection = "DESC";

  /** 关键词搜索 */
  private String keyword;

  /** 创建时间范围 - 开始时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAtStart;

  /** 创建时间范围 - 结束时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAtEnd;

  /** 更新时间范围 - 开始时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updatedAtStart;

  /** 更新时间范围 - 结束时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updatedAtEnd;

  /** 创建者ID列表 */
  private List<UUID> createdByIds;

  /** 是否包含已删除的记录 */
  private Boolean includeDeleted = false;

  /** 是否只查询已删除的记录 */
  private Boolean onlyDeleted = false;

  // 构造函数
  protected BaseQueryDTO() {}

  // Getters and Setters
  public Integer getPage() {
    return page;
  }

  public void setPage(Integer page) {
    this.page = page != null && page > 0 ? page : 1;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size != null && size > 0 && size <= 100 ? size : 10;
  }

  public String getSortBy() {
    return sortBy;
  }

  public void setSortBy(String sortBy) {
    this.sortBy = sortBy;
  }

  public String getSortDirection() {
    return sortDirection;
  }

  public void setSortDirection(String sortDirection) {
    this.sortDirection =
        sortDirection != null
                && ("ASC".equalsIgnoreCase(sortDirection) || "DESC".equalsIgnoreCase(sortDirection))
            ? sortDirection.toUpperCase()
            : "DESC";
  }

  public String getKeyword() {
    return keyword;
  }

  public void setKeyword(String keyword) {
    this.keyword = keyword != null ? keyword.trim() : null;
  }

  public LocalDateTime getCreatedAtStart() {
    return createdAtStart;
  }

  public void setCreatedAtStart(LocalDateTime createdAtStart) {
    this.createdAtStart = createdAtStart;
  }

  public LocalDateTime getCreatedAtEnd() {
    return createdAtEnd;
  }

  public void setCreatedAtEnd(LocalDateTime createdAtEnd) {
    this.createdAtEnd = createdAtEnd;
  }

  public LocalDateTime getUpdatedAtStart() {
    return updatedAtStart;
  }

  public void setUpdatedAtStart(LocalDateTime updatedAtStart) {
    this.updatedAtStart = updatedAtStart;
  }

  public LocalDateTime getUpdatedAtEnd() {
    return updatedAtEnd;
  }

  public void setUpdatedAtEnd(LocalDateTime updatedAtEnd) {
    this.updatedAtEnd = updatedAtEnd;
  }

  public List<UUID> getCreatedByIds() {
    return createdByIds;
  }

  public void setCreatedByIds(List<UUID> createdByIds) {
    this.createdByIds = createdByIds;
  }

  public Boolean getIncludeDeleted() {
    return includeDeleted;
  }

  public void setIncludeDeleted(Boolean includeDeleted) {
    this.includeDeleted = includeDeleted != null ? includeDeleted : false;
  }

  public Boolean getOnlyDeleted() {
    return onlyDeleted;
  }

  public void setOnlyDeleted(Boolean onlyDeleted) {
    this.onlyDeleted = onlyDeleted != null ? onlyDeleted : false;
  }

  /** 获取偏移量（用于数据库查询） */
  public int getOffset() {
    return (page - 1) * size;
  }

  /** 检查是否有关键词搜索 */
  public boolean hasKeyword() {
    return keyword != null && !keyword.trim().isEmpty();
  }

  /** 检查是否有创建时间范围查询 */
  public boolean hasCreatedAtRange() {
    return createdAtStart != null || createdAtEnd != null;
  }

  /** 检查是否有更新时间范围查询 */
  public boolean hasUpdatedAtRange() {
    return updatedAtStart != null || updatedAtEnd != null;
  }

  /** 检查是否有创建者过滤 */
  public boolean hasCreatedByFilter() {
    return createdByIds != null && !createdByIds.isEmpty();
  }

  /** 检查是否需要排序 */
  public boolean hasSorting() {
    return sortBy != null && !sortBy.trim().isEmpty();
  }

  /** 验证查询参数 */
  public void validate() {
    // 验证时间范围
    if (createdAtStart != null && createdAtEnd != null && createdAtStart.isAfter(createdAtEnd)) {
      throw new IllegalArgumentException("创建时间开始时间不能晚于结束时间");
    }

    if (updatedAtStart != null && updatedAtEnd != null && updatedAtStart.isAfter(updatedAtEnd)) {
      throw new IllegalArgumentException("更新时间开始时间不能晚于结束时间");
    }

    // 验证删除状态参数
    if (Boolean.TRUE.equals(includeDeleted) && Boolean.TRUE.equals(onlyDeleted)) {
      throw new IllegalArgumentException("不能同时设置includeDeleted和onlyDeleted为true");
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "{"
        + "page="
        + page
        + ", size="
        + size
        + ", sortBy='"
        + sortBy
        + '\''
        + ", sortDirection='"
        + sortDirection
        + '\''
        + ", keyword='"
        + keyword
        + '\''
        + ", includeDeleted="
        + includeDeleted
        + ", onlyDeleted="
        + onlyDeleted
        + '}';
  }
}

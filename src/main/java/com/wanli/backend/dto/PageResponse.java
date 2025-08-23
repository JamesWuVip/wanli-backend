package com.wanli.backend.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 分页响应包装类 提供统一的分页数据返回格式
 *
 * @param <T> 数据类型
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 数据列表 */
  private List<T> content;

  /** 当前页码（从1开始） */
  private Integer page;

  /** 每页大小 */
  private Integer size;

  /** 总记录数 */
  private Long totalElements;

  /** 总页数 */
  private Integer totalPages;

  /** 是否为第一页 */
  private Boolean first;

  /** 是否为最后一页 */
  private Boolean last;

  /** 是否有下一页 */
  private Boolean hasNext;

  /** 是否有上一页 */
  private Boolean hasPrevious;

  /** 当前页的记录数 */
  private Integer numberOfElements;

  /** 是否为空页 */
  private Boolean empty;

  /** 排序信息 */
  private SortInfo sort;

  // 构造函数
  public PageResponse() {}

  public PageResponse(List<T> content, Integer page, Integer size, Long totalElements) {
    this.content = content;
    this.page = page;
    this.size = size;
    this.totalElements = totalElements;
    this.calculateDerivedFields();
  }

  /**
   * 创建分页响应
   *
   * @param content 数据列表
   * @param page 当前页码
   * @param size 每页大小
   * @param totalElements 总记录数
   * @param <T> 数据类型
   * @return 分页响应对象
   */
  public static <T> PageResponse<T> of(
      List<T> content, Integer page, Integer size, Long totalElements) {
    return new PageResponse<>(content, page, size, totalElements);
  }

  /**
   * 创建分页响应（带排序信息）
   *
   * @param content 数据列表
   * @param page 当前页码
   * @param size 每页大小
   * @param totalElements 总记录数
   * @param sortBy 排序字段
   * @param sortDirection 排序方向
   * @param <T> 数据类型
   * @return 分页响应对象
   */
  public static <T> PageResponse<T> of(
      List<T> content,
      Integer page,
      Integer size,
      Long totalElements,
      String sortBy,
      String sortDirection) {
    PageResponse<T> response = new PageResponse<>(content, page, size, totalElements);
    response.setSort(new SortInfo(sortBy, sortDirection));
    return response;
  }

  /**
   * 创建空的分页响应
   *
   * @param page 当前页码
   * @param size 每页大小
   * @param <T> 数据类型
   * @return 空的分页响应对象
   */
  public static <T> PageResponse<T> empty(Integer page, Integer size) {
    return new PageResponse<>(List.of(), page, size, 0L);
  }

  /** 计算派生字段 */
  private void calculateDerivedFields() {
    if (totalElements == null) {
      totalElements = 0L;
    }

    if (size == null || size <= 0) {
      size = 10;
    }

    if (page == null || page <= 0) {
      page = 1;
    }

    // 计算总页数
    this.totalPages = (int) Math.ceil((double) totalElements / size);
    if (this.totalPages < 1) {
      this.totalPages = 1;
    }

    // 计算当前页记录数
    this.numberOfElements = content != null ? content.size() : 0;

    // 计算分页状态
    this.first = page == 1;
    this.last = page >= totalPages;
    this.hasNext = page < totalPages;
    this.hasPrevious = page > 1;
    this.empty = numberOfElements == 0;
  }

  // Getters and Setters
  public List<T> getContent() {
    return content;
  }

  public void setContent(List<T> content) {
    this.content = content;
    this.calculateDerivedFields();
  }

  public Integer getPage() {
    return page;
  }

  public void setPage(Integer page) {
    this.page = page;
    this.calculateDerivedFields();
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
    this.calculateDerivedFields();
  }

  public Long getTotalElements() {
    return totalElements;
  }

  public void setTotalElements(Long totalElements) {
    this.totalElements = totalElements;
    this.calculateDerivedFields();
  }

  public Integer getTotalPages() {
    return totalPages;
  }

  public Boolean getFirst() {
    return first;
  }

  public Boolean getLast() {
    return last;
  }

  public Boolean getHasNext() {
    return hasNext;
  }

  public Boolean getHasPrevious() {
    return hasPrevious;
  }

  public Integer getNumberOfElements() {
    return numberOfElements;
  }

  public Boolean getEmpty() {
    return empty;
  }

  public SortInfo getSort() {
    return sort;
  }

  public void setSort(SortInfo sort) {
    this.sort = sort;
  }

  /** 排序信息类 */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class SortInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sortBy;
    private String direction;
    private Boolean sorted;

    public SortInfo() {}

    public SortInfo(String sortBy, String direction) {
      this.sortBy = sortBy;
      this.direction = direction;
      this.sorted = sortBy != null && !sortBy.trim().isEmpty();
    }

    // Getters and Setters
    public String getSortBy() {
      return sortBy;
    }

    public void setSortBy(String sortBy) {
      this.sortBy = sortBy;
      this.sorted = sortBy != null && !sortBy.trim().isEmpty();
    }

    public String getDirection() {
      return direction;
    }

    public void setDirection(String direction) {
      this.direction = direction;
    }

    public Boolean getSorted() {
      return sorted;
    }
  }

  @Override
  public String toString() {
    return "PageResponse{"
        + "page="
        + page
        + ", size="
        + size
        + ", totalElements="
        + totalElements
        + ", totalPages="
        + totalPages
        + ", numberOfElements="
        + numberOfElements
        + ", empty="
        + empty
        + '}';
  }
}

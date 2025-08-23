package com.wanli.backend.util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/** 数据库查询优化工具类 提供分页、批量操作、查询优化等功能 */
@Component
public class DatabaseOptimizationUtil {

  // 默认分页大小
  private static final int DEFAULT_PAGE_SIZE = 20;
  // 最大分页大小
  private static final int MAX_PAGE_SIZE = 100;
  // 批量操作默认大小
  private static final int DEFAULT_BATCH_SIZE = 50;

  /** 分页参数类 */
  public static class PageParams {
    private final int page;
    private final int size;
    private final String sortBy;
    private final String sortDirection;

    public PageParams(int page, int size, String sortBy, String sortDirection) {
      this.page = Math.max(0, page);
      this.size = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
      this.sortBy = ValidationUtil.isNotBlank(sortBy) ? sortBy : "createdAt";
      this.sortDirection = "asc".equalsIgnoreCase(sortDirection) ? "asc" : "desc";
    }

    public int getPage() {
      return page;
    }

    public int getSize() {
      return size;
    }

    public String getSortBy() {
      return sortBy;
    }

    public String getSortDirection() {
      return sortDirection;
    }
  }

  /** 批量操作结果类 */
  public static class BatchResult<T> {
    private final List<T> successful;
    private final List<BatchError> errors;
    private final int totalProcessed;

    public BatchResult(List<T> successful, List<BatchError> errors, int totalProcessed) {
      this.successful = successful != null ? successful : new ArrayList<>();
      this.errors = errors != null ? errors : new ArrayList<>();
      this.totalProcessed = totalProcessed;
    }

    public List<T> getSuccessful() {
      return successful;
    }

    public List<BatchError> getErrors() {
      return errors;
    }

    public int getTotalProcessed() {
      return totalProcessed;
    }

    public int getSuccessCount() {
      return successful.size();
    }

    public int getErrorCount() {
      return errors.size();
    }

    public boolean hasErrors() {
      return !errors.isEmpty();
    }
  }

  /** 批量操作错误类 */
  public static class BatchError {
    private final int index;
    private final Object item;
    private final String error;

    public BatchError(int index, Object item, String error) {
      this.index = index;
      this.item = item;
      this.error = error;
    }

    public int getIndex() {
      return index;
    }

    public Object getItem() {
      return item;
    }

    public String getError() {
      return error;
    }
  }

  /**
   * 创建分页参数
   *
   * @param page 页码（从0开始）
   * @param size 每页大小
   * @return PageParams实例
   */
  public static PageParams createPageParams(int page, int size) {
    return new PageParams(page, size, null, null);
  }

  /**
   * 创建分页参数（带排序）
   *
   * @param page 页码（从0开始）
   * @param size 每页大小
   * @param sortBy 排序字段
   * @param sortDirection 排序方向（asc/desc）
   * @return PageParams实例
   */
  public static PageParams createPageParams(
      int page, int size, String sortBy, String sortDirection) {
    return new PageParams(page, size, sortBy, sortDirection);
  }

  /**
   * 将PageParams转换为Spring Data的Pageable
   *
   * @param pageParams 分页参数
   * @return Pageable实例
   */
  public static Pageable toPageable(PageParams pageParams) {
    Sort sort =
        "asc".equalsIgnoreCase(pageParams.getSortDirection())
            ? Sort.by(pageParams.getSortBy()).ascending()
            : Sort.by(pageParams.getSortBy()).descending();

    return PageRequest.of(pageParams.getPage(), pageParams.getSize(), sort);
  }

  /**
   * 创建默认分页参数
   *
   * @return 默认PageParams实例
   */
  public static PageParams createDefaultPageParams() {
    return new PageParams(0, DEFAULT_PAGE_SIZE, "createdAt", "desc");
  }

  /**
   * 批量处理数据
   *
   * @param items 要处理的数据列表
   * @param processor 处理函数
   * @param batchSize 批量大小
   * @return 批量处理结果
   */
  public static <T, R> BatchResult<R> processBatch(
      List<T> items, Function<T, R> processor, int batchSize) {

    if (items == null || items.isEmpty()) {
      return new BatchResult<>(new ArrayList<>(), new ArrayList<>(), 0);
    }

    List<R> successful = new ArrayList<>();
    List<BatchError> errors = new ArrayList<>();
    int actualBatchSize = Math.max(1, Math.min(batchSize, DEFAULT_BATCH_SIZE));

    try (PerformanceMonitor.Monitor monitor =
        PerformanceMonitor.monitor("DatabaseOptimizationUtil.processBatch")) {

      for (int i = 0; i < items.size(); i += actualBatchSize) {
        int endIndex = Math.min(i + actualBatchSize, items.size());
        List<T> batch = items.subList(i, endIndex);

        for (int j = 0; j < batch.size(); j++) {
          T item = batch.get(j);
          int globalIndex = i + j;

          try {
            R result = processor.apply(item);
            if (result != null) {
              successful.add(result);
            }
          } catch (Exception e) {
            errors.add(new BatchError(globalIndex, item, e.getMessage()));
            LogUtil.logError(
                "DatabaseOptimizationUtil.processBatch", "", "Batch processing error", "", e);
          }
        }

        // 记录批量处理进度
        LogUtil.logBusiness(
            "DatabaseOptimizationUtil.batchProgress",
            Map.of(
                "processed",
                endIndex,
                "total",
                items.size(),
                "successful",
                successful.size(),
                "errors",
                errors.size()));
      }
    }

    return new BatchResult<>(successful, errors, items.size());
  }

  /**
   * 批量处理数据（使用默认批量大小）
   *
   * @param items 要处理的数据列表
   * @param processor 处理函数
   * @return 批量处理结果
   */
  public static <T, R> BatchResult<R> processBatch(List<T> items, Function<T, R> processor) {
    return processBatch(items, processor, DEFAULT_BATCH_SIZE);
  }

  /**
   * 将列表分割为指定大小的批次
   *
   * @param list 原始列表
   * @param batchSize 批次大小
   * @return 分割后的批次列表
   */
  public static <T> List<List<T>> partition(List<T> list, int batchSize) {
    if (list == null || list.isEmpty()) {
      return new ArrayList<>();
    }

    int actualBatchSize = Math.max(1, batchSize);
    List<List<T>> partitions = new ArrayList<>();

    for (int i = 0; i < list.size(); i += actualBatchSize) {
      int endIndex = Math.min(i + actualBatchSize, list.size());
      partitions.add(new ArrayList<>(list.subList(i, endIndex)));
    }

    return partitions;
  }

  /**
   * 创建IN查询的安全参数列表 避免IN查询参数过多导致的性能问题
   *
   * @param ids ID列表
   * @param maxBatchSize 最大批次大小
   * @return 分批后的ID列表
   */
  public static <T> List<List<T>> createSafeInQueryBatches(List<T> ids, int maxBatchSize) {
    if (ids == null || ids.isEmpty()) {
      return new ArrayList<>();
    }

    // 去重
    List<T> uniqueIds = ids.stream().distinct().collect(Collectors.toList());

    // 限制最大批次大小，避免数据库IN查询性能问题
    int safeBatchSize = Math.min(maxBatchSize, 1000);

    return partition(uniqueIds, safeBatchSize);
  }

  /**
   * 创建安全的排序参数 防止SQL注入和无效排序字段
   *
   * @param sortBy 排序字段
   * @param allowedFields 允许的排序字段列表
   * @param defaultField 默认排序字段
   * @return 安全的排序字段
   */
  public static String createSafeSortField(
      String sortBy, Set<String> allowedFields, String defaultField) {
    if (ValidationUtil.isBlank(sortBy)) {
      return defaultField;
    }

    // 移除可能的SQL注入字符
    String cleanSortBy = sortBy.replaceAll("[^a-zA-Z0-9_]", "");

    if (allowedFields.contains(cleanSortBy)) {
      return cleanSortBy;
    }

    LogUtil.logError(
        "DatabaseOptimizationUtil.invalidSortField",
        "",
        "Invalid sort field",
        "",
        new IllegalArgumentException("Invalid sort field"));

    return defaultField;
  }

  /**
   * 验证分页参数的合理性
   *
   * @param page 页码
   * @param size 每页大小
   * @return 验证后的分页参数
   */
  public static PageParams validateAndFixPageParams(int page, int size) {
    int validPage = Math.max(0, page);
    int validSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);

    if (page != validPage || size != validSize) {
      LogUtil.logBusiness(
          "DatabaseOptimizationUtil.pageParamsAdjusted",
          Map.of(
              "originalPage",
              page,
              "adjustedPage",
              validPage,
              "originalSize",
              size,
              "adjustedSize",
              validSize));
    }

    return new PageParams(validPage, validSize, null, null);
  }
}

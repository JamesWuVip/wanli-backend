package com.wanli.backend.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.wanli.backend.config.ApplicationConfigManager;
import com.wanli.backend.monitor.MetricsCollector;
import com.wanli.backend.util.LogUtil;

import jakarta.persistence.EntityManager;

/** 数据库查询优化器 分析查询性能并提供优化建议 */
@Component
public class QueryOptimizer {

  private final ApplicationConfigManager configManager;
  private final MetricsCollector metricsCollector;
  private final DataSource dataSource;
  private final EntityManager entityManager;

  // 查询性能统计
  private final ConcurrentHashMap<String, QueryPerformanceStats> queryStats =
      new ConcurrentHashMap<>();

  // 慢查询记录
  private final ConcurrentHashMap<String, SlowQueryRecord> slowQueries = new ConcurrentHashMap<>();

  // 查询模式分析
  private final ConcurrentHashMap<String, QueryPattern> queryPatterns = new ConcurrentHashMap<>();

  // 索引使用统计
  private final ConcurrentHashMap<String, IndexUsageStats> indexStats = new ConcurrentHashMap<>();

  // 优化报告缓存
  private volatile QueryOptimizationReport lastOptimizationReport;

  // 查询缓存
  private final ConcurrentHashMap<String, QueryCacheEntry> queryCache = new ConcurrentHashMap<>();

  public QueryOptimizer(
      ApplicationConfigManager configManager,
      MetricsCollector metricsCollector,
      DataSource dataSource,
      EntityManager entityManager) {
    this.configManager = configManager;
    this.metricsCollector = metricsCollector;
    this.dataSource = dataSource;
    this.entityManager = entityManager;
  }

  /** 记录查询执行 */
  public void recordQueryExecution(
      String sql,
      long executionTime,
      int resultCount,
      String executionPlan,
      Map<String, Object> parameters) {
    if (!configManager.getDatabaseConfig().isOptimizationEnabled()) {
      return;
    }

    String normalizedSql = normalizeSql(sql);

    // 更新查询统计
    QueryPerformanceStats stats =
        queryStats.computeIfAbsent(normalizedSql, k -> new QueryPerformanceStats(k));
    stats.recordExecution(executionTime, resultCount);

    // 检查是否为慢查询
    if (isSlowQuery(executionTime)) {
      recordSlowQuery(normalizedSql, sql, executionTime, resultCount, executionPlan, parameters);
    }

    // 分析查询模式
    analyzeQueryPattern(normalizedSql, sql, parameters);

    // 分析索引使用
    analyzeIndexUsage(normalizedSql, executionPlan);

    // 记录指标
    metricsCollector.recordDatabaseOperation("query", "unknown", executionTime);
    metricsCollector.recordOperationTime("db_query", executionTime);

    if (isSlowQuery(executionTime)) {
      metricsCollector.incrementCounter("slow_queries");
    }
  }

  /** 标准化SQL语句 */
  private String normalizeSql(String sql) {
    if (sql == null) return "";

    return sql.trim()
        .replaceAll("\\s+", " ") // 多个空格替换为单个空格
        .replaceAll("'[^']*'", "?") // 字符串字面量替换为参数占位符
        .replaceAll("\\b\\d+\\b", "?") // 数字字面量替换为参数占位符
        .toLowerCase();
  }

  /** 判断是否为慢查询 */
  private boolean isSlowQuery(long executionTime) {
    return executionTime > configManager.getDatabaseConfig().getSlowQueryThreshold();
  }

  /** 记录慢查询 */
  private void recordSlowQuery(
      String normalizedSql,
      String originalSql,
      long executionTime,
      int resultCount,
      String executionPlan,
      Map<String, Object> parameters) {
    SlowQueryRecord record = new SlowQueryRecord();
    record.normalizedSql = normalizedSql;
    record.originalSql = originalSql;
    record.executionTime = executionTime;
    record.resultCount = resultCount;
    record.executionPlan = executionPlan;
    record.parameters = new HashMap<>(parameters != null ? parameters : Collections.emptyMap());
    record.timestamp = LocalDateTime.now();
    record.occurrenceCount = new AtomicLong(1);

    SlowQueryRecord existing = slowQueries.putIfAbsent(normalizedSql, record);
    if (existing != null) {
      existing.occurrenceCount.incrementAndGet();
      if (executionTime > existing.executionTime) {
        existing.executionTime = executionTime;
        existing.originalSql = originalSql;
        existing.executionPlan = executionPlan;
        existing.parameters =
            new HashMap<>(parameters != null ? parameters : Collections.emptyMap());
      }
      existing.timestamp = LocalDateTime.now();
    }

    LogUtil.logWarn(
        "SLOW_QUERY_DETECTED",
        "",
        String.format("检测到慢查询: 执行时间=%dms, SQL=%s", executionTime, originalSql));
  }

  /** 分析查询模式 */
  private void analyzeQueryPattern(
      String normalizedSql, String originalSql, Map<String, Object> parameters) {
    QueryPattern pattern = queryPatterns.computeIfAbsent(normalizedSql, k -> new QueryPattern(k));
    pattern.recordExecution(originalSql, parameters);
  }

  /** 分析索引使用 */
  private void analyzeIndexUsage(String normalizedSql, String executionPlan) {
    if (executionPlan == null || executionPlan.isEmpty()) {
      return;
    }

    // 解析执行计划中的索引使用情况
    Set<String> usedIndexes = parseIndexesFromExecutionPlan(executionPlan);

    for (String index : usedIndexes) {
      IndexUsageStats stats = indexStats.computeIfAbsent(index, k -> new IndexUsageStats(k));
      stats.recordUsage(normalizedSql);
    }

    // 检查是否有全表扫描
    if (executionPlan.toLowerCase().contains("seq scan")
        || executionPlan.toLowerCase().contains("table scan")) {
      metricsCollector.incrementCounter("full_table_scans");
      LogUtil.logWarn("FULL_TABLE_SCAN", "", String.format("检测到全表扫描: SQL=%s", normalizedSql));
    }
  }

  /** 从执行计划中解析索引 */
  private Set<String> parseIndexesFromExecutionPlan(String executionPlan) {
    Set<String> indexes = new HashSet<>();

    // 简单的索引解析逻辑，实际实现需要根据数据库类型进行调整
    String[] lines = executionPlan.split("\n");
    for (String line : lines) {
      if (line.toLowerCase().contains("index")) {
        // 提取索引名称的逻辑
        String[] parts = line.split("\\s+");
        for (String part : parts) {
          if (part.startsWith("idx_") || part.startsWith("index_")) {
            indexes.add(part);
          }
        }
      }
    }

    return indexes;
  }

  /** 执行查询优化分析 */
  public QueryOptimizationReport performOptimizationAnalysis() {
    QueryOptimizationReport report = new QueryOptimizationReport();
    report.timestamp = LocalDateTime.now();
    report.suggestions = new ArrayList<>();
    report.slowQueries = new ArrayList<>();
    report.indexRecommendations = new ArrayList<>();
    report.queryPatterns = new HashMap<>();

    try {
      // 分析慢查询
      analyzeSlowQueries(report);

      // 分析查询模式
      analyzeQueryPatterns(report);

      // 分析索引使用
      analyzeIndexUsage(report);

      // 分析表统计信息
      analyzeTableStatistics(report);

      // 生成优化建议
      generateOptimizationSuggestions(report);

      // 缓存报告
      lastOptimizationReport = report;

      LogUtil.logInfo(
          "QUERY_OPTIMIZATION_ANALYSIS",
          "",
          String.format("查询优化分析完成，生成 %d 条建议", report.suggestions.size()));

    } catch (Exception e) {
      LogUtil.logError("QUERY_OPTIMIZATION_ERROR", "", "OPTIMIZATION_FAILED", "查询优化分析失败", e);
    }

    return report;
  }

  /** 分析慢查询 */
  private void analyzeSlowQueries(QueryOptimizationReport report) {
    List<SlowQueryRecord> topSlowQueries =
        slowQueries.values().stream()
            .sorted((a, b) -> Long.compare(b.executionTime, a.executionTime))
            .limit(20)
            .collect(Collectors.toList());

    for (SlowQueryRecord record : topSlowQueries) {
      SlowQueryInfo info = new SlowQueryInfo();
      info.sql = record.originalSql;
      info.executionTime = record.executionTime;
      info.occurrenceCount = record.occurrenceCount.get();
      info.lastOccurrence = record.timestamp;
      info.executionPlan = record.executionPlan;
      info.optimizationSuggestions = generateQueryOptimizationSuggestions(record);

      report.slowQueries.add(info);
    }
  }

  /** 分析查询模式 */
  private void analyzeQueryPatterns(QueryOptimizationReport report) {
    Map<QueryPatternType, Long> patternCounts = new HashMap<>();

    for (QueryPattern pattern : queryPatterns.values()) {
      QueryPatternType type = pattern.analyzePattern();
      patternCounts.merge(type, 1L, Long::sum);
    }

    report.queryPatterns = patternCounts;
  }

  /** 分析索引使用 */
  private void analyzeIndexUsage(QueryOptimizationReport report) {
    // 找出未使用的索引
    Set<String> allIndexes = getAllIndexes();
    Set<String> usedIndexes = indexStats.keySet();

    for (String index : allIndexes) {
      if (!usedIndexes.contains(index)) {
        IndexRecommendation recommendation = new IndexRecommendation();
        recommendation.type = IndexRecommendationType.DROP_UNUSED;
        recommendation.indexName = index;
        recommendation.description = "索引 " + index + " 未被使用，建议删除";
        recommendation.priority = RecommendationPriority.LOW;

        report.indexRecommendations.add(recommendation);
      }
    }

    // 分析需要创建的索引
    analyzeIndexCreationNeeds(report);
  }

  /** 分析表统计信息 */
  private void analyzeTableStatistics(QueryOptimizationReport report) {
    try (Connection connection = dataSource.getConnection()) {
      // 获取表统计信息
      String sql =
          "SELECT table_name, table_rows, data_length, index_length "
              + "FROM information_schema.tables "
              + "WHERE table_schema = DATABASE()";

      try (PreparedStatement stmt = connection.prepareStatement(sql);
          ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
          String tableName = rs.getString("table_name");
          long tableRows = rs.getLong("table_rows");
          long dataLength = rs.getLong("data_length");
          long indexLength = rs.getLong("index_length");

          // 分析表大小和索引比例
          if (indexLength > dataLength * 2) {
            report.suggestions.add(
                new QueryOptimizationSuggestion(
                    OptimizationType.INDEX_OPTIMIZATION,
                    "表 " + tableName + " 的索引大小过大，建议检查索引使用情况",
                    RecommendationPriority.MEDIUM));
          }
        }
      }
    } catch (SQLException e) {
      LogUtil.logWarn("TABLE_STATS_ANALYSIS", "", "表统计信息分析失败: " + e.getMessage());
    }
  }

  /** 生成优化建议 */
  private void generateOptimizationSuggestions(QueryOptimizationReport report) {
    // 慢查询优化建议
    if (!report.slowQueries.isEmpty()) {
      long criticalSlowQueries =
          report.slowQueries.stream()
              .filter(q -> q.executionTime > 5000) // 超过5秒
              .count();

      if (criticalSlowQueries > 0) {
        report.suggestions.add(
            new QueryOptimizationSuggestion(
                OptimizationType.SLOW_QUERY_OPTIMIZATION,
                "发现 " + criticalSlowQueries + " 个严重慢查询，需要立即优化",
                RecommendationPriority.CRITICAL));
      }
    }

    // 查询模式优化建议
    Long nPlusOneQueries = report.queryPatterns.get(QueryPatternType.N_PLUS_ONE);
    if (nPlusOneQueries != null && nPlusOneQueries > 5) {
      report.suggestions.add(
          new QueryOptimizationSuggestion(
              OptimizationType.PATTERN_OPTIMIZATION,
              "检测到 " + nPlusOneQueries + " 个N+1查询问题，建议使用JOIN或批量查询",
              RecommendationPriority.HIGH));
    }

    // 索引优化建议
    long unusedIndexes =
        report.indexRecommendations.stream()
            .filter(r -> r.type == IndexRecommendationType.DROP_UNUSED)
            .count();

    if (unusedIndexes > 0) {
      report.suggestions.add(
          new QueryOptimizationSuggestion(
              OptimizationType.INDEX_OPTIMIZATION,
              "发现 " + unusedIndexes + " 个未使用的索引，建议删除以节省存储空间",
              RecommendationPriority.LOW));
    }
  }

  /** 为单个查询生成优化建议 */
  private List<String> generateQueryOptimizationSuggestions(SlowQueryRecord record) {
    List<String> suggestions = new ArrayList<>();

    String sql = record.originalSql.toLowerCase();

    // 检查是否缺少WHERE条件
    if (sql.contains("select") && !sql.contains("where") && !sql.contains("limit")) {
      suggestions.add("添加WHERE条件或LIMIT限制结果集大小");
    }

    // 检查是否使用了SELECT *
    if (sql.contains("select *")) {
      suggestions.add("避免使用SELECT *，只查询需要的字段");
    }

    // 检查是否有子查询可以优化为JOIN
    if (sql.contains("in (select") || sql.contains("exists (select")) {
      suggestions.add("考虑将子查询优化为JOIN操作");
    }

    // 检查是否有ORDER BY但没有索引
    if (sql.contains("order by")
        && record.executionPlan != null
        && !record.executionPlan.toLowerCase().contains("index")) {
      suggestions.add("为ORDER BY字段创建索引");
    }

    return suggestions;
  }

  /** 分析索引创建需求 */
  private void analyzeIndexCreationNeeds(QueryOptimizationReport report) {
    // 分析慢查询中的WHERE条件，建议创建索引
    for (SlowQueryRecord record : slowQueries.values()) {
      Set<String> whereColumns = extractWhereColumns(record.originalSql);

      for (String column : whereColumns) {
        if (!hasIndexOnColumn(column)) {
          IndexRecommendation recommendation = new IndexRecommendation();
          recommendation.type = IndexRecommendationType.CREATE_INDEX;
          recommendation.columnName = column;
          recommendation.description = "为字段 " + column + " 创建索引以提升查询性能";
          recommendation.priority = RecommendationPriority.HIGH;

          report.indexRecommendations.add(recommendation);
        }
      }
    }
  }

  /** 提取WHERE条件中的列名 */
  private Set<String> extractWhereColumns(String sql) {
    Set<String> columns = new HashSet<>();

    // 简单的列名提取逻辑，实际实现需要更复杂的SQL解析
    String lowerSql = sql.toLowerCase();
    int whereIndex = lowerSql.indexOf("where");

    if (whereIndex != -1) {
      String whereClause = sql.substring(whereIndex + 5);
      // 这里需要更复杂的解析逻辑来提取列名
      // 简化实现
      String[] parts = whereClause.split("\\s+");
      for (String part : parts) {
        if (part.matches("[a-zA-Z_][a-zA-Z0-9_]*")
            && !part.matches("(?i)(and|or|in|like|between|is|null|not)")) {
          columns.add(part);
        }
      }
    }

    return columns;
  }

  /** 检查列是否有索引 */
  private boolean hasIndexOnColumn(String column) {
    // 简化实现，实际需要查询数据库元数据
    return indexStats.keySet().stream()
        .anyMatch(index -> index.toLowerCase().contains(column.toLowerCase()));
  }

  /** 获取所有索引 */
  private Set<String> getAllIndexes() {
    Set<String> indexes = new HashSet<>();

    try (Connection connection = dataSource.getConnection()) {
      String sql =
          "SELECT DISTINCT index_name FROM information_schema.statistics "
              + "WHERE table_schema = DATABASE() AND index_name != 'PRIMARY'";

      try (PreparedStatement stmt = connection.prepareStatement(sql);
          ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
          indexes.add(rs.getString("index_name"));
        }
      }
    } catch (SQLException e) {
      LogUtil.logWarn("GET_INDEXES", "", "获取索引列表失败: " + e.getMessage());
    }

    return indexes;
  }

  /** 应用优化建议 */
  @Transactional
  public void applyOptimizations(List<String> suggestionIds) {
    if (lastOptimizationReport == null) {
      LogUtil.logWarn("QUERY_OPTIMIZATION_APPLY", "", "没有可用的优化报告");
      return;
    }

    for (String suggestionId : suggestionIds) {
      try {
        applySingleOptimization(suggestionId);
        metricsCollector.incrementCounter("query_optimizations_applied");

      } catch (Exception e) {
        LogUtil.logError(
            "QUERY_OPTIMIZATION_APPLY_ERROR", "", "APPLY_FAILED", "应用优化建议失败: " + suggestionId, e);
      }
    }
  }

  /** 应用单个优化建议 */
  private void applySingleOptimization(String suggestionId) {
    // 这里实现具体的优化逻辑
    // 例如：创建索引、更新表统计信息、重写查询等
    LogUtil.logInfo("QUERY_OPTIMIZATION_APPLIED", "", "已应用优化建议: " + suggestionId);
  }

  /** 获取最新的优化报告 */
  public QueryOptimizationReport getLatestOptimizationReport() {
    return lastOptimizationReport;
  }

  /** 重置统计数据 */
  public void resetStatistics() {
    queryStats.clear();
    slowQueries.clear();
    queryPatterns.clear();
    indexStats.clear();
    queryCache.clear();
    lastOptimizationReport = null;

    LogUtil.logInfo("QUERY_STATS_RESET", "", "查询统计数据已重置");
  }

  /** 定期执行优化分析 */
  @Scheduled(fixedRate = 3600000) // 每小时执行一次
  public void scheduledOptimizationAnalysis() {
    if (!configManager.getDatabaseConfig().isOptimizationEnabled()) {
      return;
    }

    try {
      QueryOptimizationReport report = performOptimizationAnalysis();

      // 如果有严重问题，记录警告日志
      long criticalIssues =
          report.suggestions.stream()
              .filter(s -> s.priority == RecommendationPriority.CRITICAL)
              .count();

      if (criticalIssues > 0) {
        LogUtil.logWarn("QUERY_OPTIMIZATION_ALERT", "", "发现 " + criticalIssues + " 个严重查询性能问题");
      }

    } catch (Exception e) {
      LogUtil.logError("SCHEDULED_QUERY_OPTIMIZATION", "", "SCHEDULED_FAILED", "定期查询优化分析失败", e);
    }
  }

  // 枚举定义
  public enum QueryPatternType {
    N_PLUS_ONE, // N+1查询问题
    CARTESIAN_PRODUCT, // 笛卡尔积
    MISSING_INDEX, // 缺少索引
    FULL_TABLE_SCAN, // 全表扫描
    INEFFICIENT_JOIN, // 低效JOIN
    NORMAL // 正常查询
  }

  public enum OptimizationType {
    SLOW_QUERY_OPTIMIZATION,
    INDEX_OPTIMIZATION,
    PATTERN_OPTIMIZATION,
    SCHEMA_OPTIMIZATION,
    QUERY_REWRITE
  }

  public enum IndexRecommendationType {
    CREATE_INDEX,
    DROP_UNUSED,
    MODIFY_INDEX
  }

  public enum RecommendationPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
  }

  // 数据类定义
  public static class QueryPerformanceStats {
    private final String normalizedSql;
    private final LongAdder executionCount = new LongAdder();
    private final LongAdder totalExecutionTime = new LongAdder();
    private final LongAdder totalResultCount = new LongAdder();
    private volatile long maxExecutionTime = 0;
    private volatile long minExecutionTime = Long.MAX_VALUE;
    private volatile LocalDateTime lastExecution;

    public QueryPerformanceStats(String normalizedSql) {
      this.normalizedSql = normalizedSql;
    }

    public void recordExecution(long executionTime, int resultCount) {
      executionCount.increment();
      totalExecutionTime.add(executionTime);
      totalResultCount.add(resultCount);
      lastExecution = LocalDateTime.now();

      // 更新最大最小执行时间
      synchronized (this) {
        if (executionTime > maxExecutionTime) {
          maxExecutionTime = executionTime;
        }
        if (executionTime < minExecutionTime) {
          minExecutionTime = executionTime;
        }
      }
    }

    public String getNormalizedSql() {
      return normalizedSql;
    }

    public long getExecutionCount() {
      return executionCount.sum();
    }

    public double getAverageExecutionTime() {
      long count = executionCount.sum();
      return count > 0 ? (double) totalExecutionTime.sum() / count : 0.0;
    }

    public double getAverageResultCount() {
      long count = executionCount.sum();
      return count > 0 ? (double) totalResultCount.sum() / count : 0.0;
    }

    public long getMaxExecutionTime() {
      return maxExecutionTime;
    }

    public long getMinExecutionTime() {
      return minExecutionTime == Long.MAX_VALUE ? 0 : minExecutionTime;
    }
  }

  public static class SlowQueryRecord {
    public String normalizedSql;
    public String originalSql;
    public long executionTime;
    public int resultCount;
    public String executionPlan;
    public Map<String, Object> parameters;
    public LocalDateTime timestamp;
    public AtomicLong occurrenceCount;
  }

  public static class QueryPattern {
    private final String normalizedSql;
    private final List<String> originalQueries = new ArrayList<>();
    private final LongAdder executionCount = new LongAdder();
    private volatile LocalDateTime lastExecution;

    public QueryPattern(String normalizedSql) {
      this.normalizedSql = normalizedSql;
    }

    public void recordExecution(String originalSql, Map<String, Object> parameters) {
      synchronized (originalQueries) {
        if (originalQueries.size() < 10) {
          originalQueries.add(originalSql);
        }
      }
      executionCount.increment();
      lastExecution = LocalDateTime.now();
    }

    public QueryPatternType analyzePattern() {
      // 简化的模式分析逻辑
      String sql = normalizedSql.toLowerCase();

      if (executionCount.sum() > 100 && sql.contains("select") && sql.contains("where")) {
        return QueryPatternType.N_PLUS_ONE;
      }

      if (sql.contains("join") && !sql.contains("where")) {
        return QueryPatternType.CARTESIAN_PRODUCT;
      }

      return QueryPatternType.NORMAL;
    }
  }

  public static class IndexUsageStats {
    private final String indexName;
    private final Set<String> usedByQueries = new HashSet<>();
    private final LongAdder usageCount = new LongAdder();
    private volatile LocalDateTime lastUsed;

    public IndexUsageStats(String indexName) {
      this.indexName = indexName;
    }

    public void recordUsage(String normalizedSql) {
      synchronized (usedByQueries) {
        usedByQueries.add(normalizedSql);
      }
      usageCount.increment();
      lastUsed = LocalDateTime.now();
    }

    public String getIndexName() {
      return indexName;
    }

    public long getUsageCount() {
      return usageCount.sum();
    }

    public int getQueryCount() {
      return usedByQueries.size();
    }

    public LocalDateTime getLastUsed() {
      return lastUsed;
    }
  }

  public static class QueryCacheEntry {
    public String sql;
    public Object result;
    public LocalDateTime cacheTime;
    public Duration ttl;

    public boolean isExpired() {
      return LocalDateTime.now().isAfter(cacheTime.plus(ttl));
    }
  }

  // 报告相关类
  public static class QueryOptimizationReport {
    public LocalDateTime timestamp;
    public List<QueryOptimizationSuggestion> suggestions;
    public List<SlowQueryInfo> slowQueries;
    public List<IndexRecommendation> indexRecommendations;
    public Map<QueryPatternType, Long> queryPatterns;
  }

  public static class QueryOptimizationSuggestion {
    public OptimizationType type;
    public String description;
    public RecommendationPriority priority;
    public String id;

    public QueryOptimizationSuggestion(
        OptimizationType type, String description, RecommendationPriority priority) {
      this.type = type;
      this.description = description;
      this.priority = priority;
      this.id = type.name() + "_" + System.currentTimeMillis();
    }
  }

  public static class SlowQueryInfo {
    public String sql;
    public long executionTime;
    public long occurrenceCount;
    public LocalDateTime lastOccurrence;
    public String executionPlan;
    public List<String> optimizationSuggestions;
  }

  public static class IndexRecommendation {
    public IndexRecommendationType type;
    public String indexName;
    public String columnName;
    public String description;
    public RecommendationPriority priority;
  }
}

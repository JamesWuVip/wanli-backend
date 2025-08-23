package com.wanli.backend.batch;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.wanli.backend.async.AsyncTaskProcessor;
import com.wanli.backend.config.ApplicationConfigManager;
import com.wanli.backend.monitor.MetricsCollector;
import com.wanli.backend.util.LogUtil;

/** 批处理服务 提供高效的批量数据处理功能 */
@Service
public class BatchProcessor {

  private final ApplicationConfigManager configManager;
  private final MetricsCollector metricsCollector;
  private final AsyncTaskProcessor asyncTaskProcessor;
  private final TransactionTemplate transactionTemplate;

  // 批处理执行器
  private final ThreadPoolExecutor batchExecutor;
  private final ScheduledExecutorService scheduledExecutor;

  // 批处理任务管理
  private final ConcurrentHashMap<String, BatchJob> activeBatchJobs = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, BatchJobResult> completedBatchJobs =
      new ConcurrentHashMap<>();

  // 批处理统计
  private final AtomicLong totalBatchJobsSubmitted = new AtomicLong(0);
  private final AtomicLong totalBatchJobsCompleted = new AtomicLong(0);
  private final AtomicLong totalBatchJobsFailed = new AtomicLong(0);
  private final AtomicLong totalItemsProcessed = new AtomicLong(0);

  public BatchProcessor(
      ApplicationConfigManager configManager,
      MetricsCollector metricsCollector,
      AsyncTaskProcessor asyncTaskProcessor,
      TransactionTemplate transactionTemplate) {
    this.configManager = configManager;
    this.metricsCollector = metricsCollector;
    this.asyncTaskProcessor = asyncTaskProcessor;
    this.transactionTemplate = transactionTemplate;

    // 初始化线程池
    this.batchExecutor = createBatchExecutor();
    this.scheduledExecutor = Executors.newScheduledThreadPool(2);

    // 启动监控
    startMonitoring();
  }

  /** 创建批处理执行器 */
  private ThreadPoolExecutor createBatchExecutor() {
    return new ThreadPoolExecutor(
        configManager.getBatchConfig().getCorePoolSize(),
        configManager.getBatchConfig().getMaxPoolSize(),
        configManager.getBatchConfig().getKeepAliveTime(),
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(configManager.getBatchConfig().getQueueCapacity()),
        new BatchThreadFactory(),
        new ThreadPoolExecutor.CallerRunsPolicy());
  }

  /** 启动监控 */
  private void startMonitoring() {
    // 批处理任务监控
    scheduledExecutor.scheduleAtFixedRate(this::monitorBatchJobs, 0, 30, TimeUnit.SECONDS);

    // 清理已完成的任务
    scheduledExecutor.scheduleAtFixedRate(this::cleanupCompletedJobs, 0, 10, TimeUnit.MINUTES);
  }

  /** 提交批处理任务 */
  public <T, R> CompletableFuture<BatchJobResult<R>> submitBatchJob(
      String jobId, Collection<T> items, Function<T, R> processor, BatchOptions options) {

    if (jobId == null) {
      jobId = generateJobId();
    }

    BatchJob<T, R> batchJob =
        new BatchJob<>(
            jobId,
            new ArrayList<>(items),
            processor,
            options != null ? options : BatchOptions.defaultOptions());

    CompletableFuture<BatchJobResult<R>> future = new CompletableFuture<>();
    batchJob.setFuture(future);

    // 添加到活跃任务列表
    activeBatchJobs.put(jobId, batchJob);

    // 提交任务
    batchExecutor.submit(() -> executeBatchJob(batchJob));

    // 更新统计
    totalBatchJobsSubmitted.incrementAndGet();
    metricsCollector.incrementCounter("batch_jobs_submitted");

    LogUtil.logInfo(
        "BATCH_JOB_SUBMITTED", "", String.format("批处理任务已提交: %s, 项目数量: %d", jobId, items.size()));

    return future;
  }

  /** 提交简单批处理任务 */
  public <T, R> CompletableFuture<BatchJobResult<R>> submitBatchJob(
      Collection<T> items, Function<T, R> processor) {
    return submitBatchJob(null, items, processor, null);
  }

  /** 提交分页批处理任务 */
  public <T, R> CompletableFuture<BatchJobResult<R>> submitPagedBatchJob(
      String jobId, PagedDataSource<T> dataSource, Function<T, R> processor, BatchOptions options) {

    if (jobId == null) {
      jobId = generateJobId();
    }

    final String finalJobId = jobId; // 创建final副本供lambda使用
    CompletableFuture<BatchJobResult<R>> future = new CompletableFuture<>();

    // 异步执行分页批处理
    asyncTaskProcessor.submitTask(
        AsyncTaskProcessor.TaskType.BATCH_PROCESSING,
        finalJobId,
        () -> {
          try {
            executePagedBatchJob(finalJobId, dataSource, processor, options, future);
          } catch (Exception e) {
            future.completeExceptionally(e);
          }
        });

    return future;
  }

  /** 执行批处理任务 */
  private <T, R> void executeBatchJob(BatchJob<T, R> batchJob) {
    String jobId = batchJob.getJobId();
    long startTime = System.currentTimeMillis();

    try {
      // 更新任务状态
      batchJob.setStatus(BatchJobStatus.RUNNING);
      batchJob.setStartTime(LocalDateTime.now());

      java.util.Map<String, Object> context = new java.util.HashMap<>();
      context.put("jobId", jobId);
      LogUtil.logBusiness("BATCH_JOB_STARTED", context);

      // 执行批处理
      BatchJobResult<R> result = processBatch(batchJob);

      // 任务完成
      long executionTime = System.currentTimeMillis() - startTime;
      completeBatchJob(batchJob, result, executionTime);

    } catch (Exception e) {
      long executionTime = System.currentTimeMillis() - startTime;
      handleBatchJobFailure(batchJob, e, executionTime);
    } finally {
      activeBatchJobs.remove(jobId);
    }
  }

  /** 执行分页批处理任务 */
  private <T, R> BatchJobResult<R> executePagedBatchJob(
      String jobId,
      PagedDataSource<T> dataSource,
      Function<T, R> processor,
      BatchOptions options,
      CompletableFuture<BatchJobResult<R>> future) {

    long startTime = System.currentTimeMillis();
    List<R> allResults = new ArrayList<>();
    List<BatchError> allErrors = new ArrayList<>();
    AtomicLong processedCount = new AtomicLong(0);
    AtomicLong errorCount = new AtomicLong(0);

    try {
      java.util.Map<String, Object> context = new java.util.HashMap<>();
      context.put("jobId", jobId);
      LogUtil.logBusiness("PAGED_BATCH_JOB_STARTED", context);

      int pageNumber = 0;
      boolean hasMoreData = true;

      while (hasMoreData) {
        // 获取分页数据
        Pageable pageable = PageRequest.of(pageNumber, options.getPageSize());
        Page<T> page = dataSource.getPage(pageable);

        if (page.isEmpty()) {
          hasMoreData = false;
          continue;
        }

        // 处理当前页数据
        BatchJob<T, R> pageBatchJob =
            new BatchJob<>(jobId + "_page_" + pageNumber, page.getContent(), processor, options);

        BatchJobResult<R> pageResult = processBatch(pageBatchJob);

        // 合并结果
        allResults.addAll(pageResult.getResults());
        allErrors.addAll(pageResult.getErrors());
        processedCount.addAndGet(pageResult.getProcessedCount());
        errorCount.addAndGet(pageResult.getErrorCount());

        // 更新进度
        double progress = (double) (pageNumber + 1) / page.getTotalPages();
        metricsCollector.recordGauge("batch_job_progress_" + jobId, progress * 100);

        java.util.Map<String, Object> progressContext = new java.util.HashMap<>();
        progressContext.put("jobId", jobId);
        progressContext.put("currentPage", pageNumber + 1);
        progressContext.put("totalPages", page.getTotalPages());
        progressContext.put("processedCount", processedCount.get());
        progressContext.put("errorCount", errorCount.get());
        LogUtil.logBusiness("PAGED_BATCH_PROGRESS", progressContext);

        pageNumber++;
        hasMoreData = page.hasNext();

        // 检查是否需要暂停
        if (options.getPauseInterval() > 0 && pageNumber % options.getPauseBatchCount() == 0) {
          Thread.sleep(options.getPauseInterval());
        }
      }

      // 创建最终结果
      long executionTime = System.currentTimeMillis() - startTime;
      BatchJobResult<R> finalResult =
          new BatchJobResult<>(
              jobId,
              BatchJobStatus.COMPLETED,
              allResults,
              allErrors,
              processedCount.get(),
              errorCount.get(),
              executionTime);

      // 保存结果
      completedBatchJobs.put(jobId, finalResult);
      future.complete(finalResult);

      // 更新统计
      totalBatchJobsCompleted.incrementAndGet();
      totalItemsProcessed.addAndGet(processedCount.get());

      LogUtil.logInfo(
          "PAGED_BATCH_JOB_COMPLETED",
          "",
          String.format(
              "分页批处理任务完成: %s, 处理: %d, 错误: %d, 耗时: %dms",
              jobId, processedCount.get(), errorCount.get(), executionTime));

      return finalResult;

    } catch (Exception e) {
      long executionTime = System.currentTimeMillis() - startTime;
      BatchJobResult<R> errorResult =
          new BatchJobResult<>(
              jobId,
              BatchJobStatus.FAILED,
              allResults,
              allErrors,
              processedCount.get(),
              errorCount.get(),
              executionTime);
      errorResult.setException(e);

      completedBatchJobs.put(jobId, errorResult);
      future.completeExceptionally(e);

      totalBatchJobsFailed.incrementAndGet();

      LogUtil.logError(
          "PAGED_BATCH_JOB_FAILED", "", "BATCH_ERROR", String.format("分页批处理任务失败: %s", jobId), e);

      return errorResult;
    }
  }

  /** 处理批次数据 */
  private <T, R> BatchJobResult<R> processBatch(BatchJob<T, R> batchJob) {
    String jobId = batchJob.getJobId();
    List<T> items = batchJob.getItems();
    Function<T, R> processor = batchJob.getProcessor();
    BatchOptions options = batchJob.getOptions();

    List<R> results = new ArrayList<>();
    List<BatchError> errors = new ArrayList<>();
    AtomicLong processedCount = new AtomicLong(0);
    AtomicLong errorCount = new AtomicLong(0);

    // 分批处理
    int batchSize = options.getBatchSize();
    List<List<T>> batches = partitionList(items, batchSize);

    if (options.isParallel()) {
      // 并行处理
      processBatchesInParallel(
          batches, processor, options, results, errors, processedCount, errorCount);
    } else {
      // 串行处理
      processBatchesSequentially(
          batches, processor, options, results, errors, processedCount, errorCount);
    }

    // 创建结果
    BatchJobResult<R> result =
        new BatchJobResult<>(
            jobId,
            BatchJobStatus.COMPLETED,
            results,
            errors,
            processedCount.get(),
            errorCount.get(),
            0);

    return result;
  }

  /** 并行处理批次 */
  private <T, R> void processBatchesInParallel(
      List<List<T>> batches,
      Function<T, R> processor,
      BatchOptions options,
      List<R> results,
      List<BatchError> errors,
      AtomicLong processedCount,
      AtomicLong errorCount) {

    List<CompletableFuture<BatchResult<R>>> futures =
        batches.stream()
            .map(
                batch ->
                    CompletableFuture.supplyAsync(
                        () -> processSingleBatch(batch, processor, options), batchExecutor))
            .collect(Collectors.toList());

    // 等待所有批次完成
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    // 收集结果
    for (CompletableFuture<BatchResult<R>> future : futures) {
      try {
        BatchResult<R> batchResult = future.get();
        synchronized (results) {
          results.addAll(batchResult.getResults());
        }
        synchronized (errors) {
          errors.addAll(batchResult.getErrors());
        }
        processedCount.addAndGet(batchResult.getProcessedCount());
        errorCount.addAndGet(batchResult.getErrorCount());
      } catch (Exception e) {
        LogUtil.logError(
            "BATCH_PARALLEL_PROCESSING_ERROR", "", "PROCESSING_ERROR", "并行批处理收集结果失败", e);
      }
    }
  }

  /** 串行处理批次 */
  private <T, R> void processBatchesSequentially(
      List<List<T>> batches,
      Function<T, R> processor,
      BatchOptions options,
      List<R> results,
      List<BatchError> errors,
      AtomicLong processedCount,
      AtomicLong errorCount) {

    for (int i = 0; i < batches.size(); i++) {
      List<T> batch = batches.get(i);

      try {
        BatchResult<R> batchResult = processSingleBatch(batch, processor, options);

        results.addAll(batchResult.getResults());
        errors.addAll(batchResult.getErrors());
        processedCount.addAndGet(batchResult.getProcessedCount());
        errorCount.addAndGet(batchResult.getErrorCount());

        // 暂停间隔
        if (options.getPauseInterval() > 0 && (i + 1) % options.getPauseBatchCount() == 0) {
          Thread.sleep(options.getPauseInterval());
        }

      } catch (Exception e) {
        LogUtil.logError(
            "BATCH_SEQUENTIAL_PROCESSING_ERROR",
            "",
            "PROCESSING_ERROR",
            String.format("串行批处理第%d批失败", i + 1),
            e);

        if (!options.isContinueOnError()) {
          throw new RuntimeException("批处理失败，停止执行", e);
        }
      }
    }
  }

  /** 处理单个批次 */
  private <T, R> BatchResult<R> processSingleBatch(
      List<T> batch, Function<T, R> processor, BatchOptions options) {

    List<R> results = new ArrayList<>();
    List<BatchError> errors = new ArrayList<>();

    for (int i = 0; i < batch.size(); i++) {
      T item = batch.get(i);

      try {
        // 应用过滤器
        if (options.getFilter() != null && !options.getFilter().test(item)) {
          continue;
        }

        // 处理项目
        R result;
        if (options.isTransactional()) {
          result = transactionTemplate.execute(status -> processor.apply(item));
        } else {
          result = processor.apply(item);
        }

        if (result != null) {
          results.add(result);
        }

        // 执行回调
        if (options.getOnItemProcessed() != null) {
          options.getOnItemProcessed().accept(item);
        }

      } catch (Exception e) {
        BatchError error = new BatchError(i, item, e);
        errors.add(error);

        // 执行错误回调
        if (options.getOnItemError() != null) {
          options.getOnItemError().accept(error);
        }

        if (!options.isContinueOnError()) {
          throw new RuntimeException("批处理项目失败，停止执行", e);
        }
      }
    }

    return new BatchResult<>(results, errors, batch.size() - errors.size(), errors.size());
  }

  /** 完成批处理任务 */
  private <T, R> void completeBatchJob(
      BatchJob<T, R> batchJob, BatchJobResult<R> result, long executionTime) {
    String jobId = batchJob.getJobId();

    // 更新任务状态
    batchJob.setStatus(BatchJobStatus.COMPLETED);
    batchJob.setEndTime(LocalDateTime.now());
    result.setExecutionTime(executionTime);

    // 完成Future
    batchJob.getFuture().complete(result);

    // 保存结果
    completedBatchJobs.put(jobId, result);

    // 更新统计
    totalBatchJobsCompleted.incrementAndGet();
    totalItemsProcessed.addAndGet(result.getProcessedCount());

    // 记录指标
    metricsCollector.incrementCounter("batch_jobs_completed");
    metricsCollector.recordOperationTime("batch_job_execution", executionTime);
    metricsCollector.incrementCounter("batch_items_processed", result.getProcessedCount());

    java.util.Map<String, Object> context = new java.util.HashMap<>();
    context.put("jobId", jobId);
    context.put("processedCount", result.getProcessedCount());
    context.put("errorCount", result.getErrorCount());
    context.put("executionTime", executionTime);
    LogUtil.logBusiness("BATCH_JOB_COMPLETED", context);
  }

  /** 处理批处理任务失败 */
  private <T, R> void handleBatchJobFailure(
      BatchJob<T, R> batchJob, Exception exception, long executionTime) {
    String jobId = batchJob.getJobId();

    // 更新任务状态
    batchJob.setStatus(BatchJobStatus.FAILED);
    batchJob.setEndTime(LocalDateTime.now());

    // 创建失败结果
    BatchJobResult<R> result =
        new BatchJobResult<>(
            jobId,
            BatchJobStatus.FAILED,
            new ArrayList<>(),
            new ArrayList<>(),
            0,
            0,
            executionTime);
    result.setException(exception);

    // 完成Future
    batchJob.getFuture().completeExceptionally(exception);

    // 保存结果
    completedBatchJobs.put(jobId, result);

    // 更新统计
    totalBatchJobsFailed.incrementAndGet();

    // 记录指标
    metricsCollector.incrementCounter("batch_jobs_failed");

    LogUtil.logError(
        "BATCH_JOB_FAILED",
        "",
        "JOB_FAILED",
        String.format("批处理任务失败: %s, 耗时: %dms", jobId, executionTime),
        exception);
  }

  /** 取消批处理任务 */
  public boolean cancelBatchJob(String jobId) {
    BatchJob batchJob = activeBatchJobs.get(jobId);
    if (batchJob != null) {
      batchJob.setStatus(BatchJobStatus.CANCELLED);
      batchJob.getFuture().cancel(true);
      activeBatchJobs.remove(jobId);

      metricsCollector.incrementCounter("batch_jobs_cancelled");

      java.util.Map<String, Object> context = new java.util.HashMap<>();
      context.put("jobId", jobId);
      LogUtil.logBusiness("BATCH_JOB_CANCELLED", context);

      return true;
    }
    return false;
  }

  /** 获取批处理任务状态 */
  public BatchJobStatus getBatchJobStatus(String jobId) {
    BatchJob activeBatchJob = activeBatchJobs.get(jobId);
    if (activeBatchJob != null) {
      return activeBatchJob.getStatus();
    }

    BatchJobResult completedBatchJob = completedBatchJobs.get(jobId);
    if (completedBatchJob != null) {
      return completedBatchJob.getStatus();
    }

    return BatchJobStatus.NOT_FOUND;
  }

  /** 获取批处理任务结果 */
  public BatchJobResult getBatchJobResult(String jobId) {
    return completedBatchJobs.get(jobId);
  }

  /** 获取活跃的批处理任务 */
  public List<BatchJob> getActiveBatchJobs() {
    return new ArrayList<>(activeBatchJobs.values());
  }

  /** 获取批处理统计信息 */
  public BatchStatistics getBatchStatistics() {
    BatchStatistics stats = new BatchStatistics();
    stats.totalJobsSubmitted = totalBatchJobsSubmitted.get();
    stats.totalJobsCompleted = totalBatchJobsCompleted.get();
    stats.totalJobsFailed = totalBatchJobsFailed.get();
    stats.totalItemsProcessed = totalItemsProcessed.get();
    stats.activeJobsCount = activeBatchJobs.size();
    stats.threadPoolSize = batchExecutor.getPoolSize();
    stats.activeThreads = batchExecutor.getActiveCount();
    stats.queueSize = batchExecutor.getQueue().size();

    return stats;
  }

  /** 分割列表 */
  private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
    return IntStream.range(0, (list.size() + batchSize - 1) / batchSize)
        .mapToObj(i -> list.subList(i * batchSize, Math.min((i + 1) * batchSize, list.size())))
        .collect(Collectors.toList());
  }

  /** 生成任务ID */
  private String generateJobId() {
    return "batch_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
  }

  /** 监控批处理任务 */
  private void monitorBatchJobs() {
    try {
      BatchStatistics stats = getBatchStatistics();

      // 记录指标
      metricsCollector.recordGauge("batch_jobs_active", stats.activeJobsCount);
      metricsCollector.recordGauge("batch_thread_pool_size", stats.threadPoolSize);
      metricsCollector.recordGauge("batch_active_threads", stats.activeThreads);
      metricsCollector.recordGauge("batch_queue_size", stats.queueSize);

      // 检查线程池使用率
      if (stats.threadPoolSize > 0) {
        double threadUsage = (double) stats.activeThreads / stats.threadPoolSize;
        if (threadUsage > 0.9) {
          LogUtil.logError(
              "BATCH_THREAD_POOL_HIGH_USAGE",
              "",
              "HIGH_USAGE",
              String.format("批处理线程池使用率过高: %.1f%%", threadUsage * 100),
              null);
        }
      }

    } catch (Exception e) {
      LogUtil.logError("BATCH_JOB_MONITOR_ERROR", "", "MONITOR_ERROR", "批处理任务监控失败", e);
    }
  }

  /** 清理已完成的任务 */
  private void cleanupCompletedJobs() {
    try {
      LocalDateTime cutoffTime =
          LocalDateTime.now()
              .minus(Duration.ofMinutes(configManager.getBatchConfig().getJobRetentionMinutes()));

      List<String> jobsToRemove =
          completedBatchJobs.entrySet().stream()
              .filter(
                  entry -> {
                    BatchJobResult result = entry.getValue();
                    return result.getCompletionTime() != null
                        && result.getCompletionTime().isBefore(cutoffTime);
                  })
              .map(Map.Entry::getKey)
              .collect(Collectors.toList());

      for (String jobId : jobsToRemove) {
        completedBatchJobs.remove(jobId);
      }

      if (!jobsToRemove.isEmpty()) {
        java.util.Map<String, Object> context = new java.util.HashMap<>();
        context.put("cleanedJobsCount", jobsToRemove.size());
        LogUtil.logBusiness("BATCH_JOB_CLEANUP", context);
      }

    } catch (Exception e) {
      LogUtil.logError("BATCH_JOB_CLEANUP_ERROR", "", "CLEANUP_ERROR", "清理批处理任务失败", e);
    }
  }

  /** 关闭批处理器 */
  public void shutdown() {
    java.util.Map<String, Object> context = new java.util.HashMap<>();
    LogUtil.logBusiness("BATCH_PROCESSOR_SHUTDOWN", context);

    batchExecutor.shutdown();
    scheduledExecutor.shutdown();

    try {
      if (!batchExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
        batchExecutor.shutdownNow();
      }
      if (!scheduledExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
        scheduledExecutor.shutdownNow();
      }
    } catch (InterruptedException e) {
      batchExecutor.shutdownNow();
      scheduledExecutor.shutdownNow();
      Thread.currentThread().interrupt();
    }

    context = new java.util.HashMap<>();
    LogUtil.logBusiness("BATCH_PROCESSOR_SHUTDOWN_COMPLETE", context);
  }

  // 枚举定义
  public enum BatchJobStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED,
    NOT_FOUND
  }

  // 接口定义
  public interface PagedDataSource<T> {
    Page<T> getPage(Pageable pageable);
  }

  // 数据类定义
  public static class BatchOptions {
    private int batchSize = 100;
    private boolean parallel = false;
    private boolean transactional = false;
    private boolean continueOnError = true;
    private long pauseInterval = 0; // 毫秒
    private int pauseBatchCount = 10;
    private int pageSize = 1000;
    private Predicate<Object> filter;
    private Consumer<Object> onItemProcessed;
    private Consumer<BatchError> onItemError;

    public static BatchOptions defaultOptions() {
      return new BatchOptions();
    }

    public BatchOptions batchSize(int batchSize) {
      this.batchSize = batchSize;
      return this;
    }

    public BatchOptions parallel(boolean parallel) {
      this.parallel = parallel;
      return this;
    }

    public BatchOptions transactional(boolean transactional) {
      this.transactional = transactional;
      return this;
    }

    public BatchOptions continueOnError(boolean continueOnError) {
      this.continueOnError = continueOnError;
      return this;
    }

    public BatchOptions pauseInterval(long pauseInterval) {
      this.pauseInterval = pauseInterval;
      return this;
    }

    public BatchOptions pauseBatchCount(int pauseBatchCount) {
      this.pauseBatchCount = pauseBatchCount;
      return this;
    }

    public BatchOptions pageSize(int pageSize) {
      this.pageSize = pageSize;
      return this;
    }

    public BatchOptions filter(Predicate<Object> filter) {
      this.filter = filter;
      return this;
    }

    public BatchOptions onItemProcessed(Consumer<Object> onItemProcessed) {
      this.onItemProcessed = onItemProcessed;
      return this;
    }

    public BatchOptions onItemError(Consumer<BatchError> onItemError) {
      this.onItemError = onItemError;
      return this;
    }

    // Getters
    public int getBatchSize() {
      return batchSize;
    }

    public boolean isParallel() {
      return parallel;
    }

    public boolean isTransactional() {
      return transactional;
    }

    public boolean isContinueOnError() {
      return continueOnError;
    }

    public long getPauseInterval() {
      return pauseInterval;
    }

    public int getPauseBatchCount() {
      return pauseBatchCount;
    }

    public int getPageSize() {
      return pageSize;
    }

    public Predicate<Object> getFilter() {
      return filter;
    }

    public Consumer<Object> getOnItemProcessed() {
      return onItemProcessed;
    }

    public Consumer<BatchError> getOnItemError() {
      return onItemError;
    }
  }

  public static class BatchJob<T, R> {
    private final String jobId;
    private final List<T> items;
    private final Function<T, R> processor;
    private final BatchOptions options;
    private volatile BatchJobStatus status = BatchJobStatus.PENDING;
    private volatile LocalDateTime submitTime;
    private volatile LocalDateTime startTime;
    private volatile LocalDateTime endTime;
    private CompletableFuture<BatchJobResult<R>> future;

    public BatchJob(String jobId, List<T> items, Function<T, R> processor, BatchOptions options) {
      this.jobId = jobId;
      this.items = items;
      this.processor = processor;
      this.options = options;
      this.submitTime = LocalDateTime.now();
    }

    // Getters and Setters
    public String getJobId() {
      return jobId;
    }

    public List<T> getItems() {
      return items;
    }

    public Function<T, R> getProcessor() {
      return processor;
    }

    public BatchOptions getOptions() {
      return options;
    }

    public BatchJobStatus getStatus() {
      return status;
    }

    public void setStatus(BatchJobStatus status) {
      this.status = status;
    }

    public LocalDateTime getSubmitTime() {
      return submitTime;
    }

    public LocalDateTime getStartTime() {
      return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
      this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
      return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
      this.endTime = endTime;
    }

    public CompletableFuture<BatchJobResult<R>> getFuture() {
      return future;
    }

    public void setFuture(CompletableFuture<BatchJobResult<R>> future) {
      this.future = future;
    }
  }

  public static class BatchJobResult<R> {
    private final String jobId;
    private final BatchJobStatus status;
    private final List<R> results;
    private final List<BatchError> errors;
    private final long processedCount;
    private final long errorCount;
    private long executionTime;
    private Exception exception;
    private final LocalDateTime completionTime;

    public BatchJobResult(
        String jobId,
        BatchJobStatus status,
        List<R> results,
        List<BatchError> errors,
        long processedCount,
        long errorCount,
        long executionTime) {
      this.jobId = jobId;
      this.status = status;
      this.results = results;
      this.errors = errors;
      this.processedCount = processedCount;
      this.errorCount = errorCount;
      this.executionTime = executionTime;
      this.completionTime = LocalDateTime.now();
    }

    // Getters and Setters
    public String getJobId() {
      return jobId;
    }

    public BatchJobStatus getStatus() {
      return status;
    }

    public List<R> getResults() {
      return results;
    }

    public List<BatchError> getErrors() {
      return errors;
    }

    public long getProcessedCount() {
      return processedCount;
    }

    public long getErrorCount() {
      return errorCount;
    }

    public long getExecutionTime() {
      return executionTime;
    }

    public void setExecutionTime(long executionTime) {
      this.executionTime = executionTime;
    }

    public Exception getException() {
      return exception;
    }

    public void setException(Exception exception) {
      this.exception = exception;
    }

    public LocalDateTime getCompletionTime() {
      return completionTime;
    }
  }

  public static class BatchResult<R> {
    private final List<R> results;
    private final List<BatchError> errors;
    private final long processedCount;
    private final long errorCount;

    public BatchResult(
        List<R> results, List<BatchError> errors, long processedCount, long errorCount) {
      this.results = results;
      this.errors = errors;
      this.processedCount = processedCount;
      this.errorCount = errorCount;
    }

    // Getters
    public List<R> getResults() {
      return results;
    }

    public List<BatchError> getErrors() {
      return errors;
    }

    public long getProcessedCount() {
      return processedCount;
    }

    public long getErrorCount() {
      return errorCount;
    }
  }

  public static class BatchError {
    private final int index;
    private final Object item;
    private final Exception exception;
    private final LocalDateTime timestamp;

    public BatchError(int index, Object item, Exception exception) {
      this.index = index;
      this.item = item;
      this.exception = exception;
      this.timestamp = LocalDateTime.now();
    }

    // Getters
    public int getIndex() {
      return index;
    }

    public Object getItem() {
      return item;
    }

    public Exception getException() {
      return exception;
    }

    public LocalDateTime getTimestamp() {
      return timestamp;
    }
  }

  public static class BatchStatistics {
    public long totalJobsSubmitted;
    public long totalJobsCompleted;
    public long totalJobsFailed;
    public long totalItemsProcessed;
    public int activeJobsCount;
    public int threadPoolSize;
    public int activeThreads;
    public int queueSize;
  }

  // 自定义线程工厂
  private static class BatchThreadFactory implements ThreadFactory {
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    @Override
    public Thread newThread(Runnable r) {
      Thread thread = new Thread(r, "batch-processor-" + threadNumber.getAndIncrement());
      thread.setDaemon(false);
      thread.setPriority(Thread.NORM_PRIORITY);
      return thread;
    }
  }
}

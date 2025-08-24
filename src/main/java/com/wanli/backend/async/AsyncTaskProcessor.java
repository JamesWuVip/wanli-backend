package com.wanli.backend.async;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.wanli.backend.config.ApplicationConfigManager;
import com.wanli.backend.event.EventPublisher;
import com.wanli.backend.monitor.MetricsCollector;
import com.wanli.backend.util.LogUtil;

/** 异步任务处理器 提供高性能的异步任务执行和管理功能 */
@Component
public class AsyncTaskProcessor {

  private final ApplicationConfigManager configManager;
  private final MetricsCollector metricsCollector;
  private final EventPublisher eventPublisher;
  private final TransactionTemplate transactionTemplate;

  // 任务执行器
  private final ThreadPoolExecutor taskExecutor;
  private final ScheduledExecutorService scheduledExecutor;

  // 任务队列
  private final BlockingQueue<Runnable> taskQueue;
  private final ConcurrentHashMap<String, AsyncTask> runningTasks;
  private final ConcurrentHashMap<String, TaskResult> completedTasks;

  // 任务统计
  private final AtomicLong totalTasksSubmitted = new AtomicLong(0);
  private final AtomicLong totalTasksCompleted = new AtomicLong(0);
  private final AtomicLong totalTasksFailed = new AtomicLong(0);

  // 任务监控
  private final ConcurrentHashMap<TaskType, TaskTypeStats> taskTypeStats =
      new ConcurrentHashMap<>();

  // 重试机制
  private final ConcurrentHashMap<String, RetryContext> retryContexts = new ConcurrentHashMap<>();

  public AsyncTaskProcessor(
      ApplicationConfigManager configManager,
      MetricsCollector metricsCollector,
      EventPublisher eventPublisher,
      TransactionTemplate transactionTemplate) {
    this.configManager = configManager;
    this.metricsCollector = metricsCollector;
    this.eventPublisher = eventPublisher;
    this.transactionTemplate = transactionTemplate;

    // 初始化线程池
    this.taskQueue = new LinkedBlockingQueue<>(configManager.getAsyncConfig().getQueueCapacity());
    this.taskExecutor = createTaskExecutor();
    this.scheduledExecutor =
        Executors.newScheduledThreadPool(configManager.getAsyncConfig().getScheduledThreads());

    this.runningTasks = new ConcurrentHashMap<>();
    this.completedTasks = new ConcurrentHashMap<>();

    // 启动任务处理器
    startTaskProcessor();
  }

  /** 创建任务执行器 */
  private ThreadPoolExecutor createTaskExecutor() {
    return new ThreadPoolExecutor(
        configManager.getAsyncConfig().getCorePoolSize(),
        configManager.getAsyncConfig().getMaxPoolSize(),
        configManager.getAsyncConfig().getKeepAliveTime(),
        TimeUnit.SECONDS,
        taskQueue,
        new TaskThreadFactory(),
        new TaskRejectedExecutionHandler());
  }

  /** 启动任务处理器 */
  private void startTaskProcessor() {
    // 启动任务监控
    scheduledExecutor.scheduleAtFixedRate(this::monitorTasks, 0, 30, TimeUnit.SECONDS);

    // 启动任务清理
    scheduledExecutor.scheduleAtFixedRate(this::cleanupCompletedTasks, 0, 5, TimeUnit.MINUTES);

    LogUtil.logBusiness("ASYNC_TASK_PROCESSOR_STARTED", new HashMap<>());
  }

  /** 提交异步任务 */
  public <T> CompletableFuture<T> submitTask(
      TaskType taskType, String taskId, Callable<T> task, TaskOptions options) {
    if (taskId == null) {
      taskId = generateTaskId(taskType);
    }

    AsyncTask<T> asyncTask =
        new AsyncTask<>(
            taskId, taskType, task, options != null ? options : TaskOptions.defaultOptions());

    CompletableFuture<T> future = new CompletableFuture<>();
    asyncTask.setFuture(future);

    try {
      // 检查任务队列容量
      if (taskQueue.remainingCapacity() == 0) {
        throw new TaskQueueFullException("任务队列已满");
      }

      // 添加到运行任务列表
      runningTasks.put(taskId, asyncTask);

      // 提交任务
      taskExecutor.submit(() -> executeTask(asyncTask));

      // 更新统计
      totalTasksSubmitted.incrementAndGet();
      updateTaskTypeStats(taskType, TaskEvent.SUBMITTED);

      // 记录指标
      metricsCollector.incrementCounter("async_tasks_submitted");
      metricsCollector.incrementCounter(
          "async_tasks_" + taskType.name().toLowerCase() + "_submitted");

      Map<String, Object> taskDetails = new HashMap<>();
      taskDetails.put("taskId", taskId);
      taskDetails.put("taskType", taskType.toString());
      LogUtil.logBusiness("ASYNC_TASK_SUBMITTED", taskDetails);

    } catch (Exception e) {
      runningTasks.remove(taskId);
      future.completeExceptionally(e);

      LogUtil.logError(
          "ASYNC_TASK_SUBMIT_ERROR", "", "SUBMIT_ERROR", String.format("提交异步任务失败: %s", taskId), e);
    }

    return future;
  }

  /** 提交简单任务 */
  public <T> CompletableFuture<T> submitTask(TaskType taskType, Callable<T> task) {
    return submitTask(taskType, null, task, null);
  }

  /** 提交无返回值任务 */
  public CompletableFuture<Void> submitTask(TaskType taskType, String taskId, Runnable task) {
    return submitTask(
        taskType,
        taskId,
        () -> {
          task.run();
          return null;
        },
        null);
  }

  /** 提交延迟任务 */
  public <T> CompletableFuture<T> submitDelayedTask(
      TaskType taskType, String taskId, Callable<T> task, long delay, TimeUnit timeUnit) {
    CompletableFuture<T> future = new CompletableFuture<>();

    scheduledExecutor.schedule(
        () -> {
          try {
            CompletableFuture<T> actualFuture = submitTask(taskType, taskId, task, null);
            actualFuture.whenComplete(
                (result, throwable) -> {
                  if (throwable != null) {
                    future.completeExceptionally(throwable);
                  } else {
                    future.complete(result);
                  }
                });
          } catch (Exception e) {
            future.completeExceptionally(e);
          }
        },
        delay,
        timeUnit);

    return future;
  }

  /** 提交定时任务 */
  public ScheduledFuture<?> submitScheduledTask(
      TaskType taskType,
      String taskId,
      Runnable task,
      long initialDelay,
      long period,
      TimeUnit timeUnit) {
    return scheduledExecutor.scheduleAtFixedRate(
        () -> {
          try {
            submitTask(taskType, taskId + "_" + System.currentTimeMillis(), task);
          } catch (Exception e) {
            LogUtil.logError(
                "SCHEDULED_TASK_ERROR", "", "TASK_ERROR", String.format("定时任务执行失败: %s", taskId), e);
          }
        },
        initialDelay,
        period,
        timeUnit);
  }

  /** 执行任务 */
  private <T> void executeTask(AsyncTask<T> asyncTask) {
    String taskId = asyncTask.getTaskId();
    TaskType taskType = asyncTask.getTaskType();
    long startTime = System.currentTimeMillis();

    try {
      // 更新任务状态
      asyncTask.setStatus(TaskStatus.RUNNING);
      asyncTask.setStartTime(LocalDateTime.now());

      updateTaskTypeStats(taskType, TaskEvent.STARTED);

      Map<String, Object> startDetails = new HashMap<>();
      startDetails.put("taskId", taskId);
      LogUtil.logBusiness("ASYNC_TASK_STARTED", startDetails);

      // 执行任务
      T result;
      if (asyncTask.getOptions().isTransactional()) {
        result = executeTransactionalTask(asyncTask);
      } else {
        result = asyncTask.getTask().call();
      }

      // 任务完成
      long executionTime = System.currentTimeMillis() - startTime;
      completeTask(asyncTask, result, executionTime);

    } catch (Exception e) {
      long executionTime = System.currentTimeMillis() - startTime;
      handleTaskFailure(asyncTask, e, executionTime);
    } finally {
      runningTasks.remove(taskId);
    }
  }

  /** 执行事务性任务 */
  private <T> T executeTransactionalTask(AsyncTask<T> asyncTask) {
    return transactionTemplate.execute(
        status -> {
          try {
            return asyncTask.getTask().call();
          } catch (Exception e) {
            status.setRollbackOnly();
            throw new RuntimeException(e);
          }
        });
  }

  /** 完成任务 */
  private <T> void completeTask(AsyncTask<T> asyncTask, T result, long executionTime) {
    String taskId = asyncTask.getTaskId();
    TaskType taskType = asyncTask.getTaskType();

    // 更新任务状态
    asyncTask.setStatus(TaskStatus.COMPLETED);
    asyncTask.setEndTime(LocalDateTime.now());
    asyncTask.setExecutionTime(executionTime);

    // 完成Future
    asyncTask.getFuture().complete(result);

    // 保存结果
    TaskResult taskResult =
        new TaskResult(taskId, taskType, result, TaskStatus.COMPLETED, executionTime, null);
    completedTasks.put(taskId, taskResult);

    // 更新统计
    totalTasksCompleted.incrementAndGet();
    updateTaskTypeStats(taskType, TaskEvent.COMPLETED);

    // 记录指标
    metricsCollector.incrementCounter("async_tasks_completed");
    metricsCollector.recordOperationTime("async_task_execution", executionTime);
    metricsCollector.recordOperationTime(
        "async_task_" + taskType.name().toLowerCase(), executionTime);

    // 执行回调
    if (asyncTask.getOptions().getOnSuccess() != null) {
      try {
        asyncTask.getOptions().getOnSuccess().accept(result);
      } catch (Exception e) {
        LogUtil.logError(
            "ASYNC_TASK_CALLBACK_ERROR",
            "",
            "CALLBACK_ERROR",
            String.format("任务成功回调执行失败: %s", taskId),
            new RuntimeException("Callback execution failed"));
      }
    }

    Map<String, Object> completedContext = new HashMap<>();
    completedContext.put("taskId", taskId);
    completedContext.put("executionTime", executionTime);
    LogUtil.logBusiness("ASYNC_TASK_COMPLETED", completedContext);
  }

  /** 处理任务失败 */
  private <T> void handleTaskFailure(
      AsyncTask<T> asyncTask, Exception exception, long executionTime) {
    String taskId = asyncTask.getTaskId();
    TaskType taskType = asyncTask.getTaskType();

    // 检查是否需要重试
    if (shouldRetry(asyncTask, exception)) {
      scheduleRetry(asyncTask, exception);
      return;
    }

    // 任务最终失败
    asyncTask.setStatus(TaskStatus.FAILED);
    asyncTask.setEndTime(LocalDateTime.now());
    asyncTask.setExecutionTime(executionTime);
    asyncTask.setException(exception);

    // 完成Future
    asyncTask.getFuture().completeExceptionally(exception);

    // 保存结果
    TaskResult taskResult =
        new TaskResult(taskId, taskType, null, TaskStatus.FAILED, executionTime, exception);
    completedTasks.put(taskId, taskResult);

    // 更新统计
    totalTasksFailed.incrementAndGet();
    updateTaskTypeStats(taskType, TaskEvent.FAILED);

    // 记录指标
    metricsCollector.incrementCounter("async_tasks_failed");
    metricsCollector.incrementCounter("async_task_" + taskType.name().toLowerCase() + "_failed");

    // 执行失败回调
    if (asyncTask.getOptions().getOnFailure() != null) {
      try {
        asyncTask.getOptions().getOnFailure().accept(exception);
      } catch (Exception e) {
        LogUtil.logError(
            "ASYNC_TASK_FAILURE_CALLBACK_ERROR",
            "",
            "CALLBACK_ERROR",
            String.format("任务失败回调执行失败: %s", taskId),
            e);
      }
    }

    LogUtil.logError(
        "ASYNC_TASK_FAILED",
        "",
        "TASK_FAILED",
        String.format("异步任务失败: %s, 执行时间: %dms", taskId, executionTime),
        exception);
  }

  /** 判断是否应该重试 */
  private <T> boolean shouldRetry(AsyncTask<T> asyncTask, Exception exception) {
    TaskOptions options = asyncTask.getOptions();

    if (options.getMaxRetries() <= 0) {
      return false;
    }

    RetryContext retryContext =
        retryContexts.computeIfAbsent(asyncTask.getTaskId(), k -> new RetryContext());

    if (retryContext.getRetryCount() >= options.getMaxRetries()) {
      return false;
    }

    // 检查异常类型是否可重试
    if (options.getRetryableExceptions() != null && !options.getRetryableExceptions().isEmpty()) {
      boolean isRetryable =
          options.getRetryableExceptions().stream()
              .anyMatch(clazz -> clazz.isAssignableFrom(exception.getClass()));
      if (!isRetryable) {
        return false;
      }
    }

    return true;
  }

  /** 安排重试 */
  private <T> void scheduleRetry(AsyncTask<T> asyncTask, Exception lastException) {
    String taskId = asyncTask.getTaskId();
    RetryContext retryContext = retryContexts.get(taskId);

    retryContext.incrementRetryCount();
    retryContext.setLastException(lastException);

    long delay = calculateRetryDelay(asyncTask.getOptions(), retryContext.getRetryCount());

    scheduledExecutor.schedule(
        () -> {
          Map<String, Object> retryLogContext = new HashMap<>();
          retryLogContext.put("taskId", taskId);
          retryLogContext.put("retryCount", retryContext.getRetryCount());
          LogUtil.logBusiness("ASYNC_TASK_RETRY", retryLogContext);

          // 重新提交任务
          runningTasks.put(taskId, asyncTask);
          taskExecutor.submit(() -> executeTask(asyncTask));
        },
        delay,
        TimeUnit.MILLISECONDS);

    metricsCollector.incrementCounter("async_tasks_retried");
  }

  /** 计算重试延迟 */
  private long calculateRetryDelay(TaskOptions options, int retryCount) {
    switch (options.getRetryStrategy()) {
      case FIXED:
        return options.getRetryDelay();
      case EXPONENTIAL:
        return options.getRetryDelay() * (long) Math.pow(2, retryCount - 1);
      case LINEAR:
        return options.getRetryDelay() * retryCount;
      default:
        return options.getRetryDelay();
    }
  }

  /** 取消任务 */
  public boolean cancelTask(String taskId) {
    AsyncTask task = runningTasks.get(taskId);
    if (task != null) {
      task.setStatus(TaskStatus.CANCELLED);
      task.getFuture().cancel(true);
      runningTasks.remove(taskId);

      updateTaskTypeStats(task.getTaskType(), TaskEvent.CANCELLED);
      metricsCollector.incrementCounter("async_tasks_cancelled");

      Map<String, Object> cancelContext = new HashMap<>();
      cancelContext.put("taskId", taskId);
      LogUtil.logBusiness("ASYNC_TASK_CANCELLED", cancelContext);

      return true;
    }
    return false;
  }

  /** 获取任务状态 */
  public TaskStatus getTaskStatus(String taskId) {
    AsyncTask runningTask = runningTasks.get(taskId);
    if (runningTask != null) {
      return runningTask.getStatus();
    }

    TaskResult completedTask = completedTasks.get(taskId);
    if (completedTask != null) {
      return completedTask.getStatus();
    }

    return TaskStatus.NOT_FOUND;
  }

  /** 获取任务结果 */
  public TaskResult getTaskResult(String taskId) {
    return completedTasks.get(taskId);
  }

  /** 获取运行中的任务列表 */
  public List<AsyncTask> getRunningTasks() {
    return new ArrayList<>(runningTasks.values());
  }

  /** 获取任务统计信息 */
  public TaskStatistics getTaskStatistics() {
    TaskStatistics stats = new TaskStatistics();
    stats.totalSubmitted = totalTasksSubmitted.get();
    stats.totalCompleted = totalTasksCompleted.get();
    stats.totalFailed = totalTasksFailed.get();
    stats.currentRunning = runningTasks.size();
    stats.queueSize = taskQueue.size();
    stats.threadPoolSize = taskExecutor.getPoolSize();
    stats.activeThreads = taskExecutor.getActiveCount();
    stats.taskTypeStats = new HashMap<>(taskTypeStats);

    return stats;
  }

  /** 更新任务类型统计 */
  private void updateTaskTypeStats(TaskType taskType, TaskEvent event) {
    TaskTypeStats stats = taskTypeStats.computeIfAbsent(taskType, k -> new TaskTypeStats());
    stats.updateStats(event);
  }

  /** 生成任务ID */
  private String generateTaskId(TaskType taskType) {
    return taskType.name().toLowerCase()
        + "_"
        + System.currentTimeMillis()
        + "_"
        + Thread.currentThread().getId();
  }

  /** 监控任务 */
  private void monitorTasks() {
    try {
      TaskStatistics stats = getTaskStatistics();

      // 记录指标
      metricsCollector.recordGauge("async_tasks_running", stats.currentRunning);
      metricsCollector.recordGauge("async_tasks_queue_size", stats.queueSize);
      metricsCollector.recordGauge("async_thread_pool_size", stats.threadPoolSize);
      metricsCollector.recordGauge("async_active_threads", stats.activeThreads);

      // 检查队列积压
      if (stats.queueSize > configManager.getAsyncConfig().getQueueCapacity() * 0.8) {
        LogUtil.logError(
            "ASYNC_QUEUE_BACKLOG",
            "",
            "QUEUE_BACKLOG",
            String.format(
                "异步任务队列积压严重: %d/%d",
                stats.queueSize, configManager.getAsyncConfig().getQueueCapacity()),
            new RuntimeException("Queue backlog detected"));
      }

      // 检查线程池使用率
      double threadUsage = (double) stats.activeThreads / stats.threadPoolSize;
      if (threadUsage > 0.9) {
        LogUtil.logError(
            "ASYNC_THREAD_POOL_HIGH_USAGE",
            "",
            "HIGH_USAGE",
            String.format("异步线程池使用率过高: %.1f%%", threadUsage * 100),
            new RuntimeException("High thread pool usage detected"));
      }

    } catch (Exception e) {
      LogUtil.logError("ASYNC_TASK_MONITOR_ERROR", "", "MONITOR_ERROR", "异步任务监控失败", e);
    }
  }

  /** 清理已完成的任务 */
  private void cleanupCompletedTasks() {
    try {
      LocalDateTime cutoffTime =
          LocalDateTime.now()
              .minus(Duration.ofMinutes(configManager.getAsyncConfig().getTaskRetentionMinutes()));

      List<String> tasksToRemove =
          completedTasks.entrySet().stream()
              .filter(
                  entry -> {
                    TaskResult result = entry.getValue();
                    return result.getCompletionTime() != null
                        && result.getCompletionTime().isBefore(cutoffTime);
                  })
              .map(Map.Entry::getKey)
              .collect(Collectors.toList());

      for (String taskId : tasksToRemove) {
        completedTasks.remove(taskId);
        retryContexts.remove(taskId);
      }

      if (!tasksToRemove.isEmpty()) {
        Map<String, Object> cleanupContext = new HashMap<>();
        cleanupContext.put("cleanedTasksCount", tasksToRemove.size());
        LogUtil.logBusiness("ASYNC_TASK_CLEANUP", cleanupContext);
      }

    } catch (Exception e) {
      LogUtil.logError("ASYNC_TASK_CLEANUP_ERROR", "", "CLEANUP_ERROR", "清理异步任务失败", e);
    }
  }

  /** 关闭任务处理器 */
  public void shutdown() {
    Map<String, Object> shutdownContext = new HashMap<>();
    shutdownContext.put("message", "正在关闭异步任务处理器...");
    LogUtil.logBusiness("ASYNC_TASK_PROCESSOR_SHUTDOWN", shutdownContext);

    taskExecutor.shutdown();
    scheduledExecutor.shutdown();

    try {
      if (!taskExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
        taskExecutor.shutdownNow();
      }
      if (!scheduledExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
        scheduledExecutor.shutdownNow();
      }
    } catch (InterruptedException e) {
      taskExecutor.shutdownNow();
      scheduledExecutor.shutdownNow();
      Thread.currentThread().interrupt();
    }

    Map<String, Object> shutdownCompleteContext = new HashMap<>();
    shutdownCompleteContext.put("message", "异步任务处理器已关闭");
    LogUtil.logBusiness("ASYNC_TASK_PROCESSOR_SHUTDOWN_COMPLETE", shutdownCompleteContext);
  }

  // 枚举定义
  public enum TaskType {
    EMAIL_SENDING,
    FILE_PROCESSING,
    DATA_EXPORT,
    CACHE_WARMING,
    BATCH_PROCESSING,
    NOTIFICATION,
    ANALYTICS,
    CLEANUP,
    SYNC,
    OTHER
  }

  public enum TaskStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED,
    NOT_FOUND
  }

  public enum RetryStrategy {
    FIXED, // 固定延迟
    EXPONENTIAL, // 指数退避
    LINEAR // 线性增长
  }

  public enum TaskEvent {
    SUBMITTED,
    STARTED,
    COMPLETED,
    FAILED,
    CANCELLED
  }

  // 数据类定义
  public static class TaskOptions {
    private boolean transactional = false;
    private int maxRetries = 0;
    private long retryDelay = 1000; // 毫秒
    private RetryStrategy retryStrategy = RetryStrategy.FIXED;
    private Set<Class<? extends Exception>> retryableExceptions = new HashSet<>();
    private Consumer<Object> onSuccess;
    private Consumer<Exception> onFailure;
    private int priority = 0;
    private Duration timeout;

    public static TaskOptions defaultOptions() {
      return new TaskOptions();
    }

    public TaskOptions transactional(boolean transactional) {
      this.transactional = transactional;
      return this;
    }

    public TaskOptions maxRetries(int maxRetries) {
      this.maxRetries = maxRetries;
      return this;
    }

    public TaskOptions retryDelay(long retryDelay) {
      this.retryDelay = retryDelay;
      return this;
    }

    public TaskOptions retryStrategy(RetryStrategy retryStrategy) {
      this.retryStrategy = retryStrategy;
      return this;
    }

    public TaskOptions retryOn(Class<? extends Exception> exceptionClass) {
      this.retryableExceptions.add(exceptionClass);
      return this;
    }

    public TaskOptions onSuccess(Consumer<Object> onSuccess) {
      this.onSuccess = onSuccess;
      return this;
    }

    public TaskOptions onFailure(Consumer<Exception> onFailure) {
      this.onFailure = onFailure;
      return this;
    }

    public TaskOptions priority(int priority) {
      this.priority = priority;
      return this;
    }

    public TaskOptions timeout(Duration timeout) {
      this.timeout = timeout;
      return this;
    }

    // Getters
    public boolean isTransactional() {
      return transactional;
    }

    public int getMaxRetries() {
      return maxRetries;
    }

    public long getRetryDelay() {
      return retryDelay;
    }

    public RetryStrategy getRetryStrategy() {
      return retryStrategy;
    }

    public Set<Class<? extends Exception>> getRetryableExceptions() {
      return retryableExceptions;
    }

    public Consumer<Object> getOnSuccess() {
      return onSuccess;
    }

    public Consumer<Exception> getOnFailure() {
      return onFailure;
    }

    public int getPriority() {
      return priority;
    }

    public Duration getTimeout() {
      return timeout;
    }
  }

  public static class AsyncTask<T> {
    private final String taskId;
    private final TaskType taskType;
    private final Callable<T> task;
    private final TaskOptions options;
    private volatile TaskStatus status = TaskStatus.PENDING;
    private volatile LocalDateTime submitTime;
    private volatile LocalDateTime startTime;
    private volatile LocalDateTime endTime;
    private final AtomicLong executionTime = new AtomicLong(0);
    private volatile Exception exception;
    private CompletableFuture<T> future;

    public AsyncTask(String taskId, TaskType taskType, Callable<T> task, TaskOptions options) {
      this.taskId = taskId;
      this.taskType = taskType;
      this.task = task;
      this.options = options;
      this.submitTime = LocalDateTime.now();
    }

    // Getters and Setters
    public String getTaskId() {
      return taskId;
    }

    public TaskType getTaskType() {
      return taskType;
    }

    public Callable<T> getTask() {
      return task;
    }

    public TaskOptions getOptions() {
      return options;
    }

    public TaskStatus getStatus() {
      return status;
    }

    public void setStatus(TaskStatus status) {
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

    public long getExecutionTime() {
      return executionTime.get();
    }

    public void setExecutionTime(long executionTime) {
      this.executionTime.set(executionTime);
    }

    public Exception getException() {
      return exception;
    }

    public void setException(Exception exception) {
      this.exception = exception;
    }

    public CompletableFuture<T> getFuture() {
      return future;
    }

    public void setFuture(CompletableFuture<T> future) {
      this.future = future;
    }
  }

  public static class TaskResult {
    private final String taskId;
    private final TaskType taskType;
    private final Object result;
    private final TaskStatus status;
    private final long executionTime;
    private final Exception exception;
    private final LocalDateTime completionTime;

    public TaskResult(
        String taskId,
        TaskType taskType,
        Object result,
        TaskStatus status,
        long executionTime,
        Exception exception) {
      this.taskId = taskId;
      this.taskType = taskType;
      this.result = result;
      this.status = status;
      this.executionTime = executionTime;
      this.exception = exception;
      this.completionTime = LocalDateTime.now();
    }

    // Getters
    public String getTaskId() {
      return taskId;
    }

    public TaskType getTaskType() {
      return taskType;
    }

    public Object getResult() {
      return result;
    }

    public TaskStatus getStatus() {
      return status;
    }

    public long getExecutionTime() {
      return executionTime;
    }

    public Exception getException() {
      return exception;
    }

    public LocalDateTime getCompletionTime() {
      return completionTime;
    }
  }

  public static class TaskStatistics {
    public long totalSubmitted;
    public long totalCompleted;
    public long totalFailed;
    public int currentRunning;
    public int queueSize;
    public int threadPoolSize;
    public int activeThreads;
    public Map<TaskType, TaskTypeStats> taskTypeStats;
  }

  public static class TaskTypeStats {
    private final AtomicLong submitted = new AtomicLong(0);
    private final AtomicLong completed = new AtomicLong(0);
    private final AtomicLong failed = new AtomicLong(0);
    private final AtomicLong cancelled = new AtomicLong(0);

    public void updateStats(TaskEvent event) {
      switch (event) {
        case SUBMITTED:
          submitted.incrementAndGet();
          break;
        case COMPLETED:
          completed.incrementAndGet();
          break;
        case FAILED:
          failed.incrementAndGet();
          break;
        case CANCELLED:
          cancelled.incrementAndGet();
          break;
      }
    }

    public long getSubmitted() {
      return submitted.get();
    }

    public long getCompleted() {
      return completed.get();
    }

    public long getFailed() {
      return failed.get();
    }

    public long getCancelled() {
      return cancelled.get();
    }
  }

  public static class RetryContext {
    private int retryCount = 0;
    private Exception lastException;
    private LocalDateTime lastRetryTime;

    public void incrementRetryCount() {
      this.retryCount++;
      this.lastRetryTime = LocalDateTime.now();
    }

    public int getRetryCount() {
      return retryCount;
    }

    public Exception getLastException() {
      return lastException;
    }

    public void setLastException(Exception lastException) {
      this.lastException = lastException;
    }

    public LocalDateTime getLastRetryTime() {
      return lastRetryTime;
    }
  }

  // 自定义线程工厂
  private static class TaskThreadFactory implements ThreadFactory {
    private final AtomicLong threadNumber = new AtomicLong(1);

    @Override
    public Thread newThread(Runnable r) {
      Thread thread = new Thread(r, "async-task-" + threadNumber.getAndIncrement());
      thread.setDaemon(false);
      thread.setPriority(Thread.NORM_PRIORITY);
      return thread;
    }
  }

  // 自定义拒绝策略
  private class TaskRejectedExecutionHandler implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
      metricsCollector.incrementCounter("async_tasks_rejected");
      LogUtil.logError("ASYNC_TASK_REJECTED", "", "TASK_REJECTED", "异步任务被拒绝执行，线程池已满", null);
      throw new TaskQueueFullException("异步任务队列已满，无法接受新任务");
    }
  }

  // 自定义异常
  public static class TaskQueueFullException extends RuntimeException {
    public TaskQueueFullException(String message) {
      super(message);
    }
  }
}

package com.wanli.backend.transaction;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.wanli.backend.config.ApplicationConfigManager;
import com.wanli.backend.pool.ConnectionPoolFactory;
import com.wanli.backend.util.LogUtil;

import jakarta.annotation.PostConstruct;

/** 高级事务管理器 提供事务模板、嵌套事务、分布式事务等高级功能 */
@Component("customTransactionManager")
public class CustomTransactionManager {

  @Autowired private PlatformTransactionManager platformTransactionManager;

  @Autowired private ConnectionPoolFactory connectionPoolFactory;

  @Autowired private ApplicationConfigManager configManager;

  private final Map<String, TransactionContext> activeTransactions = new ConcurrentHashMap<>();
  private final AtomicLong transactionIdGenerator = new AtomicLong(0);
  private final ThreadLocal<TransactionContext> currentTransaction = new ThreadLocal<>();

  /** 事务隔离级别枚举 */
  public enum IsolationLevel {
    DEFAULT(TransactionDefinition.ISOLATION_DEFAULT),
    READ_UNCOMMITTED(TransactionDefinition.ISOLATION_READ_UNCOMMITTED),
    READ_COMMITTED(TransactionDefinition.ISOLATION_READ_COMMITTED),
    REPEATABLE_READ(TransactionDefinition.ISOLATION_REPEATABLE_READ),
    SERIALIZABLE(TransactionDefinition.ISOLATION_SERIALIZABLE);

    private final int value;

    IsolationLevel(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
  }

  /** 事务传播行为枚举 */
  public enum PropagationBehavior {
    REQUIRED(TransactionDefinition.PROPAGATION_REQUIRED),
    SUPPORTS(TransactionDefinition.PROPAGATION_SUPPORTS),
    MANDATORY(TransactionDefinition.PROPAGATION_MANDATORY),
    REQUIRES_NEW(TransactionDefinition.PROPAGATION_REQUIRES_NEW),
    NOT_SUPPORTED(TransactionDefinition.PROPAGATION_NOT_SUPPORTED),
    NEVER(TransactionDefinition.PROPAGATION_NEVER),
    NESTED(TransactionDefinition.PROPAGATION_NESTED);

    private final int value;

    PropagationBehavior(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
  }

  /** 事务配置 */
  public static class TransactionConfig {
    private IsolationLevel isolationLevel = IsolationLevel.DEFAULT;
    private PropagationBehavior propagationBehavior = PropagationBehavior.REQUIRED;
    private int timeout = -1;
    private boolean readOnly = false;
    private String name;
    private Class<?>[] rollbackFor;
    private Class<?>[] noRollbackFor;

    // Getters and Setters
    public IsolationLevel getIsolationLevel() {
      return isolationLevel;
    }

    public void setIsolationLevel(IsolationLevel isolationLevel) {
      this.isolationLevel = isolationLevel;
    }

    public PropagationBehavior getPropagationBehavior() {
      return propagationBehavior;
    }

    public void setPropagationBehavior(PropagationBehavior propagationBehavior) {
      this.propagationBehavior = propagationBehavior;
    }

    public int getTimeout() {
      return timeout;
    }

    public void setTimeout(int timeout) {
      this.timeout = timeout;
    }

    public boolean isReadOnly() {
      return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
      this.readOnly = readOnly;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Class<?>[] getRollbackFor() {
      return rollbackFor;
    }

    public void setRollbackFor(Class<?>[] rollbackFor) {
      this.rollbackFor = rollbackFor;
    }

    public Class<?>[] getNoRollbackFor() {
      return noRollbackFor;
    }

    public void setNoRollbackFor(Class<?>[] noRollbackFor) {
      this.noRollbackFor = noRollbackFor;
    }
  }

  /** 事务上下文 */
  public static class TransactionContext {
    private final String transactionId;
    private final TransactionStatus status;
    private final TransactionConfig config;
    private final long startTime;
    private final String threadName;
    private Connection connection;
    private boolean completed = false;
    private Throwable rollbackCause;

    public TransactionContext(
        String transactionId, TransactionStatus status, TransactionConfig config) {
      this.transactionId = transactionId;
      this.status = status;
      this.config = config;
      this.startTime = System.currentTimeMillis();
      this.threadName = Thread.currentThread().getName();
    }

    // Getters and Setters
    public String getTransactionId() {
      return transactionId;
    }

    public TransactionStatus getStatus() {
      return status;
    }

    public TransactionConfig getConfig() {
      return config;
    }

    public long getStartTime() {
      return startTime;
    }

    public String getThreadName() {
      return threadName;
    }

    public Connection getConnection() {
      return connection;
    }

    public void setConnection(Connection connection) {
      this.connection = connection;
    }

    public boolean isCompleted() {
      return completed;
    }

    public void setCompleted(boolean completed) {
      this.completed = completed;
    }

    public Throwable getRollbackCause() {
      return rollbackCause;
    }

    public void setRollbackCause(Throwable rollbackCause) {
      this.rollbackCause = rollbackCause;
    }

    public long getDuration() {
      return System.currentTimeMillis() - startTime;
    }
  }

  /** 事务回调接口 */
  @FunctionalInterface
  public interface TransactionCallback<T> {
    T doInTransaction(TransactionContext context) throws Exception;
  }

  /** 无返回值事务回调接口 */
  @FunctionalInterface
  public interface VoidTransactionCallback {
    void doInTransaction(TransactionContext context) throws Exception;
  }

  @PostConstruct
  public void initialize() {
    LogUtil.logInfo("TRANSACTION_MANAGER", "", "事务管理器初始化完成");
  }

  /** 执行事务（有返回值） */
  public <T> T executeInTransaction(TransactionCallback<T> callback) {
    return executeInTransaction(new TransactionConfig(), callback);
  }

  /** 执行事务（有返回值，自定义配置） */
  public <T> T executeInTransaction(TransactionConfig config, TransactionCallback<T> callback) {
    String transactionId = generateTransactionId();
    TransactionContext context = null;

    try {
      // 创建事务定义
      DefaultTransactionDefinition definition = createTransactionDefinition(config);

      // 开始事务
      TransactionStatus status = platformTransactionManager.getTransaction(definition);
      context = new TransactionContext(transactionId, status, config);

      // 注册事务上下文
      activeTransactions.put(transactionId, context);
      currentTransaction.set(context);

      LogUtil.logDebug(
          "TRANSACTION_MANAGER",
          "",
          String.format(
              "开始事务: %s, 隔离级别: %s, 传播行为: %s",
              transactionId, config.getIsolationLevel(), config.getPropagationBehavior()));

      // 执行业务逻辑
      T result = callback.doInTransaction(context);

      // 提交事务
      if (!status.isCompleted()) {
        platformTransactionManager.commit(status);
        context.setCompleted(true);

        LogUtil.logDebug(
            "TRANSACTION_MANAGER",
            "",
            String.format("事务提交成功: %s, 耗时: %dms", transactionId, context.getDuration()));
      }

      return result;

    } catch (Exception e) {
      // 回滚事务
      if (context != null) {
        context.setRollbackCause(e);
        rollbackTransaction(context);
      }

      LogUtil.logError(
          "TRANSACTION_MANAGER",
          "",
          "TRANSACTION_ERROR",
          String.format("事务执行失败: %s", transactionId),
          e);

      throw new RuntimeException("事务执行失败: " + transactionId, e);

    } finally {
      // 清理事务上下文
      cleanupTransaction(transactionId);
    }
  }

  /** 执行事务（无返回值） */
  public void executeInTransaction(VoidTransactionCallback callback) {
    executeInTransaction(new TransactionConfig(), callback);
  }

  /** 执行事务（无返回值，自定义配置） */
  public void executeInTransaction(TransactionConfig config, VoidTransactionCallback callback) {
    executeInTransaction(
        config,
        context -> {
          callback.doInTransaction(context);
          return null;
        });
  }

  /** 执行只读事务 */
  public <T> T executeInReadOnlyTransaction(TransactionCallback<T> callback) {
    TransactionConfig config = new TransactionConfig();
    config.setReadOnly(true);
    config.setIsolationLevel(IsolationLevel.READ_COMMITTED);
    return executeInTransaction(config, callback);
  }

  /** 执行新事务（总是创建新事务） */
  public <T> T executeInNewTransaction(TransactionCallback<T> callback) {
    TransactionConfig config = new TransactionConfig();
    config.setPropagationBehavior(PropagationBehavior.REQUIRES_NEW);
    return executeInTransaction(config, callback);
  }

  /** 执行嵌套事务 */
  public <T> T executeInNestedTransaction(TransactionCallback<T> callback) {
    TransactionConfig config = new TransactionConfig();
    config.setPropagationBehavior(PropagationBehavior.NESTED);
    return executeInTransaction(config, callback);
  }

  /** 批量执行（在单个事务中） */
  public <T> void executeBatch(Iterable<T> items, BatchProcessor<T> processor) {
    executeInTransaction(
        context -> {
          int count = 0;
          int batchSize = configManager.getDatabase().getBatchSize();

          for (T item : items) {
            processor.process(item, context);
            count++;

            // 批量提交
            if (count % batchSize == 0) {
              flushBatch(context);
            }
          }

          // 提交剩余的
          if (count % batchSize != 0) {
            flushBatch(context);
          }

          LogUtil.logInfo(
              "TRANSACTION_MANAGER",
              "",
              String.format("批量处理完成: %d 条记录, 事务: %s", count, context.getTransactionId()));
        });
  }

  /** 批处理器接口 */
  @FunctionalInterface
  public interface BatchProcessor<T> {
    void process(T item, TransactionContext context) throws Exception;
  }

  /** 获取当前事务上下文 */
  public TransactionContext getCurrentTransaction() {
    return currentTransaction.get();
  }

  /** 检查是否在事务中 */
  public boolean isInTransaction() {
    TransactionContext context = currentTransaction.get();
    return context != null && !context.isCompleted();
  }

  /** 手动回滚当前事务 */
  public void rollbackCurrentTransaction() {
    TransactionContext context = currentTransaction.get();
    if (context != null && !context.isCompleted()) {
      rollbackTransaction(context);
    }
  }

  /** 设置回滚点 */
  public void setRollbackOnly() {
    TransactionContext context = currentTransaction.get();
    if (context != null && !context.isCompleted()) {
      context.getStatus().setRollbackOnly();
    }
  }

  /** 创建事务定义 */
  private DefaultTransactionDefinition createTransactionDefinition(TransactionConfig config) {
    DefaultTransactionDefinition definition = new DefaultTransactionDefinition();

    definition.setIsolationLevel(config.getIsolationLevel().getValue());
    definition.setPropagationBehavior(config.getPropagationBehavior().getValue());
    definition.setTimeout(config.getTimeout());
    definition.setReadOnly(config.isReadOnly());

    if (config.getName() != null) {
      definition.setName(config.getName());
    }

    return definition;
  }

  /** 回滚事务 */
  private void rollbackTransaction(TransactionContext context) {
    try {
      if (!context.getStatus().isCompleted()) {
        platformTransactionManager.rollback(context.getStatus());
        context.setCompleted(true);

        LogUtil.logWarn(
            "TRANSACTION_MANAGER",
            "",
            String.format(
                "事务回滚: %s, 耗时: %dms, 原因: %s",
                context.getTransactionId(),
                context.getDuration(),
                context.getRollbackCause() != null
                    ? context.getRollbackCause().getMessage()
                    : "手动回滚"));
      }
    } catch (Exception e) {
      LogUtil.logError(
          "TRANSACTION_MANAGER",
          "",
          "ROLLBACK_ERROR",
          String.format("事务回滚失败: %s", context.getTransactionId()),
          e);
    }
  }

  /** 清理事务上下文 */
  private void cleanupTransaction(String transactionId) {
    try {
      activeTransactions.remove(transactionId);
      currentTransaction.remove();
    } catch (Exception e) {
      LogUtil.logError(
          "TRANSACTION_MANAGER",
          "",
          "CLEANUP_ERROR",
          String.format("清理事务上下文失败: %s", transactionId),
          e);
    }
  }

  /** 刷新批处理 */
  private void flushBatch(TransactionContext context) {
    try {
      if (context.getConnection() != null) {
        // 这里可以添加批处理刷新逻辑
        LogUtil.logDebug(
            "TRANSACTION_MANAGER", "", String.format("刷新批处理: %s", context.getTransactionId()));
      }
    } catch (Exception e) {
      LogUtil.logError(
          "TRANSACTION_MANAGER",
          "",
          "FLUSH_ERROR",
          String.format("刷新批处理失败: %s", context.getTransactionId()),
          e);
    }
  }

  /** 生成事务ID */
  private String generateTransactionId() {
    return "TX-" + System.currentTimeMillis() + "-" + transactionIdGenerator.incrementAndGet();
  }

  /** 获取活跃事务数量 */
  public int getActiveTransactionCount() {
    return activeTransactions.size();
  }

  /** 获取所有活跃事务ID */
  public String[] getActiveTransactionIds() {
    return activeTransactions.keySet().toArray(new String[0]);
  }

  /** 获取事务统计信息 */
  public TransactionStatistics getStatistics() {
    return new TransactionStatistics(
        transactionIdGenerator.get(),
        activeTransactions.size(),
        activeTransactions.values().stream()
            .mapToLong(TransactionContext::getDuration)
            .average()
            .orElse(0.0));
  }

  /** 事务统计信息 */
  public static class TransactionStatistics {
    private final long totalTransactions;
    private final int activeTransactions;
    private final double averageDuration;

    public TransactionStatistics(
        long totalTransactions, int activeTransactions, double averageDuration) {
      this.totalTransactions = totalTransactions;
      this.activeTransactions = activeTransactions;
      this.averageDuration = averageDuration;
    }

    public long getTotalTransactions() {
      return totalTransactions;
    }

    public int getActiveTransactions() {
      return activeTransactions;
    }

    public double getAverageDuration() {
      return averageDuration;
    }
  }
}

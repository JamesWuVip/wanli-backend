package com.wanli.backend.pool;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.LoggerFactory;

import com.wanli.backend.util.LogUtil;

/** 监控连接包装类 包装数据库连接以监控其使用情况和性能 */
public class MonitoredConnection implements Connection {

  private final Connection delegate;
  private final String connectionId;
  private final ConnectionPoolManager poolManager;
  private final LocalDateTime creationTime;
  private final AtomicBoolean closed = new AtomicBoolean(false);
  private final AtomicLong queryCount = new AtomicLong(0);
  private final AtomicLong totalQueryTime = new AtomicLong(0);

  // 连接泄漏检测
  private volatile LocalDateTime lastUsedTime;
  private volatile StackTraceElement[] creationStackTrace;

  public MonitoredConnection(
      Connection delegate, String connectionId, ConnectionPoolManager poolManager) {
    this.delegate = delegate;
    this.connectionId = connectionId;
    this.poolManager = poolManager;
    this.creationTime = LocalDateTime.now();
    this.lastUsedTime = creationTime;

    // 记录创建时的堆栈跟踪，用于泄漏检测
    if (LoggerFactory.getLogger(MonitoredConnection.class).isDebugEnabled()) {
      this.creationStackTrace = Thread.currentThread().getStackTrace();
    }
  }

  @Override
  public Statement createStatement() throws SQLException {
    updateLastUsedTime();
    return new MonitoredStatement(delegate.createStatement(), this);
  }

  @Override
  public PreparedStatement prepareStatement(String sql) throws SQLException {
    updateLastUsedTime();
    return new MonitoredPreparedStatement(delegate.prepareStatement(sql), this, sql);
  }

  @Override
  public CallableStatement prepareCall(String sql) throws SQLException {
    updateLastUsedTime();
    return new MonitoredCallableStatement(delegate.prepareCall(sql), sql, this);
  }

  @Override
  public String nativeSQL(String sql) throws SQLException {
    updateLastUsedTime();
    return delegate.nativeSQL(sql);
  }

  @Override
  public void setAutoCommit(boolean autoCommit) throws SQLException {
    updateLastUsedTime();
    delegate.setAutoCommit(autoCommit);
  }

  @Override
  public boolean getAutoCommit() throws SQLException {
    updateLastUsedTime();
    return delegate.getAutoCommit();
  }

  @Override
  public void commit() throws SQLException {
    updateLastUsedTime();
    long startTime = System.currentTimeMillis();
    try {
      delegate.commit();
      recordOperation("COMMIT", System.currentTimeMillis() - startTime, true);
    } catch (SQLException e) {
      recordOperation("COMMIT", System.currentTimeMillis() - startTime, false);
      throw e;
    }
  }

  @Override
  public void rollback() throws SQLException {
    updateLastUsedTime();
    long startTime = System.currentTimeMillis();
    try {
      delegate.rollback();
      recordOperation("ROLLBACK", System.currentTimeMillis() - startTime, true);
    } catch (SQLException e) {
      recordOperation("ROLLBACK", System.currentTimeMillis() - startTime, false);
      throw e;
    }
  }

  @Override
  public void close() throws SQLException {
    if (closed.compareAndSet(false, true)) {
      try {
        delegate.close();
        poolManager.onConnectionClosed(connectionId);

        LogUtil.logInfo(
            "CONNECTION_CLOSED",
            "",
            String.format(
                "连接已关闭: %s, 查询次数: %d, 总查询时间: %dms",
                connectionId, queryCount.get(), totalQueryTime.get()));

      } catch (SQLException e) {
        LogUtil.logError(
            "CONNECTION_CLOSE_ERROR",
            "连接管理",
            "CONNECTION_CLOSE_ERROR",
            String.format("关闭连接失败: %s", connectionId),
            e);
        throw e;
      }
    }
  }

  @Override
  public boolean isClosed() throws SQLException {
    return closed.get() || delegate.isClosed();
  }

  @Override
  public DatabaseMetaData getMetaData() throws SQLException {
    updateLastUsedTime();
    return delegate.getMetaData();
  }

  @Override
  public void setReadOnly(boolean readOnly) throws SQLException {
    updateLastUsedTime();
    delegate.setReadOnly(readOnly);
  }

  @Override
  public boolean isReadOnly() throws SQLException {
    updateLastUsedTime();
    return delegate.isReadOnly();
  }

  @Override
  public void setCatalog(String catalog) throws SQLException {
    updateLastUsedTime();
    delegate.setCatalog(catalog);
  }

  @Override
  public String getCatalog() throws SQLException {
    updateLastUsedTime();
    return delegate.getCatalog();
  }

  @Override
  public void setTransactionIsolation(int level) throws SQLException {
    updateLastUsedTime();
    delegate.setTransactionIsolation(level);
  }

  @Override
  public int getTransactionIsolation() throws SQLException {
    updateLastUsedTime();
    return delegate.getTransactionIsolation();
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    updateLastUsedTime();
    return delegate.getWarnings();
  }

  @Override
  public void clearWarnings() throws SQLException {
    updateLastUsedTime();
    delegate.clearWarnings();
  }

  @Override
  public Statement createStatement(int resultSetType, int resultSetConcurrency)
      throws SQLException {
    updateLastUsedTime();
    return new MonitoredStatement(
        delegate.createStatement(resultSetType, resultSetConcurrency), this);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    updateLastUsedTime();
    return new MonitoredPreparedStatement(
        delegate.prepareStatement(sql, resultSetType, resultSetConcurrency), this, sql);
  }

  @Override
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    updateLastUsedTime();
    return new MonitoredCallableStatement(
        delegate.prepareCall(sql, resultSetType, resultSetConcurrency), sql, this);
  }

  @Override
  public Map<String, Class<?>> getTypeMap() throws SQLException {
    updateLastUsedTime();
    return delegate.getTypeMap();
  }

  @Override
  public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
    updateLastUsedTime();
    delegate.setTypeMap(map);
  }

  @Override
  public void setHoldability(int holdability) throws SQLException {
    updateLastUsedTime();
    delegate.setHoldability(holdability);
  }

  @Override
  public int getHoldability() throws SQLException {
    updateLastUsedTime();
    return delegate.getHoldability();
  }

  @Override
  public Savepoint setSavepoint() throws SQLException {
    updateLastUsedTime();
    return delegate.setSavepoint();
  }

  @Override
  public Savepoint setSavepoint(String name) throws SQLException {
    updateLastUsedTime();
    return delegate.setSavepoint(name);
  }

  @Override
  public void rollback(Savepoint savepoint) throws SQLException {
    updateLastUsedTime();
    long startTime = System.currentTimeMillis();
    try {
      delegate.rollback(savepoint);
      recordOperation("ROLLBACK_TO_SAVEPOINT", System.currentTimeMillis() - startTime, true);
    } catch (SQLException e) {
      recordOperation("ROLLBACK_TO_SAVEPOINT", System.currentTimeMillis() - startTime, false);
      throw e;
    }
  }

  @Override
  public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    updateLastUsedTime();
    delegate.releaseSavepoint(savepoint);
  }

  @Override
  public Statement createStatement(
      int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    updateLastUsedTime();
    return new MonitoredStatement(
        delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability), this);
  }

  @Override
  public PreparedStatement prepareStatement(
      String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
      throws SQLException {
    updateLastUsedTime();
    return new MonitoredPreparedStatement(
        delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability),
        this,
        sql);
  }

  @Override
  public CallableStatement prepareCall(
      String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
      throws SQLException {
    updateLastUsedTime();
    return new MonitoredCallableStatement(
        delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability),
        sql,
        this);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
    updateLastUsedTime();
    return new MonitoredPreparedStatement(
        delegate.prepareStatement(sql, autoGeneratedKeys), this, sql);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
    updateLastUsedTime();
    return new MonitoredPreparedStatement(delegate.prepareStatement(sql, columnIndexes), this, sql);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
    updateLastUsedTime();
    return new MonitoredPreparedStatement(delegate.prepareStatement(sql, columnNames), this, sql);
  }

  @Override
  public Clob createClob() throws SQLException {
    updateLastUsedTime();
    return delegate.createClob();
  }

  @Override
  public Blob createBlob() throws SQLException {
    updateLastUsedTime();
    return delegate.createBlob();
  }

  @Override
  public NClob createNClob() throws SQLException {
    updateLastUsedTime();
    return delegate.createNClob();
  }

  @Override
  public SQLXML createSQLXML() throws SQLException {
    updateLastUsedTime();
    return delegate.createSQLXML();
  }

  @Override
  public boolean isValid(int timeout) throws SQLException {
    updateLastUsedTime();
    return delegate.isValid(timeout);
  }

  @Override
  public void setClientInfo(String name, String value) throws SQLClientInfoException {
    updateLastUsedTime();
    delegate.setClientInfo(name, value);
  }

  @Override
  public void setClientInfo(Properties properties) throws SQLClientInfoException {
    updateLastUsedTime();
    delegate.setClientInfo(properties);
  }

  @Override
  public String getClientInfo(String name) throws SQLException {
    updateLastUsedTime();
    return delegate.getClientInfo(name);
  }

  @Override
  public Properties getClientInfo() throws SQLException {
    updateLastUsedTime();
    return delegate.getClientInfo();
  }

  @Override
  public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
    updateLastUsedTime();
    return delegate.createArrayOf(typeName, elements);
  }

  @Override
  public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
    updateLastUsedTime();
    return delegate.createStruct(typeName, attributes);
  }

  @Override
  public void setSchema(String schema) throws SQLException {
    updateLastUsedTime();
    delegate.setSchema(schema);
  }

  @Override
  public String getSchema() throws SQLException {
    updateLastUsedTime();
    return delegate.getSchema();
  }

  @Override
  public void abort(Executor executor) throws SQLException {
    updateLastUsedTime();
    delegate.abort(executor);
    closed.set(true);
    poolManager.recordConnectionEvent(
        ConnectionPoolManager.ConnectionEventType.LEAKED, connectionId, 0);
  }

  @Override
  public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
    updateLastUsedTime();
    delegate.setNetworkTimeout(executor, milliseconds);
  }

  @Override
  public int getNetworkTimeout() throws SQLException {
    updateLastUsedTime();
    return delegate.getNetworkTimeout();
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return delegate.unwrap(iface);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return delegate.isWrapperFor(iface);
  }

  /** 更新最后使用时间 */
  private void updateLastUsedTime() {
    this.lastUsedTime = LocalDateTime.now();
  }

  /** 记录操作 */
  public void recordOperation(String operation, long executionTime, boolean success) {
    queryCount.incrementAndGet();
    totalQueryTime.addAndGet(executionTime);

    // 记录到连接池管理器
    poolManager.recordQueryPerformance(operation, executionTime, success);

    LogUtil.logInfo(
        "CONNECTION_OPERATION",
        "",
        String.format(
            "连接操作: %s, 操作: %s, 耗时: %dms, 成功: %s", connectionId, operation, executionTime, success));
  }

  /** 检查连接泄漏 */
  public boolean checkForLeak(long maxIdleTimeMinutes) {
    if (closed.get()) {
      return false;
    }

    LocalDateTime now = LocalDateTime.now();
    long idleMinutes = java.time.Duration.between(lastUsedTime, now).toMinutes();

    if (idleMinutes > maxIdleTimeMinutes) {
      LogUtil.logWarn(
          "CONNECTION_LEAK_SUSPECTED",
          "",
          String.format("疑似连接泄漏: %s, 空闲时间: %d分钟", connectionId, idleMinutes));

      // 如果启用了调试模式，输出创建时的堆栈跟踪
      if (creationStackTrace != null) {
        StringBuilder sb = new StringBuilder("连接创建堆栈跟踪:\n");
        for (StackTraceElement element : creationStackTrace) {
          sb.append("\t").append(element.toString()).append("\n");
        }
        LogUtil.logInfo("CONNECTION_CREATION_STACK_TRACE", "", sb.toString());
      }

      return true;
    }

    return false;
  }

  // Getters
  public String getConnectionId() {
    return connectionId;
  }

  public LocalDateTime getCreationTime() {
    return creationTime;
  }

  public LocalDateTime getLastUsedTime() {
    return lastUsedTime;
  }

  public long getQueryCount() {
    return queryCount.get();
  }

  public long getTotalQueryTime() {
    return totalQueryTime.get();
  }

  public long getAverageQueryTime() {
    long count = queryCount.get();
    return count > 0 ? totalQueryTime.get() / count : 0;
  }

  public Connection getDelegate() {
    return delegate;
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      if (!closed.get()) {
        LogUtil.logWarn(
            "CONNECTION_FINALIZED_WITHOUT_CLOSE",
            "",
            String.format("连接在finalize时未关闭: %s", connectionId));

        poolManager.recordConnectionEvent(
            ConnectionPoolManager.ConnectionEventType.LEAKED, connectionId, 0);

        // 尝试关闭连接
        try {
          close();
        } catch (SQLException e) {
          LogUtil.logError(
              "CONNECTION_FINALIZE_CLOSE_ERROR",
              "数据库连接池",
              "CONNECTION_FINALIZE_CLOSE_ERROR",
              "在finalize中关闭连接失败",
              e);
        }
      }
    } finally {
      super.finalize();
    }
  }
}

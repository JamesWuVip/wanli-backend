package com.wanli.backend.pool;

import java.sql.*;
import java.util.concurrent.atomic.AtomicLong;

import com.wanli.backend.util.LogUtil;

/** 监控Statement包装类 包装Statement以监控SQL执行性能 */
public class MonitoredStatement implements Statement {

  protected final Statement delegate;
  protected final MonitoredConnection connection;
  protected final AtomicLong executionCount = new AtomicLong(0);
  protected final AtomicLong totalExecutionTime = new AtomicLong(0);

  public MonitoredStatement(Statement delegate, MonitoredConnection connection) {
    this.delegate = delegate;
    this.connection = connection;
  }

  @Override
  public ResultSet executeQuery(String sql) throws SQLException {
    long startTime = System.currentTimeMillis();
    try {
      ResultSet result = delegate.executeQuery(sql);
      recordExecution(sql, System.currentTimeMillis() - startTime, true);
      return result;
    } catch (SQLException e) {
      recordExecution(sql, System.currentTimeMillis() - startTime, false);
      throw e;
    }
  }

  @Override
  public int executeUpdate(String sql) throws SQLException {
    long startTime = System.currentTimeMillis();
    try {
      int result = delegate.executeUpdate(sql);
      recordExecution(sql, System.currentTimeMillis() - startTime, true);
      return result;
    } catch (SQLException e) {
      recordExecution(sql, System.currentTimeMillis() - startTime, false);
      throw e;
    }
  }

  @Override
  public void close() throws SQLException {
    delegate.close();
  }

  @Override
  public int getMaxFieldSize() throws SQLException {
    return delegate.getMaxFieldSize();
  }

  @Override
  public void setMaxFieldSize(int max) throws SQLException {
    delegate.setMaxFieldSize(max);
  }

  @Override
  public int getMaxRows() throws SQLException {
    return delegate.getMaxRows();
  }

  @Override
  public void setMaxRows(int max) throws SQLException {
    delegate.setMaxRows(max);
  }

  @Override
  public void setEscapeProcessing(boolean enable) throws SQLException {
    delegate.setEscapeProcessing(enable);
  }

  @Override
  public int getQueryTimeout() throws SQLException {
    return delegate.getQueryTimeout();
  }

  @Override
  public void setQueryTimeout(int seconds) throws SQLException {
    delegate.setQueryTimeout(seconds);
  }

  @Override
  public void cancel() throws SQLException {
    delegate.cancel();
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    return delegate.getWarnings();
  }

  @Override
  public void clearWarnings() throws SQLException {
    delegate.clearWarnings();
  }

  @Override
  public void setCursorName(String name) throws SQLException {
    delegate.setCursorName(name);
  }

  @Override
  public boolean execute(String sql) throws SQLException {
    long startTime = System.currentTimeMillis();
    try {
      boolean result = delegate.execute(sql);
      recordExecution(sql, System.currentTimeMillis() - startTime, true);
      return result;
    } catch (SQLException e) {
      recordExecution(sql, System.currentTimeMillis() - startTime, false);
      throw e;
    }
  }

  @Override
  public ResultSet getResultSet() throws SQLException {
    return delegate.getResultSet();
  }

  @Override
  public int getUpdateCount() throws SQLException {
    return delegate.getUpdateCount();
  }

  @Override
  public boolean getMoreResults() throws SQLException {
    return delegate.getMoreResults();
  }

  @Override
  public void setFetchDirection(int direction) throws SQLException {
    delegate.setFetchDirection(direction);
  }

  @Override
  public int getFetchDirection() throws SQLException {
    return delegate.getFetchDirection();
  }

  @Override
  public void setFetchSize(int rows) throws SQLException {
    delegate.setFetchSize(rows);
  }

  @Override
  public int getFetchSize() throws SQLException {
    return delegate.getFetchSize();
  }

  @Override
  public int getResultSetConcurrency() throws SQLException {
    return delegate.getResultSetConcurrency();
  }

  @Override
  public int getResultSetType() throws SQLException {
    return delegate.getResultSetType();
  }

  @Override
  public void addBatch(String sql) throws SQLException {
    delegate.addBatch(sql);
  }

  @Override
  public void clearBatch() throws SQLException {
    delegate.clearBatch();
  }

  @Override
  public int[] executeBatch() throws SQLException {
    long startTime = System.currentTimeMillis();
    try {
      int[] result = delegate.executeBatch();
      recordExecution("BATCH_EXECUTION", System.currentTimeMillis() - startTime, true);
      return result;
    } catch (SQLException e) {
      recordExecution("BATCH_EXECUTION", System.currentTimeMillis() - startTime, false);
      throw e;
    }
  }

  @Override
  public Connection getConnection() throws SQLException {
    return connection;
  }

  @Override
  public boolean getMoreResults(int current) throws SQLException {
    return delegate.getMoreResults(current);
  }

  @Override
  public ResultSet getGeneratedKeys() throws SQLException {
    return delegate.getGeneratedKeys();
  }

  @Override
  public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
    long startTime = System.currentTimeMillis();
    try {
      int result = delegate.executeUpdate(sql, autoGeneratedKeys);
      recordExecution(sql, System.currentTimeMillis() - startTime, true);
      return result;
    } catch (SQLException e) {
      recordExecution(sql, System.currentTimeMillis() - startTime, false);
      throw e;
    }
  }

  @Override
  public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
    long startTime = System.currentTimeMillis();
    try {
      int result = delegate.executeUpdate(sql, columnIndexes);
      recordExecution(sql, System.currentTimeMillis() - startTime, true);
      return result;
    } catch (SQLException e) {
      recordExecution(sql, System.currentTimeMillis() - startTime, false);
      throw e;
    }
  }

  @Override
  public int executeUpdate(String sql, String[] columnNames) throws SQLException {
    long startTime = System.currentTimeMillis();
    try {
      int result = delegate.executeUpdate(sql, columnNames);
      recordExecution(sql, System.currentTimeMillis() - startTime, true);
      return result;
    } catch (SQLException e) {
      recordExecution(sql, System.currentTimeMillis() - startTime, false);
      throw e;
    }
  }

  @Override
  public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
    long startTime = System.currentTimeMillis();
    try {
      boolean result = delegate.execute(sql, autoGeneratedKeys);
      recordExecution(sql, System.currentTimeMillis() - startTime, true);
      return result;
    } catch (SQLException e) {
      recordExecution(sql, System.currentTimeMillis() - startTime, false);
      throw e;
    }
  }

  @Override
  public boolean execute(String sql, int[] columnIndexes) throws SQLException {
    long startTime = System.currentTimeMillis();
    try {
      boolean result = delegate.execute(sql, columnIndexes);
      recordExecution(sql, System.currentTimeMillis() - startTime, true);
      return result;
    } catch (SQLException e) {
      recordExecution(sql, System.currentTimeMillis() - startTime, false);
      throw e;
    }
  }

  @Override
  public boolean execute(String sql, String[] columnNames) throws SQLException {
    long startTime = System.currentTimeMillis();
    try {
      boolean result = delegate.execute(sql, columnNames);
      recordExecution(sql, System.currentTimeMillis() - startTime, true);
      return result;
    } catch (SQLException e) {
      recordExecution(sql, System.currentTimeMillis() - startTime, false);
      throw e;
    }
  }

  @Override
  public int getResultSetHoldability() throws SQLException {
    return delegate.getResultSetHoldability();
  }

  @Override
  public boolean isClosed() throws SQLException {
    return delegate.isClosed();
  }

  @Override
  public void setPoolable(boolean poolable) throws SQLException {
    delegate.setPoolable(poolable);
  }

  @Override
  public boolean isPoolable() throws SQLException {
    return delegate.isPoolable();
  }

  @Override
  public void closeOnCompletion() throws SQLException {
    delegate.closeOnCompletion();
  }

  @Override
  public boolean isCloseOnCompletion() throws SQLException {
    return delegate.isCloseOnCompletion();
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return delegate.unwrap(iface);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return delegate.isWrapperFor(iface);
  }

  /** 记录SQL执行 */
  protected void recordExecution(String sql, long executionTime, boolean success) {
    executionCount.incrementAndGet();
    totalExecutionTime.addAndGet(executionTime);

    // 记录到连接
    connection.recordOperation(sql, executionTime, success);

    // 记录详细日志
    LogUtil.logInfo(
        "SQL_EXECUTION",
        "",
        String.format(
            "SQL执行: %s, 耗时: %dms, 成功: %s, 连接: %s",
            truncateSql(sql), executionTime, success, connection.getConnectionId()));
  }

  /** 截断SQL用于日志记录 */
  private String truncateSql(String sql) {
    if (sql == null) {
      return "null";
    }

    String cleanSql = sql.replaceAll("\\s+", " ").trim();
    if (cleanSql.length() > 100) {
      return cleanSql.substring(0, 97) + "...";
    }
    return cleanSql;
  }

  // Getters
  public long getExecutionCount() {
    return executionCount.get();
  }

  public long getTotalExecutionTime() {
    return totalExecutionTime.get();
  }

  public long getAverageExecutionTime() {
    long count = executionCount.get();
    return count > 0 ? totalExecutionTime.get() / count : 0;
  }

  public Statement getDelegate() {
    return delegate;
  }
}

package com.wanli.backend.pool;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicLong;

import com.wanli.backend.util.LogUtil;

/** 监控PreparedStatement包装类 包装PreparedStatement以监控预编译SQL执行性能 */
public class MonitoredPreparedStatement extends MonitoredStatement implements PreparedStatement {

  private final PreparedStatement preparedDelegate;
  private final String originalSql;
  private final AtomicLong parameterSetCount = new AtomicLong(0);

  public MonitoredPreparedStatement(
      PreparedStatement delegate, MonitoredConnection connection, String sql) {
    super(delegate, connection);
    this.preparedDelegate = delegate;
    this.originalSql = sql;
  }

  @Override
  public ResultSet executeQuery() throws SQLException {
    long startTime = System.currentTimeMillis();
    try {
      ResultSet result = preparedDelegate.executeQuery();
      recordExecution(originalSql, System.currentTimeMillis() - startTime, true);
      return result;
    } catch (SQLException e) {
      recordExecution(originalSql, System.currentTimeMillis() - startTime, false);
      throw e;
    }
  }

  @Override
  public int executeUpdate() throws SQLException {
    long startTime = System.currentTimeMillis();
    try {
      int result = preparedDelegate.executeUpdate();
      recordExecution(originalSql, System.currentTimeMillis() - startTime, true);
      return result;
    } catch (SQLException e) {
      recordExecution(originalSql, System.currentTimeMillis() - startTime, false);
      throw e;
    }
  }

  @Override
  public void setNull(int parameterIndex, int sqlType) throws SQLException {
    preparedDelegate.setNull(parameterIndex, sqlType);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setBoolean(int parameterIndex, boolean x) throws SQLException {
    preparedDelegate.setBoolean(parameterIndex, x);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setByte(int parameterIndex, byte x) throws SQLException {
    preparedDelegate.setByte(parameterIndex, x);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setShort(int parameterIndex, short x) throws SQLException {
    preparedDelegate.setShort(parameterIndex, x);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setInt(int parameterIndex, int x) throws SQLException {
    preparedDelegate.setInt(parameterIndex, x);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setLong(int parameterIndex, long x) throws SQLException {
    preparedDelegate.setLong(parameterIndex, x);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setFloat(int parameterIndex, float x) throws SQLException {
    preparedDelegate.setFloat(parameterIndex, x);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setDouble(int parameterIndex, double x) throws SQLException {
    preparedDelegate.setDouble(parameterIndex, x);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
    preparedDelegate.setBigDecimal(parameterIndex, x);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setString(int parameterIndex, String x) throws SQLException {
    preparedDelegate.setString(parameterIndex, x);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setBytes(int parameterIndex, byte[] x) throws SQLException {
    preparedDelegate.setBytes(parameterIndex, x);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setDate(int parameterIndex, Date x) throws SQLException {
    preparedDelegate.setDate(parameterIndex, x);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setTime(int parameterIndex, Time x) throws SQLException {
    preparedDelegate.setTime(parameterIndex, x);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
    preparedDelegate.setTimestamp(parameterIndex, x);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
    preparedDelegate.setAsciiStream(parameterIndex, x, length);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
    preparedDelegate.setUnicodeStream(parameterIndex, x, length);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
    preparedDelegate.setBinaryStream(parameterIndex, x, length);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void clearParameters() throws SQLException {
    preparedDelegate.clearParameters();
  }

  @Override
  public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
    preparedDelegate.setObject(parameterIndex, x, targetSqlType);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setObject(int parameterIndex, Object x) throws SQLException {
    preparedDelegate.setObject(parameterIndex, x);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public boolean execute() throws SQLException {
    long startTime = System.currentTimeMillis();
    try {
      boolean result = preparedDelegate.execute();
      recordExecution(originalSql, System.currentTimeMillis() - startTime, true);
      return result;
    } catch (SQLException e) {
      recordExecution(originalSql, System.currentTimeMillis() - startTime, false);
      throw e;
    }
  }

  @Override
  public void addBatch() throws SQLException {
    preparedDelegate.addBatch();
  }

  @Override
  public void setCharacterStream(int parameterIndex, Reader reader, int length)
      throws SQLException {
    preparedDelegate.setCharacterStream(parameterIndex, reader, length);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setRef(int parameterIndex, Ref x) throws SQLException {
    preparedDelegate.setRef(parameterIndex, x);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setBlob(int parameterIndex, Blob x) throws SQLException {
    preparedDelegate.setBlob(parameterIndex, x);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setClob(int parameterIndex, Clob x) throws SQLException {
    preparedDelegate.setClob(parameterIndex, x);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setArray(int parameterIndex, Array x) throws SQLException {
    preparedDelegate.setArray(parameterIndex, x);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public ResultSetMetaData getMetaData() throws SQLException {
    return preparedDelegate.getMetaData();
  }

  @Override
  public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
    preparedDelegate.setDate(parameterIndex, x, cal);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
    preparedDelegate.setTime(parameterIndex, x, cal);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
    preparedDelegate.setTimestamp(parameterIndex, x, cal);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
    preparedDelegate.setNull(parameterIndex, sqlType, typeName);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setURL(int parameterIndex, URL x) throws SQLException {
    preparedDelegate.setURL(parameterIndex, x);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public ParameterMetaData getParameterMetaData() throws SQLException {
    return preparedDelegate.getParameterMetaData();
  }

  @Override
  public void setRowId(int parameterIndex, RowId x) throws SQLException {
    preparedDelegate.setRowId(parameterIndex, x);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setNString(int parameterIndex, String value) throws SQLException {
    preparedDelegate.setNString(parameterIndex, value);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setNCharacterStream(int parameterIndex, Reader value, long length)
      throws SQLException {
    preparedDelegate.setNCharacterStream(parameterIndex, value, length);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setNClob(int parameterIndex, NClob value) throws SQLException {
    preparedDelegate.setNClob(parameterIndex, value);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
    preparedDelegate.setClob(parameterIndex, reader, length);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setBlob(int parameterIndex, InputStream inputStream, long length)
      throws SQLException {
    preparedDelegate.setBlob(parameterIndex, inputStream, length);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
    preparedDelegate.setNClob(parameterIndex, reader, length);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
    preparedDelegate.setSQLXML(parameterIndex, xmlObject);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength)
      throws SQLException {
    preparedDelegate.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
    preparedDelegate.setAsciiStream(parameterIndex, x, length);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
    preparedDelegate.setBinaryStream(parameterIndex, x, length);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setCharacterStream(int parameterIndex, Reader reader, long length)
      throws SQLException {
    preparedDelegate.setCharacterStream(parameterIndex, reader, length);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
    preparedDelegate.setAsciiStream(parameterIndex, x);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
    preparedDelegate.setBinaryStream(parameterIndex, x);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
    preparedDelegate.setCharacterStream(parameterIndex, reader);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
    preparedDelegate.setNCharacterStream(parameterIndex, value);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setClob(int parameterIndex, Reader reader) throws SQLException {
    preparedDelegate.setClob(parameterIndex, reader);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
    preparedDelegate.setBlob(parameterIndex, inputStream);
    parameterSetCount.incrementAndGet();
  }

  @Override
  public void setNClob(int parameterIndex, Reader reader) throws SQLException {
    preparedDelegate.setNClob(parameterIndex, reader);
    parameterSetCount.incrementAndGet();
  }

  @Override
  protected void recordExecution(String sql, long executionTime, boolean success) {
    super.recordExecution(sql, executionTime, success);

    // 记录PreparedStatement特有的统计信息
    LogUtil.logInfo(
        "PREPARED_STATEMENT_EXECUTION",
        "数据库连接池",
        String.format(
            "PreparedStatement执行: %s, 耗时: %dms, 成功: %s, 参数设置次数: %d, 连接: %s",
            truncateSql(sql),
            executionTime,
            success,
            parameterSetCount.get(),
            connection.getConnectionId()));
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
  public String getOriginalSql() {
    return originalSql;
  }

  public long getParameterSetCount() {
    return parameterSetCount.get();
  }

  public PreparedStatement getPreparedDelegate() {
    return preparedDelegate;
  }
}

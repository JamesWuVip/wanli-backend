package com.wanli.backend.pool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wanli.backend.config.ApplicationConfigManager;
import com.wanli.backend.util.LogUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/** 数据库连接池工厂 统一管理不同类型的数据库连接池 */
@Component
public class ConnectionPoolFactory {

  @Autowired private ApplicationConfigManager configManager;

  @Autowired private ConnectionPoolManager poolManager;

  private final Map<String, HikariDataSource> dataSources = new ConcurrentHashMap<>();
  private final AtomicBoolean initialized = new AtomicBoolean(false);
  private final AtomicBoolean shutdown = new AtomicBoolean(false);

  /** 连接池类型枚举 */
  public enum PoolType {
    PRIMARY("primary", "主数据库连接池"),
    READONLY("readonly", "只读数据库连接池"),
    BATCH("batch", "批处理数据库连接池"),
    REPORT("report", "报表数据库连接池");

    private final String code;
    private final String description;

    PoolType(String code, String description) {
      this.code = code;
      this.description = description;
    }

    public String getCode() {
      return code;
    }

    public String getDescription() {
      return description;
    }
  }

  /** 连接池配置 */
  public static class PoolConfig {
    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClassName;
    private int maximumPoolSize = 10;
    private int minimumIdle = 5;
    private long connectionTimeout = 30000;
    private long idleTimeout = 600000;
    private long maxLifetime = 1800000;
    private long leakDetectionThreshold = 60000;
    private boolean autoCommit = true;
    private String poolName;
    private Map<String, String> dataSourceProperties;

    // Getters and Setters
    public String getJdbcUrl() {
      return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
      this.jdbcUrl = jdbcUrl;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public String getDriverClassName() {
      return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
      this.driverClassName = driverClassName;
    }

    public int getMaximumPoolSize() {
      return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
      this.maximumPoolSize = maximumPoolSize;
    }

    public int getMinimumIdle() {
      return minimumIdle;
    }

    public void setMinimumIdle(int minimumIdle) {
      this.minimumIdle = minimumIdle;
    }

    public long getConnectionTimeout() {
      return connectionTimeout;
    }

    public void setConnectionTimeout(long connectionTimeout) {
      this.connectionTimeout = connectionTimeout;
    }

    public long getIdleTimeout() {
      return idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
      this.idleTimeout = idleTimeout;
    }

    public long getMaxLifetime() {
      return maxLifetime;
    }

    public void setMaxLifetime(long maxLifetime) {
      this.maxLifetime = maxLifetime;
    }

    public long getLeakDetectionThreshold() {
      return leakDetectionThreshold;
    }

    public void setLeakDetectionThreshold(long leakDetectionThreshold) {
      this.leakDetectionThreshold = leakDetectionThreshold;
    }

    public boolean isAutoCommit() {
      return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
      this.autoCommit = autoCommit;
    }

    public String getPoolName() {
      return poolName;
    }

    public void setPoolName(String poolName) {
      this.poolName = poolName;
    }

    public Map<String, String> getDataSourceProperties() {
      return dataSourceProperties;
    }

    public void setDataSourceProperties(Map<String, String> dataSourceProperties) {
      this.dataSourceProperties = dataSourceProperties;
    }
  }

  @PostConstruct
  public void initialize() {
    if (initialized.compareAndSet(false, true)) {
      try {
        initializeDefaultPools();
        LogUtil.logInfo("CONNECTION_POOL_FACTORY", "", "连接池工厂初始化完成");
      } catch (Exception e) {
        LogUtil.logError("CONNECTION_POOL_FACTORY", "INIT_FAILED", "连接池工厂初始化失败", "连接池工厂初始化失败", e);
        initialized.set(false);
        throw new RuntimeException("连接池工厂初始化失败", e);
      }
    }
  }

  /** 初始化默认连接池 */
  private void initializeDefaultPools() {
    // 创建主连接池
    PoolConfig primaryConfig = createDefaultConfig(PoolType.PRIMARY);
    createDataSource(PoolType.PRIMARY.getCode(), primaryConfig);

    // 根据配置创建其他连接池
    if (configManager.getDatabase().isEnableReadonlyPool()) {
      PoolConfig readonlyConfig = createDefaultConfig(PoolType.READONLY);
      readonlyConfig.setMaximumPoolSize(configManager.getDatabase().getReadonlyPoolSize());
      createDataSource(PoolType.READONLY.getCode(), readonlyConfig);
    }

    if (configManager.getDatabase().isEnableBatchPool()) {
      PoolConfig batchConfig = createDefaultConfig(PoolType.BATCH);
      batchConfig.setMaximumPoolSize(configManager.getDatabase().getBatchPoolSize());
      batchConfig.setAutoCommit(false); // 批处理通常需要手动提交
      createDataSource(PoolType.BATCH.getCode(), batchConfig);
    }
  }

  /** 创建默认配置 */
  private PoolConfig createDefaultConfig(PoolType poolType) {
    PoolConfig config = new PoolConfig();

    // 从配置管理器获取数据库配置
    ApplicationConfigManager.DatabaseConfig dbConfig = configManager.getDatabase();

    config.setJdbcUrl(dbConfig.getUrl());
    config.setUsername(dbConfig.getUsername());
    config.setPassword(dbConfig.getPassword());
    config.setDriverClassName(dbConfig.getDriverClassName());
    config.setMaximumPoolSize(dbConfig.getMaxPoolSize());
    config.setMinimumIdle(dbConfig.getMinPoolSize());
    config.setConnectionTimeout(dbConfig.getConnectionTimeout());
    config.setIdleTimeout(dbConfig.getIdleTimeout());
    config.setMaxLifetime(dbConfig.getMaxLifetime());
    config.setLeakDetectionThreshold(dbConfig.getLeakDetectionThreshold());
    config.setPoolName(poolType.getDescription());

    return config;
  }

  /** 创建数据源 */
  public synchronized DataSource createDataSource(String poolName, PoolConfig config) {
    if (shutdown.get()) {
      throw new IllegalStateException("连接池工厂已关闭");
    }

    if (dataSources.containsKey(poolName)) {
      LogUtil.logWarn(
          "CONNECTION_POOL_FACTORY", "", String.format("连接池 %s 已存在，将关闭旧连接池并创建新的", poolName));
      closeDataSource(poolName);
    }

    try {
      HikariConfig hikariConfig = new HikariConfig();

      // 基本配置
      hikariConfig.setJdbcUrl(config.getJdbcUrl());
      hikariConfig.setUsername(config.getUsername());
      hikariConfig.setPassword(config.getPassword());
      hikariConfig.setDriverClassName(config.getDriverClassName());

      // 连接池配置
      hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize());
      hikariConfig.setMinimumIdle(config.getMinimumIdle());
      hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
      hikariConfig.setIdleTimeout(config.getIdleTimeout());
      hikariConfig.setMaxLifetime(config.getMaxLifetime());
      hikariConfig.setLeakDetectionThreshold(config.getLeakDetectionThreshold());
      hikariConfig.setAutoCommit(config.isAutoCommit());
      hikariConfig.setPoolName(config.getPoolName());

      // 数据源属性
      if (config.getDataSourceProperties() != null) {
        config.getDataSourceProperties().forEach(hikariConfig::addDataSourceProperty);
      }

      // 性能优化配置
      hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
      hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
      hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
      hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
      hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
      hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
      hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
      hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
      hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
      hikariConfig.addDataSourceProperty("maintainTimeStats", "false");

      HikariDataSource dataSource = new HikariDataSource(hikariConfig);
      dataSources.put(poolName, dataSource);

      // 注册到连接池管理器
      poolManager.registerDataSource(poolName, dataSource);

      LogUtil.logInfo(
          "CONNECTION_POOL_FACTORY",
          "",
          String.format(
              "成功创建连接池: %s, 最大连接数: %d, 最小空闲连接数: %d",
              poolName, config.getMaximumPoolSize(), config.getMinimumIdle()));

      return dataSource;

    } catch (Exception e) {
      LogUtil.logError(
          "CONNECTION_POOL_FACTORY",
          "",
          "CREATE_POOL_ERROR",
          String.format("创建连接池失败: %s", poolName),
          e);
      throw new RuntimeException("创建连接池失败: " + poolName, e);
    }
  }

  /** 获取数据源 */
  public DataSource getDataSource(String poolName) {
    HikariDataSource dataSource = dataSources.get(poolName);
    if (dataSource == null) {
      throw new IllegalArgumentException("连接池不存在: " + poolName);
    }
    if (dataSource.isClosed()) {
      throw new IllegalStateException("连接池已关闭: " + poolName);
    }
    return dataSource;
  }

  /** 获取数据源（按类型） */
  public DataSource getDataSource(PoolType poolType) {
    return getDataSource(poolType.getCode());
  }

  /** 获取主数据源 */
  public DataSource getPrimaryDataSource() {
    return getDataSource(PoolType.PRIMARY);
  }

  /** 获取只读数据源 */
  public DataSource getReadonlyDataSource() {
    DataSource dataSource = dataSources.get(PoolType.READONLY.getCode());
    return dataSource != null ? dataSource : getPrimaryDataSource();
  }

  /** 获取批处理数据源 */
  public DataSource getBatchDataSource() {
    DataSource dataSource = dataSources.get(PoolType.BATCH.getCode());
    return dataSource != null ? dataSource : getPrimaryDataSource();
  }

  /** 关闭指定连接池 */
  public synchronized void closeDataSource(String poolName) {
    HikariDataSource dataSource = dataSources.remove(poolName);
    if (dataSource != null && !dataSource.isClosed()) {
      try {
        // 从连接池管理器注销
        poolManager.unregisterDataSource(poolName);

        dataSource.close();
        LogUtil.logInfo("CONNECTION_POOL_FACTORY", "", String.format("连接池 %s 已关闭", poolName));
      } catch (Exception e) {
        LogUtil.logError(
            "CONNECTION_POOL_FACTORY",
            "",
            "CLOSE_POOL_ERROR",
            String.format("关闭连接池失败: %s", poolName),
            e);
      }
    }
  }

  /** 检查连接池是否存在 */
  public boolean hasDataSource(String poolName) {
    HikariDataSource dataSource = dataSources.get(poolName);
    return dataSource != null && !dataSource.isClosed();
  }

  /** 获取所有连接池名称 */
  public String[] getDataSourceNames() {
    return dataSources.keySet().toArray(new String[0]);
  }

  /** 获取连接池数量 */
  public int getDataSourceCount() {
    return dataSources.size();
  }

  /** 重新加载连接池配置 */
  public synchronized void reloadConfiguration() {
    if (shutdown.get()) {
      return;
    }

    try {
      LogUtil.logInfo("CONNECTION_POOL_FACTORY", "", "开始重新加载连接池配置");

      // 重新初始化默认连接池
      initializeDefaultPools();

      LogUtil.logInfo("CONNECTION_POOL_FACTORY", "", "连接池配置重新加载完成");
    } catch (Exception e) {
      LogUtil.logError("CONNECTION_POOL_FACTORY", "", "RELOAD_CONFIG_ERROR", "重新加载连接池配置失败", e);
    }
  }

  @PreDestroy
  public void shutdown() {
    if (shutdown.compareAndSet(false, true)) {
      LogUtil.logInfo("CONNECTION_POOL_FACTORY", "", "开始关闭连接池工厂");

      // 关闭所有连接池
      dataSources.keySet().forEach(this::closeDataSource);

      LogUtil.logInfo("CONNECTION_POOL_FACTORY", "", "连接池工厂已关闭");
    }
  }

  // Getters
  public boolean isInitialized() {
    return initialized.get();
  }

  public boolean isShutdown() {
    return shutdown.get();
  }
}

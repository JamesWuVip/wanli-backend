package com.wanli.backend.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** ConfigUtil 单元测试 */
@ExtendWith(MockitoExtension.class)
class ConfigUtilTest {

  @InjectMocks private ConfigUtil configUtil;

  @BeforeEach
  void setUp() {
    // 设置测试用的配置值
    ReflectionTestUtils.setField(configUtil, "jwtSecret", "test-jwt-secret");
    ReflectionTestUtils.setField(configUtil, "jwtExpiration", 3600000L);
    ReflectionTestUtils.setField(configUtil, "databaseUrl", "jdbc:h2:mem:testdb");
    ReflectionTestUtils.setField(configUtil, "showSql", true);
    ReflectionTestUtils.setField(configUtil, "cacheDefaultExpireMinutes", 30);
    ReflectionTestUtils.setField(configUtil, "cacheMaxSize", 1000);
    ReflectionTestUtils.setField(configUtil, "logLevel", "INFO");
    ReflectionTestUtils.setField(configUtil, "performanceLoggingEnabled", true);
    ReflectionTestUtils.setField(configUtil, "passwordMinLength", 8);
    ReflectionTestUtils.setField(configUtil, "maxLoginAttempts", 5);
    ReflectionTestUtils.setField(configUtil, "sessionTimeout", 1800);
    ReflectionTestUtils.setField(configUtil, "maxLessonsPerCourse", 100);
    ReflectionTestUtils.setField(configUtil, "maxCoursesPerUser", 50);
    ReflectionTestUtils.setField(configUtil, "maxFileSize", 10485760L);
    ReflectionTestUtils.setField(configUtil, "allowedFileTypes", "jpg,png,pdf,doc,docx");

    // 清空动态配置缓存
    configUtil.clearDynamicConfig();
  }

  @Test
  void testGetJwtSecret() {
    // 测试获取JWT密钥
    String secret = configUtil.getJwtSecret();
    assertEquals("test-jwt-secret", secret);
  }

  @Test
  void testGetJwtExpiration() {
    // 测试获取JWT过期时间
    Long expiration = configUtil.getJwtExpiration();
    assertEquals(3600000L, expiration);
  }

  @Test
  void testGetDatabaseUrl() {
    // 测试获取数据库URL
    String url = configUtil.getDatabaseUrl();
    assertEquals("jdbc:h2:mem:testdb", url);
  }

  @Test
  void testIsShowSql() {
    // 测试是否显示SQL
    Boolean showSql = configUtil.isShowSql();
    assertTrue(showSql);
  }

  @Test
  void testGetCacheDefaultExpireMinutes() {
    // 测试获取缓存默认过期时间
    Integer expireMinutes = configUtil.getCacheDefaultExpireMinutes();
    assertEquals(30, expireMinutes);
  }

  @Test
  void testGetCacheMaxSize() {
    // 测试获取缓存最大大小
    Integer maxSize = configUtil.getCacheMaxSize();
    assertEquals(1000, maxSize);
  }

  @Test
  void testGetLogLevel() {
    // 测试获取日志级别
    String logLevel = configUtil.getLogLevel();
    assertEquals("INFO", logLevel);
  }

  @Test
  void testIsPerformanceLoggingEnabled() {
    // 测试是否启用性能日志
    Boolean enabled = configUtil.isPerformanceLoggingEnabled();
    assertTrue(enabled);
  }

  @Test
  void testGetPasswordMinLength() {
    // 测试获取密码最小长度
    Integer minLength = configUtil.getPasswordMinLength();
    assertEquals(8, minLength);
  }

  @Test
  void testGetMaxLoginAttempts() {
    // 测试获取最大登录尝试次数
    Integer maxAttempts = configUtil.getMaxLoginAttempts();
    assertEquals(5, maxAttempts);
  }

  @Test
  void testGetSessionTimeout() {
    // 测试获取会话超时时间
    Integer timeout = configUtil.getSessionTimeout();
    assertEquals(1800, timeout);
  }

  @Test
  void testGetMaxLessonsPerCourse() {
    // 测试获取每个课程最大课时数
    Integer maxLessons = configUtil.getMaxLessonsPerCourse();
    assertEquals(100, maxLessons);
  }

  @Test
  void testGetMaxCoursesPerUser() {
    // 测试获取每个用户最大课程数
    Integer maxCourses = configUtil.getMaxCoursesPerUser();
    assertEquals(50, maxCourses);
  }

  @Test
  void testGetMaxFileSize() {
    // 测试获取最大文件大小
    Long maxSize = configUtil.getMaxFileSize();
    assertEquals(10485760L, maxSize);
  }

  @Test
  void testGetAllowedFileTypes() {
    // 测试获取允许的文件类型
    String[] allowedTypes = configUtil.getAllowedFileTypes();
    assertNotNull(allowedTypes);
    assertEquals(5, allowedTypes.length);
    assertEquals("jpg", allowedTypes[0]);
    assertEquals("png", allowedTypes[1]);
    assertEquals("pdf", allowedTypes[2]);
    assertEquals("doc", allowedTypes[3]);
    assertEquals("docx", allowedTypes[4]);
  }

  @Test
  void testSetConfig() {
    // 测试动态设置配置值
    String key = "test.config.key";
    String value = "test-value";

    configUtil.setConfig(key, value);

    String result = configUtil.getConfig(key, null, String.class);
    assertEquals(value, result);
  }

  @Test
  void testGetConfig() {
    // 测试获取配置值
    String key = "test.get.key";
    String defaultValue = "default-value";

    // 获取不存在的配置，应该返回默认值
    String result = configUtil.getConfig(key, defaultValue, String.class);
    assertEquals(defaultValue, result);

    // 设置配置后再获取
    String setValue = "set-value";
    configUtil.setConfig(key, setValue);
    result = configUtil.getConfig(key, defaultValue, String.class);
    assertEquals(setValue, result);
  }

  @Test
  void testGetConfigWithDifferentTypes() {
    // 测试获取不同类型的配置值

    // 字符串类型
    configUtil.setConfig("string.key", "string-value");
    String stringResult = configUtil.getConfig("string.key", "default", String.class);
    assertEquals("string-value", stringResult);

    // 整数类型
    configUtil.setConfig("integer.key", 123);
    Integer intResult = configUtil.getConfig("integer.key", 0, Integer.class);
    assertEquals(123, intResult);

    // 布尔类型
    configUtil.setConfig("boolean.key", true);
    Boolean boolResult = configUtil.getConfig("boolean.key", false, Boolean.class);
    assertTrue(boolResult);

    // 长整型
    configUtil.setConfig("long.key", 123456789L);
    Long longResult = configUtil.getConfig("long.key", 0L, Long.class);
    assertEquals(123456789L, longResult);
  }

  @Test
  void testRemoveConfig() {
    // 测试移除配置
    String key = "test.remove.key";
    String value = "remove-value";

    // 设置配置
    configUtil.setConfig(key, value);
    assertEquals(value, configUtil.getConfig(key, null, String.class));

    // 移除配置
    configUtil.removeConfig(key);
    assertNull(configUtil.getConfig(key, null, String.class));
  }

  @Test
  void testClearDynamicConfig() {
    // 测试清空所有动态配置
    configUtil.setConfig("key1", "value1");
    configUtil.setConfig("key2", "value2");
    configUtil.setConfig("key3", "value3");

    // 确认配置已设置
    assertEquals("value1", configUtil.getConfig("key1", null, String.class));
    assertEquals("value2", configUtil.getConfig("key2", null, String.class));
    assertEquals("value3", configUtil.getConfig("key3", null, String.class));

    // 清空动态配置
    configUtil.clearDynamicConfig();

    // 确认配置已清空
    assertNull(configUtil.getConfig("key1", null, String.class));
    assertNull(configUtil.getConfig("key2", null, String.class));
    assertNull(configUtil.getConfig("key3", null, String.class));
  }

  @Test
  void testGetAllConfigs() {
    // 测试获取所有配置
    configUtil.setConfig("dynamic.key1", "dynamic-value1");
    configUtil.setConfig("dynamic.key2", "dynamic-value2");

    Map<String, Object> allConfigs = configUtil.getAllConfigs();

    assertNotNull(allConfigs);

    // 检查静态配置
    assertEquals("test-jwt-secret", allConfigs.get("jwt.secret"));
    assertEquals(3600000L, allConfigs.get("jwt.expiration"));
    assertEquals("jdbc:h2:mem:testdb", allConfigs.get("database.url"));
    assertEquals(true, allConfigs.get("database.show-sql"));
    assertEquals(30, allConfigs.get("cache.default-expire-minutes"));
    assertEquals(1000, allConfigs.get("cache.max-size"));
    assertEquals("INFO", allConfigs.get("log.level"));
    assertEquals(true, allConfigs.get("log.performance.enabled"));
    assertEquals(8, allConfigs.get("security.password.min-length"));
    assertEquals(5, allConfigs.get("security.login.max-attempts"));
    assertEquals(1800, allConfigs.get("security.session.timeout"));
    assertEquals(100, allConfigs.get("business.course.max-lessons"));
    assertEquals(50, allConfigs.get("business.user.max-courses"));
    assertEquals(10485760L, allConfigs.get("file.upload.max-size"));
    assertEquals("jpg,png,pdf,doc,docx", allConfigs.get("file.upload.allowed-types"));

    // 检查动态配置
    assertEquals("dynamic-value1", allConfigs.get("dynamic.key1"));
    assertEquals("dynamic-value2", allConfigs.get("dynamic.key2"));
  }

  @Test
  void testValidateConfig() {
    // 测试配置验证

    // 有效配置应该通过验证
    assertTrue(configUtil.validateConfig());

    // 测试JWT密钥为空的情况
    ReflectionTestUtils.setField(configUtil, "jwtSecret", "");
    assertFalse(configUtil.validateConfig());

    // 恢复JWT密钥，测试JWT过期时间无效的情况
    ReflectionTestUtils.setField(configUtil, "jwtSecret", "test-jwt-secret");
    ReflectionTestUtils.setField(configUtil, "jwtExpiration", 0L);
    assertFalse(configUtil.validateConfig());

    // 恢复JWT过期时间，测试密码最小长度无效的情况
    ReflectionTestUtils.setField(configUtil, "jwtExpiration", 3600000L);
    ReflectionTestUtils.setField(configUtil, "passwordMinLength", 5);
    assertFalse(configUtil.validateConfig());

    // 恢复所有配置
    ReflectionTestUtils.setField(configUtil, "passwordMinLength", 8);
    assertTrue(configUtil.validateConfig());
  }

  @Test
  void testReloadConfig() {
    // 测试重新加载配置
    configUtil.setConfig("reload.key1", "reload-value1");
    configUtil.setConfig("reload.key2", "reload-value2");

    // 确认配置已设置
    assertEquals("reload-value1", configUtil.getConfig("reload.key1", null, String.class));
    assertEquals("reload-value2", configUtil.getConfig("reload.key2", null, String.class));

    // 重新加载配置
    configUtil.reloadConfig();

    // 确认动态配置已清空
    assertNull(configUtil.getConfig("reload.key1", null, String.class));
    assertNull(configUtil.getConfig("reload.key2", null, String.class));
  }

  @Test
  void testConfigCaching() {
    // 测试配置缓存机制
    String key = "cache.test.key";
    String value = "cache-test-value";

    // 第一次设置配置
    configUtil.setConfig(key, value);

    // 多次获取应该返回相同的值（从缓存获取）
    for (int i = 0; i < 5; i++) {
      String result = configUtil.getConfig(key, null, String.class);
      assertEquals(value, result);
    }
  }

  @Test
  void testNullAndEmptyValues() {
    // 测试null和空值处理

    // 设置null值
    configUtil.setConfig("null.key", null);
    assertNull(configUtil.getConfig("null.key", "default", String.class));

    // 设置空字符串
    configUtil.setConfig("empty.key", "");
    assertEquals("", configUtil.getConfig("empty.key", "default", String.class));

    // 获取不存在的key应该返回默认值
    assertEquals("default", configUtil.getConfig("non.existent.key", "default", String.class));
  }

  @Test
  void testTypeConversion() {
    // 测试类型转换
    String key = "type.test.key";

    // 设置字符串值
    configUtil.setConfig(key, "123");

    // 尝试以不同类型获取
    String stringResult = configUtil.getConfig(key, "default", String.class);
    assertEquals("123", stringResult);

    // 错误的类型转换应该返回默认值
    Integer intResult = configUtil.getConfig(key, 999, Integer.class);
    assertEquals(999, intResult); // 应该返回默认值，因为类型不匹配
  }

  @Test
  void testAllowedFileTypesEdgeCases() {
    // 测试允许文件类型的边界情况

    // 空字符串
    ReflectionTestUtils.setField(configUtil, "allowedFileTypes", "");
    String[] emptyTypes = configUtil.getAllowedFileTypes();
    assertEquals(1, emptyTypes.length);
    assertEquals("", emptyTypes[0]);

    // null值
    ReflectionTestUtils.setField(configUtil, "allowedFileTypes", null);
    String[] nullTypes = configUtil.getAllowedFileTypes();
    assertEquals(0, nullTypes.length);

    // 单个类型
    ReflectionTestUtils.setField(configUtil, "allowedFileTypes", "pdf");
    String[] singleType = configUtil.getAllowedFileTypes();
    assertEquals(1, singleType.length);
    assertEquals("pdf", singleType[0]);
  }
}

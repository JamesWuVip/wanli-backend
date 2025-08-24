package com.wanli.backend.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/** CacheUtil 单元测试 */
@SpringBootTest
@SpringJUnitConfig
class CacheUtilTest {

  private CacheUtil cacheUtil;

  @Mock private ConfigUtil configUtil;

  @BeforeEach
  void setUp() {
    // 创建CacheUtil实例
    cacheUtil = new CacheUtil();
    // 清空缓存
    cacheUtil.clear();
  }

  @Test
  void testPutAndGet() {
    // 测试基本的put和get操作
    String key = "test-key";
    String value = "test-value";

    cacheUtil.put(key, value);
    String result = cacheUtil.get(key, String.class);

    assertEquals(value, result);
  }

  @Test
  void testPutWithExpiration() throws InterruptedException {
    // 测试带过期时间的缓存
    String key = "expire-key";
    String value = "expire-value";

    cacheUtil.put(key, value, java.time.Duration.ofMillis(100));

    // 立即获取应该成功
    assertEquals(value, cacheUtil.get(key, String.class));

    // 等待过期
    Thread.sleep(150);

    // 过期后应该返回null
    assertNull(cacheUtil.get(key, String.class));
  }

  @Test
  void testGetWithDefaultValue() {
    // 测试获取不存在的key时返回默认值
    String key = "non-existent-key";
    String defaultValue = "default";

    String result = cacheUtil.get(key, String.class);
    if (result == null) {
      result = defaultValue;
    }

    assertEquals(defaultValue, result);
  }

  @Test
  void testGetOrCompute() {
    // 测试getOrCompute方法
    String key = "compute-key";
    String computedValue = "computed-value";

    String result = cacheUtil.getOrCompute(key, String.class, () -> computedValue);

    assertEquals(computedValue, result);

    // 再次调用应该从缓存获取，不会重新计算
    String cachedResult = cacheUtil.getOrCompute(key, String.class, () -> "new-value");
    assertEquals(computedValue, cachedResult);
  }

  @Test
  void testGetOrComputeWithExpiration() throws InterruptedException {
    // 测试带过期时间的getOrCompute
    String key = "compute-expire-key";
    String computedValue = "computed-expire-value";

    String result = cacheUtil.getOrCompute(key, String.class, () -> computedValue, 1);
    assertEquals(computedValue, result);

    // 等待过期
    Thread.sleep(150);

    // 过期后重新计算
    String newValue = "new-computed-value";
    String newResult = cacheUtil.getOrCompute(key, String.class, () -> newValue, 1);
    assertEquals(newValue, newResult);
  }

  @Test
  void testRemove() {
    // 测试删除缓存
    String key = "remove-key";
    String value = "remove-value";

    cacheUtil.put(key, value);
    assertTrue(cacheUtil.exists(key));

    cacheUtil.remove(key);
    assertFalse(cacheUtil.exists(key));
    assertNull(cacheUtil.get(key, String.class));
  }

  @Test
  void testExists() {
    // 测试exists方法
    String key = "exists-key";
    String value = "exists-value";

    assertFalse(cacheUtil.exists(key));

    cacheUtil.put(key, value);
    assertTrue(cacheUtil.exists(key));
  }

  @Test
  void testClear() {
    // 测试清空缓存
    cacheUtil.put("key1", "value1");
    cacheUtil.put("key2", "value2");

    assertFalse(cacheUtil.isEmpty());

    cacheUtil.clear();

    assertTrue(cacheUtil.isEmpty());
    assertEquals(0, cacheUtil.size());
  }

  @Test
  void testSize() {
    // 测试size方法
    assertEquals(0, cacheUtil.size());

    cacheUtil.put("key1", "value1");
    assertEquals(1, cacheUtil.size());

    cacheUtil.put("key2", "value2");
    assertEquals(2, cacheUtil.size());

    cacheUtil.remove("key1");
    assertEquals(1, cacheUtil.size());
  }

  @Test
  void testIsEmpty() {
    // 测试isEmpty方法
    assertTrue(cacheUtil.isEmpty());

    cacheUtil.put("key", "value");
    assertFalse(cacheUtil.isEmpty());

    cacheUtil.clear();
    assertTrue(cacheUtil.isEmpty());
  }

  @Test
  void testGetStats() {
    // 测试获取缓存统计信息
    cacheUtil.put("key1", "value1");
    cacheUtil.get("key1", String.class);
    cacheUtil.get("non-existent", String.class);

    CacheUtil.CacheStats stats = cacheUtil.getStats();

    assertNotNull(stats);
    assertTrue(stats.getTotalEntries() >= 0);
    assertTrue(stats.getTotalAccessCount() >= 0);
  }

  @Test
  void testMultiGet() {
    // 测试批量获取
    cacheUtil.put("key1", "value1");
    cacheUtil.put("key2", "value2");
    cacheUtil.put("key3", "value3");

    List<String> keys = Arrays.asList("key1", "key2", "key4");
    Map<String, Object> results = cacheUtil.multiGet(keys);

    assertEquals(2, results.size());
    assertEquals("value1", results.get("key1"));
    assertEquals("value2", results.get("key2"));
    assertNull(results.get("key4"));
  }

  @Test
  void testMultiPut() {
    // 测试批量设置
    Map<String, Object> data =
        Map.of(
            "key1", "value1",
            "key2", "value2",
            "key3", "value3");

    cacheUtil.multiPut(data, 30);

    assertEquals("value1", cacheUtil.get("key1", String.class));
    assertEquals("value2", cacheUtil.get("key2", String.class));
    assertEquals("value3", cacheUtil.get("key3", String.class));
  }

  @Test
  void testRemoveByPattern() {
    // 测试按模式删除
    cacheUtil.put("user:1:profile", "profile1");
    cacheUtil.put("user:2:profile", "profile2");
    cacheUtil.put("user:1:settings", "settings1");
    cacheUtil.put("course:1:info", "course1");

    assertEquals(4, cacheUtil.size());

    cacheUtil.removeByPattern("user:*:profile");

    assertEquals(2, cacheUtil.size());
    assertNull(cacheUtil.get("user:1:profile", String.class));
    assertNull(cacheUtil.get("user:2:profile", String.class));
    assertNotNull(cacheUtil.get("user:1:settings", String.class));
    assertNotNull(cacheUtil.get("course:1:info", String.class));
  }

  @Test
  void testExpire() throws InterruptedException {
    // 测试设置过期时间
    String key = "expire-test-key";
    String value = "expire-test-value";

    cacheUtil.put(key, value);
    cacheUtil.expire(key, 100);

    // 立即获取应该成功
    assertEquals(value, cacheUtil.get(key, String.class));

    // 等待过期
    Thread.sleep(150);

    // 过期后应该返回null
    assertNull(cacheUtil.get(key, String.class));
  }

  @Test
  void testGetExpire() {
    // 测试获取过期时间
    String key = "expire-time-key";
    String value = "expire-time-value";

    cacheUtil.put(key, value, 1000);

    long expireTime = cacheUtil.getExpire(key);

    assertTrue(expireTime > 0);
    assertTrue(expireTime <= 1000);
  }

  @Test
  void testCleanExpiredEntries() throws InterruptedException {
    // 测试清理过期条目
    cacheUtil.put("key1", "value1", 50);
    cacheUtil.put("key2", "value2", 200);

    assertEquals(2, cacheUtil.size());

    // 等待第一个key过期
    Thread.sleep(100);

    // 由于cleanExpiredEntries是私有方法，我们通过触发其他操作来间接测试过期清理
    // 通过调用size()方法来触发内部清理
    cacheUtil.size();

    assertEquals(1, cacheUtil.size());
    assertNull(cacheUtil.get("key1", String.class));
    assertNotNull(cacheUtil.get("key2", String.class));
  }

  @Test
  void testConcurrentAccess() throws Exception {
    // 测试并发访问
    String key = "concurrent-key";
    int threadCount = 10;

    CompletableFuture<?>[] futures = new CompletableFuture[threadCount];

    for (int i = 0; i < threadCount; i++) {
      final int index = i;
      futures[i] =
          CompletableFuture.runAsync(
              () -> {
                cacheUtil.put(key + index, "value" + index);
                String result = cacheUtil.get(key + index, String.class);
                assertEquals("value" + index, result);
              });
    }

    CompletableFuture.allOf(futures).get();

    assertEquals(threadCount, cacheUtil.size());
  }

  @Test
  void testNullValueHandling() {
    // 测试null值处理
    String key = "null-key";

    cacheUtil.put(key, null);

    // 应该能够存储和获取null值
    assertTrue(cacheUtil.exists(key));
    assertNull(cacheUtil.get(key, String.class));
  }

  @Test
  void testTypeConversion() {
    // 测试类型转换
    String key = "type-key";
    Integer value = 123;

    cacheUtil.put(key, value);

    Integer result = cacheUtil.get(key, Integer.class);
    assertEquals(value, result);

    // 错误的类型转换应该返回null
    String wrongType = cacheUtil.get(key, String.class);
    assertNull(wrongType);
  }
}

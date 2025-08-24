package com.wanli.backend.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** DatabaseUtil 单元测试 */
@ExtendWith(MockitoExtension.class)
class DatabaseUtilTest {

  @Mock private DataSource dataSource;

  @Mock private PlatformTransactionManager transactionManager;

  @Mock private TransactionTemplate transactionTemplate;

  @Mock private JpaRepository<TestEntity, Long> repository;

  @Mock private Connection connection;

  @InjectMocks private DatabaseUtil databaseUtil;

  private TestEntity testEntity;

  @BeforeEach
  void setUp() {
    testEntity = new TestEntity(1L, "Test Entity");
  }

  @Test
  void testFindByIdSafely_Success() {
    // 准备测试数据
    Long id = 1L;
    when(repository.findById(id)).thenReturn(Optional.of(testEntity));

    // 执行测试
    Optional<TestEntity> result = databaseUtil.findByIdSafely(repository, id);

    // 验证结果
    assertTrue(result.isPresent());
    assertEquals(testEntity, result.get());
    verify(repository).findById(id);
  }

  @Test
  void testFindByIdSafely_NotFound() {
    // 准备测试数据
    Long id = 999L;
    when(repository.findById(id)).thenReturn(Optional.empty());

    // 执行测试
    Optional<TestEntity> result = databaseUtil.findByIdSafely(repository, id);

    // 验证结果
    assertFalse(result.isPresent());
    verify(repository).findById(id);
  }

  @Test
  void testFindByIdSafely_Exception() {
    // 准备测试数据
    Long id = 1L;
    when(repository.findById(id)).thenThrow(new RuntimeException("Database error"));

    // 执行测试
    Optional<TestEntity> result = databaseUtil.findByIdSafely(repository, id);

    // 验证结果
    assertFalse(result.isPresent());
    verify(repository).findById(id);
  }

  @Test
  void testSaveSafely_Success() {
    // 准备测试数据
    when(repository.save(testEntity)).thenReturn(testEntity);

    // 执行测试
    Optional<TestEntity> result = databaseUtil.saveSafely(repository, testEntity);

    // 验证结果
    assertTrue(result.isPresent());
    assertEquals(testEntity, result.get());
    verify(repository).save(testEntity);
  }

  @Test
  void testSaveSafely_Exception() {
    // 准备测试数据
    when(repository.save(testEntity)).thenThrow(new RuntimeException("Save failed"));

    // 执行测试
    Optional<TestEntity> result = databaseUtil.saveSafely(repository, testEntity);

    // 验证结果
    assertFalse(result.isPresent());
    verify(repository).save(testEntity);
  }

  @Test
  void testDeleteByIdSafely_Success() {
    // 准备测试数据
    Long id = 1L;
    doNothing().when(repository).deleteById(id);

    // 执行测试
    boolean result = databaseUtil.deleteByIdSafely(repository, id);

    // 验证结果
    assertTrue(result);
    verify(repository).deleteById(id);
  }

  @Test
  void testDeleteByIdSafely_Exception() {
    // 准备测试数据
    Long id = 1L;
    doThrow(new RuntimeException("Delete failed")).when(repository).deleteById(id);

    // 执行测试
    boolean result = databaseUtil.deleteByIdSafely(repository, id);

    // 验证结果
    assertFalse(result);
    verify(repository).deleteById(id);
  }

  @Test
  void testFindAllSafely_Success() {
    // 准备测试数据
    List<TestEntity> entities =
        Arrays.asList(new TestEntity(1L, "Entity 1"), new TestEntity(2L, "Entity 2"));
    when(repository.findAll()).thenReturn(entities);

    // 执行测试
    List<TestEntity> result = databaseUtil.findAllSafely(repository);

    // 验证结果
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(entities, result);
    verify(repository).findAll();
  }

  @Test
  void testFindAllSafely_Exception() {
    // 准备测试数据
    when(repository.findAll()).thenThrow(new RuntimeException("Find all failed"));

    // 执行测试
    List<TestEntity> result = databaseUtil.findAllSafely(repository);

    // 验证结果
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(repository).findAll();
  }

  @Test
  void testCountSafely_Success() {
    // 准备测试数据
    long expectedCount = 10L;
    when(repository.count()).thenReturn(expectedCount);

    // 执行测试
    long result = databaseUtil.countSafely(repository);

    // 验证结果
    assertEquals(expectedCount, result);
    verify(repository).count();
  }

  @Test
  void testCountSafely_Exception() {
    // 准备测试数据
    when(repository.count()).thenThrow(new RuntimeException("Count failed"));

    // 执行测试
    long result = databaseUtil.countSafely(repository);

    // 验证结果
    assertEquals(0L, result);
    verify(repository).count();
  }

  @Test
  void testExistsByIdSafely_True() {
    // 准备测试数据
    Long id = 1L;
    when(repository.existsById(id)).thenReturn(true);

    // 执行测试
    boolean result = databaseUtil.existsByIdSafely(repository, id);

    // 验证结果
    assertTrue(result);
    verify(repository).existsById(id);
  }

  @Test
  void testExistsByIdSafely_False() {
    // 准备测试数据
    Long id = 999L;
    when(repository.existsById(id)).thenReturn(false);

    // 执行测试
    boolean result = databaseUtil.existsByIdSafely(repository, id);

    // 验证结果
    assertFalse(result);
    verify(repository).existsById(id);
  }

  @Test
  void testExistsByIdSafely_Exception() {
    // 准备测试数据
    Long id = 1L;
    when(repository.existsById(id)).thenThrow(new RuntimeException("Exists check failed"));

    // 执行测试
    boolean result = databaseUtil.existsByIdSafely(repository, id);

    // 验证结果
    assertFalse(result);
    verify(repository).existsById(id);
  }

  @Test
  void testSaveAllSafely_Success() {
    // 准备测试数据
    List<TestEntity> entities =
        Arrays.asList(new TestEntity(1L, "Entity 1"), new TestEntity(2L, "Entity 2"));
    when(repository.saveAll(entities)).thenReturn(entities);

    // 执行测试
    List<TestEntity> result = databaseUtil.saveAllSafely(repository, entities);

    // 验证结果
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(entities, result);
    verify(repository).saveAll(entities);
  }

  @Test
  void testSaveAllSafely_Exception() {
    // 准备测试数据
    List<TestEntity> entities =
        Arrays.asList(new TestEntity(1L, "Entity 1"), new TestEntity(2L, "Entity 2"));
    when(repository.saveAll(entities)).thenThrow(new RuntimeException("Save all failed"));

    // 执行测试
    List<TestEntity> result = databaseUtil.saveAllSafely(repository, entities);

    // 验证结果
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(repository).saveAll(entities);
  }

  @Test
  void testDeleteAllSafely_Success() {
    // 准备测试数据
    List<TestEntity> entities =
        Arrays.asList(new TestEntity(1L, "Entity 1"), new TestEntity(2L, "Entity 2"));
    doNothing().when(repository).deleteAll(entities);

    // 执行测试
    boolean result = databaseUtil.deleteAllSafely(repository, entities);

    // 验证结果
    assertTrue(result);
    verify(repository).deleteAll(entities);
  }

  @Test
  void testDeleteAllSafely_Exception() {
    // 准备测试数据
    List<TestEntity> entities =
        Arrays.asList(new TestEntity(1L, "Entity 1"), new TestEntity(2L, "Entity 2"));
    doThrow(new RuntimeException("Delete all failed")).when(repository).deleteAll(entities);

    // 执行测试
    boolean result = databaseUtil.deleteAllSafely(repository, entities);

    // 验证结果
    assertFalse(result);
    verify(repository).deleteAll(entities);
  }

  @Test
  void testExecuteInTransaction_Success() {
    // 准备测试数据
    String operation = "TEST_TRANSACTION";
    String userId = "testUser";
    String expectedResult = "Transaction result";
    Supplier<String> transactionSupplier = () -> expectedResult;

    // 执行测试
    String result = DatabaseUtil.executeInTransaction(operation, userId, transactionSupplier);

    // 验证结果
    assertEquals(expectedResult, result);
  }

  @Test
  void testExecuteInTransaction_Exception() {
    // 准备测试数据
    String operation = "TEST_TRANSACTION";
    String userId = "testUser";
    Supplier<String> transactionSupplier =
        () -> {
          throw new RuntimeException("Transaction failed");
        };

    // 执行测试并验证异常
    assertThrows(
        RuntimeException.class,
        () -> {
          DatabaseUtil.executeInTransaction(operation, userId, transactionSupplier);
        });
  }

  @Test
  void testIsHealthy_Healthy() {
    // 准备测试数据
    when(repository.count()).thenReturn(10L);

    // 执行测试
    boolean result = databaseUtil.isHealthy(repository);

    // 验证结果
    assertTrue(result);
    verify(repository).count();
  }

  @Test
  void testIsHealthy_Unhealthy() {
    // 准备测试数据
    when(repository.count()).thenThrow(new RuntimeException("Database connection failed"));

    // 执行测试
    boolean result = databaseUtil.isHealthy(repository);

    // 验证结果
    assertFalse(result);
    verify(repository).count();
  }

  @Test
  void testIsHealthy_Exception() {
    // 准备测试数据
    when(repository.count()).thenThrow(new RuntimeException("Database error"));

    // 执行测试
    boolean result = databaseUtil.isHealthy(repository);

    // 验证结果
    assertFalse(result);
    verify(repository).count();
  }

  @Test
  void testGetStats() {
    // 执行测试
    DatabaseUtil.DatabaseStats stats = databaseUtil.getStats();

    // 验证结果
    assertNotNull(stats);
    assertTrue(stats.getQueryCount() >= 0);
    assertTrue(stats.getUpdateCount() >= 0);
    assertTrue(stats.getAverageQueryTime() >= 0);
    assertTrue(stats.getAverageUpdateTime() >= 0);
  }

  @Test
  void testExecuteQuery() {
    // 准备测试数据
    String operation = "TEST_QUERY";
    String tableName = "test_table";
    String userId = "testUser";
    String expectedResult = "Query result";
    Supplier<String> queryOperation = () -> expectedResult;

    // 执行测试
    String result = DatabaseUtil.executeQuery(operation, tableName, userId, queryOperation);

    // 验证结果
    assertEquals(expectedResult, result);
  }

  @Test
  void testExecuteUpdate() {
    // 准备测试数据
    String operation = "TEST_UPDATE";
    String tableName = "test_table";
    String userId = "testUser";
    int expectedResult = 1;
    Supplier<Integer> updateOperation = () -> expectedResult;

    // 执行测试
    int result = DatabaseUtil.executeUpdate(operation, tableName, userId, updateOperation);

    // 验证结果
    assertEquals(expectedResult, result);
  }

  @Test
  void testSaveAndFlushSafely_Success() {
    // 准备测试数据
    when(repository.saveAndFlush(testEntity)).thenReturn(testEntity);

    // 执行测试
    Optional<TestEntity> result = databaseUtil.saveAndFlushSafely(repository, testEntity);

    // 验证结果
    assertTrue(result.isPresent());
    assertEquals(testEntity, result.get());
    verify(repository).saveAndFlush(testEntity);
  }

  @Test
  void testSaveAndFlushSafely_Exception() {
    // 准备测试数据
    when(repository.saveAndFlush(testEntity))
        .thenThrow(new RuntimeException("Save and flush failed"));

    // 执行测试
    Optional<TestEntity> result = databaseUtil.saveAndFlushSafely(repository, testEntity);

    // 验证结果
    assertFalse(result.isPresent());
    verify(repository).saveAndFlush(testEntity);
  }

  @Test
  void testNullInputHandling() {
    // 测试null输入处理

    // findByIdSafely with null id
    Optional<TestEntity> result1 = databaseUtil.findByIdSafely(repository, null);
    assertFalse(result1.isPresent());

    // saveSafely with null entity
    Optional<TestEntity> result2 = databaseUtil.saveSafely(repository, null);
    assertFalse(result2.isPresent());

    // deleteByIdSafely with null id
    boolean result3 = databaseUtil.deleteByIdSafely(repository, null);
    assertFalse(result3);

    // existsByIdSafely with null id
    boolean result4 = databaseUtil.existsByIdSafely(repository, null);
    assertFalse(result4);

    // saveAllSafely with null list
    List<TestEntity> result5 = databaseUtil.saveAllSafely(repository, null);
    assertNotNull(result5);
    assertTrue(result5.isEmpty());

    // deleteAllSafely with null list
    boolean result6 = databaseUtil.deleteAllSafely(repository, null);
    assertFalse(result6);
  }

  @Test
  void testEmptyListHandling() {
    // 测试空列表处理
    List<TestEntity> emptyList = Arrays.asList();

    // saveAllSafely with empty list
    when(repository.saveAll(emptyList)).thenReturn(emptyList);
    List<TestEntity> result1 = databaseUtil.saveAllSafely(repository, emptyList);
    assertNotNull(result1);
    assertTrue(result1.isEmpty());

    // deleteAllSafely with empty list
    doNothing().when(repository).deleteAll(emptyList);
    boolean result2 = databaseUtil.deleteAllSafely(repository, emptyList);
    assertTrue(result2);
  }

  // 测试实体类
  private static class TestEntity {
    private Long id;
    private String name;

    public TestEntity(Long id, String name) {
      this.id = id;
      this.name = name;
    }

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null || getClass() != obj.getClass()) return false;
      TestEntity that = (TestEntity) obj;
      return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
      return id != null ? id.hashCode() : 0;
    }
  }
}

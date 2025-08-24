# 测试覆盖率改进计划

## 当前状态

- **当前覆盖率**: 43%
- **目标覆盖率**: 80%
- **差距**: 37%
- **状态**: ❌ 未达标

## 覆盖率分析

### 主要未覆盖的组件

基于当前项目结构，以下组件需要增加测试覆盖：

1. **控制器层 (Controllers)**
   - `AuthController` - 用户认证相关接口
   - `CourseController` - 课程管理接口
   - `LessonController` - 课时管理接口
   - `HealthController` - 健康检查接口

2. **服务层 (Services)**
   - `AuthService` - 认证业务逻辑
   - `CourseService` - 课程业务逻辑
   - `LessonService` - 课时业务逻辑

3. **配置类 (Configuration)**
   - `SecurityConfig` - 安全配置
   - `JwtAuthenticationFilter` - JWT过滤器
   - `GlobalExceptionHandler` - 全局异常处理

4. **实体类 (Entities)**
   - `User` - 用户实体
   - `Course` - 课程实体
   - `Lesson` - 课时实体

## 改进计划

### 阶段一：核心业务逻辑测试 (目标: 60%)

#### 1. 服务层测试

**AuthService 测试**
```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    
    @InjectMocks
    private AuthService authService;
    
    @Test
    void testRegisterUser_Success() {
        // 测试用户注册成功场景
    }
    
    @Test
    void testRegisterUser_UserAlreadyExists() {
        // 测试用户已存在场景
    }
    
    @Test
    void testLogin_Success() {
        // 测试登录成功场景
    }
    
    @Test
    void testLogin_InvalidCredentials() {
        // 测试登录失败场景
    }
}
```

**CourseService 测试**
```java
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {
    
    @Mock
    private CourseRepository courseRepository;
    
    @InjectMocks
    private CourseService courseService;
    
    @Test
    void testCreateCourse_Success() {
        // 测试创建课程成功
    }
    
    @Test
    void testGetCourseById_Found() {
        // 测试根据ID查找课程
    }
    
    @Test
    void testGetCourseById_NotFound() {
        // 测试课程不存在场景
    }
    
    @Test
    void testUpdateCourse_Success() {
        // 测试更新课程
    }
    
    @Test
    void testDeleteCourse_Success() {
        // 测试删除课程
    }
}
```

**LessonService 测试**
```java
@ExtendWith(MockitoExtension.class)
class LessonServiceTest {
    
    @Mock
    private LessonRepository lessonRepository;
    
    @Mock
    private CourseRepository courseRepository;
    
    @InjectMocks
    private LessonService lessonService;
    
    @Test
    void testCreateLesson_Success() {
        // 测试创建课时
    }
    
    @Test
    void testGetLessonsByCourseId() {
        // 测试根据课程ID获取课时列表
    }
    
    @Test
    void testUpdateLesson_Success() {
        // 测试更新课时
    }
    
    @Test
    void testDeleteLesson_Success() {
        // 测试删除课时
    }
}
```

#### 2. 实体类测试

**User 实体测试**
```java
class UserTest {
    
    @Test
    void testUserCreation() {
        // 测试用户对象创建
    }
    
    @Test
    void testUserValidation() {
        // 测试用户数据验证
    }
    
    @Test
    void testUserEqualsAndHashCode() {
        // 测试equals和hashCode方法
    }
}
```

### 阶段二：控制器层测试 (目标: 75%)

#### 1. Web层集成测试

**AuthController 测试**
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
class AuthControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void testRegister_Success() throws Exception {
        // 测试用户注册接口
    }
    
    @Test
    void testLogin_Success() throws Exception {
        // 测试用户登录接口
    }
    
    @Test
    void testLogin_InvalidCredentials() throws Exception {
        // 测试登录失败场景
    }
}
```

**CourseController 测试**
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
class CourseControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testCreateCourse_Success() throws Exception {
        // 测试创建课程接口
    }
    
    @Test
    void testGetCourses() throws Exception {
        // 测试获取课程列表
    }
    
    @Test
    void testGetCourseById() throws Exception {
        // 测试根据ID获取课程
    }
    
    @Test
    void testUpdateCourse() throws Exception {
        // 测试更新课程
    }
    
    @Test
    void testDeleteCourse() throws Exception {
        // 测试删除课程
    }
}
```

### 阶段三：配置和异常处理测试 (目标: 80%+)

#### 1. 安全配置测试

**SecurityConfig 测试**
```java
@SpringBootTest
class SecurityConfigTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testPublicEndpointsAccessible() throws Exception {
        // 测试公开端点可访问
    }
    
    @Test
    void testProtectedEndpointsRequireAuth() throws Exception {
        // 测试受保护端点需要认证
    }
}
```

#### 2. 异常处理测试

**GlobalExceptionHandler 测试**
```java
@SpringBootTest
class GlobalExceptionHandlerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testHandleValidationException() throws Exception {
        // 测试验证异常处理
    }
    
    @Test
    void testHandleResourceNotFoundException() throws Exception {
        // 测试资源未找到异常
    }
    
    @Test
    void testHandleGenericException() throws Exception {
        // 测试通用异常处理
    }
}
```

## 测试工具和配置

### 测试配置文件

**application-test.properties**
```properties
# 测试数据库配置
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA配置
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# JWT配置
jwt.secret=test-secret-key-for-testing-purposes-only
jwt.expiration=3600000

# 日志配置
logging.level.com.wanli=DEBUG
logging.level.org.springframework.security=DEBUG
```

### 测试基类

**BaseIntegrationTest**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@Rollback
public abstract class BaseIntegrationTest {
    
    @Autowired
    protected MockMvc mockMvc;
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    @Autowired
    protected TestEntityManager entityManager;
    
    protected String asJsonString(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
    
    protected <T> T fromJsonString(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }
}
```

### 测试数据构建器

**TestDataBuilder**
```java
public class TestDataBuilder {
    
    public static User createTestUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        return user;
    }
    
    public static Course createTestCourse() {
        Course course = new Course();
        course.setTitle("测试课程");
        course.setDescription("这是一个测试课程");
        course.setPrice(new BigDecimal("99.99"));
        return course;
    }
    
    public static Lesson createTestLesson(Course course) {
        Lesson lesson = new Lesson();
        lesson.setTitle("测试课时");
        lesson.setContent("这是测试课时内容");
        lesson.setCourse(course);
        lesson.setOrderIndex(1);
        return lesson;
    }
}
```

## 执行计划

### 第1周：服务层测试
- [ ] 完成 AuthService 测试
- [ ] 完成 CourseService 测试
- [ ] 完成 LessonService 测试
- [ ] 目标覆盖率：60%

### 第2周：控制器层测试
- [ ] 完成 AuthController 测试
- [ ] 完成 CourseController 测试
- [ ] 完成 LessonController 测试
- [ ] 完成 HealthController 测试
- [ ] 目标覆盖率：75%

### 第3周：配置和异常处理测试
- [ ] 完成 SecurityConfig 测试
- [ ] 完成 GlobalExceptionHandler 测试
- [ ] 完成 JwtAuthenticationFilter 测试
- [ ] 完成实体类测试
- [ ] 目标覆盖率：80%+

## 质量保证措施

### 1. 测试质量检查
- 每个测试方法都应该有明确的测试目标
- 使用 AAA 模式（Arrange, Act, Assert）
- 测试用例应该覆盖正常流程和异常流程
- 使用有意义的测试方法名称

### 2. 持续集成
- 每次提交都运行完整测试套件
- 覆盖率报告自动生成
- 覆盖率低于80%时构建失败

### 3. 代码审查
- 新增测试代码需要经过代码审查
- 确保测试代码质量和可维护性
- 验证测试用例的有效性

## 临时解决方案

在完成完整测试覆盖之前，可以临时调整覆盖率阈值：

```xml
<!-- pom.xml 中临时调整 -->
<rule>
    <element>BUNDLE</element>
    <limits>
        <limit>
            <counter>INSTRUCTION</counter>
            <value>COVEREDRATIO</value>
            <minimum>0.60</minimum> <!-- 临时降低到60% -->
        </limit>
    </limits>
</rule>
```

**注意：** 这只是临时措施，最终目标仍然是达到80%的覆盖率。

## 监控和报告

### 每日检查
- 查看覆盖率趋势
- 识别新增未覆盖代码
- 更新测试计划

### 每周报告
- 覆盖率进展报告
- 测试质量评估
- 问题和风险识别

### 工具支持
- JaCoCo 覆盖率报告
- SonarCloud 质量分析
- IDE 插件实时反馈

## 相关资源

- [JUnit 5 用户指南](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito 文档](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot 测试指南](https://spring.io/guides/gs/testing-web/)
- [JaCoCo 文档](https://www.jacoco.org/jacoco/trunk/doc/)

---

**下一步行动：** 开始执行阶段一的服务层测试，优先完成核心业务逻辑的测试覆盖。
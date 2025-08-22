# 万里在线教育平台 - 工程规范文档

**版本:** V1.0  
**制定日期:** 2025年8月22日  
**适用范围:** 万里在线教育平台后端项目  
**维护团队:** 后端开发团队

## 📋 目录

1. [概述](#概述)
2. [命名规范](#命名规范)
3. [代码结构规范](#代码结构规范)
4. [编码规范](#编码规范)
5. [安全规范](#安全规范)
6. [数据库规范](#数据库规范)
7. [API设计规范](#api设计规范)
8. [测试规范](#测试规范)
9. [文档规范](#文档规范)
10. [版本控制规范](#版本控制规范)
11. [代码质量检查](#代码质量检查)

## 🎯 概述

本文档定义了万里在线教育平台后端开发的工程规范，旨在确保代码质量、可维护性和团队协作效率。所有开发人员必须严格遵守本规范。

### 核心原则

- **一致性** - 统一的命名和编码风格
- **可读性** - 清晰易懂的代码结构
- **可维护性** - 模块化和低耦合设计
- **安全性** - 遵循安全最佳实践
- **可测试性** - 便于单元测试和集成测试

## 🏷️ 命名规范

### Java类和接口命名

#### 实体类 (Entity)
```java
// ✅ 正确示例
public class User { }           // 用户实体
public class Course { }         // 课程实体
public class Lesson { }         // 课时实体
public class Homework { }       // 作业实体
public class Question { }       // 题目实体
public class Submission { }     // 提交实体
public class StudentAnswer { }  // 学生答案实体

// ❌ 错误示例
public class user { }           // 首字母应大写
public class UserInfo { }       // 避免Info后缀
public class UserData { }       // 避免Data后缀
```

#### 控制器类 (Controller)
```java
// ✅ 正确示例
@RestController
public class AuthController { }     // 认证控制器
public class CourseController { }   // 课程控制器
public class LessonController { }   // 课时控制器

// ❌ 错误示例
public class AuthCtrl { }           // 避免缩写
public class CourseHandler { }      // 统一使用Controller后缀
```

#### 服务类 (Service)
```java
// ✅ 正确示例
@Service
public class UserService { }        // 用户服务
public class CourseService { }      // 课程服务
public class AuthService { }        // 认证服务

// 接口命名
public interface UserService { }    // 服务接口
public class UserServiceImpl implements UserService { } // 实现类
```

#### 仓储类 (Repository)
```java
// ✅ 正确示例
public interface UserRepository extends JpaRepository<User, Long> { }
public interface CourseRepository extends JpaRepository<Course, Long> { }

// ❌ 错误示例
public interface UserDao { }        // 统一使用Repository
public interface UserMapper { }     // 统一使用Repository
```

### 方法命名

#### CRUD操作
```java
// ✅ 正确示例
public User createUser(CreateUserRequest request) { }       // 创建
public User getUserById(Long id) { }                        // 查询单个
public List<User> getAllUsers() { }                         // 查询列表
public List<User> getUsersByRole(String role) { }           // 条件查询
public User updateUser(Long id, UpdateUserRequest request) { } // 更新
public void deleteUser(Long id) { }                         // 删除
public void softDeleteUser(Long id) { }                     // 软删除

// ❌ 错误示例
public User add(CreateUserRequest request) { }              // 动词不明确
public User find(Long id) { }                               // 动词不明确
public User modify(Long id, UpdateUserRequest request) { }  // 使用update更清晰
```

#### 业务方法
```java
// ✅ 正确示例
public boolean validateUserCredentials(String username, String password) { }
public String generateJwtToken(User user) { }
public boolean isUserEnrolledInCourse(Long userId, Long courseId) { }
public List<Course> getAvailableCoursesForUser(Long userId) { }

// ❌ 错误示例
public boolean check(String username, String password) { }  // 方法名不明确
public String token(User user) { }                          // 方法名不明确
```

### 变量命名

#### 成员变量
```java
// ✅ 正确示例
private Long userId;                    // 用户ID
private String username;                // 用户名
private String hashedPassword;          // 加密后的密码
private LocalDateTime createdAt;        // 创建时间
private LocalDateTime updatedAt;        // 更新时间
private LocalDateTime deletedAt;        // 删除时间
private Boolean isActive;               // 是否激活
private UserRole role;                  // 用户角色

// ❌ 错误示例
private Long id;                        // 不够具体
private String pwd;                     // 避免缩写
private Date created;                   // 使用LocalDateTime
private boolean active;                 // 布尔值使用is前缀
```

#### 局部变量
```java
// ✅ 正确示例
List<Course> availableCourses = courseService.getAvailableCourses();
Optional<User> userOptional = userRepository.findById(userId);
CreateCourseRequest courseRequest = new CreateCourseRequest();

// ❌ 错误示例
List<Course> list = courseService.getAvailableCourses();    // 变量名不明确
Optional<User> opt = userRepository.findById(userId);       // 避免缩写
```

### 常量命名
```java
// ✅ 正确示例
public static final String JWT_SECRET_KEY = "wanli-education-secret";
public static final int JWT_EXPIRATION_TIME = 86400; // 24小时
public static final String DEFAULT_USER_ROLE = "ROLE_STUDENT";
public static final int MAX_COURSE_TITLE_LENGTH = 100;
public static final String API_VERSION_V1 = "/api/v1";

// ❌ 错误示例
public static final String SECRET = "secret";              // 常量名不够具体
public static final int TIME = 86400;                       // 常量名不明确
```

## 🏗️ 代码结构规范

### 包结构
```
com.wanli.education/
├── WanliEducationApplication.java          # 主启动类
├── config/                                 # 配置类
│   ├── SecurityConfig.java
│   ├── JwtConfig.java
│   └── DatabaseConfig.java
├── controller/                             # 控制器层
│   ├── AuthController.java
│   ├── CourseController.java
│   └── LessonController.java
├── service/                                # 服务层
│   ├── UserService.java
│   ├── CourseService.java
│   └── AuthService.java
├── repository/                             # 数据访问层
│   ├── UserRepository.java
│   ├── CourseRepository.java
│   └── LessonRepository.java
├── entity/                                 # 实体类
│   ├── User.java
│   ├── Course.java
│   └── Lesson.java
├── dto/                                    # 数据传输对象
│   ├── request/
│   │   ├── CreateUserRequest.java
│   │   └── CreateCourseRequest.java
│   └── response/
│       ├── UserResponse.java
│       └── CourseResponse.java
├── exception/                              # 异常处理
│   ├── GlobalExceptionHandler.java
│   ├── BusinessException.java
│   └── ValidationException.java
├── security/                               # 安全相关
│   ├── JwtAuthenticationFilter.java
│   ├── JwtTokenProvider.java
│   └── UserDetailsServiceImpl.java
└── util/                                   # 工具类
    ├── DateUtil.java
    ├── ValidationUtil.java
    └── PasswordUtil.java
```

### 类结构顺序
```java
// ✅ 正确的类结构顺序
@Entity
@Table(name = "users")
public class User {
    
    // 1. 静态常量
    public static final String DEFAULT_ROLE = "ROLE_STUDENT";
    
    // 2. 成员变量（按访问修饰符排序：public -> protected -> private）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    // 3. 构造函数（无参构造函数在前）
    public User() {}
    
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    // 4. Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    // 5. 业务方法
    public boolean isActive() {
        return deletedAt == null;
    }
    
    // 6. equals, hashCode, toString
    @Override
    public boolean equals(Object obj) { /* ... */ }
    
    @Override
    public int hashCode() { /* ... */ }
    
    @Override
    public String toString() { /* ... */ }
}
```

## 💻 编码规范

### 注解使用

#### Spring注解
```java
// ✅ 正确示例
@RestController
@RequestMapping("/api/auth")
@Validated
@Slf4j
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody CreateUserRequest request) {
        // 实现逻辑
    }
}

// ❌ 错误示例
@Controller  // 应使用@RestController
@RequestMapping(value = "/api/auth", method = RequestMethod.GET) // 应在方法级别指定
public class AuthController {
    @Autowired
    AuthService authService; // 缺少访问修饰符
}
```

#### JPA注解
```java
// ✅ 正确示例
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### 异常处理

#### 全局异常处理器
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(ValidationException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        return ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBusinessException(BusinessException ex) {
        log.warn("Business error: {}", ex.getMessage());
        return ErrorResponse.builder()
                .code(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
```

#### 自定义异常
```java
// ✅ 正确示例
public class BusinessException extends RuntimeException {
    private final String errorCode;
    
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

// 使用示例
if (userRepository.existsByUsername(username)) {
    throw new BusinessException("USER_ALREADY_EXISTS", 
            "用户名已存在: " + username);
}
```

### 日志规范

```java
@Slf4j
@Service
public class UserService {
    
    public User createUser(CreateUserRequest request) {
        log.info("Creating user with username: {}", request.getUsername());
        
        try {
            // 业务逻辑
            User user = new User();
            // ...
            User savedUser = userRepository.save(user);
            
            log.info("User created successfully with ID: {}", savedUser.getId());
            return savedUser;
            
        } catch (Exception ex) {
            log.error("Failed to create user with username: {}", 
                    request.getUsername(), ex);
            throw new BusinessException("USER_CREATION_FAILED", 
                    "用户创建失败");
        }
    }
}
```

## 🔒 安全规范

### 密码处理
```java
// ✅ 正确示例
@Service
public class AuthService {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User registerUser(CreateUserRequest request) {
        // 密码加密
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(hashedPassword); // 存储加密后的密码
        
        return userRepository.save(user);
    }
    
    public boolean validatePassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
}

// ❌ 错误示例
public User registerUser(CreateUserRequest request) {
    User user = new User();
    user.setPassword(request.getPassword()); // 直接存储明文密码
    return userRepository.save(user);
}
```

### JWT令牌处理
```java
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private int jwtExpirationInMs;
    
    public String generateToken(UserDetails userDetails) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);
        
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
    
    // 不在日志中输出敏感信息
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (JwtException ex) {
            log.warn("Invalid JWT token"); // 不输出token内容
            return false;
        }
    }
}
```

### 输入验证
```java
// ✅ 正确示例
public class CreateUserRequest {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 100, message = "密码长度必须在8-100个字符之间")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
             message = "密码必须包含大小写字母和数字")
    private String password;
    
    @Email(message = "邮箱格式不正确")
    private String email;
}
```

## 🗄️ 数据库规范

### 表命名
```sql
-- ✅ 正确示例
CREATE TABLE users (...)           -- 用户表
CREATE TABLE courses (...)         -- 课程表
CREATE TABLE lessons (...)         -- 课时表
CREATE TABLE homeworks (...)       -- 作业表
CREATE TABLE questions (...)       -- 题目表
CREATE TABLE submissions (...)     -- 提交表
CREATE TABLE student_answers (...) -- 学生答案表

-- ❌ 错误示例
CREATE TABLE User (...)            -- 表名应小写
CREATE TABLE course_info (...)     -- 避免info后缀
CREATE TABLE tbl_lesson (...)      -- 避免tbl前缀
```

### 字段命名
```sql
-- ✅ 正确示例
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    role ENUM('ROLE_STUDENT', 'ROLE_HQ_TEACHER') NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);

-- ❌ 错误示例
CREATE TABLE users (
    ID BIGINT,                     -- 字段名应小写
    userName VARCHAR(50),          -- 使用下划线分隔
    pwd VARCHAR(255),              -- 避免缩写
    createTime TIMESTAMP           -- 使用下划线分隔
);
```

### 索引命名
```sql
-- ✅ 正确示例
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_courses_status ON courses(status);
CREATE INDEX idx_lessons_course_id ON lessons(course_id);
CREATE UNIQUE INDEX uk_users_username ON users(username);

-- ❌ 错误示例
CREATE INDEX username_idx ON users(username);     -- 索引名格式不统一
CREATE INDEX index1 ON users(email);              -- 索引名不明确
```

## 🌐 API设计规范

### RESTful API设计
```java
// ✅ 正确示例
@RestController
@RequestMapping("/api/v1")
public class CourseController {
    
    // 获取课程列表
    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponse>> getCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // 实现逻辑
    }
    
    // 获取单个课程
    @GetMapping("/courses/{id}")
    public ResponseEntity<CourseResponse> getCourse(@PathVariable Long id) {
        // 实现逻辑
    }
    
    // 创建课程
    @PostMapping("/courses")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CreateCourseRequest request) {
        // 实现逻辑
    }
    
    // 更新课程
    @PutMapping("/courses/{id}")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseRequest request) {
        // 实现逻辑
    }
    
    // 删除课程
    @DeleteMapping("/courses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        // 实现逻辑
    }
}
```

### 响应格式标准化
```java
// ✅ 统一的响应格式
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    
    // 成功响应
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("操作成功")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    // 错误响应
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
```

## 🧪 测试规范

### 单元测试
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    @DisplayName("创建用户 - 成功场景")
    void createUser_Success() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // When
        User result = userService.createUser(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        
        verify(userRepository).existsByUsername("testuser");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    @DisplayName("创建用户 - 用户名已存在")
    void createUser_UsernameExists_ThrowsException() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("existinguser");
        
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户名已存在: existinguser");
        
        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).save(any(User.class));
    }
}
```

### 集成测试
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class AuthControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    @DisplayName("用户注册 - 集成测试")
    void registerUser_IntegrationTest() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("integrationtest");
        request.setPassword("Password123");
        request.setEmail("test@example.com");
        
        // When
        ResponseEntity<UserResponse> response = restTemplate.postForEntity(
                "/api/auth/register", request, UserResponse.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("integrationtest");
        
        // 验证数据库中的数据
        Optional<User> savedUser = userRepository.findByUsername("integrationtest");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getEmail()).isEqualTo("test@example.com");
    }
}
```

## 📚 文档规范

### 代码注释
```java
/**
 * 用户服务类
 * 
 * 提供用户相关的业务逻辑处理，包括用户注册、登录、信息更新等功能。
 * 
 * @author 万里教育开发团队
 * @version 1.0
 * @since 2025-08-22
 */
@Service
@Slf4j
public class UserService {
    
    /**
     * 创建新用户
     * 
     * @param request 用户创建请求，包含用户名、密码等信息
     * @return 创建成功的用户对象
     * @throws BusinessException 当用户名已存在时抛出
     */
    public User createUser(CreateUserRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("USER_ALREADY_EXISTS", 
                    "用户名已存在: " + request.getUsername());
        }
        
        // 创建用户对象并保存
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.ROLE_STUDENT);
        
        return userRepository.save(user);
    }
}
```

### API文档
```java
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "用户认证相关API")
public class AuthController {
    
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新的用户账号")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "注册成功",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "请求参数错误",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "用户名已存在",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> register(
            @Parameter(description = "用户注册信息", required = true)
            @Valid @RequestBody CreateUserRequest request) {
        // 实现逻辑
    }
}
```

## 📝 版本控制规范

### 分支命名
```bash
# ✅ 正确示例
main                    # 主分支
staging                 # 测试分支
feature/user-auth      # 功能分支
feature/course-mgmt    # 功能分支
bugfix/login-error     # 修复分支
hotfix/security-patch  # 热修复分支

# ❌ 错误示例
dev                    # 使用staging
feat-auth             # 使用feature/前缀
fix-bug               # 使用bugfix/前缀
```

### 提交信息格式
```bash
# ✅ 正确示例
feat: 添加用户注册功能
fix: 修复JWT令牌验证错误
docs: 更新API文档
test: 添加用户服务单元测试
refactor: 重构课程服务代码结构
style: 统一代码格式
chore: 更新依赖版本

# ❌ 错误示例
添加功能                # 缺少类型前缀
Fixed bug              # 使用中文，首字母小写
Update                 # 描述不够具体
```

## 🔍 代码质量检查

### SonarQube规则

本项目使用SonarQube进行代码质量检查，必须满足以下质量门要求：

- **代码覆盖率** ≥ 80%
- **重复代码率** ≤ 3%
- **维护性评级** ≥ A
- **可靠性评级** ≥ A
- **安全性评级** ≥ A
- **技术债务比率** ≤ 5%

### 代码检查清单

#### 提交前检查
- [ ] 代码符合命名规范
- [ ] 添加了必要的单元测试
- [ ] 测试覆盖率达到要求
- [ ] 没有硬编码的敏感信息
- [ ] 添加了适当的日志记录
- [ ] 异常处理完整
- [ ] 代码注释清晰
- [ ] 通过SonarQube质量检查

#### 代码审查要点
- [ ] 业务逻辑正确性
- [ ] 安全性考虑
- [ ] 性能优化
- [ ] 代码可读性
- [ ] 错误处理机制
- [ ] 测试用例完整性

## 🚀 持续改进

### 规范更新机制

1. **定期评审** - 每季度评审一次工程规范
2. **团队反馈** - 收集开发团队的改进建议
3. **版本控制** - 规范文档进行版本管理
4. **培训推广** - 新团队成员必须学习本规范

### 违规处理

1. **自动检查** - SonarQube自动检查代码质量
2. **人工审查** - Code Review发现的问题
3. **改进措施** - 提供具体的修改建议
4. **知识分享** - 定期分享最佳实践

---

**本规范由万里在线教育平台后端开发团队制定和维护**

**最后更新:** 2025年8月22日  
**下次评审:** 2025年11月22日
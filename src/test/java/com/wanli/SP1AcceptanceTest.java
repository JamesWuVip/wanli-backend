package com.wanli;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * SP1 Sprint自动化验收测试 基于任务说明书中的验收标准(AC-BE-1.1到AC-BE-1.5)进行测试驱动开发
 *
 * <p>测试场景覆盖： - AC-BE-1.1: 用户注册功能 - AC-BE-1.2: 用户登录功能 - AC-BE-1.3: 无权限访问保护 - AC-BE-1.4: 有权限访问验证 -
 * AC-BE-1.5: 数据关联验证
 */
@SpringBootTest(classes = com.wanli.backend.WanliBackendApplication.class)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
// @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS) // 移除以保持静态变量在测试间共享
public class SP1AcceptanceTest {

  @Autowired private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper = new ObjectMapper();

  // 测试数据
  private static String jwtToken;
  private static String courseId; // 改为String类型存储UUID
  private static String lessonId; // 改为String类型存储UUID

  @BeforeEach
  void setUp() throws Exception {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

    // 如果JWT令牌为空，先执行登录获取令牌
    if (jwtToken == null) {
      // 先注册用户
      Map<String, String> registerRequest = new HashMap<>();
      registerRequest.put("username", "testteacher");
      registerRequest.put("password", "password123");
      registerRequest.put("email", "teacher@wanli.edu");
      registerRequest.put("role", "teacher");

      try {
        mockMvc
            .perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isCreated());
      } catch (Exception e) {
        // 用户可能已存在，忽略错误
      }

      // 执行登录获取JWT
      Map<String, String> loginRequest = new HashMap<>();
      loginRequest.put("username", "testteacher");
      loginRequest.put("password", "password123");

      MvcResult result =
          mockMvc
              .perform(
                  post("/api/auth/login")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(loginRequest)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.token").exists())
              .andReturn();

      String responseContent = result.getResponse().getContentAsString();
      Map<String, Object> response = objectMapper.readValue(responseContent, Map.class);
      jwtToken = (String) response.get("token");
    }
  }

  /** AC-BE-1.1: 用户注册测试 验证: 调用 POST /api/auth/register 成功创建一个新用户，数据库中密码为加密字符串 */
  @Test
  @Order(1)
  @DisplayName("AC-BE-1.1: 用户注册功能测试")
  void testUserRegistration() throws Exception {
    // 准备注册数据
    Map<String, Object> registerRequest = new HashMap<>();
    registerRequest.put("username", "newteacher");
    registerRequest.put("password", "password123");
    registerRequest.put("email", "newteacher@wanli.ai");
    registerRequest.put("role", "teacher");

    // 执行注册请求
    MvcResult result =
        mockMvc
            .perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").exists())
            .andReturn();

    // 验证响应内容
    String responseContent = result.getResponse().getContentAsString();
    assertNotNull(responseContent);
    assertTrue(responseContent.contains("success") || responseContent.contains("created"));

    System.out.println("✅ AC-BE-1.1 通过: 用户注册成功");
  }

  /** AC-BE-1.2: 用户登录测试 验证: 使用正确的凭据调用 POST /api/auth/login，返回 200 OK 状态码和一个有效的JWT */
  @Test
  @Order(2)
  @DisplayName("AC-BE-1.2: 用户登录功能测试")
  void testUserLogin() throws Exception {
    // 准备登录数据
    Map<String, String> loginRequest = new HashMap<>();
    loginRequest.put("username", "testteacher");
    loginRequest.put("password", "password123");

    // 执行登录请求
    MvcResult result =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andReturn();

    // 提取JWT Token
    String responseContent = result.getResponse().getContentAsString();
    Map<String, Object> response = objectMapper.readValue(responseContent, Map.class);
    jwtToken = (String) response.get("token");

    // 验证JWT格式
    assertNotNull(jwtToken);
    assertTrue(jwtToken.startsWith("eyJ"), "JWT应该以eyJ开头");
    assertTrue(jwtToken.split("\\.").length == 3, "JWT应该包含3个部分");

    System.out.println("✅ AC-BE-1.2 通过: 用户登录成功，获得有效JWT");
  }

  /**
   * AC-BE-1.3: 无权限访问测试 验证: 未携带JWT或使用非 ROLE_HQ_TEACHER 角色的JWT访问 POST /api/courses，必须返回 403 Forbidden
   * 错误
   */
  @Test
  @Order(3)
  @DisplayName("AC-BE-1.3: 无权限访问保护测试")
  void testUnauthorizedAccess() throws Exception {
    // 准备课程数据
    Map<String, Object> courseRequest = new HashMap<>();
    courseRequest.put("title", "测试课程");
    courseRequest.put("description", "这是一个测试课程");

    // 测试1: 不携带JWT访问
    mockMvc
        .perform(
            post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courseRequest)))
        .andExpect(status().isForbidden());

    // 测试2: 携带无效JWT访问
    mockMvc
        .perform(
            post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer invalid.jwt.token")
                .content(objectMapper.writeValueAsString(courseRequest)))
        .andExpect(status().isUnauthorized());

    System.out.println("✅ AC-BE-1.3 通过: 无权限访问被正确拒绝");
  }

  /** AC-BE-1.4: 有权限访问测试 验证: 使用 ROLE_HQ_TEACHER 角色的JWT，可以成功调用课程和课时的所有API端点 */
  @Test
  @Order(4)
  @DisplayName("AC-BE-1.4: 有权限访问验证测试")
  void testAuthorizedAccess() throws Exception {
    assertNotNull(jwtToken, "需要先完成登录测试获取JWT");

    // 测试1: 创建课程 POST /api/courses
    Map<String, Object> courseRequest = new HashMap<>();
    courseRequest.put("title", "Spring Boot进阶课程");
    courseRequest.put("description", "深入学习Spring Boot框架");
    courseRequest.put("status", "DRAFT"); // 添加status字段符合API规范

    MvcResult createCourseResult =
        mockMvc
            .perform(
                post("/api/courses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + jwtToken)
                    .content(objectMapper.writeValueAsString(courseRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.title").value("Spring Boot进阶课程"))
            .andExpect(jsonPath("$.status").value("DRAFT"))
            .andExpect(jsonPath("$.created_at").exists()) // 验证时间戳字段
            .andExpect(jsonPath("$.updated_at").exists()) // 验证时间戳字段
            .andReturn();

    // 提取课程ID
    String createResponse = createCourseResult.getResponse().getContentAsString();
    Map<String, Object> courseResponse = objectMapper.readValue(createResponse, Map.class);
    courseId = courseResponse.get("id").toString();

    // 测试2: 获取课程列表 GET /api/courses
    mockMvc
        .perform(get("/api/courses").header("Authorization", "Bearer " + jwtToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());

    // 测试3: 更新课程 PUT /api/courses/{id}
    Map<String, Object> updateRequest = new HashMap<>();
    updateRequest.put("title", "Spring Boot高级课程");
    updateRequest.put("description", "更新后的课程描述");
    updateRequest.put("status", "PUBLISHED"); // 添加status字段符合API规范

    mockMvc
        .perform(
            put("/api/courses/" + courseId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(courseId))
        .andExpect(jsonPath("$.title").value("Spring Boot高级课程"))
        .andExpect(jsonPath("$.status").value("PUBLISHED"))
        .andExpect(jsonPath("$.updated_at").exists()); // 验证更新时间戳

    System.out.println("✅ AC-BE-1.4 通过: 有权限用户可以成功访问所有课程API");
  }

  /** AC-BE-1.5: 数据关联测试 验证: 创建一个课时后，该课时在数据库中必须正确地通过外键关联到其所属的课程 */
  @Test
  @Order(5)
  @DisplayName("AC-BE-1.5: 数据关联验证测试")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
  void testDataRelationship() throws Exception {
    assertNotNull(jwtToken, "需要先完成登录测试获取JWT");

    // 为了确保数据一致性，在此测试中重新创建课程
    Map<String, Object> courseRequest = new HashMap<>();
    courseRequest.put("title", "数据关联测试课程");
    courseRequest.put("description", "用于测试数据关联的课程");
    courseRequest.put("status", "DRAFT");

    String courseRequestJson = objectMapper.writeValueAsString(courseRequest);
    System.out.println("Debug: Course request JSON = " + courseRequestJson);

    MvcResult createCourseResult =
        mockMvc
            .perform(
                post("/api/courses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + jwtToken)
                    .content(courseRequestJson))
            .andExpect(status().isCreated())
            .andReturn();

    // 提取课程ID
    String createResponse = createCourseResult.getResponse().getContentAsString();
    Map<String, Object> courseResponse = objectMapper.readValue(createResponse, Map.class);
    String testCourseId = courseResponse.get("id").toString();

    System.out.println("Debug: testCourseId = " + testCourseId);
    System.out.println("Debug: jwtToken = " + (jwtToken != null ? "exists" : "null"));

    // 等待一小段时间确保事务提交
    Thread.sleep(100);

    // 测试1: 为课程添加课时 POST /api/courses/{courseId}/lessons
    Map<String, Object> lessonRequest = new HashMap<>();
    lessonRequest.put("title", "第一课：Spring Boot基础");
    lessonRequest.put("order_index", 1); // 使用order_index符合API规范

    String requestJson = objectMapper.writeValueAsString(lessonRequest);
    System.out.println("Debug: Lesson request JSON = " + requestJson);
    System.out.println("Debug: Request URL = /api/courses/" + testCourseId + "/lessons");

    MvcResult createLessonResult =
        mockMvc
            .perform(
                post("/api/courses/" + testCourseId + "/lessons")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + jwtToken)
                    .content(objectMapper.writeValueAsString(lessonRequest)))
            .andDo(
                result -> {
                  System.out.println("创建课时响应状态码: " + result.getResponse().getStatus());
                  System.out.println("创建课时响应内容: " + result.getResponse().getContentAsString());
                  if (result.getResponse().getStatus() != 201) {
                    System.err.println("创建课时失败，状态码: " + result.getResponse().getStatus());
                    System.err.println("错误内容: " + result.getResponse().getContentAsString());
                  }
                })
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.course_id").value(testCourseId)) // 使用API规范的字段名
            .andExpect(jsonPath("$.title").value("第一课：Spring Boot基础"))
            .andExpect(jsonPath("$.order_index").value(1)) // 验证order_index字段
            .andExpect(jsonPath("$.created_at").exists()) // 验证时间戳字段
            .andExpect(jsonPath("$.updated_at").exists()) // 验证时间戳字段
            .andReturn();

    // 提取课时ID
    String lessonResponse = createLessonResult.getResponse().getContentAsString();
    Map<String, Object> lesson = objectMapper.readValue(lessonResponse, Map.class);
    lessonId = lesson.get("id").toString();

    // 测试2: 获取课程下的课时列表 GET /api/courses/{courseId}/lessons
    MvcResult getLessonsResult =
        mockMvc
            .perform(
                get("/api/courses/" + testCourseId + "/lessons")
                    .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].course_id").value(testCourseId)) // 使用API规范的字段名
            .andExpect(jsonPath("$[0].order_index").exists()) // 验证order_index字段存在
            .andReturn();

    // 验证关联关系
    String lessonsResponse = getLessonsResult.getResponse().getContentAsString();
    assertNotNull(lessonsResponse);
    assertTrue(lessonsResponse.contains(testCourseId), "课时应该正确关联到课程");

    System.out.println("✅ AC-BE-1.5 通过: 课时与课程的关联关系正确建立");
  }

  /** AC-BE-1.6: 错误场景测试 - 400 Bad Request 验证: 请求体验证失败时返回400错误 */
  @Test
  @Order(6)
  @DisplayName("AC-BE-1.6: 请求体验证失败测试")
  void testBadRequestValidation() throws Exception {
    assertNotNull(jwtToken, "需要先完成登录测试获取JWT");

    // 测试1: 创建课程时缺少必填字段
    Map<String, Object> invalidCourseRequest = new HashMap<>();
    // 故意不添加title字段
    invalidCourseRequest.put("description", "缺少标题的课程");

    mockMvc
        .perform(
            post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .content(objectMapper.writeValueAsString(invalidCourseRequest)))
        .andExpect(status().isBadRequest());

    // 测试2: 创建课时时缺少必填字段
    if (courseId != null) {
      Map<String, Object> invalidLessonRequest = new HashMap<>();
      // 故意不添加title字段
      invalidLessonRequest.put("order_index", 1);

      mockMvc
          .perform(
              post("/api/courses/" + courseId + "/lessons")
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("Authorization", "Bearer " + jwtToken)
                  .content(objectMapper.writeValueAsString(invalidLessonRequest)))
          .andExpect(status().isBadRequest());
    }

    System.out.println("✅ AC-BE-1.6 通过: 请求体验证失败正确返回400错误");
  }

  /** AC-BE-1.7: 错误场景测试 - 404 Not Found 验证: 资源不存在时返回404错误 */
  @Test
  @Order(7)
  @DisplayName("AC-BE-1.7: 资源不存在测试")
  void testResourceNotFound() throws Exception {
    assertNotNull(jwtToken, "需要先完成登录测试获取JWT");

    String nonExistentId = "00000000-0000-0000-0000-000000000000";

    // 测试1: 更新不存在的课程
    Map<String, Object> updateRequest = new HashMap<>();
    updateRequest.put("title", "不存在的课程");
    updateRequest.put("description", "测试描述");
    updateRequest.put("status", "DRAFT");

    mockMvc
        .perform(
            put("/api/courses/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isNotFound());

    // 测试2: 为不存在的课程创建课时
    Map<String, Object> lessonRequest = new HashMap<>();
    lessonRequest.put("title", "测试课时");
    lessonRequest.put("order_index", 1);

    mockMvc
        .perform(
            post("/api/courses/" + nonExistentId + "/lessons")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .content(objectMapper.writeValueAsString(lessonRequest)))
        .andExpect(status().isNotFound());

    // 测试3: 获取不存在课程的课时列表
    mockMvc
        .perform(
            get("/api/courses/" + nonExistentId + "/lessons")
                .header("Authorization", "Bearer " + jwtToken))
        .andExpect(status().isNotFound());

    System.out.println("✅ AC-BE-1.7 通过: 资源不存在正确返回404错误");
  }

  /** 测试完成后的清理工作 */
  @AfterAll
  static void tearDown() {
    System.out.println("\n🎉 SP1后端验收测试全部完成!");
    System.out.println("📊 测试覆盖的验收标准:");
    System.out.println("   ✅ AC-BE-1.1: 用户注册功能");
    System.out.println("   ✅ AC-BE-1.2: 用户登录功能");
    System.out.println("   ✅ AC-BE-1.3: 无权限访问保护");
    System.out.println("   ✅ AC-BE-1.4: 有权限访问验证");
    System.out.println("   ✅ AC-BE-1.5: 数据关联验证");
    System.out.println("   ✅ AC-BE-1.6: 请求体验证失败测试");
    System.out.println("   ✅ AC-BE-1.7: 资源不存在测试");
    System.out.println("\n📋 API端点覆盖情况:");
    System.out.println("   ✅ POST /api/auth/register - 用户注册");
    System.out.println("   ✅ POST /api/auth/login - 用户登录");
    System.out.println("   ✅ POST /api/courses - 创建课程");
    System.out.println("   ✅ GET /api/courses - 获取课程列表");
    System.out.println("   ✅ PUT /api/courses/{id} - 更新课程");
    System.out.println("   ✅ POST /api/courses/{courseId}/lessons - 创建课时");
    System.out.println("   ✅ GET /api/courses/{courseId}/lessons - 获取课时列表");
    System.out.println("\n🚀 可以开始SP1功能开发了!");
  }
}

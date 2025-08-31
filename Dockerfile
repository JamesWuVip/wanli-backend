# 多阶段构建
FROM openjdk:17-jdk-slim AS builder

WORKDIR /app

# 安装Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# 复制Maven配置文件
COPY pom.xml ./
COPY src ./src/

# 构建应用
RUN mvn clean package -DskipTests

# 生产镜像
FROM openjdk:17-jre-slim AS production

WORKDIR /app

# 创建非root用户
RUN groupadd -r spring && useradd -r -g spring spring

# 安装curl用于健康检查
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 复制JAR文件
COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

# 切换到非root用户
USER spring

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/api/test/health || exit 1

# 启动应用
CMD ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}", "app.jar"]
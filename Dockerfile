FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制Maven包装器和pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# 赋予mvnw执行权限
RUN chmod +x ./mvnw

# 下载依赖（利用Docker缓存）
RUN ./mvnw dependency:go-offline

# 复制源代码
COPY src ./src

# 构建应用
RUN ./mvnw clean package -DskipTests

# 暴露端口
EXPOSE 8080

# 设置环境变量
ENV SPRING_PROFILES_ACTIVE=staging
ENV SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL}
ENV SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}
ENV SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}
ENV JWT_SECRET=${JWT_SECRET}
ENV PORT=${PORT}

# 运行应用
CMD ["java", "-jar", "target/wanli-backend-0.0.1-SNAPSHOT.jar"]
FROM maven:3.8.5-openjdk-11 AS builder
WORKDIR /app

# 先复制 POM，预下载依赖
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并打包
COPY src ./src
RUN mvn package -Pprod -DskipTests -B

# Stage 2: 运行时镜像
FROM openjdk:11-jre-slim
WORKDIR /app

# 把打好的 fat-jar 拷贝过来（名称请和实际 target 下的 jar 保持一致）
COPY --from=builder /app/target/unified-messaging-1.0.0-jar-with-dependencies.jar app.jar

# Cloud Run 默认会把流量导到 $PORT（通常为 8080）
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]

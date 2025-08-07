FROM maven:3.8.5-openjdk-11 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -Pprod -DskipTests -B

FROM openjdk:11-jre-slim
WORKDIR /app

COPY --from=builder /app/target/unified-messaging-1.0.0-jar-with-dependencies.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

# ---- Build stage ----
    FROM maven:3.9-eclipse-temurin-17 AS build
    WORKDIR /app
    
    # Cache deps first
    COPY pom.xml .
    RUN mvn -q -e -DskipTests dependency:go-offline
    
    # Build
    COPY src ./src
    RUN mvn -q -e -DskipTests package
    
    # ---- Run stage ----
    FROM eclipse-temurin:17-jre
    WORKDIR /app
    
    # Copy shaded jar
    COPY --from=build /app/target/app.jar /app/app.jar
    
    # Hint: set FIRESTORE_PROJECT_ID to be explicit inside containers
    ENV FIRESTORE_PROJECT_ID=""
    ENV PORT=8080
    
    EXPOSE 8080
    ENTRYPOINT ["java","-jar","/app/app.jar"]
    
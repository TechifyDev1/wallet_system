# Stage 1: Build using Gradle with JDK 21
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app
# Copy build files
COPY build.gradle settings.gradle ./
COPY src ./src
# Build the JAR
RUN gradle bootJar --no-daemon -x test --info -Dorg.gradle.jvmargs="-Xmx512m -XX:MaxMetaspaceSize=256m"

# Stage 2: Runtime with JRE 21
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# Copy the built JAR
COPY --from=builder /app/build/libs/*.jar app.jar
# Render uses PORT env var; Spring Boot usually listens on 8080 by default
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# ==============================================================================
# EduConnect — Multi-Stage Production Dockerfile
# Stage 1: Maven Build with Java 21
# Stage 2: Minimal, Secure Eclipse Temurin 21 JRE Runtime
# ==============================================================================

# Stage 1: Build the Application
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build

# Copy Maven wrapper and POM first for efficient layer caching
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B

# Copy backend source code and package executable JAR
COPY backend/src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal Runtime Environment
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# Create non-root system group and user for container security
RUN groupadd -r educonnect && useradd -r -g educonnect -d /app educonnect

# Create directory for persisted file uploads and assign ownership
RUN mkdir -p /app/uploads/resources && chown -R educonnect:educonnect /app

# Copy the packaged Spring Boot executable JAR from the builder stage
COPY --from=builder /build/target/educonnect-backend-*.jar app.jar
RUN chown educonnect:educonnect app.jar

# Switch to unprivileged user
USER educonnect:educonnect

# Expose HTTP port
EXPOSE 8080

# Configure JVM memory limits and production flags
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Application entrypoint
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Multi-stage build for miniEMS Backend
# Stage 1: Build stage with Maven
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Configure Maven mirror for China mainland
COPY maven-settings.xml /root/.m2/settings.xml

# Copy pom.xml first to leverage Docker cache
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application with assembly
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install bash for start script
RUN apk add --no-cache bash

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the assembled application structure
COPY --from=builder /app/target/ems-0.0.1-bin/. .

# Create logs directory
RUN mkdir -p /app/logs && \
    chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8081

# Health check endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Run the application using start script
ENTRYPOINT ["/app/bin/start.sh"]

# ==============================================================================
# Stage 1: Build stage
# ==============================================================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Cache Maven dependencies by copying pom.xml first
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production artifact
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==============================================================================
# Stage 2: Runtime stage
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-privileged application user and group for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create directory for file uploads and assign ownership
RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app

# Copy the built jar from builder stage
COPY --from=builder --chown=appuser:appgroup /build/target/ai-chat-platform-*.jar app.jar

# Switch to non-root user
USER appuser

# Expose default port
EXPOSE 8080

# Configure conservative JVM memory flags tailored for Render Free 512 MiB container
ENV PORT=8080 \
    JAVA_OPTS="-Xms180m -Xmx220m -XX:MetaspaceSize=96m -XX:MaxMetaspaceSize=112m -XX:ReservedCodeCacheSize=48m -XX:MaxDirectMemorySize=32m -Xss512k -XX:+UseSerialGC"

# Run Spring Boot application, dynamically binding to $PORT provided by Render
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${PORT} -Djava.security.egd=file:/dev/./urandom -jar app.jar"]

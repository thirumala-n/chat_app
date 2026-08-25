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

# Configure lightweight JVM memory flags tailored for Render Free 512 MiB container
ENV PORT=8080 \
    JAVA_OPTS="-Xms128m -Xmx160m -XX:MetaspaceSize=96m -XX:MaxMetaspaceSize=128m -XX:ReservedCodeCacheSize=32m -XX:MaxDirectMemorySize=24m -Xss256k -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:CICompilerCount=2 -Dserver.tomcat.threads.max=20 -Dserver.tomcat.threads.min-spare=2 -Dspring.datasource.hikari.maximum-pool-size=3 -Dspring.datasource.hikari.minimum-idle=1"

# Run Spring Boot application, dynamically binding to $PORT provided by Render
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${PORT} -Djava.security.egd=file:/dev/./urandom -jar app.jar"]

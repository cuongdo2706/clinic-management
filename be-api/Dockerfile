FROM eclipse-temurin:26-jdk-alpine AS builder
WORKDIR /app

COPY gradlew .
COPY gradle/ gradle/
RUN chmod +x gradlew

COPY build.gradle.kts .
COPY settings.gradle.kts .
RUN ./gradlew dependencies --no-daemon -q

COPY src/ src/
RUN ./gradlew bootJar --no-daemon -q

RUN java -Djarmode=layertools \
    -jar build/libs/*.jar \
    extract \
    --destination build/extracted

# ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:26-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder --chown=appuser:appgroup /app/build/extracted/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /app/build/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /app/build/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /app/build/extracted/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseContainerSupport", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "org.springframework.boot.loader.launch.JarLauncher"]
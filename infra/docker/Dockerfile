# syntax=docker/dockerfile:1
# HealthHub production image: Spring Boot backend + built Vue SPA in one container.
# Build from the repo root:  docker build -f infra/docker/Dockerfile -t healthhub .

FROM maven:3.9-eclipse-temurin-25 AS backend-build
WORKDIR /src
COPY pom.xml mvnw ./
COPY .mvn .mvn/
COPY backend/ backend/
RUN ./mvnw -q -DskipTests package

FROM node:22-alpine AS frontend-build
WORKDIR /ui
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
# openapi-ts.config.ts reads ../docs/openapi.yaml relative to frontend/
COPY docs/openapi.yaml /docs/openapi.yaml
RUN npm run build

FROM eclipse-temurin:25-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=backend-build /src/backend/app/target/app-*.jar app.jar
COPY --from=frontend-build /ui/dist static
# static locations: built SPA first, then classpath defaults
ENV SPRING_WEB_RESOURCES_STATIC_LOCATIONS="file:/app/static/,classpath:/static/,classpath:/public/"
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -fsS http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]

# ─────────────────────────────────────────────
# Stage 1 : Build with Maven (multi-module)
# ─────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy parent pom and all module poms to leverage Maven layer caching
COPY pom.xml .
COPY config-server/pom.xml        config-server/
COPY discovery-server/pom.xml     discovery-server/
COPY gateway-service/pom.xml      gateway-service/
COPY auth-service/pom.xml         auth-service/
COPY user-service/pom.xml         user-service/
COPY product-service/pom.xml      product-service/
COPY order-service/pom.xml        order-service/
COPY notification-service/pom.xml notification-service/

# Resolve and cache dependencies (without sources)
RUN mvn dependency:go-offline -pl config-server -am -q

# Copier les sources du module ciblé
COPY config-server/src config-server/src

# Build only this module and its dependencies (-am = also make)
RUN mvn clean package -pl config-server -am -DskipTests

# ─────────────────────────────────────────────
# Stage 2 : Production image (lightweight JRE)
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/config-server/target/*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]

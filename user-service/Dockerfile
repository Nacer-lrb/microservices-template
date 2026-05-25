# ─────────────────────────────────────────────
# Stage 1 : Build
# ─────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY config-server/pom.xml        config-server/
COPY discovery-server/pom.xml     discovery-server/
COPY gateway-service/pom.xml      gateway-service/
COPY auth-service/pom.xml         auth-service/
COPY user-service/pom.xml         user-service/
COPY product-service/pom.xml      product-service/
COPY order-service/pom.xml        order-service/
COPY notification-service/pom.xml notification-service/

RUN mvn dependency:go-offline -pl user-service -am -q

COPY user-service/src user-service/src

RUN mvn clean package -pl user-service -am -DskipTests

# ─────────────────────────────────────────────
# Stage 2 : Production image
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/user-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]

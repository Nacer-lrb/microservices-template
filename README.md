# Microservices Template

A professional starter template for building a microservices architecture using Spring Boot 3 and Spring Cloud.

## Current Architecture

This project is divided into several modules (microservices):

*   **infrastructure/**: Contains the `docker-compose` setup providing PostgreSQL (local port 5433), Redis (6379), Kafka (in KRaft mode), and pgAdmin (port 5050).
*   **config-server** (Port `8888`): Centralizes configuration for all microservices (using the local `native` profile).
*   **discovery-server** (Port `8761`): Eureka server for dynamic service registration and discovery.
*   **auth-service** (Port `8081`): Service managing user registration, login, and JWT token issuance (Stateless). It relies on the isolated `auth_db` database.
*   **user-service** (Port `8082`): Manages user business profiles (FirstName, LastName, Email, Phone). Relies on `user_db`.
*   **product-service** (Port `8083`): Manages the product catalog and inventory. Relies on `product_db`.
*   **gateway-service** (Port `8060`): Single entry point (API Gateway) that intercepts incoming requests, verifies JWT tokens, and routes traffic to the appropriate backend services.

## Getting Started (Development Environment)

### 1. Start the Infrastructure
Ensure Docker is running, then spin up the databases, cache, and message broker:
```bash
cd infrastructure
docker compose -f docker-compose.infrastructure.yml up -d
```

### 2. Start the Microservices
Open separate terminals and compile/run the services in this exact order:

1.  **Config Server**:
    ```bash
    cd config-server
    mvn spring-boot:run
    ```
2.  **Discovery Server (Eureka)**:
    ```bash
    cd discovery-server
    mvn spring-boot:run
    ```
3.  **Auth Service**:
    ```bash
    cd auth-service
    mvn spring-boot:run
    ```
4.  **User Service**:
    ```bash
    cd user-service
    mvn spring-boot:run
    ```
5.  **Product Service**:
    ```bash
    cd product-service
    mvn spring-boot:run
    ```
6.  **Gateway Service**:
    ```bash
    cd gateway-service
    mvn spring-boot:run
    ```

## Quick Security Tests

*   **Eureka Dashboard**: `http://localhost:8761`
*   **pgAdmin Web Interface**: `http://localhost:5050` (Login: `admin@admin.com` / `admin`)
*   **Create an account** (via Gateway): 
    `POST http://localhost:8060/auth/register`
*   **Login** (via Gateway): 
    `POST http://localhost:8060/auth/login` (Returns the JWT token in the response).
*   **Protected Access**: Any other route (e.g., `/api/users`, `/api/products`) on port `8060` will require an `Authorization: Bearer <your_token>` Header.

---
* This README will be updated continuously .........................................*

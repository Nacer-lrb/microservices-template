#  Microservices Template — Spring Boot 3 & Spring Cloud

A professional, production-ready microservices template built with **Spring Boot 3.4**, **Spring Cloud**, **Kafka KRaft**, **PostgreSQL**, **Redis**, and **Docker**.

---

##  Architecture Overview

```
Frontend (Next.js :3000)
        │
        ▼
Spring Cloud Gateway (:8060)  ← JWT validation, Rate Limiting (Redis)
        │
 ┌──────┼───────┬────────────┐
 ▼      ▼       ▼            ▼
Auth   User   Product      Order
:8081  :8082  :8083        :8084
                              │  Kafka "order.created"
                              ▼
                       Notification :8085

Infrastructure: Config Server (:8888) + Eureka Discovery (:8761)
```

---
##  Services

| Service               | Port  | Database    | Description                               |
|-----------------------|-------|-------------|-------------------------------------------|
| `config-server`       | 8888  | —           | Centralized configuration (Spring Cloud Config) |
| `discovery-server`    | 8761  | —           | Service registry (Eureka)                 |
| `gateway-service`     | 8060  | —           | Single entry point — JWT validation, rate limiting |
| `auth-service`        | 8081  | `auth_db`   | JWT authentication + Redis session store  |
| `user-service`        | 8082  | `user_db`   | User profile management                   |
| `product-service`     | 8083  | `product_db`| Product catalog CRUD                      |
| `order-service`       | 8084  | `order_db`  | Orders — sync call to Product (Feign) + Kafka event |
| `notification-service`| 8085  | —           | Kafka consumer — simulates email notifications |

---

## 🛠️ Prerequisites

- **Java 17+**
- **Maven 3.9+**
- **Docker & Docker Compose v2**

---

##  Quick Start

### Option A — Infrastructure only (local development)

Starts PostgreSQL, Redis, Kafka, and pgAdmin:

```bash
docker compose -f infrastructure/docker-compose.infrastructure.yml up -d
```

Then run each Spring Boot service locally **in this order**:

```bash
# Terminal 1 — must start first
cd config-server && mvn spring-boot:run

# Terminal 2 — depends on config-server
cd discovery-server && mvn spring-boot:run

# Terminal 3+ — any order, after config + discovery are up
cd auth-service && mvn spring-boot:run
cd gateway-service && mvn spring-boot:run
cd user-service && mvn spring-boot:run
cd product-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
```

### Option B — Full Docker stack (recommended)

```bash
# Build all images and start the full stack
docker compose up --build -d

# Stream logs from all services
docker compose logs -f

# Stop all containers
docker compose down

# Stop and remove all volumes (full reset)
docker compose down -v
```

---

##  Available Interfaces

| Interface      | URL                      | Credentials            |
|----------------|--------------------------|------------------------|
| Eureka UI      | http://localhost:8761    | —                      |
| pgAdmin        | http://localhost:5050    | admin@admin.com / admin |
| API Gateway    | http://localhost:8060    | —                      |
| Config Server  | http://localhost:8888    | —                      |

---

##  API Testing

An `api-tests.http` file is available at the project root, compatible with **IntelliJ IDEA HTTP Client** and **VS Code REST Client**.

```http
### 1. Register a new user
POST http://localhost:8060/api/auth/register

### 2. Login → get JWT token
POST http://localhost:8060/api/auth/login

### 3. Create a product (requires JWT)
POST http://localhost:8060/api/products

### 4. Place an order → triggers Kafka → notification logged
POST http://localhost:8060/api/orders
```

---

##  Project Structure

```
microservices-template/
├── config-server/              # Spring Cloud Config Server
├── discovery-server/           # Eureka Server
├── gateway-service/            # Spring Cloud Gateway + JWT filter
├── auth-service/               # JWT Auth + Redis token store
├── user-service/               # User profile CRUD
├── product-service/            # Product catalog CRUD
├── order-service/              # Orders + Feign client + Kafka producer
├── notification-service/       # Kafka consumer + email simulation
├── infrastructure/
│   ├── docker-compose.infrastructure.yml  # Infra only
│   └── SQL/init.sql            # Database initialization
├── docker-compose.yml          # Full stack (infra + all services)
├── api-tests.http              # HTTP test scripts
└── pom.xml                     # Parent Maven multi-module POM
```

---

## Internal Service Communication

| Type         | Services                         | Technology         |
|--------------|----------------------------------|--------------------|
| Synchronous  | Order-Service → Product-Service  | OpenFeign + Eureka |
| Asynchronous | Order-Service → Notification     | Apache Kafka KRaft |
| Security     | Gateway → all services           | JWT (stateless)    |
| Session store| Auth-Service                     | Redis              |

---

##  Implementation Progress

- [x] Step 1 — Docker infrastructure (Postgres, Redis, Kafka KRaft)
- [x] Step 2 — Config Server + Discovery Server
- [x] Step 3 — Auth-Service (JWT) + Gateway-Service
- [x] Step 4 — User-Service + Product-Service
- [x] Step 5 — Order-Service (Feign sync call + Kafka producer)
- [x] Step 6 — Notification-Service (Kafka consumer)
- [x] Step 7 — Full Dockerization (Dockerfiles + root docker-compose.yml)
- [ ] Step 8 — Next.js Frontend


---

##  License

[MIT](LICENSE)

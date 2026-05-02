# Tool91 Backend Application

## Overview

Tool91 is a Spring Boot backend application for managing organizational policies.
It provides secure REST APIs for authentication and policy retrieval with pagination support.

---

## Architecture Diagram

```text
Client → Controller → Service → Repository → Database
```

The application follows a layered architecture:

- Controller → Handles HTTP requests
- Service → Business logic
- Repository → Database operations
- Database → Stores data (H2)

---

## Prerequisites

Make sure the following are installed:

- Java 17
- Maven
- Git
- Postman

---

## Setup Steps

### 1. Clone the repository

git clone <your-repo-url>

### 2. Navigate to backend folder

cd tool91/backend

### 3. Run the application

./mvnw spring-boot:run

### 4. Verify application

- Application runs on: http://localhost:8080
- Health API:
  GET /api/policies/health

---

## 🔧 .env Reference Table

| Variable       | Description             | Example            |
| -------------- | ----------------------- | ------------------ |
| DB_URL         | Database connection URL | jdbc:h2:mem:testdb |
| DB_USERNAME    | Database username       | sa                 |
| DB_PASSWORD    | Database password       | (empty)            |
| JWT_SECRET     | JWT secret key          | your-secret-key    |
| JWT_EXPIRATION | Token expiry time (ms)  | 3600000            |

---

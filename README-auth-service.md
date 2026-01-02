<<<<<<< HEAD
# Ebanking-3.0
=======
# 🔐 Auth Service – E-Banking Platform

Authentication and authorization microservice for an E-Banking platform, built with **Spring Boot 3**, **Keycloak**, **JWT**, **Rate Limiting**, and **Docker**.

---

## 📌 Overview

This service is responsible for:
- User authentication (login / refresh / logout)
- JWT validation and role extraction
- Protection against brute-force attacks (rate limiting)
- Integration with Keycloak as an OAuth2 Identity Provider
- Secure, stateless authentication

The service is designed following **microservices**, **security**, and **DevOps best practices**.

---

## 🧱 Architecture

Client
↓
RateLimitFilter (Bucket4j)
↓
Spring Security Filter Chain
↓
Auth Controller
↓
Keycloak (OAuth2 / OpenID Connect)


---

## ⚙️ Technologies Used

- **Java 17**
- **Spring Boot 3**
- **Spring Security (OAuth2 Resource Server)**
- **Keycloak**
- **JWT**
- **Bucket4j (Rate Limiting)**
- **Resilience4j (Circuit Breaker)**
- **PostgreSQL**
- **Docker & Docker Compose**
- **Maven**

---

## 🔑 Authentication Flow

### 1️⃣ Login
- Endpoint: `POST /auth/login`
- Credentials are sent to Keycloak
- Access Token + Refresh Token returned

### 2️⃣ Access Protected APIs
- JWT sent via `Authorization: Bearer <token>`
- Token validated locally (stateless)

### 3️⃣ Refresh Token
- Endpoint: `POST /auth/refresh`
- New tokens issued by Keycloak

### 4️⃣ Logout
- Endpoint: `POST /auth/logout`
- Refresh token invalidated in Keycloak

---

## 🛡️ Security Features

### ✅ OAuth2 / JWT
- Stateless authentication
- Role-based access control (USER / ADMIN)

### ✅ Rate Limiting
Implemented using **Bucket4j (in-memory)**:
- **Login**: 5 attempts / minute / IP + username
- **Refresh token**: 10 requests / minute / IP

This protects the service against:
- Brute-force attacks
- Token abuse
- Keycloak overload

> ℹ️ Redis-based rate limiting can be added later for distributed environments.

---

## 🐳 Docker Setup

### ▶️ Start the full environment

```bash
docker-compose up --build



## 🔑 Retrieving Keycloak Client Secret

The auth-service uses a confidential Keycloak client.
The client secret is NOT stored in the repository.

A PowerShell script is provided to retrieve it automatically.

### Usage

```powershell
.\keycloak\get-client-secret.ps1
>>>>>>> 092573c (feat(auth-service): initial auth service with keycloak, rate limiting and docker setup)

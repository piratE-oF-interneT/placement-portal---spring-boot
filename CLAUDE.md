# Placement Portal

## Project Overview
University placement management system in Spring Boot.
Multi-tenant system supporting multiple colleges. Manages student profiles,
company placement drives, applications, evaluation rounds, offers, and analytics.

## Tech Stack
- Java 21, Spring Boot 4.x
- Spring Security with JWT authentication
- PostgreSQL (production), H2 (dev/test)
- Maven
- JUnit 5 + Mockito for unit tests
- Lombok for boilerplate reduction

## Build & Run
- `./mvnw clean install` — build and run all tests
- `./mvnw spring-boot:run` — run locally with H2 on port 8080
- `./mvnw test` — run tests only
- `./mvnw verify` — full verification including integration tests

## Project Structure

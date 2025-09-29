# xtremend-config

## Purpose
Centralized configuration module used by all other services.
It now includes the global Swagger/OpenAPI setup so every module
automatically exposes its API documentation.

## Tech Used
- Spring Boot 3.x
- Java 21

## AI Agent Guidelines
- Depend only on `xtremend-common` and `xtremend-shared-services`.
- Avoid horizontal dependencies with other feature modules.

## Allowed Dependencies
- `spring-boot-starter`
- `springdoc-openapi-starter-webmvc-ui`

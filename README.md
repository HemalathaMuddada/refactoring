# xtremend-backend

This project provides the monolithic codebase for the Xtremend platform. It is organised as a multi-module Maven build to keep responsibilities separated and prepare each module for future extraction as an independent microservice.

## Modules
- xtremend-common
- xtremend-shared-services
- xtremend-auth
- xtremend-user
- xtremend-contact
- xtremend-prospecting
- xtremend-email-verification
- xtremend-email-sequence
- xtremend-ai
- xtremend-analytics
- xtremend-integrations-crm
- xtremend-integrations-provider
- xtremend-communication
- xtremend-scraper
- xtremend-extension
- xtremend-config
- xtremend-api-gateway

## Dependency Guidelines
The allowed dependency flow is strictly vertical:

```
xtremend-common -> xtremend-shared-services -> feature modules -> xtremend-api-gateway
```

- `xtremend-common` holds only DTOs, enums, utils and constants and has **no** dependencies on other project modules.
- `xtremend-shared-services` provides shared infrastructure services and depends only on `xtremend-common`.
- Feature modules (`xtremend-auth`, `xtremend-user`, etc.) may depend on `xtremend-common` and `xtremend-shared-services` but **not** on each other.
- `xtremend-api-gateway` depends on any feature modules required to expose HTTP APIs.
- Circular or lateral dependencies are not permitted.

## Tech Stack
- Java 21
- Spring Boot 3.x
- Spring Security + OAuth2
- Redis (Spring Data Redis)
- PostgreSQL
- Swagger with Springdoc OpenAPI
- Kafka/RabbitMQ placeholders
- Optional Spring Cloud Gateway


## Swagger URLs
- https://{Environment-URL}/xtremand/swagger-ui/index.html
- https://{Environment-URL}/xtremand/v3/api-docs
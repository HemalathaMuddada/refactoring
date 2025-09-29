# Project Overview

Xtremand is a Spring Boot 3 platform built with Java 21 and PostgreSQL. The codebase is organised as a multi-module Maven project to keep features isolated and scalable.

Key modules include:

- **xtremand-common** – common DTOs, enums and utilities.
- **xtremand-shared-services** – shared infrastructure services built on top of `xtremand-common`.
- **feature modules** – domain features such as `xtremand-auth`, `xtremand-user`, `xtremand-contact`, `xtremand-prospecting`, `xtremand-email-verification`, `xtremand-email-sequence`, `xtremand-ai`, `xtremand-analytics`, `xtremand-integrations-crm`, `xtremand-integrations-provider`, `xtremand-communication`, `xtremand-scraper`, and `xtremand-extension`.
- **xtremand-config** – centralised configuration utilities.
- **xtremand-api-gateway** – optional Spring Cloud Gateway front end.
- **xtremand-app** – main Spring Boot application that exposes the REST API.
- **xtremand-domain** – JPA entities and repositories shared across modules.

All modules adhere to the dependency flow defined in the project root README:

```
extremand-common -> xtremand-shared-services -> feature modules -> xtremand-api-gateway
```

This ensures a clean, vertical dependency structure with no lateral coupling.

## Swagger URLs
- https://{Environment-URL}/xtremand/swagger-ui/index.html
- https://{Environment-URL}/xtremand/v3/api-docs
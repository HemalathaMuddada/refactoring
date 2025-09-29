# xtremend-auth

## Purpose
Provides authentication and authorization services for the platform.

## Tech Used
- Spring Boot 3.x
- Spring Security & OAuth2

## AI Agent Guidelines
- Depend only on `xtremend-common` and `xtremend-shared-services`.
- Do not reference other feature modules.
- Keep security configuration and user auth logic isolated.

## Allowed Dependencies
- `spring-boot-starter-security`
- `spring-boot-starter-validation`

# Modules Breakdown

Below is a high level summary of each Maven module. All modules follow the dependency chain `xtremand-common -> xtremand-shared-services -> feature modules -> xtremand-api-gateway`.

## xtremand-common
Holds DTOs, enums and helper utilities. Has no dependencies on other project modules.

## xtremand-shared-services
Provides shared infrastructure services (email, persistence helpers, etc.) and depends only on `xtremand-common`.

## Feature Modules
Modules implementing platform functionality. Each depends on `xtremand-common` and `xtremand-shared-services` but not on one another. Current feature modules include:
- `xtremand-auth`
- `xtremand-user`
- `xtremand-contact`
- `xtremand-prospecting`
- `xtremand-email-verification`
- `xtremand-email-sequence`
- `xtremand-ai`
- `xtremand-analytics`
- `xtremand-integrations-crm`
- `xtremand-integrations-provider`
- `xtremand-communication`
- `xtremand-scraper`
- `xtremand-extension`

## xtremand-config
Central configuration utilities used across the project.

## xtremand-api-gateway
Optional gateway layer that aggregates the feature modules' APIs.

## xtremand-domain
Contains JPA entities and repositories shared by all modules.

## xtremand-app
Main Spring Boot application that exposes REST endpoints. Depends on `xtremand-domain` and the relevant feature modules.

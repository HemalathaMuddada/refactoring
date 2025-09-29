# Environments

Xtremand supports three primary environments: `dev`, `staging`, and
`prod`.

## Setup
- Configuration files follow the pattern `application-<env>.yml`.
- Secrets are stored using environment variables or external secret
  stores.
- Logging levels vary per environment; debug logs are enabled only in
  `dev`.

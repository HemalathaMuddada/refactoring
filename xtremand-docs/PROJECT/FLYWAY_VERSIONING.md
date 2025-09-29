# Flyway Versioning

Flyway manages all database schema changes within the `xtremand-app`
module. Each migration script is immutable once committed.

## Strategy
- Initial schema is defined in `V1__init.sql`.
- Subsequent changes increment the version number (`V2__`, `V3__`, etc.).
- Never edit existing scripts; create a new version for every change.

## Seed Data
The init script inserts the default roles:
`SUPER_ADMIN`, `ADMIN`, and `TEAM_MEMBER`.

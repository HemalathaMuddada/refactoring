# Database Schema

All relational tables use the prefix `xt_` to keep the schema
organized. Common auditing fields are present on every entity:
`created_at`, `updated_at`, `created_by`, and `updated_by`.

## Constraints and Indexes
- Important fields such as `email` are marked `UNIQUE`.
- Frequently queried fields include indexes for performance.

## Example Entities
### User
- `id`, `full_name`, `email`, `password`, `role_id`
- Indexed on `email`, `created_at`, and `updated_at`

### Role
- `id`, `name`
- Pre-populated with `SUPER_ADMIN`, `ADMIN`, and `TEAM_MEMBER`

# Agent Guidelines

This document defines standard rules that all Xtremand agents must follow.

## Metadata Format
- Provide a clear JSON metadata block describing the job parameters.
- Include timestamps and a unique correlation id.

## Error Handling
- Use structured logging for errors and warnings.
- Retry transient failures with exponential backoff.

## Logging
- Log significant state changes with context information.
- Avoid logging sensitive data.

## DTO Usage
- Exchange data through immutable Data Transfer Objects.
- Validate all incoming DTOs before processing.

## Modular Constraints
- Agents may depend on `xtremand-common` and `xtremand-shared-services`.
- Avoid direct coupling with other feature modules unless absolutely required.
- Respect the global dependency order:

```
extremand-common -> xtremand-shared-services -> feature modules -> xtremand-api-gateway
```

Violating this order results in brittle builds and is not permitted.

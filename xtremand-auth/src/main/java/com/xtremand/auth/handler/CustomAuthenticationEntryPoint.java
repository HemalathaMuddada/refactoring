package com.xtremand.auth.handler;

import java.io.IOException;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtremand.common.constants.ErrorCodes;
import com.xtremand.common.dto.ApiErrorResponse;
import com.xtremand.common.dto.Rfc7807ErrorResponse;
import com.xtremand.common.environment.EnvironmentUtil;
import com.xtremand.common.error.ErrorCodeRegistry;
import com.xtremand.common.error.ErrorDefinition;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Authentication entry point that returns structured RFC 7807 errors.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final ErrorCodeRegistry registry;
    private final boolean useRfc7807;
    private final EnvironmentUtil environmentUtil;

    private static final String X_REQUEST_ID = "X-Request-ID";
    private static final String UNKNOWN = "unknown";

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper,
                                          ErrorCodeRegistry registry,
                                          @Value("${xamplify.api.use-rfc7807:false}") boolean useRfc7807,
                                          EnvironmentUtil environmentUtil) {
        this.objectMapper = objectMapper;
        this.registry = registry;
        this.useRfc7807 = useRfc7807;
        this.environmentUtil = environmentUtil;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String rawPath = request.getRequestURI();
        String decodedPath = URLDecoder.decode(rawPath, StandardCharsets.UTF_8.name());
        String normalizedPath = decodedPath.replaceAll("\\p{Pd}", "-");

        String errorCode;
        String message;
        if (authException instanceof BadCredentialsException
                || authException instanceof UsernameNotFoundException) {
            errorCode = ErrorCodes.INVALID_CREDENTIALS;
            message = "Invalid username or password";
        } else {
            errorCode = ErrorCodes.INVALID_TOKEN;
            message = "Access token is invalid or expired";
        }

        Map<String, Object> detailMap = new LinkedHashMap<>();
        detailMap.put("type", authException.getClass().getSimpleName());
        detailMap.put("message", message);

        String requestId = Optional.ofNullable(request.getHeader(X_REQUEST_ID)).orElse(UNKNOWN);
        String traceId = Optional.ofNullable(MDC.get("traceId")).orElse(UNKNOWN);

        ErrorDefinition def = registry.get(errorCode);
        int codeId = def.codeId();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        if (useRfc7807) {
            String slug = slugify(def.title());
            String typeUri = "https://xamplify.com/errors/" + slug;

            Rfc7807ErrorResponse.Rfc7807ErrorResponseBuilder builder = Rfc7807ErrorResponse.builder()
                    .type(typeUri)
                    .title(def.title())
                    .status(HttpServletResponse.SC_UNAUTHORIZED)
                    .code(errorCode)
                    .codeId(codeId)
                    .detail(message)
                    .instance(normalizedPath)
                    .requestId(requestId)
                    .traceId(traceId)
                    .severity(def.severity())
                    .category(def.category())
                    .recoverable(def.recoverable());

            if (!environmentUtil.isProd()) {
                builder.debug(Map.of("exceptionType", authException.getClass().getName()));
            }

            response.setContentType("application/problem+json");
            objectMapper.writeValue(response.getOutputStream(), builder.build());
            return;
        }

        ApiErrorResponse.ApiErrorResponseBuilder apiBuilder = ApiErrorResponse.builder()
                .timestamp(Instant.now().toString())
                .status(HttpServletResponse.SC_UNAUTHORIZED)
                .errorCode(errorCode)
                .codeId(codeId)
                .message(message)
                .severity(def.severity())
                .category(def.category())
                .recoverable(def.recoverable())
                .details(List.of(detailMap))
                .path(normalizedPath)
                .requestId(requestId);

        if (!environmentUtil.isProd()) {
            apiBuilder.debug(Map.of("exceptionType", authException.getClass().getName()));
        }

        ApiErrorResponse apiError = apiBuilder.build();

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), apiError);
    }

    private String slugify(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}

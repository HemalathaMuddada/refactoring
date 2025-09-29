package com.xtremand.auth.handler;

import java.io.IOException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtremand.common.constants.ErrorCodes;
import com.xtremand.common.dto.ApiErrorResponse;
import com.xtremand.common.environment.EnvironmentUtil;
import com.xtremand.common.error.ErrorCodeRegistry;
import com.xtremand.common.error.ErrorDefinition;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

        private final ErrorCodeRegistry registry;
        private final EnvironmentUtil environmentUtil;

        public CustomAccessDeniedHandler(ErrorCodeRegistry registry, EnvironmentUtil environmentUtil) {
                this.registry = registry;
                this.environmentUtil = environmentUtil;
        }

	private static final String X_REQUEST_ID = "X-Request-ID";
	private static final String UNKNOWN = "unknown";
        private static final String ERROR_CODE = ErrorCodes.ACCESS_DENIED;

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {

		String requestId = Optional.ofNullable(request.getHeader(X_REQUEST_ID)).orElse(UNKNOWN);

		// Build a single detail map with exception info
		Map<String, Object> detailMap = new LinkedHashMap<>();
		detailMap.put("type", accessDeniedException.getClass().getSimpleName());
		detailMap.put("message", "Access denied");

        ErrorDefinition def = registry.get(ERROR_CODE);
        int codeId = def.codeId();
        ApiErrorResponse.ApiErrorResponseBuilder apiBuilder = ApiErrorResponse.builder()
                                .timestamp(Instant.now().toString())
                                .status(HttpServletResponse.SC_FORBIDDEN)
                                .errorCode(ERROR_CODE)
                                .codeId(codeId)
                                .message("You do not have permission to access this resource")
                                .severity(def.severity())
                                .details(List.of(detailMap))
                                .path(request.getRequestURI())
                                .requestId(requestId);

        if (!environmentUtil.isProd()) {
                apiBuilder.debug(Map.of("exceptionType", accessDeniedException.getClass().getName()));
        }

        ApiErrorResponse apiError = apiBuilder.build();

		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		new ObjectMapper().writeValue(response.getWriter(), apiError);
	}
}

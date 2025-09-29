package com.xtremand.common.error;

import java.io.InputStream;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.xtremand.common.constants.ErrorCodes;

/**
 * Loads error code definitions from {@code error-codes.yaml} and provides lookup by code.
 */
@Component
public class ErrorCodeRegistry {

    private final Map<String, ErrorDefinition> codeMap;

    public ErrorCodeRegistry(@Value("classpath*:error-codes.yaml") Resource[] yamls) {
        Map<String, ErrorDefinition> map = new HashMap<>();
        for (Resource yaml : yamls) {
            map.putAll(loadYaml(yaml));
        }
        this.codeMap = map;
    }

    public ErrorDefinition get(String code) {
        return codeMap.getOrDefault(code,
                new ErrorDefinition(ErrorCodes.UNKNOWN, 9999,
                        "Unknown Error", "Unexpected error",
                        "UNKNOWN", "ERROR", false));
    }

    public Collection<ErrorDefinition> getAll() {
        return codeMap.values();
    }

    public Set<String> getAllErrorCodes() {
        return codeMap.keySet();
    }

    private Map<String, ErrorDefinition> loadYaml(Resource yaml) {
        try (InputStream in = yaml.getInputStream()) {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            Map<String, Map<String, Map<String, Object>>> root = mapper.readValue(in,
                    new TypeReference<>() {});
            Map<String, Map<String, Object>> codes = root.get("error-codes");
            Map<String, ErrorDefinition> map = new HashMap<>();
            if (codes != null) {
                for (Map.Entry<String, Map<String, Object>> e : codes.entrySet()) {
                    String code = e.getKey();
                    Map<String, Object> data = e.getValue();
                    int codeId = ((Number) data.get("codeId")).intValue();
                    String title = String.valueOf(data.get("title"));
                    String message = String.valueOf(data.get("message"));
                    String category = String.valueOf(data.getOrDefault("category", "UNKNOWN"));
                    String severity = String.valueOf(data.getOrDefault("severity", "ERROR"));
                    boolean recoverable = Boolean.parseBoolean(String.valueOf(data.getOrDefault("recoverable", "false")));
                    map.put(code, new ErrorDefinition(code, codeId, title, message, category, severity, recoverable));
                }
            }
            return map;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load error codes", e);
        }
    }
}

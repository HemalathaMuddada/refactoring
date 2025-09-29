package com.xtremand.user.dto;

import java.time.LocalDateTime;

import com.xtremand.domain.enums.AiConfigType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiConfigDto {
    private Long id;
    private String email;
    private String displayName;
    private AiConfigType configType;
    private String apiKey;
    private String apiSecret;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String token;
    private boolean isDefault;

}


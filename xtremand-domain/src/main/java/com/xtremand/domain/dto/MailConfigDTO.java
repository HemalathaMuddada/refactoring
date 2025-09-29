package com.xtremand.domain.dto;

import java.time.LocalDateTime;

import com.xtremand.domain.enums.EmailConfigType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailConfigDTO {
    private Long id;
    private EmailConfigType configType;
    private String email;
    private String displayName;
    private String username;
    private String password;
    private String oauthAccessToken;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String host;
    private String port;
    private String oauthRefreshToken;
    private LocalDateTime tokenExpiry;

}

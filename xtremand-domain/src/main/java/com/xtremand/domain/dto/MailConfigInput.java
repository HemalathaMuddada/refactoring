package com.xtremand.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.xtremand.domain.entity.User;
import com.xtremand.domain.enums.EmailConfigType;

import lombok.Data;

@Data
public class MailConfigInput {
    private EmailConfigType configType;   
    private String smtpHost;
    private Integer smtpPort;
    private String email;
    private String username;
    private String password;
    private String oauthToken;
    private String oauthRefreshToken;
    private LocalDateTime tokenExpiry;
    private String displayName;
    private User createdBy;
    private List<String> importantEmail;

}


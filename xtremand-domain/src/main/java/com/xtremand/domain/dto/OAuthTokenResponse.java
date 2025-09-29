package com.xtremand.domain.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//OAuth token response DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuthTokenResponse {
	
 @JsonProperty("access_token")
 private String accessToken;

 @JsonProperty("refresh_token")
 private String refreshToken;

 @JsonProperty("expires_in")
 private Integer expiresIn;

 @JsonProperty("scope")
 private String scope;

 @JsonProperty("token_type")
 private String tokenType;

 public LocalDateTime getExpiryTime() {
     return LocalDateTime.now().plusSeconds(expiresIn != null ? expiresIn : 3600);
 }
}


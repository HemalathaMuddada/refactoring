package com.xtremand.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleUserInfo {
	
    private String id;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("verified_email")
    private Boolean verifiedEmail;
    
    private String name;
    
    private String given_name;
    
    private String family_name;
    
    private String picture;
    
    private String locale;
}
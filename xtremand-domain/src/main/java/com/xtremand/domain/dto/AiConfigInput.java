package com.xtremand.domain.dto;

import lombok.Data;

@Data
public class AiConfigInput {

	 private String email;
	    private String configType; 
	    private String apiKey;
	    private String apiSecret;
	    private String displayName;

	
}

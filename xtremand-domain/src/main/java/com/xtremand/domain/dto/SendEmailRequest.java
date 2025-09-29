package com.xtremand.domain.dto;

import java.util.List;
import java.util.Map;

import com.xtremand.domain.enums.EmailConfigType;

import lombok.Data;


@Data
public class SendEmailRequest {
	    private RecipientGroup to;
	    private RecipientGroup cc;
	    private RecipientGroup bcc;
	    private String subject;
	    private String body;
	    private String prompt; 
	    private List<Long> contactIds;
	    private Long userId;
	    private Long configId;
	    }


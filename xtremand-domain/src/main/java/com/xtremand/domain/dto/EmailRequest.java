package com.xtremand.domain.dto;

import com.xtremand.domain.enums.AiConfigType;
import com.xtremand.domain.enums.CampaignType;
import com.xtremand.domain.enums.EmailCampaignType;
import com.xtremand.domain.enums.EmailCategory;
import com.xtremand.domain.enums.Tone;
import lombok.Data;

@Data
public class EmailRequest {
    private EmailCampaignType campaignType;
    private Tone tone;
    private String industry;
    private String senderName;
    private String senderEmail;
    private String emailPrompt;
    private String templateName;
    private EmailCategory category; 
    private String subjectLine;
    private AiConfigType aiConfigType;
    private String aiEmail;
}

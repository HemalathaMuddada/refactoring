package com.xtremand.domain.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignDto {
  
	private Long id;
    private String name;
    private String type;         
    private String contentStrategy;
    private LocalDateTime scheduledAt;
    private boolean aiPersonalization;
    private LocalDate createdAt;
    private boolean sent;       
    private Long contactListId;  
    private Long emailTemplateId;
}

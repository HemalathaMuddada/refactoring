package com.xtremand.domain.dto;

import com.xtremand.domain.enums.EmailStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class EmailHistoryDto {
	private Long id;
    private EmailStatus status;
    private String subject;
    private String body;
    private LocalDateTime timestamp; 
}

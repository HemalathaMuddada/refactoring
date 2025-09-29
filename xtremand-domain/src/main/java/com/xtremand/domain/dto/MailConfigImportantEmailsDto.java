package com.xtremand.domain.dto;

import java.util.List;

import lombok.Data;
@Data
public class MailConfigImportantEmailsDto {
    private Long mailConfigId;
    private List<String> importantEmails;

    public MailConfigImportantEmailsDto(Long mailConfigId, List<String> importantEmails) {
        this.mailConfigId = mailConfigId;
        this.importantEmails = importantEmails;
    }

}


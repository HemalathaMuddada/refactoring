package com.xtremand.domain.dto;

import java.time.LocalDate;

import com.xtremand.domain.enums.EmailCategory;

import lombok.Data;
@Data
public class EmailTemplateDTO {
    private Long id;
    private String name;
    private EmailCategory category;
    private String subjectLine;
    private String content;
    private String variables;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private Long createdByUserId;
    private Long updatedByUserId;

}


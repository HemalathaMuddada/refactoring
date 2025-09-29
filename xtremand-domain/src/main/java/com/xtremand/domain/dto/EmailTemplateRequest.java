package com.xtremand.domain.dto;

import com.xtremand.domain.enums.EmailCategory;

import lombok.Data;

@Data
public class EmailTemplateRequest {
    private String name;
    private EmailCategory category;
    private String subjectLine;
    private String content;
    private String variables; 

}


package com.xtremand.domain.dto;

import lombok.Data;

@Data
public class AssistantRequest {
    private String name;
    private String vector_store_id;
    private String instructions;
}

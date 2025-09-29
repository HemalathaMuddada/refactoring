package com.xtremand.domain.dto;

import lombok.Data;

@Data
public class RAGRegisterRequest {
    private String username;
    private String email;
    private String password;
    private String tenant_name;
    private String collection_name;
}

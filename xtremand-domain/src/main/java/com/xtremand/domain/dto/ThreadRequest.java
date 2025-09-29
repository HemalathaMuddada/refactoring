package com.xtremand.domain.dto;

import lombok.Data;

@Data
public class ThreadRequest {
    private String vectorStoreId;
    private String threadId;
    private String assisstantId;
}

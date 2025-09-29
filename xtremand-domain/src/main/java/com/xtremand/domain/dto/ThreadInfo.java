package com.xtremand.domain.dto;

import lombok.Data;

@Data
public class ThreadInfo {
    private String id;
    private String created_at;
    private String vector_store_id;
    private String vector_store_id_read;
    private String title;
}

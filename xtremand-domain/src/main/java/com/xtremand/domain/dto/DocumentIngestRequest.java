package com.xtremand.domain.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class DocumentIngestRequest {
    private MultipartFile file;
    private String vector_store_id;
}

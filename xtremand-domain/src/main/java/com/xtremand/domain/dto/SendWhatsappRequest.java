package com.xtremand.domain.dto;

import java.util.List;

import lombok.Data;

@Data
public class SendWhatsappRequest {

    private String body;
    private List<Long> contactIds;

}
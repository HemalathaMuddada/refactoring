package com.xtremand.domain.dto;

import lombok.Data;

@Data
public class InputRequest {
	private DocumentIngestRequest file;
	private MessageRequest messageRequest;
	private String assisstantId;
}

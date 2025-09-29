package com.xtremand.domain.dto;

import java.util.List;

import lombok.Data;

@Data
public class AddContactsToListDto {
	
    private List<Long> contactIds;
}

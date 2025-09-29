package com.xtremand.domain.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
	public class RecipientGroup {
	    private List<Long> contactIds;
	    private List<String> emails;
	    private Map<Long, List<Long>> contactListIdToContactIds; // Map contactListId -> list of contactIds
	}

	

package com.xtremand.domain.enums;

public enum EmailCampaignType {
    COLD_OUTREACH("COLD_OUTREACH"),
    FOLLOW_UP("FOLLOW_UP"),
    INTRODUCTION("INTRODUCTION"),
    NETWORKING("NETWORKING"),
    SALES_PITCH("SALES_PITCH"),
    PARTNERSHIP("PARTNERSHIP");
    
	private final String value;

	EmailCampaignType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static boolean contains(String category) {
		for (EmailCampaignType c : values()) {
			if (c.getValue().equalsIgnoreCase(category))
				return true;
		}
		return false;
	}
}

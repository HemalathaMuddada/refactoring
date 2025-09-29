package com.xtremand.domain.enums;

public enum CampaignType {
	EMAIL_ONLY("EMAIL_ONLY"), WHATSAPP_ONLY("WHATSAPP_ONLY"), EMAIL_AND_WHATSAPP("EMAIL_AND_WHATSAPP");

	private final String type;

	CampaignType(String type) {
		this.type = type;
	}
	   public String getType() {
	        return type;
	    }
	public static CampaignType fromValue(String value) {
		if (value == null) {
			return null;
		}
		for (CampaignType type : CampaignType.values()) {
			if (type.type.equals(value)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown CampaignType: " + value);
	}
}

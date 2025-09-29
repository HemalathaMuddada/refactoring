package com.xtremand.domain.enums;

public enum EmailCategory {
	WELCOME("WELCOME"), FOLLOW_UP("FOLLOW_UP"), PROMOTION("PROMOTION"), NEWSLETTER("NEWSLETTER"),
	COLD_OUTREACH("COLD_OUTREACH"), NURTURE("NURTURE"), GENERAL("GENERAL");

	private final String value;

	private String type;

	public String getType() {
		return type;
	}

	EmailCategory(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static boolean contains(String category) {
		for (EmailCategory c : values()) {
			if (c.getValue().equalsIgnoreCase(category))
				return true;
		}
		return false;
	}

	public static EmailCategory fromValue(String givenName) {
		for (EmailCategory status : values()) {
			if (status.type.equals(givenName)) {
				return status;
			}
		}
		return null;
	}
}

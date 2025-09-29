package com.xtremand.domain.enums;

public enum Tone {
	PROFESSIONAL("PROFESSIONAL"), CASUAL("CASUAL"), FRIENDLY("FRIENDLY"), FORMAL("FORMAL"),
	ENTHUSIASTIC("ENTHUSIASTIC");

	private final String value;

	Tone(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static boolean contains(String category) {
		for (Tone c : values()) {
			if (c.getValue().equalsIgnoreCase(category))
				return true;
		}
		return false;
	}
}
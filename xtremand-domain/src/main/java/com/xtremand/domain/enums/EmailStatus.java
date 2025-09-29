package com.xtremand.domain.enums;

public enum EmailStatus {
	SENT("SENT"), NOT_SENT("NOT_SENT"), OPENED("OPENED"), CLICKED("CLICKED"), BOUNCED("BOUNCED"), REPLIED("REPLIED");

	private final String value;
	
	private String type;
	
	public String getType() {
		return type;
	}
	

	EmailStatus(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static boolean contains(String category) {
		for (EmailStatus c : values()) {
			if (c.getValue().equalsIgnoreCase(category))
				return true;
		}
		return false;
	}
	
	public static EmailStatus fromValue(String givenName) {
	    for (EmailStatus status : values()) {
	    	if (status.value.equalsIgnoreCase(givenName)) {
	            return status;
	        }
	    }
	    return null;
	}
}

package com.xtremand.domain.enums;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

	@Converter(autoApply = true)
	public class EmailStatusType implements AttributeConverter<EmailStatus, String> {

	    @Override
	    public String convertToDatabaseColumn(EmailStatus attribute) {
	        if (attribute == null) {
	            return null;
	        }
	        return attribute.getType();
	    }

	    @Override
	    public EmailStatus convertToEntityAttribute(String dbData) {
	        if (dbData == null) {
	            return null;
	        }
	        try {
	            return EmailStatus.fromValue(dbData);
	        } catch (IllegalArgumentException e) {
	            return null; 
	        }
	    }
	}
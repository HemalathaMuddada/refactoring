package com.xtremand.domain.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EmailCategoryType implements AttributeConverter<EmailCategory, String> {
    @Override
    public String convertToDatabaseColumn(EmailCategory attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public EmailCategory convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return EmailCategory.valueOf(dbData.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Unknown category value: " + dbData, e);
        }
    }
}

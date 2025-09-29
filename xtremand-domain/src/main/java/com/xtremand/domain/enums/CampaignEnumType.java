package com.xtremand.domain.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CampaignEnumType implements AttributeConverter<CampaignType, String> {

    @Override
    public String convertToDatabaseColumn(CampaignType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getType();
    }

    @Override
    public CampaignType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return CampaignType.fromValue(dbData);
        } catch (IllegalArgumentException e) {
            return null; 
        }
    }
}
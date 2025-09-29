package com.xtremand.domain.dto;

import com.xtremand.domain.entity.AiConfig;

import lombok.Data;
@Data
public class AiConfigWithDefaultFlag {
    private AiConfig aiConfig;
    private boolean isDefault;

    public AiConfigWithDefaultFlag(AiConfig aiConfig, boolean isDefault) {
        this.aiConfig = aiConfig;
        this.isDefault = isDefault;
    }

    
}


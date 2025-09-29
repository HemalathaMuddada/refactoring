package com.xtremand.config;

import com.xtremand.config.repository.IntegratedAppKeyRepository;
import com.xtremand.domain.entity.IntegratedAppKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IntegratedAppKeyService {

    private final IntegratedAppKeyRepository integratedAppKeyRepository;

    @Autowired
    public IntegratedAppKeyService(IntegratedAppKeyRepository integratedAppKeyRepository) {
        this.integratedAppKeyRepository = integratedAppKeyRepository;
    }

    @Cacheable(value = "integratedAppKeys", key = "#key")
    public String getUrl(String key) {
        Optional<IntegratedAppKey> appKey = integratedAppKeyRepository.findByKey(key);
        return appKey.map(IntegratedAppKey::getValue).orElse(null);
    }
}
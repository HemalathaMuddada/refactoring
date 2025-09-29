package com.xtremand.config.repository;

import com.xtremand.domain.entity.IntegratedAppKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IntegratedAppKeyRepository extends JpaRepository<IntegratedAppKey, Long> {
    Optional<IntegratedAppKey> findByKey(String key);
}
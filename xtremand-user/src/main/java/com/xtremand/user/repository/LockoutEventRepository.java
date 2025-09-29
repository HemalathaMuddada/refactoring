package com.xtremand.user.repository;

import com.xtremand.domain.entity.LockoutEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LockoutEventRepository extends JpaRepository<LockoutEvent, Long> {
}

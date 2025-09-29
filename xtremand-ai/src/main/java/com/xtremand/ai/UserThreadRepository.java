package com.xtremand.ai;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xtremand.domain.entity.UserThread;

@Repository
public interface UserThreadRepository extends JpaRepository<UserThread, Long> {
    Optional<UserThread> findByUserEmail(String email);
}

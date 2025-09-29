package com.xtremand.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xtremand.domain.entity.User;
import com.xtremand.domain.entity.UserActivationHistory;
import com.xtremand.domain.enums.TokenStatus;

@Repository
public interface UserActivationHistoryRepository extends JpaRepository<UserActivationHistory, Long> {

    Optional<UserActivationHistory> findByActivationToken(String activationToken);

    List<UserActivationHistory> findByUserAndStatus(User user, TokenStatus status);
}

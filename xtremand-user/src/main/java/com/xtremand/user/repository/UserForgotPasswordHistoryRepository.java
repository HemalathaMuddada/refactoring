package com.xtremand.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xtremand.domain.entity.User;
import com.xtremand.domain.entity.UserForgotPasswordHistory;
import com.xtremand.domain.enums.TokenStatus;

@Repository
public interface UserForgotPasswordHistoryRepository extends JpaRepository<UserForgotPasswordHistory, Long> {

    Optional<UserForgotPasswordHistory> findByResetToken(String resetToken);

    List<UserForgotPasswordHistory> findByUserAndStatus(User user, TokenStatus status);
}

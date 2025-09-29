package com.xtremand.auth.forgotpassword.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.auth.forgotpassword.dto.ForgotPasswordRequest;
import com.xtremand.auth.forgotpassword.dto.ResetPasswordRequest;
import com.xtremand.auth.forgotpassword.exception.InvalidResetTokenException;
import com.xtremand.auth.login.exception.AccountDeletedException;
import com.xtremand.auth.login.exception.AccountSuspendedException;
import com.xtremand.auth.login.exception.AccountUnapprovedException;
import com.xtremand.common.util.AESUtil;
import com.xtremand.domain.entity.User;
import com.xtremand.domain.entity.UserForgotPasswordHistory;
import com.xtremand.domain.enums.TokenStatus;
import com.xtremand.domain.enums.UserStatus;
import com.xtremand.user.repository.UserForgotPasswordHistoryRepository;
import com.xtremand.user.repository.UserRepository;

@Service
//@Transactional
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    private final UserRepository userRepository;
    private final UserForgotPasswordHistoryRepository forgotPasswordHistoryRepository;
    private final ForgotPasswordEmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${xtremand.security.reset-password.token-validity-hours}")
    private int tokenValidityHours;

    public ForgotPasswordServiceImpl(UserRepository userRepository,
                                     UserForgotPasswordHistoryRepository forgotPasswordHistoryRepository,
                                     ForgotPasswordEmailService emailService,
                                     PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.forgotPasswordHistoryRepository = forgotPasswordHistoryRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            // Invalidate all previous pending tokens for this user
            forgotPasswordHistoryRepository.findByUserAndStatus(user, TokenStatus.PENDING)
                    .forEach(token -> {
                        token.setStatus(TokenStatus.SUPERSEDED);
                        forgotPasswordHistoryRepository.save(token);
                    });

            String token = UUID.randomUUID().toString();
            UserForgotPasswordHistory history = UserForgotPasswordHistory.builder()
                    .user(user)
                    .resetToken(token)
                    .requestedAt(LocalDateTime.now())
                    .status(TokenStatus.PENDING)
                    .build();
            forgotPasswordHistoryRepository.save(history);

            String resetLink = frontendUrl + "/reset-password?token=" + token;
            emailService.sendForgotPasswordEmail(user.getEmail(), resetLink);
        });
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        UserForgotPasswordHistory history = forgotPasswordHistoryRepository.findByResetToken(request.getResetToken())
                .orElseThrow(() -> new InvalidResetTokenException("Invalid reset token"));

        if (history.getStatus() != TokenStatus.PENDING) {
            throw new InvalidResetTokenException("Reset token has already been used");
        }

        if (history.getRequestedAt().plusHours(tokenValidityHours).isBefore(LocalDateTime.now())) {
            history.setStatus(TokenStatus.EXPIRED);
            forgotPasswordHistoryRepository.save(history);
            throw new InvalidResetTokenException("Reset token has expired");
        }

        User user = history.getUser();

        if (user.getStatus() != UserStatus.ACTIVE) {
            switch (user.getStatus()) {
                case SUSPENDED:
                    throw new AccountSuspendedException("Your account is suspended. Please contact support.");
                case DEACTIVATED:
                    throw new AccountDeletedException("Your account has been deactivated.");
                case UNAPPROVED:
                case PENDING_APPROVAL:
                case INVITED:
                case APPROVED:
                    throw new AccountUnapprovedException("Your account is not active yet. Please approve your account first.");
                default:
                    throw new AuthenticationException("Account is not active. Password reset is not allowed.") {};
            }
        }

        String decryptedPassword = AESUtil.decrypt(request.getNewPassword());
        user.setPassword(passwordEncoder.encode(decryptedPassword));
        userRepository.save(user);

        history.setStatus(TokenStatus.COMPLETED);
        history.setResetAt(LocalDateTime.now());
        forgotPasswordHistoryRepository.save(history);
    }
}

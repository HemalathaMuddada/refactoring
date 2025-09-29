package com.xtremand.auth.activation.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.common.email.notification.NotificationService;
import com.xtremand.common.exception.AccountAlreadyActivatedException;
import com.xtremand.common.exception.ActivationTokenExpiredException;
import com.xtremand.common.exception.InvalidActivationTokenException;
import com.xtremand.domain.entity.User;
import com.xtremand.domain.entity.UserActivationHistory;
import com.xtremand.domain.enums.TokenStatus;
import com.xtremand.domain.enums.UserStatus;
import com.xtremand.user.repository.UserActivationHistoryRepository;
import com.xtremand.user.repository.UserRepository;

@Service
public class ActivationServiceImpl implements ActivationService {

	private final UserActivationHistoryRepository userActivationHistoryRepository;
	private final UserRepository userRepository;
	private final NotificationService notificationService;

	@Value("${xtremand.security.token.activation.expiry-in-hours:24}")
	private long activationTokenExpiryInHours;
	
	@Value("${frontend.url}")
    private String frontendUrl;

	public ActivationServiceImpl(UserActivationHistoryRepository userActivationHistoryRepository,
			UserRepository userRepository, NotificationService notificationService) {
		this.userActivationHistoryRepository = userActivationHistoryRepository;
		this.userRepository = userRepository;
		this.notificationService = notificationService;
	}

	@Override
	public void createActivationTokenAndSendEmail(User user) {
		// Invalidate all previous pending tokens for this user
		userActivationHistoryRepository.findByUserAndStatus(user, TokenStatus.PENDING).forEach(token -> {
			token.setStatus(TokenStatus.SUPERSEDED);
			userActivationHistoryRepository.save(token);
		});

		String token = UUID.randomUUID().toString();
		UserActivationHistory history = UserActivationHistory.builder().user(user).activationToken(token)
				.requestedAt(LocalDateTime.now()).status(TokenStatus.PENDING).build();
		userActivationHistoryRepository.save(history);

		String activationLink = frontendUrl +"/activate?token=" + token;
		notificationService.sendActivationEmail(user.getEmail(), activationLink);
	}

	@Override
	public void activateUser(String token) {
		UserActivationHistory history = userActivationHistoryRepository.findByActivationToken(token)
				.orElseThrow(() -> new InvalidActivationTokenException("Invalid activation token"));

		if (history.getStatus() == TokenStatus.COMPLETED) {
			throw new AccountAlreadyActivatedException("Account already activated");
		}

		if (history.getRequestedAt().plusHours(activationTokenExpiryInHours).isBefore(LocalDateTime.now())) {
			history.setStatus(TokenStatus.EXPIRED);
			userActivationHistoryRepository.save(history);
			throw new ActivationTokenExpiredException("Activation token expired");
		}

		User user = history.getUser();
		user.setStatus(UserStatus.ACTIVE);
		userRepository.save(user);

		history.setActivatedAt(LocalDateTime.now());
		history.setStatus(TokenStatus.COMPLETED);
		userActivationHistoryRepository.save(history);
	}
}

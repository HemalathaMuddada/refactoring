package com.xtremand.user.service;

import java.util.UUID;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.common.identity.ExternalIdentityUserService;
import com.xtremand.common.identity.ExternalUserDto;
import com.xtremand.domain.entity.User;
import com.xtremand.user.repository.UserRepository;


@Service
public class ExternalIdentityUserServiceImpl implements ExternalIdentityUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ExternalIdentityUserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public ExternalUserDto findOrCreateUserByExternalLogin(String username, String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
            .orElseGet(() -> createUser(username, email));
        return ExternalUserDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .username(user.getUsername())
            .build();
    }

    private User createUser(String username, String email) {
        User user = User.builder()
            .username(username != null ? username : email)
            .email(email)
            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
            .build();
        return userRepository.save(user);
    }
}

package com.xtremand.auth.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.auth.activation.service.ActivationService;
import com.xtremand.common.dto.UserProfile;
import com.xtremand.common.exception.DuplicateResourceException;
import com.xtremand.common.util.AESUtil;
import com.xtremand.domain.entity.Role;
import com.xtremand.domain.entity.User;
import com.xtremand.domain.entity.UserRole;
import com.xtremand.domain.enums.RoleName;
import com.xtremand.user.dto.SignupRequest;
import com.xtremand.user.repository.UserRepository;
import com.xtremand.user.role.repository.RoleRepository;
import com.xtremand.user.role.repository.UserRoleRepository;

@Service
@Transactional
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final RoleRepository roleRepository;
	private final UserRoleRepository userRoleRepository;
	private final ActivationService activationService;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository,
			UserRoleRepository userRoleRepository, ActivationService activationService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.roleRepository = roleRepository;
		this.userRoleRepository = userRoleRepository;
		this.activationService = activationService;
	}

	public UserProfile register(SignupRequest request) {
		userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
			throw new DuplicateResourceException("User", request.getEmail());
		});
		userRepository.findByUsername(request.getFullName()).ifPresent(u -> {
			throw new DuplicateResourceException("User", request.getFullName());
		});
		String decrypted = AESUtil.decrypt(request.getPassword());
		Role role = roleRepository.findByName(RoleName.TEAM_MEMBER.name())
				.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
		User user = User.builder().username(request.getFullName()).email(request.getEmail())
				.password(passwordEncoder.encode(decrypted)).build();
		user = userRepository.save(user);

		UserRole userRole = new UserRole();
		userRole.setUser(user);
		userRole.setRole(role);
		userRoleRepository.save(userRole);

		activationService.createActivationTokenAndSendEmail(user);

		return UserProfile.builder().id(user.getId()).email(user.getEmail()).fullName(user.getUsername())
				.role(role.getName()).build();
	}
}

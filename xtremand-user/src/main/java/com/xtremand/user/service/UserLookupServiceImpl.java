package com.xtremand.user.service;

import java.util.ArrayList;


import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.common.identity.AuthUserDto;
import com.xtremand.common.identity.UserLookupService;
import com.xtremand.user.repository.UserRepository;
import com.xtremand.user.role.repository.RoleRepository;

@Service
public class UserLookupServiceImpl implements UserLookupService {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;

        public UserLookupServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
                this.userRepository = userRepository;
                this.roleRepository = roleRepository;
        }

	private static String toRoleAuthority(String roleKey) {
		return "ROLE_" + roleKey.toUpperCase().replace('-', '_');
	}

        @Override
        @Transactional(readOnly = true)
        public Optional<AuthUserDto> findByEmail(String email) {
                return userRepository.findByEmailIgnoreCase(email).map(user -> {
                        List<String> roleLabels = roleRepository.findRoleKeysByUserId(user.getId());
                        List<String> roles = roleLabels.stream()
                                        .map(UserLookupServiceImpl::toRoleAuthority)
                                        .toList();
                        return AuthUserDto.builder()
                                        .id(user.getId())
                                        .email(user.getEmail())
                                        .passwordHash(user.getPassword())
                                        .active(user.isEnabled())
                                        .status(user.getStatus())
                                        .roles(new ArrayList<>(roles))
                                        .roleLabels(new ArrayList<>(roleLabels))
                                        .privileges(new ArrayList<>())
                                        .build();
                });
        }

	@Override
	public boolean existsByEmail(String email) {
	    return email != null && userRepository.existsByEmailIgnoreCase(email.trim().toLowerCase());
	}
}

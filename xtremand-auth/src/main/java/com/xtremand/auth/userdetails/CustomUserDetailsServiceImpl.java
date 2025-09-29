package com.xtremand.auth.userdetails;

import java.util.ArrayList;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.common.identity.AuthUserDto;
import com.xtremand.common.identity.UserLookupService;

@Service
//@Transactional
public class CustomUserDetailsServiceImpl implements UserDetailsService {

	private final UserLookupService userLookupService;

	private static String toRoleAuthority(String roleKey) {
		return "ROLE_" + roleKey.toUpperCase().replace('-', '_');
	}

	public CustomUserDetailsServiceImpl(UserLookupService userLookupService) {
		this.userLookupService = userLookupService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		AuthUserDto user = userLookupService.findByEmail(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		if (user.getRoles() != null) {
			user.getRoles().forEach(r -> authorities.add(new SimpleGrantedAuthority(toRoleAuthority(r))));
		}
		if (user.getPrivileges() != null) {
			user.getPrivileges().forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
		}

		return new CustomUserDetails(user.getId(), user.getEmail(), user.getPasswordHash(), user.isActive(),
				authorities);
	}
}

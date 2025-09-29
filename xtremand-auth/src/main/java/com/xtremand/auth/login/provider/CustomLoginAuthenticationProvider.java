package com.xtremand.auth.login.provider;

import java.util.ArrayList;

import java.util.List;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.xtremand.auth.login.exception.AccountDeletedException;
import com.xtremand.auth.login.exception.AccountSuspendedException;
import com.xtremand.auth.login.exception.AccountUnapprovedException;
import com.xtremand.common.identity.AuthUserDto;
import com.xtremand.common.identity.UserLookupService;
import com.xtremand.domain.enums.UserStatus;

@Component
public class CustomLoginAuthenticationProvider implements AuthenticationProvider {

	private final UserLookupService userLookupService;
	private final PasswordEncoder passwordEncoder;

	private static String toRoleAuthority(String roleKey) {
		return "ROLE_" + roleKey.toUpperCase().replace('-', '_');
	}

	public CustomLoginAuthenticationProvider(UserLookupService userLookupService, PasswordEncoder passwordEncoder) {
		this.userLookupService = userLookupService;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public Authentication authenticate(Authentication authentication) {

		String email = authentication.getName();
		String password = authentication.getCredentials().toString();

		AuthUserDto user = userLookupService.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

		if (user.getStatus() != UserStatus.APPROVED) {
			if (user.getStatus() == UserStatus.UNAPPROVED) {
				throw new AccountUnapprovedException(
						"Account not yet approved. Please check your email for activation.");
			} else if (user.getStatus() == UserStatus.SUSPENDED) {
				throw new AccountSuspendedException("Your account has been suspended. Contact support.");
			} else if (user.getStatus() == UserStatus.DEACTIVATED) {
				throw new AccountDeletedException("This account has been deleted.");
			}
		}

		if (!passwordEncoder.matches(password, user.getPasswordHash())) {
			throw new BadCredentialsException("Invalid password");
		}

		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		if (user.getRoles() != null) {
			user.getRoles().forEach(r -> authorities.add(new SimpleGrantedAuthority(toRoleAuthority(r))));
		}
		if (user.getPrivileges() != null) {
			user.getPrivileges().forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
		}

		return new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
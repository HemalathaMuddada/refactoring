package com.xtremand.common.identity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xtremand.domain.enums.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUserDto {
	private Long id;
	private String email;
	@JsonIgnore
	private String passwordHash;
	private List<String> roleLabels;
	private boolean active;
	private UserStatus status;
	@JsonIgnore
	private List<String> roles;
	private List<String> privileges;
}

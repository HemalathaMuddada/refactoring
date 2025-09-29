package com.xtremand.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

        @NotBlank
        private String fullName;

	@NotBlank
	@Email
	private String email;

        @NotBlank
        private String password;
}

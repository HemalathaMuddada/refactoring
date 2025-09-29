package com.xtremand.email.verification.dto;

import com.xtremand.email.verification.rule.EmailValidationStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailVerifierOutput {

	String email;
	EmailValidationStatus status;
	String message;

}

package com.xtremand.auth.login.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.auth.activation.service.ActivationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/activate")
@Tag(name = "Authentication", description = "User activation")
public class ActivationController {

	private final ActivationService activationService;

	public ActivationController(ActivationService activationService) {
		this.activationService = activationService;
	}

	@GetMapping
	@Operation(summary = "Activate user account")
	public ResponseEntity<String> activate(@RequestParam("token") String token) {
		activationService.activateUser(token);
		return ResponseEntity.ok("Account activated successfully");
	}
}

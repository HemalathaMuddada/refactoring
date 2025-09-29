package com.xtremand.common.identity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight user representation for external identity providers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalUserDto {
    @NotNull
    private Long id;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String username;
}

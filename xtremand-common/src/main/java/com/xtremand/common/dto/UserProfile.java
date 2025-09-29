package com.xtremand.common.dto;

import com.xtremand.domain.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    private Long id;
    private String email;
    private String fullName;
    private String role;

    public static UserProfile from(User user) {
        String roleName = user.getUserRoles().stream()
                .findFirst()
                .map(userRole -> userRole.getRole().getName())
                .orElse(null);

        return UserProfile.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getUsername())
                .role(roleName)
                .build();
    }
}

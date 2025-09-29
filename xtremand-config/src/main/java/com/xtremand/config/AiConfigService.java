package com.xtremand.config;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.xtremand.config.repository.AiConfigRepository;
import com.xtremand.domain.dto.AiConfigInput;
import com.xtremand.domain.dto.AiConfigWithDefaultFlag;
import com.xtremand.domain.entity.AiConfig;
import com.xtremand.domain.entity.User;
import com.xtremand.domain.enums.AiConfigType;
import com.xtremand.user.dto.AiConfigDto;
import com.xtremand.user.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AiConfigService {
    private final AiConfigRepository aiConfigRepository;
    private final UserRepository userRepository;

    public AiConfigService(AiConfigRepository aiConfigRepository, UserRepository userRepository) {
        this.aiConfigRepository = aiConfigRepository;
        this.userRepository = userRepository;
    }

    // Create or update, with audit
    public AiConfig saveOrUpdateAiConfig(AiConfigInput input, Authentication authentication) {
        validateInput(input);
        AiConfigType type = AiConfigType.valueOf(input.getConfigType());
        User user = userRepository.fetchByUsername(authentication.getName());
        Optional<AiConfig> optionalConfig = aiConfigRepository.findByEmailAndConfigType(input.getEmail(), type);
        AiConfig config;
        if (optionalConfig.isPresent()) {
            config = optionalConfig.get();
            config.setApiKey(encrypt(input.getApiKey()));
            config.setApiSecret(encrypt(input.getApiSecret()));
            config.setUpdatedAt(LocalDateTime.now());
            config.setUpdatedBy(user);
            config.setDisplayName(input.getDisplayName());
        } else {
            config = new AiConfig();
            config.setEmail(input.getEmail());
            config.setConfigType(type);
            config.setApiKey(encrypt(input.getApiKey()));
            config.setApiSecret(encrypt(input.getApiSecret()));
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
            config.setCreatedBy(user);
            config.setUpdatedBy(user);
            config.setDisplayName(input.getDisplayName());
        }
        return aiConfigRepository.save(config);
    }

    // Fetch single config (utility/lookup, no audit needed)
    public Optional<AiConfig> getAiConfig(String email, AiConfigType type) {
        return aiConfigRepository.findByEmailAndConfigType(email, type);
    }

  
	public List<AiConfigDto> findAllAiConfigsByUser(Authentication authentication) {
		User user = userRepository.fetchByUsername(authentication.getName());
		List<AiConfig> configs = aiConfigRepository.findByCreatedBy(user);
		List<AiConfigDto> wrappedConfigs = new ArrayList<>();

		for (AiConfig c : configs) {
			wrappedConfigs.add(new AiConfigDto(c.getId(), c.getEmail(), c.getDisplayName(), c.getConfigType(),
					c.getApiKey(), c.getApiSecret(), c.getCreatedAt(), c.getUpdatedAt(), c.getToken(), false));
		}

		wrappedConfigs
				.add(new AiConfigDto(null, "system", null, AiConfigType.OLLAMA, "", null, null, null, null, false));
		wrappedConfigs.add(
				new AiConfigDto(null, "system", null, AiConfigType.POLLINATIONS, "", null, null, null, null, true));

		return wrappedConfigs;
	}

    // Delete config (optionally log authentication/user)
    public void deleteAiConfig(Long id, Authentication authentication) {
        if (!aiConfigRepository.existsById(id)) {
            throw new IllegalArgumentException("AI Config with id " + id + " not found");
        }
        // Optionally use authentication for audit logging
        aiConfigRepository.deleteById(id);
    }

    // Update config, with audit
    public AiConfig updateAiConfig(Long id, AiConfigInput input, Authentication authentication) {
        AiConfig existingConfig = aiConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AI Config with id " + id + " not found"));
        validateInput(input);
        AiConfigType type = AiConfigType.valueOf(input.getConfigType());
        User user = userRepository.fetchByUsername(authentication.getName());
        existingConfig.setConfigType(type);
        existingConfig.setApiKey(encrypt(input.getApiKey()));
        existingConfig.setApiSecret(encrypt(input.getApiSecret()));
        existingConfig.setUpdatedAt(LocalDateTime.now());
        existingConfig.setUpdatedBy(user);
        existingConfig.setDisplayName(input.getDisplayName());
        return aiConfigRepository.save(existingConfig);
    }

    // Input validation
    private void validateInput(AiConfigInput input) {
        if (input.getConfigType() == null) {
            throw new IllegalArgumentException("Config type is required");
        }
        try {
            AiConfigType.valueOf(input.getConfigType());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid AI config type: " + input.getConfigType());
        }
        if (input.getApiKey() == null || input.getApiKey().isBlank()) {
            throw new IllegalArgumentException("API key is required for AI config");
        }
    }

    private String encrypt(String plain) {
        if (plain == null || plain.isEmpty()) return null;
        return Base64.getEncoder().encodeToString(plain.getBytes(StandardCharsets.UTF_8));
    }
}

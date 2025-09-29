package com.xtremand.config;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.xtremand.config.repository.MailConfigRepository;
import com.xtremand.domain.dto.MailConfigDTO;
import com.xtremand.domain.dto.MailConfigInput;
import com.xtremand.domain.dto.SmtpDetails;
import com.xtremand.domain.entity.MailConfig;
import com.xtremand.domain.entity.User;
import com.xtremand.domain.enums.EmailConfigType;
import com.xtremand.user.repository.UserRepository;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class MailConfigService {

    @Value("${smtp.host}")
    private String smtpHost;

    @Value("${smtp.port}")
    private String smtpPort;

    @Value("${smtp.outlook.host}")
    private String outlookHost;

    @Value("${smtp.outlook.port}")
    private String outlookPort;

    private final MailConfigRepository mailConfigRepository;
    private final UserRepository userRepository;

    public MailConfigService(MailConfigRepository mailConfigRepository, UserRepository userRepository) {
        this.mailConfigRepository = mailConfigRepository;
        this.userRepository = userRepository;
    }

    public MailConfig saveOrUpdateMailConfig(MailConfigInput input, Authentication authentication,User user) {
        if(user ==null) {
    	 user = userRepository.fetchByUsername(authentication.getName());
        }
        SmtpDetails details = validateMailConfigInput(input);
        Optional<MailConfig> optionalConfig = mailConfigRepository.findByEmailAndConfigType(input.getEmail(),input.getConfigType());
        MailConfig config;

        if (optionalConfig.isPresent()) {
            config = optionalConfig.get();
            config.setConfigType(input.getConfigType());
            config.setOauthAccessToken(input.getOauthToken());
            config.setOauthRefreshToken(input.getOauthRefreshToken());
            config.setTokenExpiry(input.getTokenExpiry());
            config.setUpdatedAt(LocalDateTime.now());
            config.setUpdatedBy(user);
            config.setUsername(input.getUsername());
            config.setHost(details.getHost());
            config.setPort(details.getPort());
            config.setDisplayName(input.getDisplayName());
//            List<String> emails = input.getImportantEmail() == null ? List.of() : input.getImportantEmail();
//            config.setImportantEmail(emails);
            if (input.getImportantEmail() == null) {
                config.setImportantEmail(new ArrayList<>());
            } else {
                config.setImportantEmail(new ArrayList<>(input.getImportantEmail()));  // must be mutable list
            }
            if(config.getCreatedAt() == null) {
                config.setCreatedAt(LocalDateTime.now());
                config.setCreatedBy(user);
            }
        } else {
            config = buildMailConfigEntity(input, details.getHost(), details.getPort(), user);
        }
        return mailConfigRepository.save(config);
    }

    private MailConfig buildMailConfigEntity(MailConfigInput input, String host, String port, User user) {
        MailConfig config = new MailConfig();
        config.setConfigType(input.getConfigType());
        config.setEmail(input.getEmail());
        config.setUsername(input.getUsername());
        config.setPassword(encryptPassword(input.getPassword()));
        config.setOauthAccessToken(input.getOauthToken());
        config.setOauthRefreshToken(input.getOauthRefreshToken());
        config.setTokenExpiry(input.getTokenExpiry());
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        config.setCreatedBy(user);
        config.setUpdatedBy(user);
        config.setHost(host);
        config.setPort(port);
        config.setDisplayName(input.getDisplayName());
//        List<String> emails = input.getImportantEmail() == null ? List.of() : input.getImportantEmail();
//        config.setImportantEmail(emails);
        if (input.getImportantEmail() == null) {
            config.setImportantEmail(new ArrayList<>());
        } else {
            config.setImportantEmail(new ArrayList<>(input.getImportantEmail()));  // must be mutable list
        }
        return config;
    }

    private SmtpDetails validateMailConfigInput(MailConfigInput input) {
        if (input.getConfigType() == null ) {
            throw new IllegalArgumentException("Invalid config type: " + input.getConfigType());
        }
        EmailConfigType configType = input.getConfigType();
        SmtpDetails smtpDetails = new SmtpDetails();
        switch (configType) {
            case SMTP_CONFIG:
                if (isNullOrEmpty(input.getUsername()) || isNullOrEmpty(input.getPassword())) {
                    throw new IllegalArgumentException("Username and password shouldn't be null or empty for SMTP config.");
                }
                smtpDetails.setHost(smtpHost);
                smtpDetails.setPort(smtpPort);
                break;
            case OUTLOOK:
            case OFFICE:
                if (isNullOrEmpty(input.getOauthToken()) && (isNullOrEmpty(input.getUsername()) || isNullOrEmpty(input.getPassword()))) {
                    throw new IllegalArgumentException(
                            "Either OAuth token or username/password must be provided for Outlook/Office365 configuration.");
                }
                smtpDetails.setHost(outlookHost);
                smtpDetails.setPort(outlookPort);
                break;
            case OAUTH_CONFIG:
                if (isNullOrEmpty(input.getOauthToken())) {
                    throw new IllegalArgumentException("OAuth token is required for OAUTH_CONFIG.");
                }
                smtpDetails.setHost(smtpHost);
                smtpDetails.setPort(smtpPort);
                break;
            default:
                throw new IllegalArgumentException("Unsupported config type: " + configType);
        }
        if (isNullOrEmpty(input.getEmail())) {
            throw new IllegalArgumentException("Email address is required.");
        }
        return smtpDetails;
    }

    private boolean isValidConfigType(String configType) {
        try {
            EmailConfigType.valueOf(configType);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String encryptPassword(String plain) {
        if (plain == null || plain.isEmpty()) {
            return null;
        }
        return Base64.getEncoder().encodeToString(plain.getBytes(StandardCharsets.UTF_8));
    }

    private boolean isNullOrEmpty(String str) {
        return (str == null || str.trim().isEmpty());
    }
    
    
    public List<MailConfigDTO> findAllMailConfigsByUser(Authentication authentication) {
        User user = userRepository.fetchByUsername(authentication.getName());
        return mailConfigRepository.findDtoByCreatedBy(user);
    }



    public void deleteMailConfig(Long id, Authentication authentication) {
        if (!mailConfigRepository.existsById(id)) {
            throw new IllegalArgumentException("MailConfig with id " + id + " not found");
        }
        mailConfigRepository.deleteById(id);
    }

    public MailConfig updateMailConfig(Long id, MailConfigInput input, Authentication authentication) {
        User user = userRepository.fetchByUsername(authentication.getName());

        MailConfig existingConfig = mailConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MailConfig with id " + id + " not found"));

        SmtpDetails details = validateMailConfigInput(input);
        existingConfig.setConfigType(input.getConfigType());
        existingConfig.setOauthAccessToken(input.getOauthToken());
        existingConfig.setOauthRefreshToken(input.getOauthRefreshToken());
        existingConfig.setTokenExpiry(input.getTokenExpiry());
        existingConfig.setUpdatedAt(LocalDateTime.now());
        existingConfig.setUpdatedBy(user);
        existingConfig.setUsername(input.getUsername());
        existingConfig.setHost(details.getHost());
        existingConfig.setPort(details.getPort());
        existingConfig.setDisplayName(input.getDisplayName());
        if (input.getPassword() != null && !input.getPassword().trim().isEmpty()) {
            existingConfig.setPassword(encryptPassword(input.getPassword()));
        }
//        List<String> emails = input.getImportantEmail() == null ? List.of() : input.getImportantEmail();
//        existingConfig.setImportantEmail(emails);
        if (input.getImportantEmail() == null) {
        	existingConfig.setImportantEmail(new ArrayList<>());
        } else {
        	existingConfig.setImportantEmail(new ArrayList<>(input.getImportantEmail()));  // must be mutable list
        }
        return mailConfigRepository.save(existingConfig);
    }
}

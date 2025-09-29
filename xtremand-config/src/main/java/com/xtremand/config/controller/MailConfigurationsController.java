package com.xtremand.config.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.xtremand.auth.oauth2.service.AuthenticationFacade;
import com.xtremand.config.MailConfigService;
import com.xtremand.domain.dto.MailConfigDTO;
import com.xtremand.domain.dto.MailConfigInput;
import com.xtremand.domain.entity.MailConfig;
import com.xtremand.domain.enums.EmailConfigType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/configuration")
public class MailConfigurationsController {

    private final MailConfigService service;
    private final AuthenticationFacade authenticationFacade;

    public MailConfigurationsController(MailConfigService service, AuthenticationFacade authenticationFacade) {
        this.service = service;
        this.authenticationFacade = authenticationFacade;
    }

    @GetMapping("/types")
    public List<String> getAllConfigTypes() {
        return Arrays.stream(EmailConfigType.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<MailConfig> createMailConfig(@RequestBody MailConfigInput input) {
        Authentication authentication = authenticationFacade.getAuthentication();
        MailConfig savedConfig = service.saveOrUpdateMailConfig(input, authentication,null);
        return ResponseEntity.ok(savedConfig);
    }

    @GetMapping
    public List<MailConfigDTO> getAllMailConfigs() {
        Authentication authentication = authenticationFacade.getAuthentication();
        return service.findAllMailConfigsByUser(authentication);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMailConfig(@PathVariable Long id) {
        Authentication authentication = authenticationFacade.getAuthentication();
        service.deleteMailConfig(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<MailConfig> updateMailConfig(@PathVariable Long id, @RequestBody MailConfigInput input) {
        Authentication authentication = authenticationFacade.getAuthentication();
        MailConfig updatedConfig = service.updateMailConfig(id, input, authentication);
        return ResponseEntity.ok(updatedConfig);
    }
}




package com.xtremand.config.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.xtremand.auth.oauth2.service.AuthenticationFacade;
import com.xtremand.config.AiConfigService;
import com.xtremand.domain.dto.AiConfigInput;
import com.xtremand.domain.dto.AiConfigWithDefaultFlag;
import com.xtremand.domain.entity.AiConfig;
import com.xtremand.domain.enums.AiConfigType;
import com.xtremand.user.dto.AiConfigDto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/configuration/ai")
public class AiConfigurationsController {

    private final AiConfigService service;
    private final AuthenticationFacade authenticationFacade;

    public AiConfigurationsController(AiConfigService service, AuthenticationFacade authenticationFacade) {
        this.service = service;
        this.authenticationFacade = authenticationFacade;
    }

    @GetMapping("/types")
    public List<String> getAllAiConfigTypes() {
        return Arrays.stream(AiConfigType.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<AiConfig> createAiConfig(@RequestBody AiConfigInput input) {
        Authentication authentication = authenticationFacade.getAuthentication();
        AiConfig savedConfig = service.saveOrUpdateAiConfig(input, authentication);
        return ResponseEntity.ok(savedConfig);
    }

    @GetMapping
    public List<AiConfigDto> getAllAiConfigs() {
        Authentication authentication = authenticationFacade.getAuthentication();
        return service.findAllAiConfigsByUser(authentication);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAiConfig(@PathVariable Long id) {
        Authentication authentication = authenticationFacade.getAuthentication();
        service.deleteAiConfig(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<AiConfig> updateAiConfig(@PathVariable Long id, @RequestBody AiConfigInput input) {
        Authentication authentication = authenticationFacade.getAuthentication();
        AiConfig updatedConfig = service.updateAiConfig(id, input, authentication);
        return ResponseEntity.ok(updatedConfig);
    }
}

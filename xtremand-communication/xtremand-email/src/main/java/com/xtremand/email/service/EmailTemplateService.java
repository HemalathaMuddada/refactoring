package com.xtremand.email.service;

import com.xtremand.common.exception.BadRequestException;
import com.xtremand.common.exception.RecordNotFoundException;
import com.xtremand.domain.dto.EmailTemplateDTO;
import com.xtremand.domain.dto.EmailTemplateRequest;
import com.xtremand.domain.entity.EmailTemplate;
import com.xtremand.domain.entity.User;
import com.xtremand.domain.enums.EmailCategory;
import com.xtremand.email.repository.EmailTemplateRepository;
import com.xtremand.email.specification.TemplateListSpecification;
import com.xtremand.user.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService {

    private final EmailTemplateRepository repository;
    private final UserRepository userRepository;

    public EmailTemplateService(EmailTemplateRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public EmailTemplate saveTemplate(EmailTemplateRequest dto, Authentication authentication) {
        User user = userRepository.fetchByUsername(authentication.getName());

        if (!EmailCategory.contains(dto.getCategory().name())) {
            throw new BadRequestException("Category not found: " + dto.getCategory().name());
        }
        EmailTemplate template = new EmailTemplate();
        template.setName(dto.getName());
        template.setCategory(dto.getCategory());
        template.setSubjectLine(dto.getSubjectLine());
        template.setContent(dto.getContent());
        template.setVariables(dto.getVariables());
        template.setCreatedBy(user);
        template.setCreatedAt(java.time.LocalDate.now());
        template.setUpdatedBy(user);
        template.setUpdatedAt(java.time.LocalDate.now());
        return repository.save(template);
    }

    public EmailTemplate getTemplateById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Email template not found with id: " + id));
    }

    public void deleteTemplateById(Long id, Authentication authentication) {
        if (!repository.existsById(id)) {
            throw new RecordNotFoundException("Email template not found with id: " + id);
        }
        // Optionally audit delete by user from authentication here
        repository.deleteById(id);
    }

    public EmailTemplate updateTemplate(Long id, EmailTemplateRequest dto, Authentication authentication) {
        EmailTemplate template = getTemplateById(id);
        User user = userRepository.fetchByUsername(authentication.getName());
        if (!EmailCategory.contains(dto.getCategory().name())) {
            throw new BadRequestException("Category not found: " + dto.getCategory().name());
        }
        template.setName(dto.getName());
        template.setCategory(dto.getCategory());
        template.setSubjectLine(dto.getSubjectLine());
        template.setContent(dto.getContent());
        template.setVariables(dto.getVariables());
        template.setUpdatedBy(user);
        template.setUpdatedAt(java.time.LocalDate.now());
        return repository.save(template);
    }

    public Map<String, Object> getTemplatesWithStats(String category, String search, Authentication authentication) {
        List<EmailTemplate> templates = getAllTemplates(category, search);
        List<EmailTemplateDTO> templateDTOs = templates.stream()
        	    .map(t -> {
        	        EmailTemplateDTO dto = new EmailTemplateDTO();
        	        dto.setId(t.getId());
        	        dto.setName(t.getName());
        	        dto.setCategory(t.getCategory());
        	        dto.setSubjectLine(t.getSubjectLine());
        	        dto.setContent(t.getContent());
        	        dto.setVariables(t.getVariables());
        	        dto.setCreatedAt(t.getCreatedAt());
        	        dto.setUpdatedAt(t.getUpdatedAt());
        	        dto.setCreatedByUserId(t.getCreatedBy() != null ? t.getCreatedBy().getId() : null);
        	        dto.setUpdatedByUserId(t.getUpdatedBy() != null ? t.getUpdatedBy().getId() : null);
        	        return dto;
        	    }).collect(Collectors.toList());
        long totalTemplates = templates.size();
        long distinctCategories = templates.stream()
            .map(EmailTemplate::getCategory)
            .filter(Objects::nonNull)
            .distinct()
            .count();
        EmailCategory mostUsedCategory = templates.stream()
            .filter(t -> t.getCategory() != null)
            .collect(Collectors.groupingBy(EmailTemplate::getCategory, Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        Map<String, Object> result = new HashMap<>();
        result.put("totalTemplates", totalTemplates);
        result.put("distinctCategories", distinctCategories);
        result.put("mostUsedCategory", mostUsedCategory);
        result.put("templates", templateDTOs);
        return result;
    }

    public List<EmailTemplate> getAllTemplates(String category, String search) {
        Specification<EmailTemplate> spec = TemplateListSpecification.build(category, search);
        return repository.findAll(spec);
    }

    public List<String> getAllCategories() {
        return Arrays.stream(EmailCategory.values()).map(EmailCategory::getValue).collect(Collectors.toList());
    }

    public List<String> getAllCampaignTypes() {
        // Assuming EmailCampaignType enum; adapt if needed
        return Arrays.stream(com.xtremand.domain.enums.EmailCampaignType.values())
                .map(com.xtremand.domain.enums.EmailCampaignType::getValue).collect(Collectors.toList());
    }

    public List<String> getAllTones() {
        return Arrays.stream(com.xtremand.domain.enums.Tone.values()).map(com.xtremand.domain.enums.Tone::getValue)
                .collect(Collectors.toList());
    }
}

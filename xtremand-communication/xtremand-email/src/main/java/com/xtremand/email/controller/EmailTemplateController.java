package com.xtremand.email.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.xtremand.auth.oauth2.service.AuthenticationFacade;
import com.xtremand.domain.dto.EmailTemplateRequest;
import com.xtremand.domain.dto.Response;
import com.xtremand.domain.entity.EmailTemplate;
import com.xtremand.email.service.EmailTemplateService;

@RestController
@RequestMapping("/email-templates")
@Tag(name = "Email Templates", description = "APIs for  email templates CRUD Operations")
public class EmailTemplateController {

    private final EmailTemplateService templateService;
    private final AuthenticationFacade authenticationFacade;

    public EmailTemplateController(EmailTemplateService templateService, AuthenticationFacade authenticationFacade) {
        this.templateService = templateService;
        this.authenticationFacade = authenticationFacade;
    }

    @Tag(name = "Email Template Management", description = "APIs for managing reusable email templates")
    @Operation(summary = "Create an email template", description = "Creates a new reusable email template.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Template created successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid category or other input",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class))) })
    @PostMapping
    public ResponseEntity<Response> createTemplate(@RequestBody EmailTemplateRequest request) {
        Authentication authentication = authenticationFacade.getAuthentication();
        templateService.saveTemplate(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(new Response("Template created successfully"));
    }

    @Tag(name = "Email Template Management")
    @Operation(summary = "Get an email template by ID", description = "Retrieves a single email template by its ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved template"),
        @ApiResponse(responseCode = "404", description = "Not Found - Template not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class))) })
    @GetMapping("/{id}")
    public ResponseEntity<EmailTemplate> getTemplateById(
        @Parameter(description = "ID of the template to retrieve") @PathVariable Long id) {
        EmailTemplate template = templateService.getTemplateById(id);
        if (template == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(template);
    }

    @Tag(name = "Email Template Management")
    @Operation(summary = "Delete an email template", description = "Deletes an existing email template by its ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Template deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Not Found - Template not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class))) })
    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteTemplate(
        @Parameter(description = "ID of the template to delete") @PathVariable Long id) {
        Authentication authentication = authenticationFacade.getAuthentication();
        templateService.deleteTemplateById(id, authentication);
        return ResponseEntity.ok(new Response("Template deleted successfully"));
    }

    @Tag(name = "Email Template Management")
    @Operation(summary = "Update an email template", description = "Updates an existing email template.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Template updated successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid category or other input",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Not Found - Template not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class))) })
    @PutMapping("/{id}")
    public ResponseEntity<Response> updateTemplate(
        @Parameter(description = "ID of the template to update") @PathVariable Long id,
        @RequestBody EmailTemplateRequest request) {
        Authentication authentication = authenticationFacade.getAuthentication();
        EmailTemplate updated = templateService.updateTemplate(id, request, authentication);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new Response("Updated successfully"));
    }

    @Tag(name = "Email Template Management")
    @Operation(summary = "Get all email templates", description = "Retrieves a list of all email templates, with optional filtering by category and search term.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved templates")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTemplates(
        @Parameter(description = "Filter templates by category") @RequestParam(required = false) String category,
        @Parameter(description = "Search term for template name or content") @RequestParam(required = false) String search) {
        Authentication authentication = authenticationFacade.getAuthentication();
        Map<String, Object> result = templateService.getTemplatesWithStats(category, search, authentication);
        return ResponseEntity.ok(result);
    }

    @Tag(name = "Email Template Management")
    @Operation(summary = "Get all template categories", description = "Retrieves a list of all available email template categories.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved categories")
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = templateService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @Tag(name = "Email Template Management")
    @Operation(summary = "Get all campaign types", description = "Retrieves a list of all available campaign types for templates.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved campaign types")
    @GetMapping("/campaignTypes")
    public ResponseEntity<List<String>> getAllCampaignTypes() {
        List<String> campaignTypes = templateService.getAllCampaignTypes();
        return ResponseEntity.ok(campaignTypes);
    }

    @Tag(name = "Email Template Management")
    @Operation(summary = "Get all template tones", description = "Retrieves a list of all available tones for templates.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved tones")
    @GetMapping("/tones")
    public ResponseEntity<List<String>> getAllTones() {
        List<String> tones = templateService.getAllTones();
        return ResponseEntity.ok(tones);
    }
}

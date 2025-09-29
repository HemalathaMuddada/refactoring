package com.xtremand.email.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.xtremand.auth.oauth2.service.AuthenticationFacade;
import com.xtremand.domain.dto.CampaignAnalyticsDto;
import com.xtremand.domain.dto.CampaignCreateRequest;
import com.xtremand.domain.dto.CampaignResponse;
import com.xtremand.domain.entity.Campaign;
import com.xtremand.email.service.CampaignService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/campaigns")
@Tag(name = "Campaign Management", description = "APIs for creating, managing, and launching email campaigns")
public class CampaignController {
    private final CampaignService campaignService;
    private final AuthenticationFacade authenticationFacade;

    public CampaignController(CampaignService campaignService, AuthenticationFacade authenticationFacade) {
        this.campaignService = campaignService;
        this.authenticationFacade = authenticationFacade;
    }

    @Operation(summary = "Create a new campaign", description = "Creates a new email campaign with a specified contact list and email template.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "Campaign created successfully"),
            @ApiResponse(responseCode = "404", description = "Not Found - Contact list or email template not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class))) })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CampaignResponse createCampaign(@RequestBody CampaignCreateRequest request) {
        Authentication authentication = authenticationFacade.getAuthentication();
        Campaign campaign = campaignService.createCampaign(request, authentication);
        CampaignResponse resp = new CampaignResponse();
        resp.setId(campaign.getId());
        resp.setName(campaign.getName());
        return resp;
    }

    @Operation(summary = "Update an existing campaign", description = "Updates the details of an existing email campaign.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "Campaign updated successfully"),
            @ApiResponse(responseCode = "404", description = "Not Found - Campaign, contact list, or email template not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class))) })
    @PutMapping("/{id}")
    public CampaignResponse updateCampaign(
            @Parameter(description = "ID of the campaign to update") @PathVariable Long id,
            @RequestBody CampaignCreateRequest request) {
        Authentication authentication = authenticationFacade.getAuthentication();
        Campaign updated = campaignService.updateCampaign(id, request, authentication);
        CampaignResponse resp = new CampaignResponse();
        resp.setId(updated.getId());
        resp.setName(updated.getName());
        return resp;
    }

    @Operation(summary = "Delete a campaign", description = "Deletes an existing campaign by its ID.")
    @ApiResponses({ @ApiResponse(responseCode = "204", description = "Campaign deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Not Found - Campaign not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class))) })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCampaign(@Parameter(description = "ID of the campaign to delete") @PathVariable Long id) {
        campaignService.deleteCampaign(id);
    }

    @Operation(summary = "Launch a campaign", description = "Initiates the sending of an email campaign.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "Campaign launched successfully"),
            @ApiResponse(responseCode = "404", description = "Not Found - Campaign not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class))) })
    @PostMapping("/{campaignId}/launch")
    public String launchCampaign(
            @Parameter(description = "ID of the campaign to launch") @PathVariable Long campaignId) {
       
    	 Authentication authentication = authenticationFacade.getAuthentication();
    	 Campaign campaign = campaignService.getCampaignById(campaignId);
        return campaignService.launchCampaign(campaign,authentication);
    }

    @Operation(summary = "Get all campaigns with analytics", description = "Returns all campaigns with stats: total, active, total sent, and response rate.")
    @GetMapping("/analytics")
    public ResponseEntity<CampaignAnalyticsDto> getAllCampaignsAnalytics() {
        CampaignAnalyticsDto analytics = campaignService.getCampaignAnalytics();
        return ResponseEntity.ok(analytics);
    }
}


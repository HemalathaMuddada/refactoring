package com.xtremand.domain.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.xtremand.domain.entity.CampaignDashboardStats;

import lombok.Data;

@Data
public class CampaignResponse {
    private Long id;
    private String name;
    private double sent;
    private double delivered;
    private double opened;
    private double clicked;
    private double replied;
    private double bounced;
    private LocalDate createdAt;
    private String contactListName;
    private int numberOfContacts;
    private CampaignDashboardStats dashboardStats;
}


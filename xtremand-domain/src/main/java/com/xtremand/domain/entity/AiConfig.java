package com.xtremand.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import com.xtremand.domain.enums.AiConfigType;

@Entity
@Table(name = "ai_configurations")
@Getter
@Setter
public class AiConfig {

    @Id
	@SequenceGenerator(name = "ai_configurations_seq_gen", sequenceName = "ai_configurations_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ai_configurations_seq_gen")
    private Long id;

    @Column(name = "email")
    private String email; 
    
    @Column(name = "display_name")
    private String displayName; 

    @Enumerated(EnumType.STRING)
    @Column(name = "config_type", nullable = false, length = 50)
    private AiConfigType configType;

    @Column(name = "api_key", nullable = false, length = 255)
    private String apiKey;

    @Column(name = "api_secret", length = 255)
    private String apiSecret; 

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "token")
    private String token;
    
	@ManyToOne
	@JoinColumn(name = "created_by")
    private User createdBy;
	
	@ManyToOne
	@JoinColumn(name = "updated_by")
    private User updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


}


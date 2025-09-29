package com.xtremand.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.xtremand.domain.enums.CampaignEnumType;
import com.xtremand.domain.enums.CampaignType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "xt_campaign")
public class Campaign {
	@Id
	@SequenceGenerator(name = "xt_campaign_seq_gen", sequenceName = "xt_campaign_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_campaign_seq_gen")
	private Long id;

	@Column(name="name")
	private String name;
	
	@Column(name="type")
	@Convert(converter = CampaignEnumType.class)
	private CampaignType type;
	
	@Column(name="content_strategy")
	private String contentStrategy;
	
	@Column(name="scheduled_at")
	private LocalDateTime scheduledAt;
	
	@Column(name="ai_personalization")
	private boolean aiPersonalization;
	
    @Column(name = "created_at")
    private LocalDate createdAt;
    
    @Column(name = "updated_at")
    private LocalDate updatedAt;
    
    @Column(name = "is_sent")
    private boolean sent = false;
    
	@ManyToOne
	@JoinColumn(name = "created_by")
    private User createdBy;
	
	@ManyToOne
	@JoinColumn(name = "updated_by")
    private User updatedBy;
	
	@ManyToOne
	@JoinColumn(name = "contact_list_id")
	private ContactList contactList;
	
	@ManyToOne
	@JoinColumn(name = "email_template_id")
	private EmailTemplate emailTemplate;
	
	@ManyToOne
	@JoinColumn(name = "sent_by")
	private MailConfig config;
	
}

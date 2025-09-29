package com.xtremand.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.xtremand.domain.enums.Country;
import com.xtremand.domain.enums.DeviceType;
import com.xtremand.domain.enums.EmailStatus;
import com.xtremand.domain.enums.EmailStatusType;

@Entity
@Table(name = "email_analytics")
@Getter
@Setter
public class EmailAnalytics {

	@Id
	@SequenceGenerator(name = "email_analytics_seq_gen", sequenceName = "email_analytics_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "email_analytics_seq_gen")
	private Long id;
	
	@Column(name = "from_email")
	private String fromEmail;
	
	@Column(name = "prompt")
	private String prompt;

	@Column(name = "subject")
	private String subject;
	
	@Column(name = "body")
	private String body;

	@Column(name = "status")
	@Convert(converter = EmailStatusType.class)
	private EmailStatus status;

	@Column(name = "sent_at")
	private LocalDateTime sentAt;

	@Column(name = "opened_at")
	private LocalDateTime openedAt;

	@Column(name = "clicked_at")
	private LocalDateTime clickedAt;

	@Column(name = "bounced_at")
	private LocalDateTime bouncedAt;

	@Column(name = "replied_at")
	private LocalDateTime repliedAt;

	@Column(name = "tracking_id")
	private UUID trackingId;

	@Column(name = "tracking_url")
	private String trackingUrl;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "campaign_id",nullable = true)
	private Campaign campaign;

	@Enumerated(EnumType.STRING)
	@Column(name = "device")
	private DeviceType device;

	@Enumerated(EnumType.STRING)
	@Column(name = "country")
	private Country country;
	
	@Column(name = "is_incoming")
	private boolean isIncoming;
	
	@Column(name = "message_id")
	private String messageId;
	
	@Column(name = "in_reply_to")
    private String inReplyTo;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "inReplyTo")
    private List<EmailAnalytics> replies;
	
	@Column(name = "is_deleted")
	private boolean isDeleted;
	
	@Column(name = "is_starred")
	private boolean isStarred;
	
	@ManyToOne
	@JoinColumn(name = "created_by")
    private User createdBy;

}

package com.xtremand.domain.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.xtremand.domain.enums.EmailConfigType;

@Entity
@Table(name = "mail_configurations")
@Getter
@Setter
public class MailConfig {
	@Id
	@SequenceGenerator(name = "mail_configurations_seq_gen", sequenceName = "mail_configurations_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mail_configurations_seq_gen")
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "config_type")
	private EmailConfigType configType;

	@Column(name = "email")
	private String email;
	
    @Column(name = "display_name")
    private String displayName;

	@Column(name = "username")
	private String username;

	@Column(name = "password")
	private String password;

	@Column(name = "oauth_access_token")
	private String oauthAccessToken;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	@Column(name = "host")
	private String host;
	
	@Column(name = "port")
	private String port;

	@Column(name = "oauth_refresh_token")
    private String oauthRefreshToken; 
	
	@Column(name = "token_expiry")
    private LocalDateTime tokenExpiry;

	@ManyToOne
	@JoinColumn(name = "created_by")
    private User createdBy;
	
	@ManyToOne
	@JoinColumn(name = "updated_by")
    private User updatedBy;
	
	@ElementCollection
	@CollectionTable(name = "mail_config_important_emails", joinColumns = @JoinColumn(name = "mail_config_id"))
	@Column(name = "important_email")
	private List<String> importantEmail;


	
	

}

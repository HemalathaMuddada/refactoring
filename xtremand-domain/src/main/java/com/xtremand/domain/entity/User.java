package com.xtremand.domain.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.xtremand.domain.enums.UserStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "xt_users", uniqueConstraints = {
		@UniqueConstraint(name = "uk_xt_user_email", columnNames = "email") }, indexes = {
				@Index(name = "idx_xt_user_email", columnList = "email"),
				@Index(name = "idx_xt_user_created_at", columnList = "created_at"),
				@Index(name = "idx_xt_user_updated_at", columnList = "updated_at") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_user_id_seq")
	@SequenceGenerator(name = "xt_user_id_seq", sequenceName = "xt_user_id_seq", allocationSize = 1)
	private Long id;

	@Column(name = "external_id", nullable = false, unique = true, updatable = false)
	private UUID externalId;

	@NotNull
	@Email
	@Column(nullable = false, unique = true)
	private String email;

	@NotNull
	@Column(nullable = false, unique = true)
	private String username;

	@Column(nullable = false)
	private String password;

	@Column(name = "is_email_verified", nullable = false)
	private boolean emailVerified;

	@Convert(disableConversion = true)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(columnDefinition = "user_status", nullable = false)
	private UserStatus status;

	@Column(nullable = false)
	private boolean enabled;

	@Column(name = "account_non_expired", nullable = false)
	private boolean accountNonExpired;

	@Column(name = "account_non_locked", nullable = false)
	private boolean accountNonLocked;

	@Column(name = "credentials_non_expired", nullable = false)
	private boolean credentialsNonExpired;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private Set<UserRole> userRoles = new HashSet<>();

	@Builder
	public User(String email, String username, String password) {
		this.email = email;
		this.username = username;
		this.password = password;
		this.externalId = UUID.randomUUID();
		this.emailVerified = false;
		this.status = UserStatus.UNAPPROVED;
		this.enabled = true;
		this.accountNonExpired = true;
		this.accountNonLocked = true;
		this.credentialsNonExpired = true;
	}
}

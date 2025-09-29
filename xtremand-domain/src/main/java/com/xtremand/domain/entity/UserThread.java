package com.xtremand.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_threads")
@Getter
@Setter
public class UserThread {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_threads_seq")
	@SequenceGenerator(name = "user_threads_seq", sequenceName = "user_threads_id_seq", allocationSize = 1)
	private Long id;

	@Column(name = "user_email", length = 100)
	private String userEmail;

	@Column(name = "thread_id")
	private String threadId;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

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

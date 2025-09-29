package com.xtremand.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "rag_sessions")
@Getter
@Setter
public class RagSession {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rag_session_seq")
	@SequenceGenerator(name = "rag_session_seq", sequenceName = "rag_session_id_seq", allocationSize = 1)
	private Long id;

	@Column(name = "vector_store_id", length = 100)
	private String vectorStoreId;

	@Column(name = "document_ingest_id", length = 100)
	private String documentIngestId;

	@Column(name = "assistant_id", length = 100)
	private String assistantId;

	@Column(name = "thread_id", length = 100)
	private String threadId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@ManyToOne
	@JoinColumn(name = "created_by")
	private User createdBy;
	
	@ManyToOne
	@JoinColumn(name = "configured_by")
	private AiConfig config;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

}

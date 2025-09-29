package com.xtremand.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "integrations")
@Getter
@Setter
public class IntegrationKeys {

	@Id
	@SequenceGenerator(name = "ai_configurations_seq_gen", sequenceName = "ai_configurations_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ai_configurations_seq_gen")
	private Long id;

	@Column(name = "key")
	private String key;

	@Column(name = "value")
	private String value;

}

package com.xtremand.domain.entity;

import java.time.LocalDate;

import com.xtremand.domain.enums.EmailCategory;
import com.xtremand.domain.enums.EmailCategoryType;
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

@Entity
@Table(name = "email_templates")
@Getter
@Setter
public class EmailTemplate {
	@Id
	@SequenceGenerator(name = "email_templates_seq_gen", sequenceName = "email_templates_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "email_templates_seq_gen")
	private Long id;

	@Column(name="name")
	private String name;
	
	@Column(name="category")
	@Convert(converter = EmailCategoryType.class)
	private EmailCategory category;
	
	@Column(name="subject")
	private String subjectLine;

	@Column(name="body")
	private String content;

	@Column(name="variables")
	private String variables;
	
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;
    
    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

}

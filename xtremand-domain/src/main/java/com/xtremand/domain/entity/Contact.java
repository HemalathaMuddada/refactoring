package com.xtremand.domain.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "xt_contacts")
public class Contact {

	@Id
	@SequenceGenerator(name = "xt_contacts_seq_gen", sequenceName = "xt_contacts_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_contacts_seq_gen")
	private Long id;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Column(name = "details")
    private String details;

    @Column(name = "company")
    private String company;

    @Column(name = "email")
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "location")
    private String location;

    @Column(name = "phone")
    private String phone;

    @Column(name = "tags")
    private String tags;
    
    @Column(name = "is_active")
	private boolean isActive;
    
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    
    @ManyToMany(mappedBy = "contacts")
    private Set<ContactList> contactLists = new HashSet<>();
    
    @PrePersist
    public void prePersist() {
        LocalDate now = LocalDate.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDate.now();
    }

    public static class AuditListener {
        @PrePersist
        public void setCreatedAt(Contact contact) {
            LocalDate now = LocalDate.now();
            contact.setCreatedAt(now);
            contact.setUpdatedAt(now);
        }
        @PreUpdate
        public void setUpdatedAt(Contact contact) {
            contact.setUpdatedAt(LocalDate.now());
        }
    }
}

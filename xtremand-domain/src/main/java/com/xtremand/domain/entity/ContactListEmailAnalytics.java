package com.xtremand.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "contact_list_email_analytics")
public class ContactListEmailAnalytics {

    @Id
	@SequenceGenerator(name = "contact_list_email_analytics_seq_gen", sequenceName = "contact_list_email_analytics_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_list_email_analytics_seq_gen")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_list_id")
    private ContactList contactList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analytics_id")
    private EmailAnalytics emailAnalytics;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    private Contact contact;
    
    @Column(name = "new_email")
    private String newEmail;
    
    @Column(name = "type")
    private String type;


}

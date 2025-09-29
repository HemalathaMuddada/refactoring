package com.xtremand.contact.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xtremand.domain.entity.Contact;


@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
	
	Contact findByEmailIgnoreCase (String email);
}
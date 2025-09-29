package com.xtremand.contact.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.xtremand.domain.entity.ContactList;


@Repository
public interface ContactListRepository extends JpaRepository<ContactList, Long>, JpaSpecificationExecutor<ContactList> {
	boolean existsByNameIgnoreCase(String name);

}



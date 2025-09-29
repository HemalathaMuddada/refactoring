package com.xtremand.domain.dto;

import java.util.List;

import lombok.Data;

@Data
public class ContactListDto {
    private Long id;
    private String name;
    private String description;
    private List<ContactDto> contacts;
    private int contactCount;
    
    public ContactListDto() {
    	super();
    }
    
    public ContactListDto(Long id, String name, String description, List<ContactDto> contacts) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.contacts = contacts;
    }
    
    public ContactListDto(Long id, String name, String description, int contactCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.contactCount = contactCount;
    }

}

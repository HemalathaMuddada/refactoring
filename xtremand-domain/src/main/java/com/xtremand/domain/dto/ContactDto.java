package com.xtremand.domain.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.xtremand.domain.entity.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactDto {
	
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String jobTitle;
    private String company;
    private String location;
    private String tags;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private boolean isActive;
    private String details;

}

package com.xtremand.sms.service;


import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Service;

import com.xtremand.contact.service.ContactService;
import com.xtremand.domain.dto.ContactDto;
import com.xtremand.domain.dto.SendSmsRequest;

import java.util.HashMap;
import java.util.Map;

@Service
public class SmsService {

    private final ProducerTemplate producerTemplate;
    private final ContactService contactService;

    public SmsService(ProducerTemplate producerTemplate, ContactService contactService) {
        this.producerTemplate = producerTemplate;
        this.contactService = contactService;
    }

    public void sendBulkSms(SendSmsRequest request) {
        for (Long contactId : request.getContactIds()) {
            ContactDto contact = contactService.getContactById(contactId,null);
            if (contact != null && contact.getPhone() != null && !contact.getPhone().isEmpty()) {
                String personalizedBody = personalizeBody(request.getBody(), contact);
                sendSms(personalizedBody, contact.getPhone());
            }
        }
    }

    private String personalizeBody(String body, ContactDto contact) {
        return body.replace("{{firstName}}", contact.getFirstName())
                .replace("{{lastName}}", contact.getLastName())
                .replace("{{phone}}", contact.getPhone());
    }

    private void sendSms(String body, String to) {
    	 if (!to.startsWith("+")) {
    	        to = "+91" + to;
    	    }
        Map<String, Object> headers = new HashMap<>();
        headers.put("To", to);
        producerTemplate.sendBodyAndHeaders("direct:sendSms", body, headers);
    }
}
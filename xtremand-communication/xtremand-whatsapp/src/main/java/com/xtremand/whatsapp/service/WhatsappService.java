package com.xtremand.whatsapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.xtremand.contact.service.ContactService;
import com.xtremand.domain.dto.ContactDto;
import com.xtremand.domain.dto.SendWhatsappRequest;

import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsappService {
	
    private final RestTemplate restTemplate;
    private final ContactService contactService;

    
    public WhatsappService(RestTemplate restTemplate,ContactService contactService) {
        this.restTemplate = restTemplate;
        this.contactService = contactService;
    }

//	@Autowired
//	private ContactService contactService;

	@Value("${whatsapp.api.url}")
	private String whatsappApiUrl;

	@Value("${whatsapp.api.token}")
	private String accessToken;

	public String sendMessage(SendWhatsappRequest sendWhatsappRequest) {
	    StringBuilder result = new StringBuilder();
	    for (Long contactId : sendWhatsappRequest.getContactIds()) {
	        ContactDto contact = contactService.getContactById(contactId,null);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);
	        headers.setBearerAuth(accessToken);

	        Map<String, Object> payload = new HashMap<>();
	        payload.put("messaging_product", "whatsapp");
	        payload.put("to", contact.getPhone());
	        payload.put("type", "text");
	        payload.put("text", Map.of("body", sendWhatsappRequest.getBody()));

	        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

	        ResponseEntity<String> response = restTemplate.postForEntity(whatsappApiUrl, entity, String.class);
	        result.append("Contact: ").append(contact.getPhone())
	        .append(" - Message queued/sent successfully (simulation)")
            .append("\n");
	    }
	    return result.toString();
	}

}

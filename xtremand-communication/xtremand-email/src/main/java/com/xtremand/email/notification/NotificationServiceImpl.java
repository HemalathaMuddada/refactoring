package com.xtremand.email.notification;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.xtremand.common.email.notification.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Value("${email.username}")
    private String fromEmail;
    
    @Value("${email.password}")
    private String smtpPassword;

    private final ProducerTemplate producerTemplate;

    public NotificationServiceImpl(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    @Override
    public void sendActivationEmail(String to, String activationLink) {
        String subject = "Activate your account";
        String body = "Please click the following link to activate your account: " + activationLink;
        Map<String, Object> headers = new HashMap<>();
        headers.put("To", to);
        headers.put("From", fromEmail);
        headers.put("Subject", subject);
        headers.put("Content-Type", "text/html; charset=UTF-8");
        headers.put("CamelSmtpUsername", fromEmail);    
        headers.put("CamelSmtpPassword", smtpPassword); 
        producerTemplate.sendBodyAndHeaders("direct:sendEmail", body, headers);
    }
}

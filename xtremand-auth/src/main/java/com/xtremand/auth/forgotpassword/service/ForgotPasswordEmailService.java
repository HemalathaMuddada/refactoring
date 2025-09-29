package com.xtremand.auth.forgotpassword.service;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import org.apache.camel.ProducerTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class ForgotPasswordEmailService {

    @Value("${email.username}")
    private String fromEmail;
    
    @Value("${email.password}")
    private String smtpPassword;

    private final ProducerTemplate producerTemplate;

    public ForgotPasswordEmailService(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    public void sendForgotPasswordEmail(String toEmail, String resetLink) {
        String subject = "Reset Your Password";
        String body = "Please click the following link to reset your password: " + resetLink;

        Map<String, Object> headers = new HashMap<>();
        headers.put("To", toEmail);
        headers.put("From", fromEmail);
        headers.put("Subject", subject);
        headers.put("Content-Type", "text/html; charset=UTF-8");
        headers.put("CamelSmtpUsername", fromEmail);    
        headers.put("CamelSmtpPassword", smtpPassword); 
        producerTemplate.sendBodyAndHeaders("direct:sendEmail", body, headers);
    }
}

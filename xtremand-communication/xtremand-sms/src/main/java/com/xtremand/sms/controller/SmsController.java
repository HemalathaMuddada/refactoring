package com.xtremand.sms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.domain.dto.SendSmsRequest;
import com.xtremand.sms.service.SmsService;

@RestController
@RequestMapping("/sms")
public class SmsController {

    private final SmsService smsService;

    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendSms(@RequestBody SendSmsRequest request) {
        smsService.sendBulkSms(request);
        return ResponseEntity.ok().build();
    }
}
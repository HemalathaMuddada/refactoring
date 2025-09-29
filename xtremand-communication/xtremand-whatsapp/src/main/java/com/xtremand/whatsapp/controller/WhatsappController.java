package com.xtremand.whatsapp.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.xtremand.domain.dto.SendWhatsappRequest;
import com.xtremand.whatsapp.service.WhatsappService;

@RestController
@RequestMapping("/whatsapp")
public class WhatsappController {
    @Autowired
    private WhatsappService whatsappService;

    @PostMapping("/send")
    public String send(@RequestBody SendWhatsappRequest sendWhatsappRequest) {
        return whatsappService.sendMessage(sendWhatsappRequest);
    }
}

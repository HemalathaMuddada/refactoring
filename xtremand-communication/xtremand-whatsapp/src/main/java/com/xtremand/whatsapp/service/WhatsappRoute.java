package com.xtremand.whatsapp.service;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WhatsappRoute extends RouteBuilder {
	
	
	@Value("${twilio.whatsappPhoneNumber}")
	private String phoneNumber;

    @Override
    public void configure() throws Exception {
        from("direct:sendWhatsapp")
            .routeId("whatsapp-sender")
            .log("Sending WhatsApp to ${header.To}")
            .toD("twilio://message/create"
                + "&to=${header.To}"
                + "&from="+ phoneNumber
                + "&body=${body}")
            .log("WhatsApp sent to ${header.To}");
    }
}
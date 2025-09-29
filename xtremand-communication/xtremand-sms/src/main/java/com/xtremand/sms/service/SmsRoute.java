package com.xtremand.sms.service;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SmsRoute extends RouteBuilder {

    @Value("${twilio.phoneNumber}")
    private String phoneNumber;

    @Override
    public void configure() throws Exception {
        from("direct:sendSms")
            .routeId("sms-sender")
            .log("Sending SMS to ${header.To}")
            .toD("twilio://message/create"
                + "?to=${header.To}"
                + "&from=" + phoneNumber
                + "&body=${body}")
            .log("SMS sent to ${header.To}");
    }
}

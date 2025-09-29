package com.xtremand.email.service;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:sendEmail")
            .log("Sending email to ${header.To} with subject ${header.Subject}")
            .process(exchange -> {
                String smtpHost = exchange.getIn().getHeader("CamelSmtpHost", String.class);
                String smtpPort = exchange.getIn().getHeader("CamelSmtpPort", String.class);
                String username = exchange.getIn().getHeader("CamelSmtpUsername", String.class);
                String password = exchange.getIn().getHeader("CamelSmtpPassword", String.class);
                if (smtpHost == null) smtpHost = "smtp.gmail.com";
                if (smtpPort == null) smtpPort = "465";
                String smtpEndpoint = String.format(
                    "smtps://%s:%s?username=%s&password=%s&mail.smtp.auth=true&mail.smtp.starttls.enable=true",
                    smtpHost, smtpPort, username, password);

                exchange.getIn().setHeader("CamelToEndpoint", smtpEndpoint);
            })
            .toD("${header.CamelToEndpoint}");
    }
}
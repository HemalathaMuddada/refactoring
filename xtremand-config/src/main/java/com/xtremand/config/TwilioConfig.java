package com.xtremand.config;
import com.twilio.type.PhoneNumber;
import org.apache.camel.CamelContext;
import org.apache.camel.component.twilio.TwilioComponent;
import org.apache.camel.component.twilio.internal.TwilioApiName;
import org.apache.camel.component.twilio.internal.TwilioConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioConfig {

    @Value("${twilio.accountSid}")
    private String accountSid;

    @Value("${twilio.authToken}")
    private String authToken;

    @Bean("twilio")
    public TwilioComponent twilioComponent(CamelContext camelContext) {
        System.out.println("TwilioConfig - accountSid=" + accountSid);
        System.out.println("TwilioConfig - authToken=" + authToken);

        if (accountSid == null || accountSid.startsWith("{") || authToken == null || authToken.startsWith("{")) {
            throw new IllegalStateException("Twilio accountSid and authToken must be configured in application.properties");
        }
        TwilioComponent component = new TwilioComponent(camelContext);
        component.setAccountSid(accountSid);
        component.setPassword(authToken);
        return component;
    }

}
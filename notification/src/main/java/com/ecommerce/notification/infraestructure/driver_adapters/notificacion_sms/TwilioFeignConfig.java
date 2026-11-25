package com.ecommerce.notification.infraestructure.driver_adapters.notificacion_sms;

import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

// Sin @Configuration a proposito: asi esta autenticacion solo aplica al TwilioClient
public class TwilioFeignConfig {

    // Twilio usa Basic Auth: usuario = Account SID, contrasena = Auth Token
    @Bean
    public BasicAuthRequestInterceptor twilioBasicAuth(@Value("${twilio.account-sid}") String accountSid,
                                                       @Value("${twilio.auth-token}") String authToken) {
        return new BasicAuthRequestInterceptor(accountSid, authToken);
    }
}

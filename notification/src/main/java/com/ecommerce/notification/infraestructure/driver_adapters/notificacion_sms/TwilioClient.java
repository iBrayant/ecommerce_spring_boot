package com.ecommerce.notification.infraestructure.driver_adapters.notificacion_sms;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@FeignClient(name = "twilio", url = "${twilio.api-url}", configuration = TwilioFeignConfig.class)
public interface TwilioClient {

    // Twilio recibe un formulario (x-www-form-urlencoded) con To, From y Body, no un JSON
    @PostMapping(value = "/2010-04-01/Accounts/{sid}/Messages.json",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    TwilioResponse enviarMensaje(@PathVariable("sid") String accountSid, Map<String, ?> formulario);
}

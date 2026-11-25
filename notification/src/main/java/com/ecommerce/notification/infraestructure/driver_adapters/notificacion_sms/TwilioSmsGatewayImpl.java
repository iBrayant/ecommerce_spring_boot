package com.ecommerce.notification.infraestructure.driver_adapters.notificacion_sms;

import com.ecommerce.notification.domain.exception.ServicioExternoException;
import com.ecommerce.notification.domain.model.gateway.SmsGateway;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TwilioSmsGatewayImpl implements SmsGateway {

    private final TwilioClient twilioClient;
    private final String accountSid;
    private final String fromNumber;

    public TwilioSmsGatewayImpl(TwilioClient twilioClient,
                                @Value("${twilio.account-sid}") String accountSid,
                                @Value("${twilio.from-number}") String fromNumber) {
        this.twilioClient = twilioClient;
        this.accountSid = accountSid;
        this.fromNumber = fromNumber;
    }

    @Override
    public String enviarSms(String telefono, String mensaje) {
        Map<String, String> formulario = Map.of(
                "To", telefono,
                "From", fromNumber,
                "Body", mensaje);
        try {
            TwilioResponse respuesta = twilioClient.enviarMensaje(accountSid, formulario);
            if (respuesta == null || respuesta.getSid() == null) {
                throw new ServicioExternoException("Twilio devolvio una respuesta vacia");
            }
            return respuesta.getSid();
        } catch (FeignException.Unauthorized e) {
            throw new ServicioExternoException("Twilio rechazo las credenciales: revisa twilio.account-sid y twilio.auth-token", e);
        } catch (FeignException e) {
            // Twilio explica el error en el cuerpo, ej: numero no verificado en cuenta de prueba
            throw new ServicioExternoException("Twilio rechazo el mensaje (" + e.status() + "): " + e.contentUTF8(), e);
        }
    }
}

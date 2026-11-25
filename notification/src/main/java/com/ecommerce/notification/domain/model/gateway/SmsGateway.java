package com.ecommerce.notification.domain.model.gateway;

public interface SmsGateway {

    // Devuelve el ID que el proveedor le asigna al mensaje
    String enviarSms(String telefono, String mensaje);
}

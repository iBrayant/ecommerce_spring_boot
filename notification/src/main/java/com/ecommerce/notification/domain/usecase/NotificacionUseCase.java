package com.ecommerce.notification.domain.usecase;

import com.ecommerce.notification.domain.model.CanalNotificacion;
import com.ecommerce.notification.domain.model.Notificacion;
import com.ecommerce.notification.domain.model.ResultadoNotificacion;
import com.ecommerce.notification.domain.model.gateway.SmsGateway;

public class NotificacionUseCase {

    public static final String ACEPTADA_POR_PROVEEDOR = "ACEPTADA_POR_PROVEEDOR";

    private final SmsGateway smsGateway;

    public NotificacionUseCase(SmsGateway smsGateway) {
        this.smsGateway = smsGateway;
    }

    // Un solo metodo para todos los canales: segun el canal se escoge el puerto
    public ResultadoNotificacion enviarNotificacion(Notificacion notificacion) {
        if (notificacion == null || notificacion.getMensaje() == null || notificacion.getMensaje().isBlank()) {
            throw new IllegalArgumentException("El mensaje es obligatorio");
        }
        if (notificacion.getCanal() == null) {
            throw new IllegalArgumentException("El canal de notificacion es obligatorio");
        }

        if (notificacion.getCanal() == CanalNotificacion.SMS) {
            String telefono = normalizarTelefono(notificacion.getTelefono());
            String idProveedor = smsGateway.enviarSms(telefono, notificacion.getMensaje());
            return new ResultadoNotificacion(ACEPTADA_POR_PROVEEDOR, CanalNotificacion.SMS, idProveedor,
                    "El proveedor acepto el mensaje");
        }

        // EMAIL, WhatsApp, etc. todavia no tienen implementacion
        throw new IllegalArgumentException("Canal de notificacion invalido: " + notificacion.getCanal());
    }

    // Quita espacios, guiones y parentesis: "+57 300-123 4567" -> "+573001234567"
    private String normalizarTelefono(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("El telefono es obligatorio para enviar un SMS");
        }
        String normalizado = telefono.replaceAll("[\\s\\-()]", "");
        if (!normalizado.matches("\\+\\d{8,15}")) {
            throw new IllegalArgumentException("El telefono debe tener indicativo y solo numeros. Ej: +573001234567");
        }
        return normalizado;
    }
}

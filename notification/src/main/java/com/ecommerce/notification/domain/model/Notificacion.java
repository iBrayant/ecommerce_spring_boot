package com.ecommerce.notification.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Notificacion {
    // Motivo de la notificacion: REGISTRO, LOGIN, COMPRA...
    private String tipo;
    // Correo del destinatario
    private String email;
    // Celular del destinatario, con indicativo. Ej: +573001234567
    private String telefono;
    private String mensaje;
    private CanalNotificacion canal;
}

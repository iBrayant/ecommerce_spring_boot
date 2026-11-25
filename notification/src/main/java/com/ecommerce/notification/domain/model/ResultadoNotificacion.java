package com.ecommerce.notification.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ResultadoNotificacion {
    private String estado;
    private CanalNotificacion canal;
    // ID que devuelve el proveedor (Twilio) para hacerle trazabilidad al envio
    private String idProveedor;
    private String detalle;
}

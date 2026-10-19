package com.ecommerce.auth.domain.model;

//OJO//
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
//OJO//

import java.math.BigDecimal;

@NoArgsConstructor

@AllArgsConstructor
@Getter
@Setter

public class Usuario {
    private String idUsuario;
    private String nombre;
    private String correo;
    private String clave;
    private String rol;
    private String numeroTelefonico;
    private Integer edad;
    private BigDecimal saldo;

}

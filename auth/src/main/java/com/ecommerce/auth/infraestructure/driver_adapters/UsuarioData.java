package com.ecommerce.auth.infraestructure.driver_adapters;

import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table (name="usuarios")

public class UsuarioData {
    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    private String idUsuario;

    private String nombre;

    @Column(length = 30, nullable = false, unique = true)
    private String correo;
    @Column(length = 60, nullable = false)
    private String clave;
    private String rol;
    private String numeroTelefonico;
    private Integer edad;

    @Column(precision = 14, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

}

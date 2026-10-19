package com.ecommerce.catalogo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Carrito {
    private Long id;
    private String usuarioId;
    private EstadoCarrito estado;
    private BigDecimal total = BigDecimal.ZERO;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaCompra;
    private List<ItemCarrito> items = new ArrayList<>();
}

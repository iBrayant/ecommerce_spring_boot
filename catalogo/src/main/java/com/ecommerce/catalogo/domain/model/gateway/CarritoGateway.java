package com.ecommerce.catalogo.domain.model.gateway;

import com.ecommerce.catalogo.domain.model.Carrito;
import com.ecommerce.catalogo.domain.model.EstadoCarrito;

public interface CarritoGateway {

    Carrito guardar(Carrito carrito);

    Carrito buscarActivoPorUsuario(String usuarioId);

    // Cambia el estado solo si el carrito esta actualmente en el estado "desde". Devuelve false si no lo estaba.
    boolean cambiarEstado(Long carritoId, EstadoCarrito desde, EstadoCarrito hasta);

    // Descuenta el stock de todos los items y guarda el carrito como comprado, todo en una sola transaccion.
    Carrito confirmarCompra(Carrito carrito);
}

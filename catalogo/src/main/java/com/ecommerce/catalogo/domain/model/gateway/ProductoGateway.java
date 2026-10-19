package com.ecommerce.catalogo.domain.model.gateway;

import com.ecommerce.catalogo.domain.model.Producto;

import java.util.List;

public interface ProductoGateway {

    Producto guardar(Producto producto);

    Producto buscarPorId(Long id);

    List<Producto> listar();

    void eliminar(Long id);
}

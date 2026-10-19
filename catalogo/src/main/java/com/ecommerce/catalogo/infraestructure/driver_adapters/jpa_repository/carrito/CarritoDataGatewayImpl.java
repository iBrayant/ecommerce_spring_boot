package com.ecommerce.catalogo.infraestructure.driver_adapters.jpa_repository.carrito;

import com.ecommerce.catalogo.domain.exception.ReglaNegocioException;
import com.ecommerce.catalogo.domain.model.Carrito;
import com.ecommerce.catalogo.domain.model.EstadoCarrito;
import com.ecommerce.catalogo.domain.model.ItemCarrito;
import com.ecommerce.catalogo.domain.model.gateway.CarritoGateway;
import com.ecommerce.catalogo.infraestructure.driver_adapters.jpa_repository.producto.ProductoDataJpaRepository;
import com.ecommerce.catalogo.infraestructure.mapper.MapperCarrito;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class CarritoDataGatewayImpl implements CarritoGateway {

    private final CarritoDataJpaRepository repository;
    private final ProductoDataJpaRepository productoRepository;
    private final MapperCarrito mapper;

    @Override
    public Carrito guardar(Carrito carrito) {
        return mapper.toCarrito(repository.save(mapper.toData(carrito)));
    }

    @Override
    public Carrito buscarActivoPorUsuario(String usuarioId) {
        return repository.findFirstByUsuarioIdAndEstado(usuarioId, EstadoCarrito.ACTIVO)
                .map(mapper::toCarrito)
                .orElse(null);
    }

    @Override
    @Transactional
    public boolean cambiarEstado(Long carritoId, EstadoCarrito desde, EstadoCarrito hasta) {
        return repository.cambiarEstado(carritoId, desde, hasta) > 0;
    }

    // Si algun producto no tiene stock suficiente se lanza la excepcion y se revierte todo lo descontado
    @Override
    @Transactional
    public Carrito confirmarCompra(Carrito carrito) {
        for (ItemCarrito item : carrito.getItems()) {
            int filas = productoRepository.descontarStock(item.getProductoId(), item.getCantidad());
            if (filas == 0) {
                throw new ReglaNegocioException("Stock insuficiente para el producto " + item.getProductoId());
            }
        }
        return mapper.toCarrito(repository.save(mapper.toData(carrito)));
    }
}

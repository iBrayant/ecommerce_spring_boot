package com.ecommerce.catalogo.domain.usecase;

import com.ecommerce.catalogo.domain.exception.RecursoNoEncontradoException;
import com.ecommerce.catalogo.domain.model.Producto;
import com.ecommerce.catalogo.domain.model.gateway.ProductoGateway;
import lombok.RequiredArgsConstructor;

import java.math.RoundingMode;
import java.util.List;

@RequiredArgsConstructor
public class ProductoUseCase {

    private final ProductoGateway productoGateway;

    public Producto crearProducto(Producto producto) {
        validarProducto(producto);
        producto.setId(null);
        producto.setPrecio(producto.getPrecio().setScale(2, RoundingMode.HALF_UP));
        return productoGateway.guardar(producto);
    }

    public Producto obtenerProducto(Long id) {
        Producto producto = productoGateway.buscarPorId(id);
        if (producto == null) {
            throw new RecursoNoEncontradoException("Producto no encontrado con id: " + id);
        }
        return producto;
    }

    public List<Producto> listarProductos() {
        return productoGateway.listar();
    }

    public Producto actualizarProducto(Long id, Producto producto) {
        obtenerProducto(id);
        validarProducto(producto);
        producto.setId(id);
        producto.setPrecio(producto.getPrecio().setScale(2, RoundingMode.HALF_UP));
        return productoGateway.guardar(producto);
    }

    public void eliminarProducto(Long id) {
        obtenerProducto(id);
        productoGateway.eliminar(id);
    }

    private void validarProducto(Producto producto) {
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (producto.getPrecio() == null || producto.getPrecio().signum() <= 0) {
            throw new IllegalArgumentException("El precio del producto debe ser mayor que cero");
        }
        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock del producto no puede ser negativo");
        }
    }
}

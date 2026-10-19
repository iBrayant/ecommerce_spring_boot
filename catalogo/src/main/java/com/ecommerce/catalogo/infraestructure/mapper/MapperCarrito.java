package com.ecommerce.catalogo.infraestructure.mapper;

import com.ecommerce.catalogo.domain.model.Carrito;
import com.ecommerce.catalogo.domain.model.ItemCarrito;
import com.ecommerce.catalogo.infraestructure.driver_adapters.jpa_repository.carrito.CarritoData;
import com.ecommerce.catalogo.infraestructure.driver_adapters.jpa_repository.carrito.ItemCarritoData;
import com.ecommerce.catalogo.infraestructure.driver_adapters.jpa_repository.producto.ProductoData;
import org.springframework.stereotype.Component;

@Component
public class MapperCarrito {

    public CarritoData toData(Carrito carrito) {
        CarritoData carritoData = new CarritoData();
        carritoData.setId(carrito.getId());
        carritoData.setUsuarioId(carrito.getUsuarioId());
        carritoData.setEstado(carrito.getEstado());
        carritoData.setTotal(carrito.getTotal());
        carritoData.setFechaCreacion(carrito.getFechaCreacion());
        carritoData.setFechaCompra(carrito.getFechaCompra());

        for (ItemCarrito item : carrito.getItems()) {
            // Solo se necesita el id del producto para armar la relacion
            ProductoData producto = new ProductoData();
            producto.setId(item.getProductoId());

            ItemCarritoData itemData = new ItemCarritoData();
            itemData.setId(item.getId());
            itemData.setCarrito(carritoData);
            itemData.setProducto(producto);
            itemData.setCantidad(item.getCantidad());
            itemData.setPrecioUnitario(item.getPrecioUnitario());
            itemData.setSubtotal(item.getSubtotal());
            carritoData.getItems().add(itemData);
        }
        return carritoData;
    }

    public Carrito toCarrito(CarritoData carritoData) {
        Carrito carrito = new Carrito();
        carrito.setId(carritoData.getId());
        carrito.setUsuarioId(carritoData.getUsuarioId());
        carrito.setEstado(carritoData.getEstado());
        carrito.setTotal(carritoData.getTotal());
        carrito.setFechaCreacion(carritoData.getFechaCreacion());
        carrito.setFechaCompra(carritoData.getFechaCompra());

        for (ItemCarritoData itemData : carritoData.getItems()) {
            ItemCarrito item = new ItemCarrito();
            item.setId(itemData.getId());
            item.setProductoId(itemData.getProducto().getId());
            item.setCantidad(itemData.getCantidad());
            item.setPrecioUnitario(itemData.getPrecioUnitario());
            item.setSubtotal(itemData.getSubtotal());
            carrito.getItems().add(item);
        }
        return carrito;
    }
}

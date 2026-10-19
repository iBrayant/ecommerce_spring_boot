package com.ecommerce.catalogo.domain.usecase;

import com.ecommerce.catalogo.domain.exception.RecursoNoEncontradoException;
import com.ecommerce.catalogo.domain.exception.ReglaNegocioException;
import com.ecommerce.catalogo.domain.model.Carrito;
import com.ecommerce.catalogo.domain.model.CompraResultado;
import com.ecommerce.catalogo.domain.model.EstadoCarrito;
import com.ecommerce.catalogo.domain.model.ItemCarrito;
import com.ecommerce.catalogo.domain.model.Producto;
import com.ecommerce.catalogo.domain.model.gateway.CarritoGateway;
import com.ecommerce.catalogo.domain.model.gateway.UsuarioGateway;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class CarritoUseCase {

    private final CarritoGateway carritoGateway;
    private final ProductoUseCase productoUseCase;
    private final UsuarioGateway usuarioGateway;

    public Carrito agregarProducto(String usuarioId, Long productoId, Integer cantidad) {
        validarCantidad(cantidad);
        if (!usuarioGateway.usuarioExiste(usuarioId)) {
            throw new RecursoNoEncontradoException("Usuario no encontrado con id: " + usuarioId);
        }
        Producto producto = productoUseCase.obtenerProducto(productoId);

        Carrito carrito = carritoGateway.buscarActivoPorUsuario(usuarioId);
        if (carrito == null) {
            carrito = new Carrito();
            carrito.setUsuarioId(usuarioId);
            carrito.setEstado(EstadoCarrito.ACTIVO);
            carrito.setFechaCreacion(LocalDateTime.now());
        }

        ItemCarrito item = buscarItem(carrito, productoId);
        int cantidadTotal = cantidad + (item == null ? 0 : item.getCantidad());
        validarStock(producto, cantidadTotal);

        if (item == null) {
            item = new ItemCarrito();
            item.setProductoId(productoId);
            carrito.getItems().add(item);
        }
        item.setCantidad(cantidadTotal);
        item.setPrecioUnitario(producto.getPrecio());
        calcularTotales(carrito);

        return carritoGateway.guardar(carrito);
    }

    public Carrito verCarrito(String usuarioId) {
        return obtenerCarritoActivo(usuarioId);
    }

    public Carrito cambiarCantidad(String usuarioId, Long productoId, Integer cantidad) {
        validarCantidad(cantidad);
        Carrito carrito = obtenerCarritoActivo(usuarioId);
        ItemCarrito item = obtenerItem(carrito, productoId);
        Producto producto = productoUseCase.obtenerProducto(productoId);

        validarStock(producto, cantidad);
        item.setCantidad(cantidad);
        item.setPrecioUnitario(producto.getPrecio());
        calcularTotales(carrito);

        return carritoGateway.guardar(carrito);
    }

    public Carrito retirarProducto(String usuarioId, Long productoId) {
        Carrito carrito = obtenerCarritoActivo(usuarioId);
        ItemCarrito item = obtenerItem(carrito, productoId);

        carrito.getItems().remove(item);
        calcularTotales(carrito);

        return carritoGateway.guardar(carrito);
    }

    public CompraResultado comprar(String usuarioId) {
        Carrito carrito = obtenerCarritoActivo(usuarioId);
        if (carrito.getItems().isEmpty()) {
            throw new IllegalArgumentException("El carrito esta vacio, agregue productos antes de comprar");
        }

        // El usuario debe existir en Auth y se consulta su saldo
        BigDecimal saldo = usuarioGateway.consultarSaldo(usuarioId);

        // Se vuelven a calcular los precios en el servidor y se valida el stock de cada producto
        for (ItemCarrito item : carrito.getItems()) {
            Producto producto = productoUseCase.obtenerProducto(item.getProductoId());
            validarStock(producto, item.getCantidad());
            item.setPrecioUnitario(producto.getPrecio());
        }
        calcularTotales(carrito);

        if (saldo.compareTo(carrito.getTotal()) < 0) {
            throw new ReglaNegocioException("Saldo insuficiente. Saldo disponible: " + saldo
                    + ", total de la compra: " + carrito.getTotal());
        }

        // Se bloquea el carrito para que la misma compra no se pueda ejecutar dos veces
        if (!carritoGateway.cambiarEstado(carrito.getId(), EstadoCarrito.ACTIVO, EstadoCarrito.EN_PROCESO)) {
            throw new ReglaNegocioException("La compra de este carrito ya esta en proceso o ya fue realizada");
        }

        BigDecimal saldoRestante;
        try {
            saldoRestante = usuarioGateway.descontarSaldo(usuarioId, carrito.getTotal());
        } catch (RuntimeException e) {
            carritoGateway.cambiarEstado(carrito.getId(), EstadoCarrito.EN_PROCESO, EstadoCarrito.ACTIVO);
            throw e;
        }

        try {
            carrito.setEstado(EstadoCarrito.COMPRADO);
            carrito.setFechaCompra(LocalDateTime.now());
            Carrito carritoComprado = carritoGateway.confirmarCompra(carrito);
            return new CompraResultado("La compra fue realizada exitosamente", saldoRestante, carritoComprado);
        } catch (RuntimeException e) {
            // Si falla el stock o el guardado, se le devuelve el dinero al usuario y el carrito vuelve a estar activo
            devolverSaldo(usuarioId, carrito.getTotal());
            carritoGateway.cambiarEstado(carrito.getId(), EstadoCarrito.EN_PROCESO, EstadoCarrito.ACTIVO);
            throw e;
        }
    }

    private void devolverSaldo(String usuarioId, BigDecimal monto) {
        try {
            usuarioGateway.devolverSaldo(usuarioId, monto);
        } catch (RuntimeException e) {
            // Si la devolucion falla se ignora para no tapar el error original de la compra
        }
    }

    private Carrito obtenerCarritoActivo(String usuarioId) {
        Carrito carrito = carritoGateway.buscarActivoPorUsuario(usuarioId);
        if (carrito == null) {
            throw new RecursoNoEncontradoException("El usuario " + usuarioId + " no tiene un carrito activo");
        }
        return carrito;
    }

    private ItemCarrito buscarItem(Carrito carrito, Long productoId) {
        return carrito.getItems().stream()
                .filter(item -> item.getProductoId().equals(productoId))
                .findFirst()
                .orElse(null);
    }

    private ItemCarrito obtenerItem(Carrito carrito, Long productoId) {
        ItemCarrito item = buscarItem(carrito, productoId);
        if (item == null) {
            throw new RecursoNoEncontradoException("El producto " + productoId + " no esta en el carrito");
        }
        return item;
    }

    private void validarCantidad(Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
    }

    private void validarStock(Producto producto, int cantidad) {
        if (cantidad > producto.getStock()) {
            throw new ReglaNegocioException("Stock insuficiente para '" + producto.getNombre()
                    + "'. Disponible: " + producto.getStock() + ", solicitado: " + cantidad);
        }
    }

    private void calcularTotales(Carrito carrito) {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemCarrito item : carrito.getItems()) {
            BigDecimal subtotal = item.getPrecioUnitario()
                    .multiply(BigDecimal.valueOf(item.getCantidad()))
                    .setScale(2, RoundingMode.HALF_UP);
            item.setSubtotal(subtotal);
            total = total.add(subtotal);
        }
        carrito.setTotal(total);
    }
}

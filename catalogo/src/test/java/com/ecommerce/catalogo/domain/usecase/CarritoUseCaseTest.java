package com.ecommerce.catalogo.domain.usecase;

import com.ecommerce.catalogo.domain.exception.ReglaNegocioException;
import com.ecommerce.catalogo.domain.model.Carrito;
import com.ecommerce.catalogo.domain.model.CompraResultado;
import com.ecommerce.catalogo.domain.model.EstadoCarrito;
import com.ecommerce.catalogo.domain.model.ItemCarrito;
import com.ecommerce.catalogo.domain.model.Producto;
import com.ecommerce.catalogo.domain.model.gateway.CarritoGateway;
import com.ecommerce.catalogo.domain.model.gateway.ProductoGateway;
import com.ecommerce.catalogo.domain.model.gateway.UsuarioGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CarritoUseCaseTest {

    private static final String USUARIO = "usuario-1";

    @Mock
    private CarritoGateway carritoGateway;
    @Mock
    private ProductoGateway productoGateway;
    @Mock
    private UsuarioGateway usuarioGateway;

    private CarritoUseCase carritoUseCase;

    @BeforeEach
    void setUp() {
        carritoUseCase = new CarritoUseCase(carritoGateway, new ProductoUseCase(productoGateway), usuarioGateway);

        // Caso del PDF: mouse x2 ($45.000) y audifonos x1 ($80.000)
        when(productoGateway.buscarPorId(1L)).thenReturn(
                new Producto(1L, "Mouse inalambrico", "", new BigDecimal("45000.00"), 10, null));
        when(productoGateway.buscarPorId(2L)).thenReturn(
                new Producto(2L, "Audifonos Bluetooth", "", new BigDecimal("80000.00"), 5, null));
        when(usuarioGateway.usuarioExiste(USUARIO)).thenReturn(true);
        when(carritoGateway.guardar(any(Carrito.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(carritoGateway.cambiarEstado(1L, EstadoCarrito.ACTIVO, EstadoCarrito.EN_PROCESO)).thenReturn(true);
        when(carritoGateway.confirmarCompra(any(Carrito.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private Carrito carritoConMouseYAudifonos() {
        Carrito carrito = new Carrito();
        carrito.setId(1L);
        carrito.setUsuarioId(USUARIO);
        carrito.setEstado(EstadoCarrito.ACTIVO);
        carrito.setItems(new ArrayList<>(List.of(
                new ItemCarrito(1L, 1L, 2, new BigDecimal("45000.00"), new BigDecimal("90000.00")),
                new ItemCarrito(2L, 2L, 1, new BigDecimal("80000.00"), new BigDecimal("80000.00")))));
        when(carritoGateway.buscarActivoPorUsuario(USUARIO)).thenReturn(carrito);
        return carrito;
    }

    @Test
    void agregarElMismoProductoSumaLaCantidadSinDuplicarElItem() {
        // El carrito ya tiene 1 mouse; se agrega otro
        Carrito carrito = new Carrito();
        carrito.setUsuarioId(USUARIO);
        carrito.getItems().add(new ItemCarrito(10L, 1L, 1, new BigDecimal("45000.00"), new BigDecimal("45000.00")));
        when(carritoGateway.buscarActivoPorUsuario(USUARIO)).thenReturn(carrito);

        Carrito resultado = carritoUseCase.agregarProducto(USUARIO, 1L, 1);

        assertEquals(1, resultado.getItems().size());
        assertEquals(2, resultado.getItems().get(0).getCantidad());
        assertEquals(new BigDecimal("90000.00"), resultado.getTotal());
    }

    @Test
    void agregarMasDelStockDisponibleSeRechaza() {
        when(carritoGateway.buscarActivoPorUsuario(USUARIO)).thenReturn(null);

        assertThrows(ReglaNegocioException.class, () -> carritoUseCase.agregarProducto(USUARIO, 2L, 6));
        verify(carritoGateway, never()).guardar(any());
    }

    @Test
    void cantidadCeroOMenorSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> carritoUseCase.agregarProducto(USUARIO, 1L, 0));
        assertThrows(IllegalArgumentException.class, () -> carritoUseCase.agregarProducto(USUARIO, 1L, -3));
    }

    @Test
    void compraExitosaDescuentaElTotalYMarcaElCarritoComoComprado() {
        carritoConMouseYAudifonos();
        when(usuarioGateway.consultarSaldo(USUARIO)).thenReturn(new BigDecimal("250000.00"));
        when(usuarioGateway.descontarSaldo(USUARIO, new BigDecimal("170000.00"))).thenReturn(new BigDecimal("80000.00"));

        CompraResultado resultado = carritoUseCase.comprar(USUARIO);

        assertEquals(new BigDecimal("80000.00"), resultado.getSaldoRestante());
        assertEquals(EstadoCarrito.COMPRADO, resultado.getCarrito().getEstado());
        assertEquals(new BigDecimal("170000.00"), resultado.getCarrito().getTotal());
        verify(usuarioGateway).descontarSaldo(USUARIO, new BigDecimal("170000.00"));
        verify(carritoGateway).confirmarCompra(any(Carrito.class));
    }

    @Test
    void saldoInsuficienteRechazaLaCompraSinCobrarNiTocarElStock() {
        carritoConMouseYAudifonos();
        when(usuarioGateway.consultarSaldo(USUARIO)).thenReturn(new BigDecimal("100000.00"));

        assertThrows(ReglaNegocioException.class, () -> carritoUseCase.comprar(USUARIO));

        verify(usuarioGateway, never()).descontarSaldo(anyString(), any());
        verify(carritoGateway, never()).confirmarCompra(any());
    }

    @Test
    void stockInsuficienteRechazaLaCompraSinCobrar() {
        Carrito carrito = carritoConMouseYAudifonos();
        carrito.getItems().get(0).setCantidad(11); // el stock del mouse es 10
        when(usuarioGateway.consultarSaldo(USUARIO)).thenReturn(new BigDecimal("999999.00"));

        assertThrows(ReglaNegocioException.class, () -> carritoUseCase.comprar(USUARIO));

        verify(usuarioGateway, never()).descontarSaldo(anyString(), any());
        verify(carritoGateway, never()).confirmarCompra(any());
    }

    @Test
    void siElCarritoYaEstaEnProcesoNoSeCobraDosVeces() {
        carritoConMouseYAudifonos();
        when(usuarioGateway.consultarSaldo(USUARIO)).thenReturn(new BigDecimal("250000.00"));
        when(carritoGateway.cambiarEstado(1L, EstadoCarrito.ACTIVO, EstadoCarrito.EN_PROCESO)).thenReturn(false);

        assertThrows(ReglaNegocioException.class, () -> carritoUseCase.comprar(USUARIO));

        verify(usuarioGateway, never()).descontarSaldo(anyString(), any());
    }

    @Test
    void siFallaElStockDespuesDeCobrarSeDevuelveElSaldoYElCarritoQuedaActivo() {
        carritoConMouseYAudifonos();
        when(usuarioGateway.consultarSaldo(USUARIO)).thenReturn(new BigDecimal("250000.00"));
        when(usuarioGateway.descontarSaldo(USUARIO, new BigDecimal("170000.00"))).thenReturn(new BigDecimal("80000.00"));
        when(carritoGateway.confirmarCompra(any(Carrito.class)))
                .thenThrow(new ReglaNegocioException("Stock insuficiente para el producto 1"));

        assertThrows(ReglaNegocioException.class, () -> carritoUseCase.comprar(USUARIO));

        verify(usuarioGateway).devolverSaldo(USUARIO, new BigDecimal("170000.00"));
        verify(carritoGateway).cambiarEstado(1L, EstadoCarrito.EN_PROCESO, EstadoCarrito.ACTIVO);
    }

    @Test
    void siFallaElDescuentoDeSaldoElCarritoVuelveAActivo() {
        carritoConMouseYAudifonos();
        when(usuarioGateway.consultarSaldo(USUARIO)).thenReturn(new BigDecimal("250000.00"));
        when(usuarioGateway.descontarSaldo(anyString(), any())).thenThrow(new ReglaNegocioException("Auth fallo"));

        assertThrows(ReglaNegocioException.class, () -> carritoUseCase.comprar(USUARIO));

        verify(carritoGateway).cambiarEstado(1L, EstadoCarrito.EN_PROCESO, EstadoCarrito.ACTIVO);
        verify(carritoGateway, never()).confirmarCompra(any());
    }
}

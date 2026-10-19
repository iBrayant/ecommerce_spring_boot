package com.ecommerce.catalogo.domain.model.gateway;

import java.math.BigDecimal;

public interface UsuarioGateway {

    boolean usuarioExiste(String usuarioId);

    BigDecimal consultarSaldo(String usuarioId);

    // Devuelve el saldo que le queda al usuario despues del descuento
    BigDecimal descontarSaldo(String usuarioId, BigDecimal monto);

    void devolverSaldo(String usuarioId, BigDecimal monto);
}

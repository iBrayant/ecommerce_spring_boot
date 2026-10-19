package com.ecommerce.auth.domain.usecase;

import com.ecommerce.auth.domain.exception.SaldoInsuficienteException;
import com.ecommerce.auth.domain.exception.UsuarioNoEncontradoException;
import com.ecommerce.auth.domain.model.Usuario;
import com.ecommerce.auth.domain.model.gateway.EncrypterGateway;
import com.ecommerce.auth.domain.model.gateway.UsuarioGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsuarioUseCaseSaldoTest {

    private static final String ID = "usuario-1";

    @Mock
    private UsuarioGateway usuarioGateway;
    @Mock
    private EncrypterGateway encrypterGateway;

    private UsuarioUseCase usuarioUseCase;

    @BeforeEach
    void setUp() {
        usuarioUseCase = new UsuarioUseCase(usuarioGateway, encrypterGateway);
        when(usuarioGateway.actualizarUsuario(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(usuarioGateway.guardarusuario(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(encrypterGateway.encrypt(any())).thenReturn("clave-encriptada");
    }

    private Usuario usuarioConSaldo(String saldo) {
        Usuario usuario = new Usuario(ID, "Ana", "ana@test.com", "hash", "CLIENTE", "3001234567", 25, new BigDecimal(saldo));
        when(usuarioGateway.buscarUsuarioPorId(ID)).thenReturn(usuario);
        return usuario;
    }

    @Test
    void usuarioNuevoIniciaConSaldoCeroAunqueLlegueOtroValor() {
        Usuario nuevo = new Usuario(null, "Ana", "ana@test.com", "1234", "CLIENTE", "3001234567", 25, new BigDecimal("999999"));

        Usuario guardado = usuarioUseCase.guardarUsuario(nuevo);

        assertEquals(BigDecimal.ZERO, guardado.getSaldo());
    }

    @Test
    void actualizarUsuarioConservaElSaldoQueYaTenia() {
        usuarioConSaldo("80000.00");
        Usuario cambios = new Usuario(ID, "Ana Editada", "ana@test.com", "1234", "CLIENTE", "3001234567", 26, new BigDecimal("1"));

        Usuario actualizado = usuarioUseCase.actualizarUsuario(cambios);

        assertEquals(new BigDecimal("80000.00"), actualizado.getSaldo());
    }

    @Test
    void recargarSumaAlSaldo() {
        usuarioConSaldo("100000.00");

        Usuario resultado = usuarioUseCase.recargarSaldo(ID, new BigDecimal("50000"));

        assertEquals(new BigDecimal("150000.00"), resultado.getSaldo());
    }

    @Test
    void recargarConMontoCeroONegativoSeRechaza() {
        usuarioConSaldo("100000.00");

        assertThrows(IllegalArgumentException.class, () -> usuarioUseCase.recargarSaldo(ID, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> usuarioUseCase.recargarSaldo(ID, new BigDecimal("-10")));
        verify(usuarioGateway, never()).actualizarUsuario(any());
    }

    @Test
    void recargarUnUsuarioNuevoLeDaElSaldoInicial() {
        usuarioConSaldo("0.00");

        Usuario resultado = usuarioUseCase.recargarSaldo(ID, new BigDecimal("250000"));

        assertEquals(new BigDecimal("250000.00"), resultado.getSaldo());
    }

    @Test
    void descontarRestaDelSaldo() {
        usuarioConSaldo("250000.00");

        Usuario resultado = usuarioUseCase.descontarSaldo(ID, new BigDecimal("170000"));

        assertEquals(new BigDecimal("80000.00"), resultado.getSaldo());
    }

    @Test
    void descontarMasDelSaldoDisponibleSeRechazaYNoModificaNada() {
        usuarioConSaldo("100000.00");

        assertThrows(SaldoInsuficienteException.class, () -> usuarioUseCase.descontarSaldo(ID, new BigDecimal("170000")));
        verify(usuarioGateway, never()).actualizarUsuario(any());
    }

    @Test
    void operarSobreUnUsuarioInexistenteLanzaNoEncontrado() {
        when(usuarioGateway.buscarUsuarioPorId("nadie")).thenReturn(null);

        assertThrows(UsuarioNoEncontradoException.class, () -> usuarioUseCase.consultarSaldo("nadie"));
        assertThrows(UsuarioNoEncontradoException.class, () -> usuarioUseCase.descontarSaldo("nadie", BigDecimal.ONE));
    }
}

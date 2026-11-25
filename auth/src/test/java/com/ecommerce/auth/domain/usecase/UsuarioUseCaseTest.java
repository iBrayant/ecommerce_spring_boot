package com.ecommerce.auth.domain.usecase;

import com.ecommerce.auth.domain.model.Usuario;
import com.ecommerce.auth.domain.model.gateway.EncrypterGateway;
import com.ecommerce.auth.domain.model.gateway.UsuarioGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioUseCaseTest {

    @Mock
    private UsuarioGateway usuarioGateway;

    @Mock
    private EncrypterGateway encrypterGateway;

    @InjectMocks
    private UsuarioUseCase usuarioUseCase;

    @Test
    void actualizarUsuarioCorrectamente()  {

        //Arrange
        Usuario usuario = mock(Usuario.class);
        Usuario usuarioExistente = mock(Usuario.class);
        Usuario usuarioActualizado = mock(Usuario.class);

        lenient().when(usuario.getIdUsuario()).thenReturn("1");
        lenient().when(usuario.getClave()).thenReturn("12345");
        lenient().when(usuarioExistente.getSaldo()).thenReturn(new BigDecimal("80000"));

        when(usuarioGateway.buscarUsuarioPorId("1"))
                .thenReturn(usuarioExistente);
        when(encrypterGateway.encrypt("12345"))
                .thenReturn("clave");
        when(usuarioGateway.actualizarUsuario(usuario))
                .thenReturn(usuarioActualizado);

        //Act
        Usuario usuarioResultado = usuarioUseCase.actualizarUsuario(usuario);

        //Assert
        assertNotNull(usuarioResultado);
        assertEquals(usuarioActualizado, usuarioResultado);
        verify(usuario).setSaldo(new BigDecimal("80000"));
        verify(usuario).setClave("clave");
        verify(usuarioGateway).actualizarUsuario(usuario);
    }
}

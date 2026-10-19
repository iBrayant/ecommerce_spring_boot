package com.ecommerce.auth.domain.usecase;
import com.ecommerce.auth.domain.exception.SaldoInsuficienteException;
import com.ecommerce.auth.domain.exception.UsuarioNoEncontradoException;
import com.ecommerce.auth.domain.model.Usuario;
import com.ecommerce.auth.domain.model.gateway.EncrypterGateway;
import com.ecommerce.auth.domain.model.gateway.UsuarioGateway;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@RequiredArgsConstructor
public class UsuarioUseCase {

    private final UsuarioGateway usuarioGateway;
    private final EncrypterGateway encrypterGateway;

    public Usuario guardarUsuario(Usuario usuario) {
        if (usuario.getCorreo() == null || usuario.getClave() == null) {
            throw new IllegalArgumentException("Correo y clave son obligatorios");
        }
        if (usuario.getNombre() == null) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (usuario.getRol() == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
        if (usuario.getNumeroTelefonico() == null) {
            throw new IllegalArgumentException("El número telefónico es obligatorio");
        }
        if (usuario.getEdad() == null || usuario.getEdad() < 18) {
            throw new IllegalArgumentException("El usuario debe ser mayor de edad");
        }
        usuario.setClave(encrypterGateway.encrypt(usuario.getClave()));
        // Todo usuario nuevo inicia con saldo 0, sin importar lo que llegue en la peticion
        usuario.setSaldo(BigDecimal.ZERO);
        Usuario usuarioGuardado = usuarioGateway.guardarusuario(usuario);
        return usuarioGuardado;
    }

    public Usuario buscarUsuario(String idUsuario) {
        return usuarioGateway.buscarUsuarioPorId(idUsuario);
    }

    public Usuario login(String correo, String clave) {
        Usuario usuario = usuarioGateway.buscarUsuarioPorCorreo(correo);
        if (usuario == null) {
            return null;
        }
        if (!encrypterGateway.checkPass(clave, usuario.getClave())) {
            throw new IllegalArgumentException("Clave incorrecta");
        }
        return usuario;
    }

    public Usuario actualizarUsuario(Usuario usuario) {
        Objects.requireNonNull(usuario.getIdUsuario(), "El id es obligatorio");
        Usuario usuarioExistente = usuarioGateway.buscarUsuarioPorId(usuario.getIdUsuario());
        if (usuarioExistente == null) {
            return null;
        }
        // El saldo no se modifica desde el CRUD general, se conserva el que ya tiene
        usuario.setSaldo(usuarioExistente.getSaldo());
        if (usuario.getClave() != null) {
            usuario.setClave(encrypterGateway.encrypt(usuario.getClave()));
        }
        return usuarioGateway.actualizarUsuario(usuario);
    }

    public boolean eliminarUsuario(String idUsuario) {
        return usuarioGateway.eliminarUsuarioPorId(idUsuario);
    }

    public Usuario consultarSaldo(String idUsuario) {
        return buscarUsuarioExistente(idUsuario);
    }

    public Usuario recargarSaldo(String idUsuario, BigDecimal monto) {
        BigDecimal valor = validarMonto(monto);
        Usuario usuario = buscarUsuarioExistente(idUsuario);
        usuario.setSaldo(usuario.getSaldo().add(valor));
        return usuarioGateway.actualizarUsuario(usuario);
    }

    public Usuario descontarSaldo(String idUsuario, BigDecimal monto) {
        BigDecimal valor = validarMonto(monto);
        Usuario usuario = buscarUsuarioExistente(idUsuario);
        if (usuario.getSaldo().compareTo(valor) < 0) {
            throw new SaldoInsuficienteException("Saldo insuficiente. Saldo actual: " + usuario.getSaldo()
                    + ", valor a descontar: " + valor);
        }
        usuario.setSaldo(usuario.getSaldo().subtract(valor));
        return usuarioGateway.actualizarUsuario(usuario);
    }

    private Usuario buscarUsuarioExistente(String idUsuario) {
        Usuario usuario = usuarioGateway.buscarUsuarioPorId(idUsuario);
        if (usuario == null) {
            throw new UsuarioNoEncontradoException("Usuario no encontrado con id: " + idUsuario);
        }
        return usuario;
    }

    private BigDecimal validarMonto(BigDecimal monto) {
        if (monto == null) {
            throw new IllegalArgumentException("El monto es obligatorio");
        }
        BigDecimal valor = monto.setScale(2, RoundingMode.HALF_UP);
        if (valor.signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }
        return valor;
    }
}

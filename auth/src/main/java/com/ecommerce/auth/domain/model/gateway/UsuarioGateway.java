package com.ecommerce.auth.domain.model.gateway;

import com.ecommerce.auth.domain.model.Usuario;

public interface UsuarioGateway {

    Usuario guardarusuario(Usuario usuario);

    Usuario buscarUsuarioPorId(String idUsuario);

    Usuario buscarUsuarioPorCorreo(String correo);

    Usuario actualizarUsuario(Usuario usuario);

    boolean eliminarUsuarioPorId(String idUsuario);

}
package com.ecommerce.auth.infraestructure.driver_adapters;
import com.ecommerce.auth.domain.model.Usuario;
import com.ecommerce.auth.domain.model.gateway.UsuarioGateway;
import com.ecommerce.auth.infraestructure.mapper.MapperUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UsuarioDataGatewayImpl implements UsuarioGateway {

    private final MapperUsuario mapperUsuario;
    private final UsuarioDataJpaRepository repository;

    @Override
    public Usuario guardarusuario(Usuario usuario) {
        UsuarioData usuarioData = mapperUsuario.toData(usuario);
        return mapperUsuario.toUsuario(repository.save(usuarioData));
    }

    @Override
    public Usuario buscarUsuarioPorId(String idUsuario) {
        return repository.findById(idUsuario)
                .map(mapperUsuario::toUsuario)
                .orElse(null);
    }

    @Override
    public Usuario buscarUsuarioPorCorreo(String correo) {
        return repository.findByCorreo(correo)
                .map(mapperUsuario::toUsuario)
                .orElse(null);
    }

    @Override
    public Usuario actualizarUsuario(Usuario usuario) {
        if (!repository.existsById(usuario.getIdUsuario())) {
            return null;
        }
        UsuarioData usuarioData = mapperUsuario.toData(usuario);
        return mapperUsuario.toUsuario(repository.save(usuarioData));
    }

    @Override
    public boolean eliminarUsuarioPorId(String idUsuario) {
        if (!repository.existsById(idUsuario)) {
            return false;
        }
        repository.deleteById(idUsuario);
        return true;
    }
}




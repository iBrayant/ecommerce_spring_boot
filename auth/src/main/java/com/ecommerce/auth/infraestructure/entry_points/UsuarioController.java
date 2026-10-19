package com.ecommerce.auth.infraestructure.entry_points;

import com.ecommerce.auth.domain.exception.SaldoInsuficienteException;
import com.ecommerce.auth.domain.exception.UsuarioNoEncontradoException;
import com.ecommerce.auth.domain.model.Usuario;
import com.ecommerce.auth.domain.usecase.UsuarioUseCase;
import com.ecommerce.auth.infraestructure.driver_adapters.UsuarioData;
import com.ecommerce.auth.infraestructure.mapper.MapperUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ecommerce/usuario")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioUseCase usuarioUseCase;
    private final MapperUsuario usuarioMapper;

    @PostMapping("/save")
    public ResponseEntity<?> saveUsuario(@RequestBody UsuarioData usuarioData) {
        try {
            Usuario usuarioGuardado = usuarioUseCase.guardarUsuario(usuarioMapper.toUsuario(usuarioData));
            return new ResponseEntity<>(usuarioGuardado, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>("Ya existe un usuario con ese correo", HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UsuarioData usuarioData) {
        try {
            Usuario usuario = usuarioUseCase.login(usuarioData.getCorreo(), usuarioData.getClave());
            if (usuario == null) {
                return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>("Login exitoso", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.OK);
        }
    }

    @GetMapping("/find/{id}")
    public ResponseEntity<Usuario> findUsuario(@PathVariable("id") String id) {
        Usuario usuario = usuarioUseCase.buscarUsuario(id);

        if (usuario != null) {
            return new ResponseEntity<>(usuario, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/update")
    public ResponseEntity<Usuario> updateUsuario(@RequestBody UsuarioData usuarioData) {
        Usuario usuario = usuarioMapper.toUsuario(usuarioData);
        Usuario usuarioActualizado = usuarioUseCase.actualizarUsuario(usuario);

        if (usuarioActualizado != null) {
            return new ResponseEntity<>(usuarioActualizado, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable("id") String id) {
        boolean eliminado = usuarioUseCase.eliminarUsuario(id);

        if (eliminado) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{id}/saldo")
    public ResponseEntity<?> consultarSaldo(@PathVariable("id") String id) {
        try {
            Usuario usuario = usuarioUseCase.consultarSaldo(id);
            return new ResponseEntity<>(usuario.getSaldo(), HttpStatus.OK);
        } catch (UsuarioNoEncontradoException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{id}/saldo/recargar")
    public ResponseEntity<?> recargarSaldo(@PathVariable("id") String id,
                                           @RequestBody UsuarioData usuarioData) {
        try {
            Usuario usuario = usuarioUseCase.recargarSaldo(id, usuarioData.getSaldo());
            return new ResponseEntity<>(usuario.getSaldo(), HttpStatus.OK);
        } catch (UsuarioNoEncontradoException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{id}/saldo/descontar")
    public ResponseEntity<?> descontarSaldo(@PathVariable("id") String id,
                                            @RequestBody UsuarioData usuarioData) {
        try {
            Usuario usuario = usuarioUseCase.descontarSaldo(id, usuarioData.getSaldo());
            return new ResponseEntity<>(usuario.getSaldo(), HttpStatus.OK);
        } catch (UsuarioNoEncontradoException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (SaldoInsuficienteException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }
}

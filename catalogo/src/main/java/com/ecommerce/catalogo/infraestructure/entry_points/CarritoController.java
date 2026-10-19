package com.ecommerce.catalogo.infraestructure.entry_points;

import com.ecommerce.catalogo.domain.exception.RecursoNoEncontradoException;
import com.ecommerce.catalogo.domain.exception.ReglaNegocioException;
import com.ecommerce.catalogo.domain.exception.ServicioExternoException;
import com.ecommerce.catalogo.domain.model.ItemCarrito;
import com.ecommerce.catalogo.domain.usecase.CarritoUseCase;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/ecommerce/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoUseCase carritoUseCase;

    // Body: { "productoId": 1, "cantidad": 2 }
    @PostMapping("/{usuarioId}/items")
    public ResponseEntity<?> agregarProducto(@PathVariable("usuarioId") String usuarioId,
                                             @RequestBody ItemCarrito item) {
        try {
            return new ResponseEntity<>(
                    carritoUseCase.agregarProducto(usuarioId, item.getProductoId(), item.getCantidad()),
                    HttpStatus.OK);
        } catch (RecursoNoEncontradoException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (ReglaNegocioException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (ServicioExternoException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_GATEWAY);
        }
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<?> verCarrito(@PathVariable("usuarioId") String usuarioId) {
        try {
            return new ResponseEntity<>(carritoUseCase.verCarrito(usuarioId), HttpStatus.OK);
        } catch (RecursoNoEncontradoException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Body: { "cantidad": 3 }
    @PutMapping("/{usuarioId}/items/{productoId}")
    public ResponseEntity<?> cambiarCantidad(@PathVariable("usuarioId") String usuarioId,
                                             @PathVariable("productoId") Long productoId,
                                             @RequestBody ItemCarrito item) {
        try {
            return new ResponseEntity<>(
                    carritoUseCase.cambiarCantidad(usuarioId, productoId, item.getCantidad()),
                    HttpStatus.OK);
        } catch (RecursoNoEncontradoException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (ReglaNegocioException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{usuarioId}/items/{productoId}")
    public ResponseEntity<?> retirarProducto(@PathVariable("usuarioId") String usuarioId,
                                             @PathVariable("productoId") Long productoId) {
        try {
            return new ResponseEntity<>(carritoUseCase.retirarProducto(usuarioId, productoId), HttpStatus.OK);
        } catch (RecursoNoEncontradoException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{usuarioId}/compra")
    public ResponseEntity<?> comprar(@PathVariable("usuarioId") String usuarioId) {
        try {
            return new ResponseEntity<>(carritoUseCase.comprar(usuarioId), HttpStatus.OK);
        } catch (RecursoNoEncontradoException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (ReglaNegocioException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (ServicioExternoException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_GATEWAY);
        }
    }
}

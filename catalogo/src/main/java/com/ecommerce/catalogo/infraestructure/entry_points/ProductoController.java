package com.ecommerce.catalogo.infraestructure.entry_points;

import com.ecommerce.catalogo.domain.exception.RecursoNoEncontradoException;
import com.ecommerce.catalogo.domain.model.Producto;
import com.ecommerce.catalogo.domain.usecase.ProductoUseCase;
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
@RequestMapping("/api/ecommerce/producto")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoUseCase productoUseCase;

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Producto producto) {
        try {
            return new ResponseEntity<>(productoUseCase.crearProducto(producto), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<?> listar() {
        return new ResponseEntity<>(productoUseCase.listarProductos(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable("id") Long id) {
        try {
            return new ResponseEntity<>(productoUseCase.obtenerProducto(id), HttpStatus.OK);
        } catch (RecursoNoEncontradoException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable("id") Long id, @RequestBody Producto producto) {
        try {
            return new ResponseEntity<>(productoUseCase.actualizarProducto(id, producto), HttpStatus.OK);
        } catch (RecursoNoEncontradoException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable("id") Long id) {
        try {
            productoUseCase.eliminarProducto(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RecursoNoEncontradoException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>("No se puede eliminar: el producto esta en un carrito", HttpStatus.CONFLICT);
        }
    }
}

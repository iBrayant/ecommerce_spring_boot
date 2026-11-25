package com.ecommerce.notification.infraestructure.entry_points;

import com.ecommerce.notification.domain.exception.ServicioExternoException;
import com.ecommerce.notification.domain.model.Notificacion;
import com.ecommerce.notification.domain.usecase.NotificacionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ecommerce/notificacion")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionUseCase notificacionUseCase;

    @PostMapping
    public ResponseEntity<?> enviar(@RequestBody Notificacion notificacion) {
        try {
            return new ResponseEntity<>(notificacionUseCase.enviarNotificacion(notificacion), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (ServicioExternoException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_GATEWAY);
        }
    }
}

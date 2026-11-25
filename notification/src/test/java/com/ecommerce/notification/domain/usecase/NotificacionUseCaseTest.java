package com.ecommerce.notification.domain.usecase;

import com.ecommerce.notification.domain.model.CanalNotificacion;
import com.ecommerce.notification.domain.model.Notificacion;
import com.ecommerce.notification.domain.model.ResultadoNotificacion;
import com.ecommerce.notification.domain.model.gateway.SmsGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionUseCaseTest {

    @Mock
    private SmsGateway smsGateway;

    private NotificacionUseCase notificacionUseCase;

    @BeforeEach
    void setUp() {
        notificacionUseCase = new NotificacionUseCase(smsGateway);
    }

    @Test
    void enviaSmsConTelefonoNormalizado() {
        when(smsGateway.enviarSms("+573001234567", "Hola")).thenReturn("SM123");

        ResultadoNotificacion resultado = notificacionUseCase.enviarNotificacion(
                new Notificacion("REGISTRO", null, " +57 300-123 4567 ", "Hola", CanalNotificacion.SMS));

        assertEquals(NotificacionUseCase.ACEPTADA_POR_PROVEEDOR, resultado.getEstado());
        assertEquals(CanalNotificacion.SMS, resultado.getCanal());
        assertEquals("SM123", resultado.getIdProveedor());
    }

    @Test
    void rechazaMensajeVacio() {
        assertThrows(IllegalArgumentException.class, () -> notificacionUseCase.enviarNotificacion(
                new Notificacion("REGISTRO", null, "+573001234567", "  ", CanalNotificacion.SMS)));
        assertThrows(IllegalArgumentException.class, () -> notificacionUseCase.enviarNotificacion(null));
        verify(smsGateway, never()).enviarSms(anyString(), anyString());
    }

    @Test
    void rechazaSmsSinTelefonoOConTelefonoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> notificacionUseCase.enviarNotificacion(
                new Notificacion("REGISTRO", null, null, "Hola", CanalNotificacion.SMS)));
        assertThrows(IllegalArgumentException.class, () -> notificacionUseCase.enviarNotificacion(
                new Notificacion("REGISTRO", null, "3001234567", "Hola", CanalNotificacion.SMS)));
        verify(smsGateway, never()).enviarSms(anyString(), anyString());
    }

    @Test
    void rechazaCanalSinImplementar() {
        assertThrows(IllegalArgumentException.class, () -> notificacionUseCase.enviarNotificacion(
                new Notificacion("REGISTRO", "a@b.com", null, "Hola", CanalNotificacion.EMAIL)));
        assertThrows(IllegalArgumentException.class, () -> notificacionUseCase.enviarNotificacion(
                new Notificacion("REGISTRO", "a@b.com", null, "Hola", null)));
    }
}

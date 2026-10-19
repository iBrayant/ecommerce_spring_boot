package com.ecommerce.catalogo.infraestructure.driver_adapters.external_repository;

import com.ecommerce.catalogo.domain.exception.RecursoNoEncontradoException;
import com.ecommerce.catalogo.domain.exception.ReglaNegocioException;
import com.ecommerce.catalogo.domain.exception.ServicioExternoException;
import com.ecommerce.catalogo.domain.model.gateway.UsuarioGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class UsuarioGatewayImpl implements UsuarioGateway {

    private static final String RUTA_USUARIO = "/api/ecommerce/usuario/";

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public UsuarioGatewayImpl(RestTemplate restTemplate, @Value("${auth.service.url}") String authUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = authUrl + RUTA_USUARIO;
    }

    @Override
    public boolean usuarioExiste(String usuarioId) {
        try {
            consultarSaldo(usuarioId);
            return true;
        } catch (RecursoNoEncontradoException e) {
            return false;
        }
    }

    // Auth responde solo con el numero del saldo, por eso se lee como BigDecimal
    @Override
    public BigDecimal consultarSaldo(String usuarioId) {
        return llamarAuth(usuarioId, () ->
                restTemplate.getForObject(baseUrl + usuarioId + "/saldo", BigDecimal.class));
    }

    // Auth recibe el monto en el campo "saldo" del JSON: { "saldo": 170000 }
    @Override
    public BigDecimal descontarSaldo(String usuarioId, BigDecimal monto) {
        return llamarAuth(usuarioId, () ->
                restTemplate.postForObject(baseUrl + usuarioId + "/saldo/descontar",
                        Map.of("saldo", monto), BigDecimal.class));
    }

    @Override
    public void devolverSaldo(String usuarioId, BigDecimal monto) {
        llamarAuth(usuarioId, () ->
                restTemplate.postForObject(baseUrl + usuarioId + "/saldo/recargar",
                        Map.of("saldo", monto), BigDecimal.class));
    }

    // Convierte los errores HTTP de Auth en excepciones del dominio de Catalogo
    private BigDecimal llamarAuth(String usuarioId, Supplier<BigDecimal> llamada) {
        try {
            BigDecimal respuesta = llamada.get();
            if (respuesta == null) {
                throw new ServicioExternoException("El servicio Auth devolvio una respuesta vacia");
            }
            return respuesta;
        } catch (HttpClientErrorException.NotFound e) {
            throw new RecursoNoEncontradoException("Usuario no encontrado con id: " + usuarioId);
        } catch (HttpClientErrorException.Conflict e) {
            throw new ReglaNegocioException("Saldo insuficiente para realizar la compra");
        } catch (HttpClientErrorException e) {
            throw new ServicioExternoException("El servicio Auth rechazo la solicitud: " + e.getStatusCode(), e);
        } catch (RestClientException e) {
            throw new ServicioExternoException("No se pudo comunicar con el servicio Auth", e);
        }
    }
}

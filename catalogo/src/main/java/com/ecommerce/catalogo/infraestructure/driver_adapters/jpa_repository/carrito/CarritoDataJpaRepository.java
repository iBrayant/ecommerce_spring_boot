package com.ecommerce.catalogo.infraestructure.driver_adapters.jpa_repository.carrito;

import com.ecommerce.catalogo.domain.model.EstadoCarrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CarritoDataJpaRepository extends JpaRepository<CarritoData, Long> {

    Optional<CarritoData> findFirstByUsuarioIdAndEstado(String usuarioId, EstadoCarrito estado);

    // Devuelve 1 si el carrito estaba en el estado "desde" y se cambio, 0 si no
    @Modifying
    @Query("update CarritoData c set c.estado = :hasta where c.id = :id and c.estado = :desde")
    int cambiarEstado(@Param("id") Long id, @Param("desde") EstadoCarrito desde, @Param("hasta") EstadoCarrito hasta);
}

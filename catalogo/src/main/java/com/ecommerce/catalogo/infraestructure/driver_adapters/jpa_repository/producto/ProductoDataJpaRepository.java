package com.ecommerce.catalogo.infraestructure.driver_adapters.jpa_repository.producto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoDataJpaRepository extends JpaRepository<ProductoData, Long> {

    // Descuenta el stock solo si alcanza. Devuelve cuantas filas se modificaron (0 = no habia stock suficiente)
    @Modifying
    @Query("update ProductoData p set p.stock = p.stock - :cantidad where p.id = :id and p.stock >= :cantidad")
    int descontarStock(@Param("id") Long id, @Param("cantidad") int cantidad);
}

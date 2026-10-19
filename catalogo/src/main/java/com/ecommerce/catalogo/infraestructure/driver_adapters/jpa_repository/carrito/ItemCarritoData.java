package com.ecommerce.catalogo.infraestructure.driver_adapters.jpa_repository.carrito;

import com.ecommerce.catalogo.infraestructure.driver_adapters.jpa_repository.producto.ProductoData;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "items_carrito",
        uniqueConstraints = @UniqueConstraint(columnNames = {"carrito_id", "producto_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ItemCarritoData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "carrito_id")
    private CarritoData carrito;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id")
    private ProductoData producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;
}

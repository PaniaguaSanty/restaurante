package com.siede.promocion.entity;

import com.siede.producto.entity.Producto;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "promocion_producto")
@Getter
@Setter
public class PromocionProducto {

    @EmbeddedId
    private PromocionProductoId id = new PromocionProductoId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("productoId")
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("promocionId")
    @JoinColumn(name = "promocion_id", nullable = false)
    private Promocion promocion;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    public void setProducto(Producto producto) {
        this.producto = producto;
        if (producto != null) {
            this.id.setProductoId(producto.getId());
        }
    }

    public void setPromocion(Promocion promocion) {
        this.promocion = promocion;
        if (promocion != null) {
            this.id.setPromocionId(promocion.getId());
        }
    }
}
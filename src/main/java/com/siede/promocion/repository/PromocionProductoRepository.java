package com.siede.promocion.repository;

import com.siede.promocion.entity.PromocionProducto;
import com.siede.promocion.entity.PromocionProductoId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PromocionProductoRepository extends JpaRepository<PromocionProducto, PromocionProductoId> {

    @Query("select count(pp) from PromocionProducto pp where pp.producto.id = :productoId")
    long countByProductoId(@Param("productoId") Long productoId);
}
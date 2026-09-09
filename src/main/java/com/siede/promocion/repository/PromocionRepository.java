package com.siede.promocion.repository;

import java.util.List;
import java.util.Optional;

import com.siede.promocion.entity.Promocion;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromocionRepository extends JpaRepository<Promocion, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    @Override
    @EntityGraph(attributePaths = { "promocionProductos", "promocionProductos.producto" })
    List<Promocion> findAll();

    @Override
    @EntityGraph(attributePaths = { "promocionProductos", "promocionProductos.producto" })
    Optional<Promocion> findById(Long id);
}
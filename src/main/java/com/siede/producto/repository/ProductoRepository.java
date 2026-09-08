package com.siede.producto.repository;

import java.util.List;
import java.util.Optional;

import com.siede.producto.entity.Producto;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    long countByCategoriaId(Long categoriaId);

    long countByInventarioId(Long inventarioId);

    @Override
    @EntityGraph(attributePaths = { "categoria", "inventario" })
    List<Producto> findAll();

    @Override
    @EntityGraph(attributePaths = { "categoria", "inventario" })
    Optional<Producto> findById(Long id);
}
package com.siede.categoria.repository;

import com.siede.categoria.entity.CategoriaProducto;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaProductoRepository extends JpaRepository<CategoriaProducto, Long> {

    boolean existsByNombreIgnoreCase(String nombre);
}
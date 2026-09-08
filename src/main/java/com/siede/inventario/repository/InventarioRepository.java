package com.siede.inventario.repository;

import com.siede.inventario.entity.Inventario;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    boolean existsByNombreIgnoreCase(String nombre);
}
package com.siede.inventario.dto;

import com.siede.shared.enums.Estado;

public record InventarioResponseDTO(
        Long inventarioId,
        String nombre,
        String descripcion,
        String ubicacion,
        Estado estado) {
}
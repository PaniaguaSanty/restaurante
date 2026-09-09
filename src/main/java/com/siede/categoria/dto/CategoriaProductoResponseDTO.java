package com.siede.categoria.dto;

import com.siede.shared.enums.Estado;

public record CategoriaProductoResponseDTO(
        Long categoriaId,
        String nombre,
        String descripcion,
        Estado estado) {
}
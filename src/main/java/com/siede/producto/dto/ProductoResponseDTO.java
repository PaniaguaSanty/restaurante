package com.siede.producto.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductoResponseDTO(
        Long productoId,
        String nombre,
        Boolean disponible,
        LocalDate fechaExpiracion,
        BigDecimal precio,
        Long categoriaId,
        String categoriaNombre,
        Long inventarioId,
        String inventarioNombre) {
}
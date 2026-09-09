package com.siede.promocion.dto;

public record PromocionProductoResponseDTO(
        Long productoId,
        String nombreProducto,
        Integer cantidad) {
}
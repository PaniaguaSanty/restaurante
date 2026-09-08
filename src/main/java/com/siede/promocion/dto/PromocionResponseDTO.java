package com.siede.promocion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.siede.promocion.enums.TipoDescuento;
import com.siede.shared.enums.Estado;

public record PromocionResponseDTO(
        Long promocionId,
        String nombre,
        String descripcion,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        TipoDescuento tipoDescuento,
        BigDecimal valorDescuento,
        Estado estado,
        List<PromocionProductoResponseDTO> productos) {
}
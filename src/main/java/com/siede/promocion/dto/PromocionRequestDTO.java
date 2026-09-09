package com.siede.promocion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.siede.promocion.enums.TipoDescuento;
import com.siede.shared.enums.Estado;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PromocionRequestDTO(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @Size(max = 255, message = "La descripcion no puede superar 255 caracteres")
        String descripcion,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate fechaFin,

        @NotNull(message = "El tipo de descuento es obligatorio")
        TipoDescuento tipoDescuento,

        @NotNull(message = "El valor del descuento es obligatorio")
        @DecimalMin(value = "0.0", message = "El valor del descuento no puede ser negativo")
        BigDecimal valorDescuento,

        Estado estado,

        @NotEmpty(message = "La promocion debe incluir al menos un producto")
        List<@Valid PromocionProductoRequestDTO> productos) {
}
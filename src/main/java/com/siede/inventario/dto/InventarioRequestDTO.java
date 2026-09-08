package com.siede.inventario.dto;

import com.siede.shared.enums.Estado;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InventarioRequestDTO(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @Size(max = 255, message = "La descripcion no puede superar 255 caracteres")
        String descripcion,

        @NotBlank(message = "La ubicacion es obligatoria")
        @Size(max = 100, message = "La ubicacion no puede superar 100 caracteres")
        String ubicacion,

        Estado estado) {
}
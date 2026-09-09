package com.siede.inventario.mapper;

import com.siede.inventario.dto.InventarioRequestDTO;
import com.siede.inventario.dto.InventarioResponseDTO;
import com.siede.inventario.entity.Inventario;

import org.springframework.stereotype.Component;

@Component
public class InventarioMapper {

    public Inventario toEntity(InventarioRequestDTO dto) {
        Inventario inventario = new Inventario();
        inventario.setNombre(dto.nombre());
        inventario.setDescripcion(dto.descripcion());
        inventario.setUbicacion(dto.ubicacion());
        if (dto.estado() != null) {
            inventario.setEstado(dto.estado());
        }
        return inventario;
    }

    public InventarioResponseDTO toResponse(Inventario inventario) {
        return new InventarioResponseDTO(
                inventario.getId(),
                inventario.getNombre(),
                inventario.getDescripcion(),
                inventario.getUbicacion(),
                inventario.getEstado());
    }
}
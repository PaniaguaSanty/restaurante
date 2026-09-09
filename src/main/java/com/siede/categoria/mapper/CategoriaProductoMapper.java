package com.siede.categoria.mapper;

import com.siede.categoria.dto.CategoriaProductoRequestDTO;
import com.siede.categoria.dto.CategoriaProductoResponseDTO;
import com.siede.categoria.entity.CategoriaProducto;

import org.springframework.stereotype.Component;

@Component
public class CategoriaProductoMapper {

    public CategoriaProducto toEntity(CategoriaProductoRequestDTO dto) {
        CategoriaProducto categoria = new CategoriaProducto();
        categoria.setNombre(dto.nombre());
        categoria.setDescripcion(dto.descripcion());
        if (dto.estado() != null) {
            categoria.setEstado(dto.estado());
        }
        return categoria;
    }

    public CategoriaProductoResponseDTO toResponse(CategoriaProducto categoria) {
        return new CategoriaProductoResponseDTO(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getEstado());
    }
}
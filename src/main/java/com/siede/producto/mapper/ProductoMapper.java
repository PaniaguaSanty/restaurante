package com.siede.producto.mapper;

import com.siede.categoria.entity.CategoriaProducto;
import com.siede.inventario.entity.Inventario;
import com.siede.producto.dto.ProductoRequestDTO;
import com.siede.producto.dto.ProductoResponseDTO;
import com.siede.producto.entity.Producto;

import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    public Producto toEntity(ProductoRequestDTO dto, CategoriaProducto categoria, Inventario inventario) {
        Producto producto = new Producto();
        producto.setNombre(dto.nombre());
        producto.setDisponible(dto.disponible() != null ? dto.disponible() : Boolean.TRUE);
        producto.setFechaExpiracion(dto.fechaExpiracion());
        producto.setPrecio(dto.precio());
        producto.setCategoria(categoria);
        producto.setInventario(inventario);
        return producto;
    }

    public ProductoResponseDTO toResponse(Producto producto) {
        return new ProductoResponseDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDisponible(),
                producto.getFechaExpiracion(),
                producto.getPrecio(),
                producto.getCategoria().getId(),
                producto.getCategoria().getNombre(),
                producto.getInventario().getId(),
                producto.getInventario().getNombre());
    }
}
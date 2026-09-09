package com.siede.producto.service;

import java.util.List;

import com.siede.categoria.entity.CategoriaProducto;
import com.siede.categoria.repository.CategoriaProductoRepository;
import com.siede.inventario.entity.Inventario;
import com.siede.inventario.repository.InventarioRepository;
import com.siede.producto.dto.ProductoRequestDTO;
import com.siede.producto.dto.ProductoResponseDTO;
import com.siede.producto.entity.Producto;
import com.siede.producto.mapper.ProductoMapper;
import com.siede.producto.repository.ProductoRepository;
import com.siede.promocion.repository.PromocionProductoRepository;
import com.siede.shared.exception.ConflictException;
import com.siede.shared.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository repository;
    private final CategoriaProductoRepository categoriaRepository;
    private final InventarioRepository inventarioRepository;
    private final PromocionProductoRepository promocionProductoRepository;
    private final ProductoMapper mapper;

    @Transactional
    public ProductoResponseDTO create(ProductoRequestDTO dto) {
        CategoriaProducto categoria = getCategoria(dto.categoriaId());
        Inventario inventario = getInventario(dto.inventarioId());
        if (repository.existsByNombreIgnoreCase(dto.nombre())) {
            throw new ConflictException("Ya existe un producto con el nombre " + dto.nombre());
        }
        Producto producto = mapper.toEntity(dto, categoria, inventario);
        return mapper.toResponse(fetchWithRelations(repository.save(producto).getId()));
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO findById(Long id) {
        return mapper.toResponse(fetchWithRelations(id));
    }

    @Transactional
    public ProductoResponseDTO update(Long id, ProductoRequestDTO dto) {
        Producto producto = getById(id);
        if (!producto.getNombre().equalsIgnoreCase(dto.nombre())
                && repository.existsByNombreIgnoreCase(dto.nombre())) {
            throw new ConflictException("Ya existe un producto con el nombre " + dto.nombre());
        }
        producto.setNombre(dto.nombre());
        producto.setDisponible(dto.disponible() != null ? dto.disponible() : producto.getDisponible());
        producto.setFechaExpiracion(dto.fechaExpiracion());
        producto.setPrecio(dto.precio());
        producto.setCategoria(getCategoria(dto.categoriaId()));
        producto.setInventario(getInventario(dto.inventarioId()));
        return mapper.toResponse(repository.save(producto));
    }

    @Transactional
    public void delete(Long id) {
        Producto producto = getById(id);
        if (promocionProductoRepository.countByProductoId(id) > 0) {
            throw new ConflictException("No se puede eliminar el producto porque esta incluido en una promocion");
        }
        repository.delete(producto);
    }

    private Producto fetchWithRelations(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto con id " + id + " no encontrado"));
    }

    private Producto getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto con id " + id + " no encontrado"));
    }

    private CategoriaProducto getCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria con id " + id + " no encontrada"));
    }

    private Inventario getInventario(Long id) {
        return inventarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario con id " + id + " no encontrada"));
    }
}
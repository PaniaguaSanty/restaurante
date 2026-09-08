package com.siede.categoria.service;

import java.util.List;

import com.siede.categoria.dto.CategoriaProductoRequestDTO;
import com.siede.categoria.dto.CategoriaProductoResponseDTO;
import com.siede.categoria.entity.CategoriaProducto;
import com.siede.categoria.mapper.CategoriaProductoMapper;
import com.siede.categoria.repository.CategoriaProductoRepository;
import com.siede.producto.repository.ProductoRepository;
import com.siede.shared.exception.ConflictException;
import com.siede.shared.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaProductoService {

    private final CategoriaProductoRepository repository;
    private final ProductoRepository productoRepository;
    private final CategoriaProductoMapper mapper;

    @Transactional
    public CategoriaProductoResponseDTO create(CategoriaProductoRequestDTO dto) {
        if (repository.existsByNombreIgnoreCase(dto.nombre())) {
            throw new ConflictException("Ya existe una categoria con el nombre " + dto.nombre());
        }
        return mapper.toResponse(repository.save(mapper.toEntity(dto)));
    }

    @Transactional(readOnly = true)
    public List<CategoriaProductoResponseDTO> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaProductoResponseDTO findById(Long id) {
        return mapper.toResponse(getById(id));
    }

    @Transactional
    public CategoriaProductoResponseDTO update(Long id, CategoriaProductoRequestDTO dto) {
        CategoriaProducto categoria = getById(id);
        if (!categoria.getNombre().equalsIgnoreCase(dto.nombre())
                && repository.existsByNombreIgnoreCase(dto.nombre())) {
            throw new ConflictException("Ya existe una categoria con el nombre " + dto.nombre());
        }
        categoria.setNombre(dto.nombre());
        categoria.setDescripcion(dto.descripcion());
        if (dto.estado() != null) {
            categoria.setEstado(dto.estado());
        }
        return mapper.toResponse(repository.save(categoria));
    }

    @Transactional
    public void delete(Long id) {
        CategoriaProducto categoria = getById(id);
        if (productoRepository.countByCategoriaId(id) > 0) {
            throw new ConflictException("No se puede eliminar la categoria porque tiene productos asociados");
        }
        repository.delete(categoria);
    }

    private CategoriaProducto getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria con id " + id + " no encontrada"));
    }
}
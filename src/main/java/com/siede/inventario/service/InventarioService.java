package com.siede.inventario.service;

import java.util.List;

import com.siede.inventario.dto.InventarioRequestDTO;
import com.siede.inventario.dto.InventarioResponseDTO;
import com.siede.inventario.entity.Inventario;
import com.siede.inventario.mapper.InventarioMapper;
import com.siede.inventario.repository.InventarioRepository;
import com.siede.producto.repository.ProductoRepository;
import com.siede.shared.exception.ConflictException;
import com.siede.shared.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioRepository repository;
    private final ProductoRepository productoRepository;
    private final InventarioMapper mapper;

    @Transactional
    public InventarioResponseDTO create(InventarioRequestDTO dto) {
        if (repository.existsByNombreIgnoreCase(dto.nombre())) {
            throw new ConflictException("Ya existe un inventario con el nombre " + dto.nombre());
        }
        return mapper.toResponse(repository.save(mapper.toEntity(dto)));
    }

    @Transactional(readOnly = true)
    public List<InventarioResponseDTO> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public InventarioResponseDTO findById(Long id) {
        return mapper.toResponse(getById(id));
    }

    @Transactional
    public InventarioResponseDTO update(Long id, InventarioRequestDTO dto) {
        Inventario inventario = getById(id);
        if (!inventario.getNombre().equalsIgnoreCase(dto.nombre())
                && repository.existsByNombreIgnoreCase(dto.nombre())) {
            throw new ConflictException("Ya existe un inventario con el nombre " + dto.nombre());
        }
        inventario.setNombre(dto.nombre());
        inventario.setDescripcion(dto.descripcion());
        inventario.setUbicacion(dto.ubicacion());
        if (dto.estado() != null) {
            inventario.setEstado(dto.estado());
        }
        return mapper.toResponse(repository.save(inventario));
    }

    @Transactional
    public void delete(Long id) {
        Inventario inventario = getById(id);
        if (productoRepository.countByInventarioId(id) > 0) {
            throw new ConflictException("No se puede eliminar el inventario porque tiene productos asociados");
        }
        repository.delete(inventario);
    }

    private Inventario getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario con id " + id + " no encontrada"));
    }
}
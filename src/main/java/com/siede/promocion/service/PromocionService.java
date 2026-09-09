package com.siede.promocion.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.siede.producto.entity.Producto;
import com.siede.producto.repository.ProductoRepository;
import com.siede.promocion.dto.PromocionProductoRequestDTO;
import com.siede.promocion.dto.PromocionRequestDTO;
import com.siede.promocion.dto.PromocionResponseDTO;
import com.siede.promocion.entity.Promocion;
import com.siede.promocion.entity.PromocionProducto;
import com.siede.promocion.enums.TipoDescuento;
import com.siede.promocion.mapper.PromocionMapper;
import com.siede.promocion.repository.PromocionRepository;
import com.siede.shared.exception.BusinessException;
import com.siede.shared.exception.ConflictException;
import com.siede.shared.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromocionService {

    private final PromocionRepository repository;
    private final ProductoRepository productoRepository;
    private final PromocionMapper mapper;

    @Transactional
    public PromocionResponseDTO create(PromocionRequestDTO dto) {
        validarReglas(dto);
        if (repository.existsByNombreIgnoreCase(dto.nombre())) {
            throw new ConflictException("Ya existe una promocion con el nombre " + dto.nombre());
        }
        Promocion promocion = mapper.toEntity(dto, buildItems(dto));
        return mapper.toResponse(fetchWithRelations(repository.save(promocion).getId()));
    }

    @Transactional(readOnly = true)
    public List<PromocionResponseDTO> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PromocionResponseDTO findById(Long id) {
        return mapper.toResponse(fetchWithRelations(id));
    }

    @Transactional
    public PromocionResponseDTO update(Long id, PromocionRequestDTO dto) {
        validarReglas(dto);
        Promocion promocion = getById(id);
        if (!promocion.getNombre().equalsIgnoreCase(dto.nombre())
                && repository.existsByNombreIgnoreCase(dto.nombre())) {
            throw new ConflictException("Ya existe una promocion con el nombre " + dto.nombre());
        }
        promocion.setNombre(dto.nombre());
        promocion.setDescripcion(dto.descripcion());
        promocion.setFechaInicio(dto.fechaInicio());
        promocion.setFechaFin(dto.fechaFin());
        promocion.setTipoDescuento(dto.tipoDescuento());
        promocion.setValorDescuento(dto.valorDescuento());
        if (dto.estado() != null) {
            promocion.setEstado(dto.estado());
        }
        promocion.reemplazarPromocionProductos(buildItems(dto));
        return mapper.toResponse(repository.save(promocion));
    }

    @Transactional
    public void delete(Long id) {
        Promocion promocion = getById(id);
        repository.delete(promocion);
    }

    private void validarReglas(PromocionRequestDTO dto) {
        if (dto.fechaFin().isBefore(dto.fechaInicio())) {
            throw new BusinessException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        if (dto.tipoDescuento() == TipoDescuento.PORCENTAJE
                && dto.valorDescuento().compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException("El porcentaje de descuento no puede superar 100");
        }
        Set<Long> productoIds = new HashSet<>();
        for (PromocionProductoRequestDTO item : dto.productos()) {
            if (!productoIds.add(item.productoId())) {
                throw new BusinessException("Una promocion no puede repetir el mismo producto");
            }
        }
    }

    private List<PromocionProducto> buildItems(PromocionRequestDTO dto) {
        return dto.productos()
                .stream()
                .map(this::toPromocionProducto)
                .toList();
    }

    private PromocionProducto toPromocionProducto(PromocionProductoRequestDTO item) {
        Producto producto = productoRepository.findById(item.productoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto con id " + item.productoId() + " no encontrado"));
        PromocionProducto promocionProducto = new PromocionProducto();
        promocionProducto.setProducto(producto);
        promocionProducto.setCantidad(item.cantidad());
        return promocionProducto;
    }

    private Promocion fetchWithRelations(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promocion con id " + id + " no encontrada"));
    }

    private Promocion getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promocion con id " + id + " no encontrada"));
    }
}
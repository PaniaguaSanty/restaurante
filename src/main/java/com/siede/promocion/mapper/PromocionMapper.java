package com.siede.promocion.mapper;

import java.util.List;

import com.siede.promocion.dto.PromocionProductoResponseDTO;
import com.siede.promocion.dto.PromocionRequestDTO;
import com.siede.promocion.dto.PromocionResponseDTO;
import com.siede.promocion.entity.Promocion;
import com.siede.promocion.entity.PromocionProducto;

import org.springframework.stereotype.Component;

@Component
public class PromocionMapper {

    public Promocion toEntity(PromocionRequestDTO dto, List<PromocionProducto> productos) {
        Promocion promocion = new Promocion();
        promocion.setNombre(dto.nombre());
        promocion.setDescripcion(dto.descripcion());
        promocion.setFechaInicio(dto.fechaInicio());
        promocion.setFechaFin(dto.fechaFin());
        promocion.setTipoDescuento(dto.tipoDescuento());
        promocion.setValorDescuento(dto.valorDescuento());
        if (dto.estado() != null) {
            promocion.setEstado(dto.estado());
        }
        productos.forEach(promocion::addPromocionProducto);
        return promocion;
    }

    public PromocionResponseDTO toResponse(Promocion promocion) {
        List<PromocionProductoResponseDTO> productos = promocion.getPromocionProductos()
                .stream()
                .map(pp -> new PromocionProductoResponseDTO(
                        pp.getProducto().getId(),
                        pp.getProducto().getNombre(),
                        pp.getCantidad()))
                .toList();
        return new PromocionResponseDTO(
                promocion.getId(),
                promocion.getNombre(),
                promocion.getDescripcion(),
                promocion.getFechaInicio(),
                promocion.getFechaFin(),
                promocion.getTipoDescuento(),
                promocion.getValorDescuento(),
                promocion.getEstado(),
                productos);
    }
}
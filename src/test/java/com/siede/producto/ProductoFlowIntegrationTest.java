package com.siede.producto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.siede.AbstractIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@AutoConfigureTestRestTemplate
class ProductoFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void crearCategoriaInventarioProductoYListar() {
        Long categoriaId = postAndGetId("/api/categorias", entity("Bebidas", "Bebidas refrescantes"), "categoriaId");
        Long inventarioId = postAndGetId("/api/inventarios",
                inventario("Almacen Central", "Stock general", "Sector A"), "inventarioId");

        Map<String, Object> producto = new HashMap<>();
        producto.put("nombre", "Coca Cola 500ml");
        producto.put("disponible", Boolean.TRUE);
        producto.put("precio", 1800.00);
        producto.put("categoriaId", categoriaId);
        producto.put("inventarioId", inventarioId);

        ResponseEntity<Map> creado = restTemplate.postForEntity("/api/productos", producto, Map.class);
        assertThat(creado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(creado.getBody().get("productoId")).isNotNull();
        assertThat(creado.getBody().get("categoriaNombre")).isEqualTo("Bebidas");
        assertThat(creado.getBody().get("inventarioNombre")).isEqualTo("Almacen Central");

        ResponseEntity<List> listado = restTemplate.getForEntity("/api/productos", List.class);
        assertThat(listado.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<?> items = listado.getBody();
        assertThat(items.stream().map(item -> (Map<?, ?>) item)
                .anyMatch(p -> "Coca Cola 500ml".equals(p.get("nombre")))).isTrue();
    }

    @Test
    void crearPromocionConProductoyListar() {
        Long categoriaId = postAndGetId("/api/categorias", entity("Postres", "Dulces"), "categoriaId");
        Long inventarioId = postAndGetId("/api/inventarios",
                inventario("Frigorifico", "Almacenamiento frio", "Sector B"), "inventarioId");

        Map<String, Object> producto = new HashMap<>();
        producto.put("nombre", "Helado de chocolate");
        producto.put("precio", 3500.00);
        producto.put("categoriaId", categoriaId);
        producto.put("inventarioId", inventarioId);
        Long productoId = postAndGetId("/api/productos", producto, "productoId");

        Map<String, Object> promocion = new HashMap<>();
        promocion.put("nombre", "2x1 Postres");
        promocion.put("fechaInicio", "2026-09-01");
        promocion.put("fechaFin", "2026-09-30");
        promocion.put("tipoDescuento", "PORCENTAJE");
        promocion.put("valorDescuento", 50.00);
        promocion.put("productos", List.of(Map.of("productoId", productoId, "cantidad", 2)));

        ResponseEntity<Map> creada = restTemplate.postForEntity("/api/promociones", promocion, Map.class);
        assertThat(creada.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(creada.getBody().get("promocionId")).isNotNull();
        assertThat(((List<?>) creada.getBody().get("productos"))).hasSize(1);

        ResponseEntity<List> listado = restTemplate.getForEntity("/api/promociones", List.class);
        assertThat(listado.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<?> items = listado.getBody();
        assertThat(items.stream().map(item -> (Map<?, ?>) item)
                .anyMatch(p -> "2x1 Postres".equals(p.get("nombre")))).isTrue();
    }

    @Test
    void validaPrecioNegativoYFechasDePromocion() {
        Map<String, Object> productoInvalido = new HashMap<>();
        productoInvalido.put("nombre", "Producto invalido");
        productoInvalido.put("precio", -100.00);

        ResponseEntity<Map> productoNegativo = restTemplate.postForEntity("/api/productos", productoInvalido, Map.class);
        assertThat(productoNegativo.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Long categoriaId = postAndGetId("/api/categorias", entity("Entradas", "Aperitivos"), "categoriaId");
        Long inventarioId = postAndGetId("/api/inventarios",
                inventario("Alacena", "Provisional", "Sector C"), "inventarioId");

        Map<String, Object> producto = new HashMap<>();
        producto.put("nombre", "Papas fritas");
        producto.put("precio", 800.00);
        producto.put("categoriaId", categoriaId);
        producto.put("inventarioId", inventarioId);
        Long productoId = postAndGetId("/api/productos", producto, "productoId");

        Map<String, Object> promocionInvalida = new HashMap<>();
        promocionInvalida.put("nombre", "Promocion con fechas invertidas");
        promocionInvalida.put("fechaInicio", "2026-10-31");
        promocionInvalida.put("fechaFin", "2026-10-01");
        promocionInvalida.put("tipoDescuento", "PORCENTAJE");
        promocionInvalida.put("valorDescuento", 10.00);
        promocionInvalida.put("productos", List.of(Map.of("productoId", productoId, "cantidad", 1)));

        ResponseEntity<Map> fechasInvertidas = restTemplate.postForEntity("/api/promociones", promocionInvalida, Map.class);
        assertThat(fechasInvertidas.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    }

    private Long postAndGetId(String url, Map<String, Object> body, String idField) {
        ResponseEntity<Map> response = restTemplate.postForEntity(url, body, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return ((Number) response.getBody().get(idField)).longValue();
    }

    private Map<String, Object> entity(String nombre, String descripcion) {
        Map<String, Object> body = new HashMap<>();
        body.put("nombre", nombre);
        body.put("descripcion", descripcion);
        return body;
    }

    private Map<String, Object> inventario(String nombre, String descripcion, String ubicacion) {
        Map<String, Object> body = entity(nombre, descripcion);
        body.put("ubicacion", ubicacion);
        return body;
    }
}
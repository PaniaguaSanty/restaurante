# Contexto SIEDE - Sistema de gestión para restaurante

Documento de contexto para retomar el trabajo en una próxima sesión de OpenCode.
Repositorio: `C:\Users\pania\OneDrive\Escritorio\restaurante\restaurante`

---

## 1. Proyecto y stack

- **Nombre**: SIEDE (siede) - `com.siede:restaurante:0.0.1-SNAPSHOT`
- **Clase principal**: `com.siede.SiedeApplication`
- **Stack**: Spring Boot 4.1.1, Java 17, Maven (wrapper `.\mvnw.cmd`), PostgreSQL 16, Spring Data JPA (Hibernate 7.4.5), Bean Validation, Flyway 12.4.0, Spring Security (sin autenticación por ahora), Actuator, Lombok 1.18.46, Spring AI 2.0.1 (`spring-ai-bom` + `spring-ai-client-chat`, sin proveedor configurado).
- **BD local**: contenedor Docker `siede-postgres` (postgres:16-alpine), puerto `5432`, db/user/pass = `siede`/`siede`/`siede`, volumen `siede-postgres-data`, red `siede-network`. Valores sobreescribibles por env `DB_HOST/DB_PORT/DB_NAME/DB_USERNAME/DB_PASSWORD`. `.env` está en `.gitignore`; usar `.env.example` de plantilla.

## 2. Cómo correr y verificar

```powershell
docker compose up -d                                     # levanta Postgres
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev   # arranca app (persistir con timeout o dejar corriendo)
.\mvnw.cmd test                                          # tests (Testcontainers levanta su propio Postgres)
.\mvnw.cmd clean compile                                 # compilar solo
.\mvnw.cmd package -DskipTests                           # jar
java -jar target\restaurante-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

- Health: `http://localhost:8080/actuator/health`.
- Perfiles YAML: `application.yml` + `application-dev.yml` / `test` / `prod`. `ddl-auto: validate` — el esquema se controla SOLO por Flyway.
- DBeaver para ver tablas: `localhost:5432`, db `siede`, user `siede`, pass `siede` → Schema `public` → Tables.
- Ver esquema por consola: `docker exec -it siede-postgres psql -U siede -d siede`.

## 3. Arquitectura

- **Monolito modular por dominio**. Paquetes bajo `com.siede`:
  `categoria`, `inventario`, `producto`, `promocion` (implementados) · `cliente`, `comanda`, `pedido`, `mesa`, `reserva`, `empleado`, `rol`, `pago` (vacíos, con `.gitkeep`) · `ai` (placeholders: config/controller/service/tool) · `shared` (config/exception/security/enums).
- Cada dominio con subpaquetes: `controller`, `dto`, `entity`, `mapper`, `repository`, `service`.
- `shared/exception`: `BusinessException`, `ResourceNotFoundException`, `ConflictException`, `ValidationException`, `ErrorResponse`, `GlobalExceptionHandler` (@RestControllerAdvice).
- `shared/security/SecurityConfig`: stateless, CSRF off, todo `permitAll` (aún sin auth).
- `shared/enums/Estado`: `ACTIVO` / `INACTIVO`.

### Restricciones del usuario
- **NO implementar aún**: comanda (incl. comanda_producto), pedido, mesa, empleado, cliente, pago, reserva, rol.
- Entidades JPA con solo `@Getter`/`@Setter` (Lombok), mappers **manuales** (sin MapStruct), sin `@ManyToMany`.

## 4. Dominio implementado: Producto (categoría → producto → inventario → promoción)

Tablas (migración `V1__init_schema.sql`):

| Tabla | Columnas clave |
|---|---|
| `inventario` | inventario_id PK, nombre(100), descripcion(255), ubicacion(100), estado |
| `categoria_producto` | categoria_id PK, nombre(100) UNIQUE, descripcion(255), estado |
| `producto` | producto_id PK, nombre(100), disponible BOOL, fecha_expiracion DATE NULL, precio NUMERIC(10,2), categoria_id FK NOT NULL, inventario_id FK NOT NULL |
| `promocion` | promocion_id PK, nombre(100), descripcion(255), fecha_inicio/fin DATE, tipo_descuento, valor_descuento NUMERIC(10,2), estado |
| `promocion_producto` | PK compuesta (producto_id, promocion_id), cantidad INT |

- CHECKs: `estado IN ('ACTIVO','INACTIVO')`, `tipo_descuento IN ('PORCENTAJE','MONTO')`, `precio >= 0`, `valor_descuento >= 0`, `fecha_fin >= fecha_inicio`, `cantidad > 0`. Índices en FKs.
- Enums: `shared/enums/Estado` (compartido), `promocion/enums/TipoDescuento` (`PORCENTAJE`/`MONTO`), mapeados `@Enumerated(STRING)` + CHECK en BD.
- `PromocionProducto` usa `@EmbeddedId` (`PromocionProductoId`) + `@MapsId`; colección en `Promocion` con `CascadeType.ALL` + `orphanRemoval`; helper `addPromocionProducto` / `reemplazarPromocionProductos`.

### Capas
- **Repos**: `ProductoRepository` sobrescribe `findAll`/`findById` con `@EntityGraph({categoria, inventario})`; `PromocionRepository` con `@EntityGraph({promocionProductos, promocionProductos.producto})` (evita N+1); `PromocionProductoRepository.countByProductoId` vía `@Query` (no derivado por ambigüedad del `@MapsId`); `existsByNombreIgnoreCase` en los demás; `countByCategoriaId`/`countByInventarioId` en Producto.
- **DTOs**: records Request/Response. Response aplanan nombres (`categoriaNombre`, `inventarioNombre`, `nombreProducto`).
- **Mappers**: manuales, `@Component`.
- **Services**: `@Service` + `@RequiredArgsConstructor` (inyección por constructor), `@Transactional`.
- **Controllers**: CRUD completo en `/api/categorias`, `/api/inventarios`, `/api/productos`, `/api/promociones`. POST → 201, DELETE → 204.

### Reglas de negocio / errores
- 404 `ResourceNotFoundException`; 409 `ConflictException` (nombre duplicado case-insensitive, o borrado con referencias); 422 `BusinessException`; 400 validación de Bean Validation.
- Reglas: `fecha_fin >= fecha_inicio`; si `PORCENTAJE`, `valor_descuento <= 100`; ≥1 producto por promoción; sin productos repetidos; borrado de producto bloqueado si está en promoción; borrado de categoría/inventario bloqueado si tienen productos.
- `disponible` y `estado` opcionales en Request: defaults `true` / `ACTIVO`.

### Endpoints
- `GET|POST /api/categorias`, `GET|PUT|DELETE /api/categorias/{id}`
- `GET|POST /api/inventarios`, `GET|PUT|DELETE /api/inventarios/{id}`
- `GET|POST /api/productos`, `GET|PUT|DELETE /api/productos/{id}`
- `GET|POST /api/promociones`, `GET|PUT|DELETE /api/promociones/{id}`

## 5. Tests

- `src/test/java/com/siede/AbstractIntegrationTest.java`: base con Testcontainers (postgres:16-alpine, db `siede_test`), `@ActiveProfiles("test")`, `@SpringBootTest(RANDOM_PORT)`.
- `src/test/java/com/siede/producto/ProductoFlowIntegrationTest.java`: flujo completo por HTTP (crear categoría → inventario → producto → promo; listados; validaciones). Usa `@AutoConfigureTestRestTemplate`.
- `SiedeApplicationTests.contextLoads`.
- Resultado actual: **4 tests OK**. Antes de tocar el dominio, correr `.\mvnw.cmd test` para no romper nada.

## 6. Gotchas técnicos (importantes; específicos de Boot 4.1.1)

- **Flyway**: Spring Boot 4 NO auto-configura Flyway con solo `flyway-core` en el classpath → requiere `spring-boot-starter-flyway` (+ `flyway-database-postgresql` runtime).
- **Testcontainers 2.x**: artefactos renombrados (`testcontainers`, `testcontainers-junit-jupiter`, `testcontainers-postgresql`); la clase es `org.testcontainers.postgresql.PostgreSQLContainer`.
- **TestRestTemplate**: en Boot 4 vive en `org.springframework.boot.resttestclient.TestRestTemplate` (no existe `org.springframework.boot.test.web.client`). Para inyectarlo: `@AutoConfigureTestRestTemplate` sobre el test + dependencia **test** `org.springframework.boot:spring-boot-restclient` (añadida al pom); sin ella falla con `NoClassDefFoundError: org.springframework.boot.restclient.RestTemplateBuilder`.
- **Spring 7**: el enum 422 se llama `HttpStatus.UNPROCESSABLE_CONTENT` (antes `UNPROCESSABLE_ENTITY`, quedó deprecado). `GlobalExceptionHandler` usa el valor deprecado y sigue compilando; en tests nuevos usar el nombre actual.
- **Versiones BOM Boot 4.1.1** de referencia: Flyway 12.4.0, Testcontainers 2.0.5, Hibernate 7.4.5, Spring Security 7.1.1, Tomcat 11.0.24, Spring Framework 7.0.9.
- Los tests con `@EntityGraph` y respuestas con nombres de relaciones evitan `LazyInitializationException` porque `create`/`update` re-leen la entidad con el graph antes de mapear a DTO.

## 7. Estado actual y próximos pasos

**Ya hecho y verificado**: infraestructura completa, dominio Producto implementado (migración V1 aplicada a la BD dev y validada por Hibernate), 4 tests verdes, app arranca en :8080, tablas y relaciones verificadas en Postgres (DBeaver listo).

**Próximos pasos sugeridos** (en orden lógico, respetando lo que el usuario vaya indicando):
1. Implementar el siguiente dominio vertical (ej. `cliente`, `empleado`+`rol`, `mesa` o `pedido`) siguiendo el mismo patrón (migración V(n) → entidades → repos → DTOs → mappers → services → controllers → tests de integración).
2. Cuando toque: agregar autenticación/autorización real en `shared/security` (hoy todo `permitAll`, contraseña de seguridad generada en consola).
3. Integrar Spring AI (los placeholders en `ai/` están vacíos; el config expone componente sin proveedor).
4. Documentar luego en un README si el usuario lo pide (hoy solo existe `HELP.md` del inicializador).

**Recuerda**: preguntar antes de implementar dominios nuevos; el usuario decide el alcance. Trabajar siempre con `.\mvnw.cmd test` y `docker compose up -d` para validar.
CREATE TABLE inventario (
    inventario_id BIGSERIAL PRIMARY KEY,
    nombre       VARCHAR(100) NOT NULL,
    descripcion  VARCHAR(255),
    ubicacion    VARCHAR(100) NOT NULL,
    estado       VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT chk_inventario_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE TABLE categoria_producto (
    categoria_id BIGSERIAL PRIMARY KEY,
    nombre       VARCHAR(100) NOT NULL,
    descripcion  VARCHAR(255),
    estado       VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT uk_categoria_producto_nombre UNIQUE (nombre),
    CONSTRAINT chk_categoria_producto_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE TABLE producto (
    producto_id      BIGSERIAL PRIMARY KEY,
    nombre           VARCHAR(100) NOT NULL,
    disponible       BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_expiracion DATE,
    precio           NUMERIC(10, 2) NOT NULL,
    categoria_id     BIGINT NOT NULL,
    inventario_id    BIGINT NOT NULL,
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria_producto (categoria_id),
    CONSTRAINT fk_producto_inventario FOREIGN KEY (inventario_id) REFERENCES inventario (inventario_id),
    CONSTRAINT chk_producto_precio CHECK (precio >= 0)
);

CREATE INDEX idx_producto_categoria ON producto (categoria_id);
CREATE INDEX idx_producto_inventario ON producto (inventario_id);

CREATE TABLE promocion (
    promocion_id    BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    descripcion     VARCHAR(255),
    fecha_inicio    DATE NOT NULL,
    fecha_fin       DATE NOT NULL,
    tipo_descuento  VARCHAR(20) NOT NULL,
    valor_descuento NUMERIC(10, 2) NOT NULL,
    estado          VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT chk_promocion_tipo_descuento CHECK (tipo_descuento IN ('PORCENTAJE', 'MONTO')),
    CONSTRAINT chk_promocion_valor_descuento CHECK (valor_descuento >= 0),
    CONSTRAINT chk_promocion_estado CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    CONSTRAINT chk_promocion_fechas CHECK (fecha_fin >= fecha_inicio)
);

CREATE TABLE promocion_producto (
    producto_id  BIGINT NOT NULL,
    promocion_id BIGINT NOT NULL,
    cantidad     INT NOT NULL,
    CONSTRAINT pk_promocion_producto PRIMARY KEY (producto_id, promocion_id),
    CONSTRAINT fk_promocion_producto_producto FOREIGN KEY (producto_id) REFERENCES producto (producto_id),
    CONSTRAINT fk_promocion_producto_promocion FOREIGN KEY (promocion_id) REFERENCES promocion (promocion_id),
    CONSTRAINT chk_promocion_producto_cantidad CHECK (cantidad > 0)
);

CREATE INDEX idx_promocion_producto_promocion ON promocion_producto (promocion_id);
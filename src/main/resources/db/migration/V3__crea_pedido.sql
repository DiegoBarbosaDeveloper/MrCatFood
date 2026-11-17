-- M5.1 · Pedidos, detalles y copias históricas de ubicación, nombre y precio.
-- PostgreSQL 18: NUMERIC exacto para importes y TIMESTAMPTZ para la creación.

CREATE TABLE pedido (
    id                    BIGSERIAL PRIMARY KEY,
    codigo                VARCHAR(20)   NOT NULL,
    cliente_id            BIGINT        NOT NULL,
    conjunto_residencial  VARCHAR(120)  NOT NULL,
    apartamento           VARCHAR(60)   NOT NULL,
    fecha_entrega         DATE          NOT NULL,
    hora_entrega          TIME          NOT NULL,
    notas                 VARCHAR(500),
    subtotal              NUMERIC(12,2) NOT NULL,
    total                 NUMERIC(12,2) NOT NULL,
    estado                VARCHAR(30)   NOT NULL,
    creado_en             TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_pedido_codigo UNIQUE (codigo),
    CONSTRAINT fk_pedido_cliente
        FOREIGN KEY (cliente_id) REFERENCES usuario (id),
    CONSTRAINT ck_pedido_subtotal_no_negativo CHECK (subtotal >= 0),
    CONSTRAINT ck_pedido_total_no_negativo CHECK (total >= 0),
    CONSTRAINT ck_pedido_estado CHECK (
        CASE estado
            WHEN 'PENDIENTE' THEN TRUE
            WHEN 'CONFIRMADO' THEN TRUE
            WHEN 'EN_PREPARACION' THEN TRUE
            WHEN 'ENTREGADO' THEN TRUE
            WHEN 'CANCELADO' THEN TRUE
            ELSE FALSE
        END
    )
);

CREATE INDEX ix_pedido_cliente ON pedido (cliente_id, creado_en DESC);
CREATE INDEX ix_pedido_estado ON pedido (estado, fecha_entrega);

CREATE TABLE detalle_pedido (
    id               BIGSERIAL PRIMARY KEY,
    pedido_id        BIGINT        NOT NULL,
    producto_id      BIGINT,
    nombre_producto  VARCHAR(120)  NOT NULL,
    precio_unitario  NUMERIC(12,2) NOT NULL,
    cantidad         INTEGER       NOT NULL,
    subtotal         NUMERIC(12,2) NOT NULL,

    CONSTRAINT fk_detalle_pedido_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedido (id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_pedido_producto
        FOREIGN KEY (producto_id) REFERENCES producto (id) ON DELETE SET NULL,
    CONSTRAINT ck_detalle_pedido_precio_no_negativo CHECK (precio_unitario >= 0),
    CONSTRAINT ck_detalle_pedido_cantidad_positiva CHECK (cantidad > 0),
    CONSTRAINT ck_detalle_pedido_subtotal_no_negativo CHECK (subtotal >= 0)
);

CREATE INDEX ix_detalle_pedido_pedido ON detalle_pedido (pedido_id);
CREATE INDEX ix_detalle_pedido_producto ON detalle_pedido (producto_id);

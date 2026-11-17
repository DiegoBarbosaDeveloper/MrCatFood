-- M4.1 · Catálogo de sándwiches, ingredientes e inventario inicial.
-- PostgreSQL 18: NUMERIC exacto para importes y versionado optimista para el stock.

CREATE TABLE producto (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(120)  NOT NULL,
    descripcion     VARCHAR(600),
    precio          NUMERIC(12,2) NOT NULL,
    stock           INTEGER       NOT NULL,
    activo          BOOLEAN       NOT NULL DEFAULT TRUE,
    version         BIGINT        NOT NULL DEFAULT 0,
    creado_en       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    actualizado_en  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_producto_precio_no_negativo CHECK (precio >= 0),
    CONSTRAINT ck_producto_stock_no_negativo CHECK (stock >= 0)
);

CREATE TABLE producto_ingrediente (
    id           BIGSERIAL PRIMARY KEY,
    producto_id  BIGINT       NOT NULL,
    nombre       VARCHAR(120) NOT NULL,

    CONSTRAINT fk_producto_ingrediente_producto
        FOREIGN KEY (producto_id) REFERENCES producto (id) ON DELETE CASCADE
);

CREATE INDEX ix_producto_ingrediente_producto ON producto_ingrediente (producto_id);

INSERT INTO producto (nombre, descripcion, precio, stock, activo) VALUES
    ('Sándwich de pollo', 'Pollo sazonado con vegetales frescos y salsa de la casa.', 15000.00, 20, TRUE),
    ('Sándwich de jamón y queso', 'Jamón y queso fundido en pan artesanal.', 12000.00, 15, TRUE),
    ('Sándwich vegetariano', 'Vegetales frescos, queso y aderezo de la casa.', 13000.00, 0, TRUE),
    ('Sándwich especial de la casa', 'Receta especial con doble proteína y queso.', 18000.00, 10, FALSE);

INSERT INTO producto_ingrediente (producto_id, nombre) VALUES
    (1, 'Pan artesanal'),
    (1, 'Pollo'),
    (1, 'Queso'),
    (1, 'Lechuga'),
    (1, 'Tomate'),
    (1, 'Salsa de la casa'),
    (2, 'Pan artesanal'),
    (2, 'Jamón'),
    (2, 'Queso'),
    (2, 'Mantequilla'),
    (3, 'Pan artesanal'),
    (3, 'Queso'),
    (3, 'Lechuga'),
    (3, 'Tomate'),
    (3, 'Cebolla'),
    (3, 'Aderezo de la casa'),
    (4, 'Pan artesanal'),
    (4, 'Pollo'),
    (4, 'Jamón'),
    (4, 'Queso'),
    (4, 'Salsa de la casa');

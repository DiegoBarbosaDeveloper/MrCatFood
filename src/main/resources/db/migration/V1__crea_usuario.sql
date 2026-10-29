-- M2.2 · Esquema base del registro de clientes.
-- PostgreSQL 18: BIGSERIAL, TIMESTAMPTZ y NOW().

CREATE TABLE usuario (
    id                    BIGSERIAL PRIMARY KEY,
    nombre                VARCHAR(120) NOT NULL,
    email                 VARCHAR(180) NOT NULL,
    telefono              VARCHAR(30)  NOT NULL,
    password_hash         VARCHAR(100) NOT NULL,
    rol                   VARCHAR(20)  NOT NULL,
    conjunto_residencial  VARCHAR(120),
    apartamento           VARCHAR(60),
    activo                BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en             TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_usuario_rol CHECK (rol IN ('CLIENTE', 'ADMIN')),
    -- RN-06: el correo se guarda normalizado, sin mayúsculas ni espacios en los extremos.
    CONSTRAINT ck_usuario_email_normalizado CHECK (email = LOWER(BTRIM(email)))
);

-- Unicidad sin distinguir mayúsculas: dos registros no pueden diferir solo en el correo.
CREATE UNIQUE INDEX ix_usuario_email ON usuario (LOWER(email));

-- HU-02: un JWT no se puede invalidar por sí solo; el cierre de sesión registra su jti.
CREATE TABLE token_revocado (
    jti        VARCHAR(64) PRIMARY KEY,
    expira_en  TIMESTAMPTZ NOT NULL
);

CREATE INDEX ix_token_revocado_expira_en ON token_revocado (expira_en);

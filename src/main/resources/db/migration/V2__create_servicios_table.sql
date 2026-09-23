CREATE TABLE servicios (
                           id BIGSERIAL PRIMARY KEY,
                           nombre VARCHAR(120) NOT NULL UNIQUE,
                           descripcion VARCHAR(500),
                           duracion_minutos INTEGER NOT NULL,
                           activo BOOLEAN NOT NULL DEFAULT TRUE,
                           version BIGINT NOT NULL DEFAULT 0,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT chk_servicios_duracion
                               CHECK (duracion_minutos > 0)
);
CREATE TABLE reservas (
                          id BIGSERIAL PRIMARY KEY,
                          usuario_id BIGINT NOT NULL,
                          servicio_id BIGINT NOT NULL,
                          fecha DATE NOT NULL,
                          hora_inicio TIME NOT NULL,
                          hora_fin TIME NOT NULL,
                          estado VARCHAR(20) NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_reservas_usuario
                              FOREIGN KEY (usuario_id)
                                  REFERENCES usuarios (id)
                                  ON DELETE CASCADE,

                          CONSTRAINT fk_reservas_servicio
                              FOREIGN KEY (servicio_id)
                                  REFERENCES servicios (id)
                                  ON DELETE CASCADE,

                          CONSTRAINT chk_reservas_estado
                              CHECK (estado IN (
                                                'PENDIENTE',
                                                'CONFIRMADA',
                                                'CANCELADA',
                                                'COMPLETADA'
                                  )),

                          CONSTRAINT chk_reservas_horario
                              CHECK (hora_inicio < hora_fin)
);
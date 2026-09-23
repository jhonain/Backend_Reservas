CREATE TABLE disponibilidades (
                                  id BIGSERIAL PRIMARY KEY,
                                  servicio_id BIGINT NOT NULL,
                                  dia_semana VARCHAR(20) NOT NULL,
                                  hora_inicio TIME NOT NULL,
                                  hora_fin TIME NOT NULL,
                                  activo BOOLEAN NOT NULL DEFAULT TRUE,
                                  version BIGINT NOT NULL DEFAULT 0,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_disponibilidades_servicio
                                      FOREIGN KEY (servicio_id)
                                          REFERENCES servicios (id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT chk_disponibilidades_dia
                                      CHECK (dia_semana IN (
                                                            'MONDAY',
                                                            'TUESDAY',
                                                            'WEDNESDAY',
                                                            'THURSDAY',
                                                            'FRIDAY',
                                                            'SATURDAY',
                                                            'SUNDAY'
                                          )),

                                  CONSTRAINT chk_disponibilidades_horario
                                      CHECK (hora_inicio < hora_fin)
);
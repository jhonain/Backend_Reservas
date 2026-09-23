CREATE INDEX idx_usuarios_correo
    ON usuarios (correo);

CREATE INDEX idx_servicios_nombre
    ON servicios (nombre);

CREATE INDEX idx_disponibilidades_servicio
    ON disponibilidades (servicio_id);

CREATE INDEX idx_reserva_servicio_fecha
    ON reservas (servicio_id, fecha);

CREATE INDEX idx_reserva_usuario_fecha
    ON reservas (usuario_id, fecha);

CREATE INDEX idx_refresh_tokens_usuario
    ON refresh_tokens (usuario_id);

CREATE INDEX idx_refresh_tokens_expira_en
    ON refresh_tokens (expira_en);
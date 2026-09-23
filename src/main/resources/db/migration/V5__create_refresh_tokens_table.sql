CREATE TABLE refresh_tokens (
                                id BIGSERIAL PRIMARY KEY,
                                usuario_id BIGINT NOT NULL,
                                token_hash VARCHAR(255) NOT NULL UNIQUE,
                                expira_en TIMESTAMP NOT NULL,
                                revocado BOOLEAN NOT NULL DEFAULT FALSE,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_refresh_tokens_usuario
                                    FOREIGN KEY (usuario_id)
                                        REFERENCES usuarios (id)
                                        ON DELETE CASCADE
);
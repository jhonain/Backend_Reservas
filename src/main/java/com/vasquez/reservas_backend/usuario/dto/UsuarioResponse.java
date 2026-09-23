package com.vasquez.reservas_backend.usuario.dto;

import com.vasquez.reservas_backend.usuario.entity.RolUsuario;
import com.vasquez.reservas_backend.usuario.entity.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(
        name = "UsuarioResponse",
        description = """
                Datos públicos y administrativos de un usuario registrado.

                Es un DTO de respuesta: no contiene ni expone la contraseña ni
                su hash. Todos sus campos son generados por el servidor.
                """
)
public record UsuarioResponse(

        @Schema(
                description = "Identificador único del usuario",
                example = "1",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Long id,

        @Schema(
                description = "Nombre completo o visible del usuario",
                example = "Juan Pérez",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String nombre,

        @Schema(
                description = "Correo electrónico único del usuario",
                example = "juan.perez@correo.com",
                format = "email",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String correo,

        @Schema(
                description = "Rol asignado al usuario",
                example = "CLIENTE",
                allowableValues = {
                        "CLIENTE",
                        "ADMIN"
                },
                accessMode = Schema.AccessMode.READ_ONLY
        )
        RolUsuario rol,

        @Schema(
                description = "Indica si el usuario puede iniciar sesión y realizar operaciones",
                example = "true",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        boolean activo,

        @Schema(
                description = "Fecha y hora UTC de creación del usuario, en formato ISO-8601",
                example = "2026-09-13T21:00:00Z",
                format = "date-time",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Instant createdAt,

        @Schema(
                description = "Fecha y hora UTC de la última actualización del usuario, en formato ISO-8601",
                example = "2026-09-13T21:30:00Z",
                format = "date-time",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Instant updatedAt
) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.isActivo(),
                usuario.getCreatedAt(),
                usuario.getUpdatedAt()
        );
    }
}
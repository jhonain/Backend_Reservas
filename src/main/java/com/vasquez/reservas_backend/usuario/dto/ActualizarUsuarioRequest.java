package com.vasquez.reservas_backend.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        name = "ActualizarUsuarioRequest",
        description = """
                Datos permitidos para actualizar un perfil de usuario.

                Actualmente, solo se permite modificar el nombre.
                """
)
public record ActualizarUsuarioRequest(

        @Schema(
                description = "Nombre completo o visible del usuario",
                example = "Juan Pérez Actualizado",
                maxLength = 120
        )
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String nombre
) {
}
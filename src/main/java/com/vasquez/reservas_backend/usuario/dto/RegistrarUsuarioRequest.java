package com.vasquez.reservas_backend.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        name = "RegistrarUsuarioRequest",
        description = """
                Datos necesarios para registrar un nuevo usuario.

                Los usuarios creados mediante el endpoint de registro obtienen
                automáticamente el rol CLIENTE.
                """
)
public record RegistrarUsuarioRequest(

        @Schema(
                description = "Nombre completo o visible del usuario",
                example = "Juan Pérez",
                maxLength = 120
        )
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String nombre,

        @Schema(
                description = "Correo electrónico único del usuario",
                example = "juan.perez@correo.com",
                format = "email",
                maxLength = 150
        )
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 150, message = "El correo no puede superar 150 caracteres")
        String correo,

        @Schema(
                description = """
                        Contraseña del usuario. Debe tener entre 8 y 100 caracteres.

                        Se recibe solamente al registrar y se almacena cifrada; nunca
                        se devuelve en las respuestas de la API.
                        """,
                example = "SecurePassword123!",
                minLength = 8,
                maxLength = 100,
                format = "password"
        )
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        String password
) {
}
package com.vasquez.reservas_backend.servicio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        name = "CrearServicioRequest",
        description = """
                Datos necesarios para crear un servicio disponible para reservas.

                El nombre debe ser único y la duración debe estar entre
                5 y 480 minutos.
                """
)
public record CrearServicioRequest(

        @Schema(
                description = "Nombre visible y único del servicio",
                example = "Corte de cabello",
                maxLength = 120
        )
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String nombre,

        @Schema(
                description = "Descripción opcional del servicio",
                example = "Corte de cabello tradicional con tijera y máquina",
                maxLength = 500,
                nullable = true
        )
        @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
        String descripcion,

        @Schema(
                description = "Duración total del servicio expresada en minutos",
                example = "30",
                minimum = "5",
                maximum = "480"
        )
        @Min(value = 5, message = "La duración mínima es 5 minutos")
        @Max(value = 480, message = "La duración máxima es 480 minutos")
        Integer duracionMinutos
) {
}
package com.vasquez.reservas_backend.servicio.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActualizarServicioRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String nombre,

        @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
        String descripcion,

        @Min(value = 5, message = "La duración mínima es 5 minutos")
        @Max(value = 480, message = "La duración máxima es 480 minutos")
        Integer duracionMinutos
) {
}

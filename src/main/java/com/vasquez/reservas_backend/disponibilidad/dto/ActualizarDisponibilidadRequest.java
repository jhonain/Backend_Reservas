package com.vasquez.reservas_backend.disponibilidad.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Schema(
        name = "ActualizarDisponibilidadRequest",
        description = """
                Datos para actualizar el día y el rango horario de una
                disponibilidad existente.
                """
)
public record ActualizarDisponibilidadRequest(

        @Schema(
                description = "Nuevo día de la semana, en inglés y mayúsculas",
                example = "TUESDAY",
                allowableValues = {
                        "MONDAY",
                        "TUESDAY",
                        "WEDNESDAY",
                        "THURSDAY",
                        "FRIDAY",
                        "SATURDAY",
                        "SUNDAY"
                }
        )
        @NotNull(message = "El día de la semana es obligatorio")
        DayOfWeek diaSemana,

        @Schema(
                description = "Nueva hora de inicio de la disponibilidad, formato HH:mm",
                example = "10:00"
        )
        @NotNull(message = "La hora de inicio es obligatoria")
        LocalTime horaInicio,

        @Schema(
                description = "Nueva hora de fin de la disponibilidad, formato HH:mm",
                example = "19:00"
        )
        @NotNull(message = "La hora de fin es obligatoria")
        LocalTime horaFin
) {
}
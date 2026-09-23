package com.vasquez.reservas_backend.disponibilidad.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Schema(
        name = "CrearDisponibilidadRequest",
        description = """
                Datos necesarios para crear una franja horaria disponible para
                un servicio en un día de la semana específico.
                """
)
public record CrearDisponibilidadRequest(

        @Schema(
                description = "Identificador del servicio al que pertenece el horario",
                example = "1"
        )
        @NotNull(message = "El servicio es obligatorio")
        Long servicioId,

        @Schema(
                description = "Día de la semana en inglés y mayúsculas",
                example = "MONDAY",
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
                description = "Hora de inicio de la franja disponible, formato HH:mm",
                example = "09:00"
        )
        @NotNull(message = "La hora de inicio es obligatoria")
        LocalTime horaInicio,

        @Schema(
                description = "Hora de fin de la franja disponible, formato HH:mm",
                example = "18:00"
        )
        @NotNull(message = "La hora de fin es obligatoria")
        LocalTime horaFin
) {
}
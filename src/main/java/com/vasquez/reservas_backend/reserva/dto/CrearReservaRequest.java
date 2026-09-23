package com.vasquez.reservas_backend.reserva.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(
        name = "CrearReservaRequest",
        description = """
                Datos necesarios para crear una reserva para el usuario autenticado.

                La fecha debe ser actual o futura. La fecha y hora completas deben
                ser futuras, estar dentro de una disponibilidad activa del servicio
                y no solaparse con otra reserva activa.
                """
)
public record CrearReservaRequest(

        @Schema(
                description = "Identificador del servicio que se desea reservar",
                example = "1"
        )
        @NotNull(message = "El servicio es obligatorio")
        Long servicioId,

        @Schema(
                description = "Fecha de la reserva en formato ISO-8601",
                example = "2026-09-21",
                format = "date"
        )
        @NotNull(message = "La fecha es obligatoria")
        @FutureOrPresent(message = "La fecha no puede estar en el pasado")
        LocalDate fecha,

        @Schema(
                description = "Hora de inicio solicitada, en formato HH:mm",
                example = "10:00"
        )
        @NotNull(message = "La hora de inicio es obligatoria")
        LocalTime horaInicio
) {
}
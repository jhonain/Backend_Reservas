package com.vasquez.reservas_backend.reserva.dto;

import com.vasquez.reservas_backend.reserva.entity.Reserva;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(
        name = "ReservaResponse",
        description = """
                Información completa de una reserva.

                Es un DTO de solo lectura: sus campos son generados por el
                servidor al crear, consultar o actualizar una reserva.
                """
)
public record ReservaResponse(

        @Schema(
                description = "Identificador único de la reserva",
                example = "1",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Long id,

        @Schema(
                description = "Identificador del usuario dueño de la reserva",
                example = "5",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Long usuarioId,

        @Schema(
                description = "Nombre del usuario dueño de la reserva",
                example = "Juan Pérez",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String usuarioNombre,

        @Schema(
                description = "Identificador del servicio reservado",
                example = "1",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Long servicioId,

        @Schema(
                description = "Nombre del servicio reservado",
                example = "Corte de cabello",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String servicioNombre,

        @Schema(
                description = "Fecha de la reserva en formato ISO-8601",
                example = "2026-09-21",
                format = "date",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        LocalDate fecha,

        @Schema(
                description = "Hora de inicio de la reserva, formato HH:mm",
                example = "10:00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        LocalTime horaInicio,

        @Schema(
                description = "Hora de término de la reserva, formato HH:mm",
                example = "10:30",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        LocalTime horaFin,

        @Schema(
                description = "Estado actual de la reserva",
                example = "PENDIENTE",
                allowableValues = {
                        "PENDIENTE",
                        "CONFIRMADA",
                        "COMPLETADA",
                        "CANCELADA"
                },
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String estado,

        @Schema(
                description = "Fecha y hora UTC de creación de la reserva, formato ISO-8601",
                example = "2026-09-13T21:00:00Z",
                format = "date-time",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Instant createdAt,

        @Schema(
                description = "Fecha y hora UTC de la última modificación, formato ISO-8601",
                example = "2026-09-13T21:30:00Z",
                format = "date-time",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Instant updatedAt
) {

    public static ReservaResponse desde(Reserva reserva) {
        return new ReservaResponse(
                reserva.getId(),
                reserva.getUsuario().getId(),
                reserva.getUsuario().getNombre(),
                reserva.getServicio().getId(),
                reserva.getServicio().getNombre(),
                reserva.getFecha(),
                reserva.getHoraInicio(),
                reserva.getHoraFin(),
                reserva.getEstado().name(),
                reserva.getCreatedAt(),
                reserva.getUpdatedAt()
        );
    }
}
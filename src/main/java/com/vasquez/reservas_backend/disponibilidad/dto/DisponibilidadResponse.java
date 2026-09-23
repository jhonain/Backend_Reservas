package com.vasquez.reservas_backend.disponibilidad.dto;

import com.vasquez.reservas_backend.disponibilidad.entity.Disponibilidad;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;

@Schema(
        name = "DisponibilidadResponse",
        description = """
                Información de una franja horaria disponible asociada a un servicio.

                Todos los campos de este objeto son generados por el servidor y
                se devuelven como respuesta de las operaciones de disponibilidad.
                """
)
public record DisponibilidadResponse(

        @Schema(
                description = "Identificador único de la disponibilidad",
                example = "1",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Long id,

        @Schema(
                description = "Identificador del servicio asociado",
                example = "1",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Long servicioId,

        @Schema(
                description = "Nombre del servicio asociado a la disponibilidad",
                example = "Corte de cabello",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String servicioNombre,

        @Schema(
                description = "Día de la semana en que aplica la disponibilidad",
                example = "MONDAY",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        DayOfWeek diaSemana,

        @Schema(
                description = "Hora de inicio de la franja disponible, formato HH:mm",
                example = "09:00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        LocalTime horaInicio,

        @Schema(
                description = "Hora de fin de la franja disponible, formato HH:mm",
                example = "18:00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        LocalTime horaFin,

        @Schema(
                description = "Indica si la disponibilidad se encuentra activa",
                example = "true",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        boolean activo,

        @Schema(
                description = "Fecha y hora UTC de creación del registro, formato ISO-8601",
                example = "2026-09-13T21:00:00Z",
                format = "date-time",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Instant createdAt,

        @Schema(
                description = "Fecha y hora UTC de la última actualización, formato ISO-8601",
                example = "2026-09-13T21:30:00Z",
                format = "date-time",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Instant updatedAt
) {

    public static DisponibilidadResponse desde(Disponibilidad disponibilidad) {
        return new DisponibilidadResponse(
                disponibilidad.getId(),
                disponibilidad.getServicio().getId(),
                disponibilidad.getServicio().getNombre(),
                disponibilidad.getDiaSemana(),
                disponibilidad.getHoraInicio(),
                disponibilidad.getHoraFin(),
                disponibilidad.isActivo(),
                disponibilidad.getCreatedAt(),
                disponibilidad.getUpdatedAt()
        );
    }
}
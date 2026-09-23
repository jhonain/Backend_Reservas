package com.vasquez.reservas_backend.servicio.dto;

import com.vasquez.reservas_backend.servicio.entity.Servicio;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(
        name = "ServicioResponse",
        description = """
                Información de un servicio registrado en el catálogo.

                Es un DTO de solo lectura: todos sus campos son generados por el
                servidor y se devuelven al crear, actualizar o consultar servicios.
                """
)
public record ServicioResponse(

        @Schema(
                description = "Identificador único del servicio",
                example = "1",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Long id,

        @Schema(
                description = "Nombre visible y único del servicio",
                example = "Corte de cabello",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String nombre,

        @Schema(
                description = "Descripción del servicio; puede ser nula",
                example = "Corte de cabello tradicional con tijera y máquina",
                nullable = true,
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String descripcion,

        @Schema(
                description = "Duración total del servicio expresada en minutos",
                example = "30",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Integer duracionMinutos,

        @Schema(
                description = "Indica si el servicio está disponible para nuevas reservas",
                example = "true",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        boolean activo,

        @Schema(
                description = "Fecha y hora UTC de creación del servicio, formato ISO-8601",
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

    public static ServicioResponse desde(Servicio servicio) {
        return new ServicioResponse(
                servicio.getId(),
                servicio.getNombre(),
                servicio.getDescripcion(),
                servicio.getDuracionMinutos(),
                servicio.isActivo(),
                servicio.getCreatedAt(),
                servicio.getUpdatedAt()
        );
    }
}
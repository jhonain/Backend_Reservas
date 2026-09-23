package com.vasquez.reservas_backend.reserva.dto;

import com.vasquez.reservas_backend.reserva.entity.EstadoReserva;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(
        name = "ActualizarEstadoReservaRequest",
        description = """
                Datos requeridos para cambiar el estado de una reserva.

                Esta operación solo puede ejecutarla un usuario con rol ADMIN.
                La transición solicitada debe respetar las reglas de negocio
                definidas para el estado actual de la reserva.
                """
)
public record ActualizarEstadoReservaRequest(

        @Schema(
                description = "Nuevo estado para la reserva",
                example = "CONFIRMADA",
                allowableValues = {
                        "PENDIENTE",
                        "CONFIRMADA",
                        "COMPLETADA",
                        "CANCELADA"
                }
        )
        @NotNull(message = "El estado es obligatorio")
        EstadoReserva estado
) {
}
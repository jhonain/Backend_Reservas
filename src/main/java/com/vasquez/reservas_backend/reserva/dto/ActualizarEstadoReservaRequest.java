package com.vasquez.reservas_backend.reserva.dto;

import com.vasquez.reservas_backend.reserva.entity.EstadoReserva;
import jakarta.validation.constraints.NotNull;

public record ActualizarEstadoReservaRequest(

        @NotNull(message = "El estado es obligatorio")
        EstadoReserva estado
) {
}
package com.vasquez.reservas_backend.reserva.dto;

import java.time.LocalDate;

public record DisponibilidadCambiadaEvent(
        String tipo,
        Long servicioId,
        LocalDate fecha
) {
}
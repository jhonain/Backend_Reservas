package com.vasquez.reservas_backend.reserva.Event;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservaCambiadaEvent(
        Long reservaId,
        Long usuarioId,
        Long servicioId,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        String tipo,
        String estado
) {
}
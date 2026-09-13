package com.vasquez.reservas_backend.reserva.dto;

import com.vasquez.reservas_backend.reserva.entity.Reserva;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record ReservaResponse(
        Long id,
        Long usuarioId,
        String usuarioNombre,
        Long servicioId,
        String servicioNombre,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        String estado,
        Instant createdAt,
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
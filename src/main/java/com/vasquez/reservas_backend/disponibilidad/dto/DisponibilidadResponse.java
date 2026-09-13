package com.vasquez.reservas_backend.disponibilidad.dto;

import com.vasquez.reservas_backend.disponibilidad.entity.Disponibilidad;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;

public record DisponibilidadResponse(
        Long id,
        Long servicioId,
        String servicioNombre,
        DayOfWeek diaSemana,
        LocalTime horaInicio,
        LocalTime horaFin,
        boolean activo,
        Instant createdAt,
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
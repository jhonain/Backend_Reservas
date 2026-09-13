package com.vasquez.reservas_backend.servicio.dto;


import com.vasquez.reservas_backend.servicio.entity.Servicio;

import java.time.Instant;

public record ServicioResponse(
        Long id,
        String nombre,
        String descripcion,
        Integer duracionMinutos,
        boolean activo,
        Instant createdAt,
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
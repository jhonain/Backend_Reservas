package com.vasquez.reservas_backend.disponibilidad.service;

import com.vasquez.reservas_backend.disponibilidad.dto.ActualizarDisponibilidadRequest;
import com.vasquez.reservas_backend.disponibilidad.dto.CrearDisponibilidadRequest;
import com.vasquez.reservas_backend.disponibilidad.dto.DisponibilidadResponse;

import java.time.DayOfWeek;
import java.util.List;

public interface DisponibilidadService {

    DisponibilidadResponse crear(CrearDisponibilidadRequest request);

    DisponibilidadResponse actualizar(Long id, ActualizarDisponibilidadRequest request);

    List<DisponibilidadResponse> listarPorServicio(Long servicioId);

    List<DisponibilidadResponse> listarPorServicioYDia(
            Long servicioId,
            DayOfWeek diaSemana
    );

    void desactivar(Long id);
}
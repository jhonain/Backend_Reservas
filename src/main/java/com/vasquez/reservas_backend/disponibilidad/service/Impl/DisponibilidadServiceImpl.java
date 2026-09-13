package com.vasquez.reservas_backend.disponibilidad.service.Impl;

import com.vasquez.reservas_backend.disponibilidad.dto.ActualizarDisponibilidadRequest;
import com.vasquez.reservas_backend.disponibilidad.dto.CrearDisponibilidadRequest;
import com.vasquez.reservas_backend.disponibilidad.dto.DisponibilidadResponse;
import com.vasquez.reservas_backend.disponibilidad.entity.Disponibilidad;
import com.vasquez.reservas_backend.disponibilidad.repository.DisponibilidadRepository;
import com.vasquez.reservas_backend.disponibilidad.service.DisponibilidadService;
import com.vasquez.reservas_backend.servicio.entity.Servicio;
import com.vasquez.reservas_backend.servicio.repository.ServicioRepository;
import com.vasquez.reservas_backend.shared.exception.BusinessException;
import com.vasquez.reservas_backend.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;

@Service
@Transactional
public class DisponibilidadServiceImpl implements DisponibilidadService {

    private final DisponibilidadRepository disponibilidadRepository;
    private final ServicioRepository servicioRepository;

    public DisponibilidadServiceImpl(
            DisponibilidadRepository disponibilidadRepository,
            ServicioRepository servicioRepository
    ) {
        this.disponibilidadRepository = disponibilidadRepository;
        this.servicioRepository = servicioRepository;
    }

    @Override
    public DisponibilidadResponse crear(CrearDisponibilidadRequest request) {
        Servicio servicio = servicioRepository.findById(request.servicioId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Servicio no encontrado con id " + request.servicioId()
                ));

        validarSinSuperposicion(
                request.servicioId(),
                request.diaSemana(),
                request.horaInicio(),
                request.horaFin(),
                null
        );

        Disponibilidad disponibilidad = new Disponibilidad(
                servicio,
                request.diaSemana(),
                request.horaInicio(),
                request.horaFin()
        );

        Disponibilidad guardada = disponibilidadRepository.save(disponibilidad);

        return DisponibilidadResponse.desde(guardada);
    }

    @Override
    public DisponibilidadResponse actualizar(
            Long id,
            ActualizarDisponibilidadRequest request
    ) {
        Disponibilidad disponibilidad = obtenerOFallar(id);

        validarSinSuperposicion(
                disponibilidad.getServicio().getId(),
                request.diaSemana(),
                request.horaInicio(),
                request.horaFin(),
                id
        );

        disponibilidad.actualizar(
                request.diaSemana(),
                request.horaInicio(),
                request.horaFin()
        );

        return DisponibilidadResponse.desde(disponibilidad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisponibilidadResponse> listarPorServicio(Long servicioId) {
        return disponibilidadRepository.findByServicioIdAndActivoTrue(servicioId)
                .stream()
                .map(DisponibilidadResponse::desde)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisponibilidadResponse> listarPorServicioYDia(
            Long servicioId,
            DayOfWeek diaSemana
    ) {
        return disponibilidadRepository
                .findByServicioIdAndDiaSemanaAndActivoTrue(servicioId, diaSemana)
                .stream()
                .map(DisponibilidadResponse::desde)
                .toList();
    }

    @Override
    public void desactivar(Long id) {
        Disponibilidad disponibilidad = obtenerOFallar(id);
        disponibilidad.desactivar();
    }

    private void validarSinSuperposicion(
            Long servicioId,
            DayOfWeek diaSemana,
            java.time.LocalTime horaInicio,
            java.time.LocalTime horaFin,
            Long idAIgnorar
    ) {
        List<Disponibilidad> existentes = disponibilidadRepository
                .findByServicioIdAndDiaSemanaAndActivoTrue(servicioId, diaSemana);

        boolean haySuperposicion = existentes.stream()
                .filter(d -> idAIgnorar == null || !d.getId().equals(idAIgnorar))
                .anyMatch(d -> d.seSuperponeCon(horaInicio, horaFin));

        if (haySuperposicion) {
            throw new BusinessException(
                    "Ya existe un bloque de disponibilidad que se superpone en ese horario"
            );
        }
    }

    private Disponibilidad obtenerOFallar(Long id) {
        return disponibilidadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Disponibilidad no encontrada con id " + id
                ));
    }
}
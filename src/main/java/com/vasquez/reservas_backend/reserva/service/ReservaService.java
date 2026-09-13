package com.vasquez.reservas_backend.reserva.service;

import com.vasquez.reservas_backend.reserva.dto.ActualizarEstadoReservaRequest;
import com.vasquez.reservas_backend.reserva.dto.CrearReservaRequest;
import com.vasquez.reservas_backend.reserva.dto.ReservaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservaService {

    ReservaResponse crear(
            CrearReservaRequest request,
            Long usuarioAutenticadoId
    );

    ReservaResponse buscarPorId(
            Long reservaId,
            Long usuarioAutenticadoId,
            boolean esAdmin
    );
    Page<ReservaResponse> listarMisReservas(
            Long usuarioAutenticadoId,
            Pageable pageable
    );

    Page<ReservaResponse> listarTodas(Pageable pageable);

    ReservaResponse actualizarEstado(
            Long reservaId,
            ActualizarEstadoReservaRequest request
    );

    void cancelar(
            Long reservaId,
            Long usuarioAutenticadoId,
            boolean esAdmin
    );
}
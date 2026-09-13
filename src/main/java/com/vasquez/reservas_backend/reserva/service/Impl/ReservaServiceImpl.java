package com.vasquez.reservas_backend.reserva.service.Impl;

import com.vasquez.reservas_backend.disponibilidad.entity.Disponibilidad;
import com.vasquez.reservas_backend.disponibilidad.repository.DisponibilidadRepository;
import com.vasquez.reservas_backend.reserva.dto.ActualizarEstadoReservaRequest;
import com.vasquez.reservas_backend.reserva.dto.CrearReservaRequest;
import com.vasquez.reservas_backend.reserva.dto.ReservaResponse;
import com.vasquez.reservas_backend.reserva.entity.EstadoReserva;
import com.vasquez.reservas_backend.reserva.entity.Reserva;
import com.vasquez.reservas_backend.reserva.repository.ReservaRepository;
import com.vasquez.reservas_backend.reserva.service.ReservaService;
import com.vasquez.reservas_backend.servicio.entity.Servicio;
import com.vasquez.reservas_backend.servicio.repository.ServicioRepository;
import com.vasquez.reservas_backend.shared.exception.BusinessException;
import com.vasquez.reservas_backend.shared.exception.ResourceNotFoundException;
import com.vasquez.reservas_backend.usuario.entity.Usuario;
import com.vasquez.reservas_backend.usuario.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicioRepository servicioRepository;
    private final DisponibilidadRepository disponibilidadRepository;

    public ReservaServiceImpl(
            ReservaRepository reservaRepository,
            UsuarioRepository usuarioRepository,
            ServicioRepository servicioRepository,
            DisponibilidadRepository disponibilidadRepository
    ) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.servicioRepository = servicioRepository;
        this.disponibilidadRepository = disponibilidadRepository;
    }

    @Override
    public ReservaResponse crear(
            CrearReservaRequest request,
            Long usuarioAutenticadoId
    ) {
        Usuario usuario = usuarioRepository.findById(usuarioAutenticadoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario autenticado no encontrado"
                ));

        if (!usuario.isActivo()) {
            throw new BusinessException(
                    "Tu cuenta está inactiva y no puede crear reservas"
            );
        }

        Servicio servicio = servicioRepository.findById(request.servicioId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Servicio no encontrado"
                ));

        if (!servicio.isActivo()) {
            throw new BusinessException(
                    "El servicio seleccionado no está disponible"
            );
        }

        LocalTime horaFin = request.horaInicio()
                .plusMinutes(servicio.getDuracionMinutos());

        validarDisponibilidad(
                servicio.getId(),
                request.fecha().getDayOfWeek(),
                request.horaInicio(),
                horaFin
        );

        boolean existeSolapamiento = reservaRepository.existeReservaSolapada(
                servicio.getId(),
                request.fecha(),
                request.horaInicio(),
                horaFin,
                Set.of(
                        EstadoReserva.PENDIENTE,
                        EstadoReserva.CONFIRMADA
                )
        );

        if (existeSolapamiento) {
            throw new BusinessException(
                    "El horario seleccionado ya no está disponible"
            );
        }

        Reserva reserva = new Reserva(
                usuario,
                servicio,
                request.fecha(),
                request.horaInicio(),
                horaFin
        );

        Reserva guardada = reservaRepository.save(reserva);

        return ReservaResponse.desde(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaResponse buscarPorId(
            Long reservaId,
            Long usuarioAutenticadoId,
            boolean esAdmin
    ) {
        Reserva reserva = obtenerOFallar(reservaId);

        validarPropietarioOAdmin(
                reserva,
                usuarioAutenticadoId,
                esAdmin
        );

        return ReservaResponse.desde(reserva);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponse> listarMisReservas(
            Long usuarioAutenticadoId,
            Pageable pageable
    ) {
        return reservaRepository
                .findByUsuarioIdOrderByFechaDescHoraInicioDesc(
                        usuarioAutenticadoId,
                        pageable
                )
                .map(ReservaResponse::desde);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponse> listarTodas(Pageable pageable) {
        return reservaRepository
                .findAllByOrderByFechaDescHoraInicioDesc(pageable)
                .map(ReservaResponse::desde);
    }

    @Override
    public ReservaResponse actualizarEstado(
            Long reservaId,
            ActualizarEstadoReservaRequest request
    ) {
        Reserva reserva = obtenerOFallar(reservaId);

        switch (request.estado()) {
            case CONFIRMADA -> reserva.confirmar();
            case COMPLETADA -> reserva.completar();
            case CANCELADA -> reserva.cancelar();
            case PENDIENTE -> throw new BusinessException(
                    "No se puede regresar una reserva al estado PENDIENTE"
            );
        }

        return ReservaResponse.desde(reserva);
    }

    @Override
    public void cancelar(
            Long reservaId,
            Long usuarioAutenticadoId,
            boolean esAdmin
    ) {
        Reserva reserva = obtenerOFallar(reservaId);

        validarPropietarioOAdmin(
                reserva,
                usuarioAutenticadoId,
                esAdmin
        );

        reserva.cancelar();
    }

    private void validarDisponibilidad(
            Long servicioId,
            DayOfWeek diaSemana,
            LocalTime horaInicio,
            LocalTime horaFin
    ) {
        List<Disponibilidad> bloques = disponibilidadRepository
                .findByServicioIdAndDiaSemanaAndActivoTrue(
                        servicioId,
                        diaSemana
                );

        boolean estaDentroDeUnBloque = bloques.stream()
                .anyMatch(bloque ->
                        !horaInicio.isBefore(bloque.getHoraInicio())
                                && !horaFin.isAfter(bloque.getHoraFin())
                );

        if (!estaDentroDeUnBloque) {
            throw new BusinessException(
                    "El horario seleccionado está fuera de la disponibilidad del servicio"
            );
        }
    }

    private void validarPropietarioOAdmin(
            Reserva reserva,
            Long usuarioAutenticadoId,
            boolean esAdmin
    ) {
        boolean esPropietario = reserva.getUsuario()
                .getId()
                .equals(usuarioAutenticadoId);

        if (!esAdmin && !esPropietario) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "No tienes permiso para acceder a esta reserva"
            );
        }
    }

    private Reserva obtenerOFallar(Long reservaId) {
        return reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reserva no encontrada con id " + reservaId
                ));
    }
}
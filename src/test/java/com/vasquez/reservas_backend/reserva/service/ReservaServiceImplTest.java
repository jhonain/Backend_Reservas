package com.vasquez.reservas_backend.reserva.service;


import com.vasquez.reservas_backend.disponibilidad.entity.Disponibilidad;
import com.vasquez.reservas_backend.disponibilidad.repository.DisponibilidadRepository;
import com.vasquez.reservas_backend.reserva.dto.CrearReservaRequest;
import com.vasquez.reservas_backend.reserva.entity.Reserva;
import com.vasquez.reservas_backend.reserva.repository.ReservaRepository;
import com.vasquez.reservas_backend.reserva.service.Impl.ReservaServiceImpl;
import com.vasquez.reservas_backend.servicio.entity.Servicio;
import com.vasquez.reservas_backend.servicio.repository.ServicioRepository;
import com.vasquez.reservas_backend.shared.exception.BusinessException;
import com.vasquez.reservas_backend.usuario.entity.RolUsuario;
import com.vasquez.reservas_backend.usuario.entity.Usuario;
import com.vasquez.reservas_backend.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private DisponibilidadRepository disponibilidadRepository;

    private ReservaServiceImpl reservaService;

    private Usuario usuario;
    private Servicio servicio;
    private CrearReservaRequest request;

    @BeforeEach
    void setUp() throws Exception {
        reservaService = new ReservaServiceImpl(
                reservaRepository,
                usuarioRepository,
                servicioRepository,
                disponibilidadRepository
        );

        usuario = new Usuario(
                "Cliente de prueba",
                "cliente@test.com",
                "hash",
                RolUsuario.CLIENTE
        );

        servicio = new Servicio(
                "Servicio de prueba",
                "Descripción",
                30
        );
        asignarId(servicio, 1L);

        request = new CrearReservaRequest(
                1L,
                LocalDate.of(2026, 9, 14),
                LocalTime.of(10, 0)
        );
    }

    @Test
    void deberiaCrearReservaCuandoDatosSonValidos() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(servicioRepository.findById(1L))
                .thenReturn(Optional.of(servicio));

        Disponibilidad disponibilidad = new Disponibilidad(
                servicio,
                request.fecha().getDayOfWeek(),
                LocalTime.of(9, 0),
                LocalTime.of(13, 0)
        );

        when(disponibilidadRepository
                .findByServicioIdAndDiaSemanaAndActivoTrue(
                        eq(1L),
                        eq(request.fecha().getDayOfWeek())
                ))
                .thenReturn(List.of(disponibilidad));

        when(reservaRepository.existeReservaSolapada(
                eq(1L),
                eq(request.fecha()),
                eq(request.horaInicio()),
                eq(LocalTime.of(10, 30)),
                any(Set.class)
        )).thenReturn(false);

        when(reservaRepository.save(any(Reserva.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = reservaService.crear(request, 1L);

        assertEquals("Servicio de prueba", response.servicioNombre());
        assertEquals(LocalTime.of(10, 0), response.horaInicio());
        assertEquals(LocalTime.of(10, 30), response.horaFin());
        assertEquals("PENDIENTE", response.estado());
    }

    @Test
    void noDeberiaCrearSiHorarioEstaFueraDeDisponibilidad() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(servicioRepository.findById(1L))
                .thenReturn(Optional.of(servicio));

        when(disponibilidadRepository
                .findByServicioIdAndDiaSemanaAndActivoTrue(
                        eq(1L),
                        eq(request.fecha().getDayOfWeek())
                ))
                .thenReturn(List.of());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reservaService.crear(request, 1L)
        );

        assertEquals(
                "El horario seleccionado está fuera de la disponibilidad del servicio",
                exception.getMessage()
        );
    }

    @Test
    void noDeberiaCrearSiHorarioEstaOcupado() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(servicioRepository.findById(1L))
                .thenReturn(Optional.of(servicio));

        Disponibilidad disponibilidad = new Disponibilidad(
                servicio,
                request.fecha().getDayOfWeek(),
                LocalTime.of(9, 0),
                LocalTime.of(13, 0)
        );

        when(disponibilidadRepository
                .findByServicioIdAndDiaSemanaAndActivoTrue(
                        eq(1L),
                        eq(request.fecha().getDayOfWeek())
                ))
                .thenReturn(List.of(disponibilidad));

        when(reservaRepository.existeReservaSolapada(
                eq(1L),
                eq(request.fecha()),
                eq(request.horaInicio()),
                eq(LocalTime.of(10, 30)),
                any(Set.class)
        )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reservaService.crear(request, 1L)
        );

        assertEquals(
                "El horario seleccionado ya no está disponible",
                exception.getMessage()
        );
    }

    private void asignarId(Servicio servicio, Long id) throws Exception {
        Field campoId = Servicio.class.getDeclaredField("id");
        campoId.setAccessible(true);
        campoId.set(servicio, id);
    }
}
package com.vasquez.reservas_backend.disponibilidad;

import com.vasquez.reservas_backend.disponibilidad.dto.ActualizarDisponibilidadRequest;
import com.vasquez.reservas_backend.disponibilidad.dto.CrearDisponibilidadRequest;
import com.vasquez.reservas_backend.disponibilidad.dto.DisponibilidadResponse;
import com.vasquez.reservas_backend.disponibilidad.entity.Disponibilidad;
import com.vasquez.reservas_backend.disponibilidad.repository.DisponibilidadRepository;
import com.vasquez.reservas_backend.disponibilidad.service.Impl.DisponibilidadServiceImpl;
import com.vasquez.reservas_backend.servicio.entity.Servicio;
import com.vasquez.reservas_backend.servicio.repository.ServicioRepository;
import com.vasquez.reservas_backend.shared.exception.BusinessException;
import com.vasquez.reservas_backend.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisponibilidadServiceImplTest {

    @Mock
    private DisponibilidadRepository disponibilidadRepository;

    @Mock
    private ServicioRepository servicioRepository;

    private DisponibilidadServiceImpl disponibilidadService;

    private Servicio servicio;

    @BeforeEach
    void setUp() throws Exception {
        disponibilidadService = new DisponibilidadServiceImpl(
                disponibilidadRepository,
                servicioRepository
        );

        servicio = new Servicio("Corte de cabello", "Descripción", 30);
        asignarId(servicio, 1L);
    }

    @Test
    void deberiaCrearDisponibilidadSinSuperposicion() {
        CrearDisponibilidadRequest request = new CrearDisponibilidadRequest(
                1L,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(13, 0)
        );

        when(servicioRepository.findById(1L))
                .thenReturn(Optional.of(servicio));
        when(disponibilidadRepository
                .findByServicioIdAndDiaSemanaAndActivoTrue(1L, DayOfWeek.MONDAY))
                .thenReturn(List.of());
        when(disponibilidadRepository.save(any(Disponibilidad.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DisponibilidadResponse response = disponibilidadService.crear(request);

        assertEquals(DayOfWeek.MONDAY, response.diaSemana());
        assertEquals(LocalTime.of(9, 0), response.horaInicio());
        verify(disponibilidadRepository).save(any(Disponibilidad.class));
    }

    @Test
    void noDeberiaCrearSiServicioNoExiste() {
        CrearDisponibilidadRequest request = new CrearDisponibilidadRequest(
                99L,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(13, 0)
        );

        when(servicioRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> disponibilidadService.crear(request)
        );
    }

    @Test
    void noDeberiaCrearSiSeSuperponeConBloqueExistente() {
        CrearDisponibilidadRequest request = new CrearDisponibilidadRequest(
                1L,
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        Disponibilidad existente = new Disponibilidad(
                servicio,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(13, 0)
        );

        when(servicioRepository.findById(1L))
                .thenReturn(Optional.of(servicio));
        when(disponibilidadRepository
                .findByServicioIdAndDiaSemanaAndActivoTrue(1L, DayOfWeek.MONDAY))
                .thenReturn(List.of(existente));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> disponibilidadService.crear(request)
        );

        assertEquals(
                "Ya existe un bloque de disponibilidad que se superpone en ese horario",
                exception.getMessage()
        );
        verify(disponibilidadRepository, never()).save(any(Disponibilidad.class));
    }

    @Test
    void deberiaActualizarIgnorandoSuPropioBloqueAlValidarSuperposicion() throws Exception {
        Disponibilidad disponibilidad = new Disponibilidad(
                servicio,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(13, 0)
        );
        asignarId(disponibilidad, 5L);

        when(disponibilidadRepository.findById(5L))
                .thenReturn(Optional.of(disponibilidad));
        when(disponibilidadRepository
                .findByServicioIdAndDiaSemanaAndActivoTrue(1L, DayOfWeek.MONDAY))
                .thenReturn(List.of(disponibilidad));

        DisponibilidadResponse response = disponibilidadService.actualizar(
                5L,
                new ActualizarDisponibilidadRequest(
                        DayOfWeek.MONDAY,
                        LocalTime.of(10, 0),
                        LocalTime.of(14, 0)
                )
        );

        assertEquals(LocalTime.of(10, 0), response.horaInicio());
        assertEquals(LocalTime.of(14, 0), response.horaFin());
    }

    @Test
    void noDeberiaActualizarSiNoExiste() {
        when(disponibilidadRepository.findById(404L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> disponibilidadService.actualizar(
                        404L,
                        new ActualizarDisponibilidadRequest(
                                DayOfWeek.MONDAY,
                                LocalTime.of(9, 0),
                                LocalTime.of(10, 0)
                        )
                )
        );
    }

    @Test
    void deberiaDesactivarDisponibilidad() throws Exception {
        Disponibilidad disponibilidad = new Disponibilidad(
                servicio,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(13, 0)
        );
        asignarId(disponibilidad, 5L);

        when(disponibilidadRepository.findById(5L))
                .thenReturn(Optional.of(disponibilidad));

        disponibilidadService.desactivar(5L);

        assertFalse(disponibilidad.isActivo());
    }

    private void asignarId(Object entidad, Long id) throws Exception {
        Field campoId = entidad.getClass().getDeclaredField("id");
        campoId.setAccessible(true);
        campoId.set(entidad, id);
    }
}

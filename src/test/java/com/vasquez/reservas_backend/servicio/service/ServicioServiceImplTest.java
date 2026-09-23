package com.vasquez.reservas_backend.servicio.service;

import com.vasquez.reservas_backend.servicio.dto.ActualizarServicioRequest;
import com.vasquez.reservas_backend.servicio.dto.CrearServicioRequest;
import com.vasquez.reservas_backend.servicio.dto.ServicioResponse;
import com.vasquez.reservas_backend.servicio.entity.Servicio;
import com.vasquez.reservas_backend.servicio.repository.ServicioRepository;
import com.vasquez.reservas_backend.servicio.service.Impl.ServicioServiceImpl;
import com.vasquez.reservas_backend.shared.exception.BusinessException;
import com.vasquez.reservas_backend.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioServiceImplTest {

    @Mock
    private ServicioRepository servicioRepository;

    private ServicioServiceImpl servicioService;

    @BeforeEach
    void setUp() {
        servicioService = new ServicioServiceImpl(servicioRepository);
    }

    @Test
    void deberiaCrearServicioConNombreDisponible() {
        CrearServicioRequest request = new CrearServicioRequest(
                "Corte de cabello",
                "Corte clásico",
                30
        );

        when(servicioRepository.existsByNombreIgnoreCase("Corte de cabello"))
                .thenReturn(false);
        when(servicioRepository.save(any(Servicio.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ServicioResponse response = servicioService.crear(request);

        assertEquals("Corte de cabello", response.nombre());
        assertEquals(30, response.duracionMinutos());
        verify(servicioRepository).save(any(Servicio.class));
    }

    @Test
    void noDeberiaCrearSiNombreYaExiste() {
        CrearServicioRequest request = new CrearServicioRequest(
                "Corte de cabello",
                "Corte clásico",
                30
        );

        when(servicioRepository.existsByNombreIgnoreCase("Corte de cabello"))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> servicioService.crear(request)
        );

        assertEquals("Ya existe un servicio con ese nombre", exception.getMessage());
        verify(servicioRepository, never()).save(any(Servicio.class));
    }

    @Test
    void deberiaActualizarSinCambiarNombre() throws Exception {
        Servicio servicio = new Servicio("Corte de cabello", "Corte clásico", 30);
        asignarId(servicio, 1L);

        when(servicioRepository.findById(1L))
                .thenReturn(Optional.of(servicio));

        ServicioResponse response = servicioService.actualizar(
                1L,
                new ActualizarServicioRequest("Corte de cabello", "Nueva descripción", 45)
        );

        assertEquals(45, response.duracionMinutos());
        assertEquals("Nueva descripción", response.descripcion());
        // No debe consultar duplicados si el nombre no cambió.
        verify(servicioRepository, never()).existsByNombreIgnoreCase(any());
    }

    @Test
    void noDeberiaActualizarSiNuevoNombreYaExisteEnOtroServicio() throws Exception {
        Servicio servicio = new Servicio("Corte de cabello", "Corte clásico", 30);
        asignarId(servicio, 1L);

        when(servicioRepository.findById(1L))
                .thenReturn(Optional.of(servicio));
        when(servicioRepository.existsByNombreIgnoreCase("Manicure"))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> servicioService.actualizar(
                        1L,
                        new ActualizarServicioRequest("Manicure", "Otra desc", 30)
                )
        );

        assertEquals("Ya existe un servicio con ese nombre", exception.getMessage());
    }

    @Test
    void noDeberiaActualizarSiServicioNoExiste() {
        when(servicioRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> servicioService.actualizar(
                        99L,
                        new ActualizarServicioRequest("X", "Y", 30)
                )
        );
    }

    @Test
    void deberiaDesactivarServicio() throws Exception {
        Servicio servicio = new Servicio("Corte de cabello", "Corte clásico", 30);
        asignarId(servicio, 1L);

        when(servicioRepository.findById(1L))
                .thenReturn(Optional.of(servicio));

        servicioService.desactivar(1L);

        assertFalse(servicio.isActivo());
    }

    private void asignarId(Servicio servicio, Long id) throws Exception {
        Field campoId = Servicio.class.getDeclaredField("id");
        campoId.setAccessible(true);
        campoId.set(servicio, id);
    }
}

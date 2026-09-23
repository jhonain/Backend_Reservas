package com.vasquez.reservas_backend.reserva.entity;

import com.vasquez.reservas_backend.servicio.entity.Servicio;
import com.vasquez.reservas_backend.usuario.entity.RolUsuario;
import com.vasquez.reservas_backend.usuario.entity.Usuario;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ReservaTest {

    @Test
    void deberiaCrearReservaEnEstadoPendiente() {
        Reserva reserva = crearReserva();
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void deberiaConfirmarUnaReservaPendiente() {
        Reserva reserva = crearReserva();
        reserva.confirmar();
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void noDeberiaCompletarUnaReservaPendiente() {
        Reserva reserva = crearReserva();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                reserva::completar
        );

        assertEquals(
                "Solo una reserva confirmada puede marcarse como completada",
                exception.getMessage()
        );
    }

    @Test
    void deberiaCompletarUnaReservaConfirmada() {
        Reserva reserva = crearReserva();
        reserva.confirmar();
        reserva.completar();
        assertEquals(EstadoReserva.COMPLETADA, reserva.getEstado());
    }

    @Test
    void noDeberiaCancelarUnaReservaCompletada() {
        Reserva reserva = crearReserva();
        reserva.confirmar();
        reserva.completar();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                reserva::cancelar
        );

        assertEquals(
                "No se puede cancelar una reserva completada",
                exception.getMessage()
        );
    }

    @Test
    void deberiaCancelarUnaReservaPendiente() {
        Reserva reserva = crearReserva();
        assertDoesNotThrow(reserva::cancelar);
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
    }

    private Reserva crearReserva() {
        Usuario usuario = new Usuario(
                "Cliente de prueba",
                "cliente@test.com",
                "hash-no-real",
                RolUsuario.CLIENTE
        );

        Servicio servicio = new Servicio(
                "Servicio de prueba",
                "Descripción",
                30
        );

        return new Reserva(
                usuario,
                servicio,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                LocalTime.of(10, 30)
        );
    }
}
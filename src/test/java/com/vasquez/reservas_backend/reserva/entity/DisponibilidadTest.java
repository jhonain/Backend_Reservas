package com.vasquez.reservas_backend.reserva.entity;

import com.vasquez.reservas_backend.disponibilidad.entity.Disponibilidad;
import com.vasquez.reservas_backend.servicio.entity.Servicio;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class DisponibilidadTest {

    @Test
    void deberiaCrearDisponibilidadValida() {
        Disponibilidad disponibilidad = new Disponibilidad(
                servicioDePrueba(),
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(13, 0)
        );

        assertTrue(disponibilidad.isActivo());
    }

    @Test
    void noDeberiaCrearSiHoraInicioEsDespuesDeHoraFin() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Disponibilidad(
                        servicioDePrueba(),
                        DayOfWeek.MONDAY,
                        LocalTime.of(14, 0),
                        LocalTime.of(9, 0)
                )
        );
    }

    @Test
    void deberiaDetectarSuperposicionDeHorarios() {
        Disponibilidad disponibilidad = new Disponibilidad(
                servicioDePrueba(),
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(13, 0)
        );

        boolean seSuperpone = disponibilidad.seSuperponeCon(
                LocalTime.of(12, 0),
                LocalTime.of(14, 0)
        );

        assertTrue(seSuperpone);
    }

    @Test
    void noDeberiaDetectarSuperposicionSiNoHayCruce() {
        Disponibilidad disponibilidad = new Disponibilidad(
                servicioDePrueba(),
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(13, 0)
        );

        boolean seSuperpone = disponibilidad.seSuperponeCon(
                LocalTime.of(15, 0),
                LocalTime.of(19, 0)
        );

        assertFalse(seSuperpone);
    }

    private Servicio servicioDePrueba() {
        return new Servicio(
                "Servicio de prueba",
                "Descripción",
                30
        );
    }
}
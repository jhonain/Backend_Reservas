package com.vasquez.reservas_backend.reserva.repository;

import com.vasquez.reservas_backend.config.JpaAuditingConfig;
import com.vasquez.reservas_backend.reserva.entity.EstadoReserva;
import com.vasquez.reservas_backend.reserva.entity.Reserva;
import com.vasquez.reservas_backend.servicio.entity.Servicio;
import com.vasquez.reservas_backend.usuario.entity.RolUsuario;
import com.vasquez.reservas_backend.usuario.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @DataJpaTest levanta solo la capa JPA con la BD H2 del perfil "test".
 * Las columnas created_at/updated_at son NOT NULL (heredadas de Auditable),
 * así que aunque ninguna aserción dependa de ellas, el INSERT falla sin
 * auditoría activa. @DataJpaTest no recoge @Configuration sueltas fuera
 * de su slice, por eso se importa explícitamente JpaAuditingConfig.
 */
@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class ReservaRepositoryTest {

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Autowired
    private ReservaRepository reservaRepository;

    private Usuario usuario;
    private Servicio servicio;
    private LocalDate fecha;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("Test User", "test@test.com", "hash", RolUsuario.CLIENTE);
        servicio = new Servicio("Servicio Test", "Descripción", 30);

        entityManager.persist(usuario);
        entityManager.persist(servicio);

        fecha = LocalDate.now().plusDays(1);
    }

    @Test
    void deberiaEncontrarReservasPorUsuarioOrdenadas() {
        Reserva reserva = new Reserva(
                usuario, servicio, fecha, LocalTime.of(10, 0), LocalTime.of(10, 30)
        );
        entityManager.persist(reserva);
        entityManager.flush();

        var resultado = reservaRepository
                .findByUsuarioIdOrderByFechaDescHoraInicioDesc(
                        usuario.getId(),
                        PageRequest.of(0, 10)
                );

        assertEquals(1, resultado.getTotalElements());
        assertEquals(reserva.getId(), resultado.getContent().get(0).getId());
    }

    @Test
    void deberiaDetectarSolapamientoCuandoHorariosSeCruzan() {
        Reserva reserva = new Reserva(
                usuario, servicio, fecha, LocalTime.of(10, 0), LocalTime.of(10, 30)
        );
        entityManager.persist(reserva);
        entityManager.flush();

        boolean existe = reservaRepository.existeReservaSolapada(
                servicio.getId(),
                fecha,
                LocalTime.of(10, 15),
                LocalTime.of(10, 45),
                Set.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA)
        );

        assertTrue(existe);
    }

    @Test
    void noDeberiaDetectarSolapamientoSiHorariosNoSeCruzan() {
        Reserva reserva = new Reserva(
                usuario, servicio, fecha, LocalTime.of(10, 0), LocalTime.of(10, 30)
        );
        entityManager.persist(reserva);
        entityManager.flush();

        boolean existe = reservaRepository.existeReservaSolapada(
                servicio.getId(),
                fecha,
                LocalTime.of(10, 30), // justo al límite, no se cruza
                LocalTime.of(11, 0),
                Set.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA)
        );

        assertFalse(existe);
    }

    @Test
    void noDeberiaDetectarSolapamientoSiElEstadoNoEstaEnLaListaBloqueante() {
        Reserva reserva = new Reserva(
                usuario, servicio, fecha, LocalTime.of(10, 0), LocalTime.of(10, 30)
        );
        reserva.cancelar();
        entityManager.persist(reserva);
        entityManager.flush();

        boolean existe = reservaRepository.existeReservaSolapada(
                servicio.getId(),
                fecha,
                LocalTime.of(10, 0),
                LocalTime.of(10, 30),
                Set.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA)
        );

        assertFalse(existe, "Una reserva CANCELADA no debe bloquear el horario");
    }

    @Test
    void noDeberiaDetectarSolapamientoSiEsOtroServicio() {
        Servicio otroServicio = new Servicio("Otro servicio", "Desc", 30);
        entityManager.persist(otroServicio);

        Reserva reserva = new Reserva(
                usuario, servicio, fecha, LocalTime.of(10, 0), LocalTime.of(10, 30)
        );
        entityManager.persist(reserva);
        entityManager.flush();

        boolean existe = reservaRepository.existeReservaSolapada(
                otroServicio.getId(),
                fecha,
                LocalTime.of(10, 0),
                LocalTime.of(10, 30),
                Set.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA)
        );

        assertFalse(existe);
    }

    @Test
    void listarTodasDeberiaOrdenarPorFechaYHoraDescendente() {
        Reserva reservaTemprano = new Reserva(
                usuario, servicio, fecha, LocalTime.of(9, 0), LocalTime.of(9, 30)
        );
        Reserva reservaTarde = new Reserva(
                usuario, servicio, fecha, LocalTime.of(15, 0), LocalTime.of(15, 30)
        );
        entityManager.persist(reservaTemprano);
        entityManager.persist(reservaTarde);
        entityManager.flush();

        var resultado = reservaRepository
                .findAllByOrderByFechaDescHoraInicioDesc(PageRequest.of(0, 10));

        assertEquals(2, resultado.getTotalElements());
        assertEquals(reservaTarde.getId(), resultado.getContent().get(0).getId());
        assertEquals(reservaTemprano.getId(), resultado.getContent().get(1).getId());
    }
}

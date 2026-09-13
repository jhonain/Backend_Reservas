package com.vasquez.reservas_backend.reserva.repository;

import com.vasquez.reservas_backend.reserva.entity.EstadoReserva;
import com.vasquez.reservas_backend.reserva.entity.Reserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
            FROM Reserva r
            WHERE r.servicio.id = :servicioId
              AND r.fecha = :fecha
              AND r.estado IN :estadosBloqueantes
              AND r.horaInicio < :horaFin
              AND r.horaFin > :horaInicio
            """)
    boolean existeReservaSolapada(
            @Param("servicioId") Long servicioId,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("estadosBloqueantes") java.util.Collection<EstadoReserva> estadosBloqueantes
    );

    Page<Reserva> findByUsuarioIdOrderByFechaDescHoraInicioDesc(
            Long usuarioId,
            Pageable pageable
    );

    Page<Reserva> findAllByOrderByFechaDescHoraInicioDesc(
            Pageable pageable
    );
}
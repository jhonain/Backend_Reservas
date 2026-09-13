package com.vasquez.reservas_backend.disponibilidad.repository;

import com.vasquez.reservas_backend.disponibilidad.entity.Disponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Long> {

    List<Disponibilidad> findByServicioIdAndActivoTrue(Long servicioId);

    List<Disponibilidad> findByServicioIdAndDiaSemanaAndActivoTrue(
            Long servicioId,
            DayOfWeek diaSemana
    );
}
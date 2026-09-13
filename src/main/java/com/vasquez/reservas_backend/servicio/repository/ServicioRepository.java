package com.vasquez.reservas_backend.servicio.repository;

import com.vasquez.reservas_backend.servicio.entity.Servicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    Page<Servicio> findByActivoTrue(Pageable pageable);
}

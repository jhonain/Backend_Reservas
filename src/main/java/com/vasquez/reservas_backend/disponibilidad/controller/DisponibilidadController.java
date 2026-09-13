package com.vasquez.reservas_backend.disponibilidad.controller;

import com.vasquez.reservas_backend.disponibilidad.dto.ActualizarDisponibilidadRequest;
import com.vasquez.reservas_backend.disponibilidad.dto.CrearDisponibilidadRequest;
import com.vasquez.reservas_backend.disponibilidad.dto.DisponibilidadResponse;
import com.vasquez.reservas_backend.disponibilidad.service.DisponibilidadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.List;

@RestController
@RequestMapping("/api/disponibilidades")
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    public DisponibilidadController(DisponibilidadService disponibilidadService) {
        this.disponibilidadService = disponibilidadService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DisponibilidadResponse crear(
            @Valid @RequestBody CrearDisponibilidadRequest request
    ) {
        return disponibilidadService.crear(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public DisponibilidadResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarDisponibilidadRequest request
    ) {
        return disponibilidadService.actualizar(id, request);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/servicio/{servicioId}")
    public List<DisponibilidadResponse> listarPorServicio(
            @PathVariable Long servicioId
    ) {
        return disponibilidadService.listarPorServicio(servicioId);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/servicio/{servicioId}/dia/{diaSemana}")
    public List<DisponibilidadResponse> listarPorServicioYDia(
            @PathVariable Long servicioId,
            @PathVariable DayOfWeek diaSemana
    ) {
        return disponibilidadService.listarPorServicioYDia(servicioId, diaSemana);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        disponibilidadService.desactivar(id);
    }
}
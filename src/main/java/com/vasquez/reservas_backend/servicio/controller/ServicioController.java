package com.vasquez.reservas_backend.servicio.controller;

import com.vasquez.reservas_backend.servicio.dto.ActualizarServicioRequest;
import com.vasquez.reservas_backend.servicio.dto.CrearServicioRequest;
import com.vasquez.reservas_backend.servicio.dto.ServicioResponse;
import com.vasquez.reservas_backend.servicio.service.ServicioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/servicios")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicioResponse crear(
            @Valid @RequestBody CrearServicioRequest request
    ) {
        return servicioService.crear(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ServicioResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarServicioRequest request
    ) {
        return servicioService.actualizar(id, request);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ServicioResponse buscarPorId(@PathVariable Long id) {
        return servicioService.buscarPorId(id);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public Page<ServicioResponse> listarActivos(Pageable pageable) {
        return servicioService.listarActivos(pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        servicioService.desactivar(id);
    }
}
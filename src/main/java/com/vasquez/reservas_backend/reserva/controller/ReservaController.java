package com.vasquez.reservas_backend.reserva.controller;

import com.vasquez.reservas_backend.auth.security.CustomUserDetails;
import com.vasquez.reservas_backend.reserva.dto.ActualizarEstadoReservaRequest;
import com.vasquez.reservas_backend.reserva.dto.CrearReservaRequest;
import com.vasquez.reservas_backend.reserva.dto.ReservaResponse;
import com.vasquez.reservas_backend.reserva.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse crear(
            @Valid @RequestBody CrearReservaRequest request,
            @AuthenticationPrincipal CustomUserDetails usuarioAutenticado
    ) {
        return reservaService.crear(request, usuarioAutenticado.getId());
    }

    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @GetMapping("/mis-reservas")
    public Page<ReservaResponse> listarMisReservas(
            @AuthenticationPrincipal CustomUserDetails usuarioAutenticado,
            Pageable pageable
    ) {
        return reservaService.listarMisReservas(
                usuarioAutenticado.getId(),
                pageable
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<ReservaResponse> listarTodas(Pageable pageable) {
        return reservaService.listarTodas(pageable);
    }

    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @GetMapping("/{id}")
    public ReservaResponse buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails usuarioAutenticado
    ) {
        return reservaService.buscarPorId(
                id,
                usuarioAutenticado.getId(),
                usuarioAutenticado.esAdmin()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/estado")
    public ReservaResponse actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoReservaRequest request
    ) {
        return reservaService.actualizarEstado(id, request);
    }

    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @PatchMapping("/{id}/cancelar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails usuarioAutenticado
    ) {

        reservaService.cancelar(
                id,
                usuarioAutenticado.getId(),
                usuarioAutenticado.esAdmin()
        );
    }
}
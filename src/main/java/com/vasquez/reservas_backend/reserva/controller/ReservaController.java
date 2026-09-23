package com.vasquez.reservas_backend.reserva.controller;

import com.vasquez.reservas_backend.auth.security.CustomUserDetails;
import com.vasquez.reservas_backend.reserva.dto.ActualizarEstadoReservaRequest;
import com.vasquez.reservas_backend.reserva.dto.CrearReservaRequest;
import com.vasquez.reservas_backend.reserva.dto.ReservaResponse;
import com.vasquez.reservas_backend.reserva.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Reservas",
        description = """
                Gestión de reservas de servicios.

                Los usuarios CLIENTE pueden crear, consultar y cancelar sus
                propias reservas. Los usuarios ADMIN pueden consultar todas las
                reservas, actualizar estados y también cancelar reservas.
                """
)
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @Operation(
            summary = "Crear una reserva",
            description = """
                    Crea una reserva para el usuario autenticado.

                    El horario debe ser futuro, debe estar dentro de la disponibilidad
                    activa del servicio y no puede solaparse con otra reserva activa.
                    La reserva se crea inicialmente con estado `PENDIENTE`.

                    Requiere rol CLIENTE o ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Reserva creada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos, fecha pasada, horario fuera de disponibilidad o solapado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT ausente o inválido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene un rol permitido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Servicio no encontrado",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse crear(
            @Valid @RequestBody CrearReservaRequest request,
            @AuthenticationPrincipal CustomUserDetails usuarioAutenticado
    ) {
        return reservaService.crear(request, usuarioAutenticado.getId());
    }

    @Operation(
            summary = "Listar mis reservas",
            description = """
                    Devuelve las reservas pertenecientes al usuario autenticado,
                    ordenadas y paginadas.

                    Requiere rol CLIENTE o ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservas obtenidas correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT ausente o inválido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene un rol permitido",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @GetMapping("/mis-reservas")
    public Page<ReservaResponse> listarMisReservas(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails usuarioAutenticado,

            @ParameterObject Pageable pageable
    ) {
        return reservaService.listarMisReservas(
                usuarioAutenticado.getId(),
                pageable
        );
    }

    @Operation(
            summary = "Listar todas las reservas",
            description = """
                    Devuelve todas las reservas registradas en el sistema,
                    utilizando paginación.

                    Requiere rol ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservas obtenidas correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT ausente o inválido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene rol ADMIN",
                    content = @Content
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<ReservaResponse> listarTodas(
            @ParameterObject Pageable pageable
    ) {
        return reservaService.listarTodas(pageable);
    }

    @Operation(
            summary = "Obtener una reserva por ID",
            description = """
                    Obtiene el detalle de una reserva.

                    Un CLIENTE solo puede consultar una reserva de su propiedad.
                    Un ADMIN puede consultar cualquier reserva.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reserva encontrada"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT ausente o inválido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El cliente intenta acceder a una reserva ajena",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reserva no encontrada",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @GetMapping("/{id}")
    public ReservaResponse buscarPorId(
            @Parameter(
                    description = "Identificador de la reserva",
                    example = "1",
                    required = true,
                    in = ParameterIn.PATH
            )
            @PathVariable Long id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails usuarioAutenticado
    ) {
        return reservaService.buscarPorId(
                id,
                usuarioAutenticado.getId(),
                usuarioAutenticado.esAdmin()
        );
    }

    @Operation(
            summary = "Actualizar el estado de una reserva",
            description = """
                    Actualiza el estado de una reserva existente.

                    Operación exclusiva del rol ADMIN. Las transiciones válidas
                    dependen de las reglas de negocio; por ejemplo, una reserva
                    `PENDIENTE` puede confirmarse y una reserva confirmada puede
                    completarse.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado de reserva actualizado correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Transición de estado no permitida o datos inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT ausente o inválido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene rol ADMIN",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reserva no encontrada",
                    content = @Content
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/estado")
    public ReservaResponse actualizarEstado(
            @Parameter(
                    description = "Identificador de la reserva",
                    example = "1",
                    required = true,
                    in = ParameterIn.PATH
            )
            @PathVariable Long id,

            @Valid @RequestBody ActualizarEstadoReservaRequest request
    ) {
        return reservaService.actualizarEstado(id, request);
    }

    @Operation(
            summary = "Cancelar una reserva",
            description = """
                    Cancela una reserva.

                    Un CLIENTE solamente puede cancelar una reserva propia.
                    Un ADMIN puede cancelar cualquier reserva. No es posible
                    cancelar una reserva que ya fue completada.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Reserva cancelada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La reserva no puede cancelarse por su estado actual",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT ausente o inválido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El cliente intenta cancelar una reserva ajena",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reserva no encontrada",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @PatchMapping("/{id}/cancelar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(
            @Parameter(
                    description = "Identificador de la reserva que se cancelará",
                    example = "1",
                    required = true,
                    in = ParameterIn.PATH
            )
            @PathVariable Long id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails usuarioAutenticado
    ) {
        reservaService.cancelar(
                id,
                usuarioAutenticado.getId(),
                usuarioAutenticado.esAdmin()
        );
    }
}
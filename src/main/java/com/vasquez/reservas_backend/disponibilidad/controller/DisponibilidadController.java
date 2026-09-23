package com.vasquez.reservas_backend.disponibilidad.controller;

import com.vasquez.reservas_backend.disponibilidad.dto.ActualizarDisponibilidadRequest;
import com.vasquez.reservas_backend.disponibilidad.dto.CrearDisponibilidadRequest;
import com.vasquez.reservas_backend.disponibilidad.dto.DisponibilidadResponse;
import com.vasquez.reservas_backend.disponibilidad.service.DisponibilidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.List;

@Tag(
        name = "Disponibilidad",
        description = """
                Gestión de horarios disponibles para cada servicio.

                Solo los usuarios con rol ADMIN pueden crear, editar o desactivar
                disponibilidades. Cualquier usuario autenticado puede consultar
                los horarios disponibles de un servicio.
                """
)
@RestController
@RequestMapping("/api/disponibilidades")
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    public DisponibilidadController(DisponibilidadService disponibilidadService) {
        this.disponibilidadService = disponibilidadService;
    }

    @Operation(
            summary = "Crear disponibilidad",
            description = """
                    Registra un rango de horario disponible para un servicio y
                    un día específico de la semana.

                    Requiere el rol ADMIN. El rango no puede tener la hora de fin
                    antes o igual a la hora de inicio, ni superponerse con un
                    horario activo existente del mismo servicio y día.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Disponibilidad creada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos, horario inválido o superpuesto",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT ausente o inválido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario autenticado no tiene rol ADMIN",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El servicio indicado no existe",
                    content = @Content
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DisponibilidadResponse crear(
            @Valid @RequestBody CrearDisponibilidadRequest request
    ) {
        return disponibilidadService.crear(request);
    }

    @Operation(
            summary = "Actualizar disponibilidad",
            description = """
                    Actualiza el rango horario de una disponibilidad existente.

                    Requiere el rol ADMIN. La nueva franja no puede superponerse
                    con otras disponibilidades activas del mismo servicio y día.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Disponibilidad actualizada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos, horario inválido o superpuesto",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT ausente o inválido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario autenticado no tiene rol ADMIN",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Disponibilidad no encontrada",
                    content = @Content
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public DisponibilidadResponse actualizar(
            @Parameter(
                    description = "Identificador de la disponibilidad a actualizar",
                    example = "1",
                    required = true,
                    in = ParameterIn.PATH
            )
            @PathVariable Long id,

            @Valid @RequestBody ActualizarDisponibilidadRequest request
    ) {
        return disponibilidadService.actualizar(id, request);
    }

    @Operation(
            summary = "Listar disponibilidades de un servicio",
            description = """
                    Obtiene todos los horarios activos configurados para un servicio.

                    Requiere que el usuario esté autenticado con un JWT válido.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Disponibilidades obtenidas correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT ausente o inválido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Servicio no encontrado",
                    content = @Content
            )
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/servicio/{servicioId}")
    public List<DisponibilidadResponse> listarPorServicio(
            @Parameter(
                    description = "Identificador del servicio",
                    example = "1",
                    required = true,
                    in = ParameterIn.PATH
            )
            @PathVariable Long servicioId
    ) {
        return disponibilidadService.listarPorServicio(servicioId);
    }

    @Operation(
            summary = "Consultar disponibilidad por día",
            description = """
                    Obtiene los horarios activos de un servicio para un día concreto
                    de la semana.

                    Requiere autenticación JWT. El valor de `diaSemana` debe ser
                    uno de: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
                    o SUNDAY.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Disponibilidades del día obtenidas correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Día de semana inválido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT ausente o inválido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Servicio no encontrado",
                    content = @Content
            )
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/servicio/{servicioId}/dia/{diaSemana}")
    public List<DisponibilidadResponse> listarPorServicioYDia(
            @Parameter(
                    description = "Identificador del servicio",
                    example = "1",
                    required = true,
                    in = ParameterIn.PATH
            )
            @PathVariable Long servicioId,

            @Parameter(
                    description = "Día de la semana en inglés y mayúsculas",
                    example = "MONDAY",
                    required = true,
                    in = ParameterIn.PATH
            )
            @PathVariable DayOfWeek diaSemana
    ) {
        return disponibilidadService.listarPorServicioYDia(
                servicioId,
                diaSemana
        );
    }

    @Operation(
            summary = "Desactivar disponibilidad",
            description = """
                    Desactiva una disponibilidad existente mediante borrado lógico.

                    La disponibilidad no se elimina físicamente de la base de datos,
                    pero deja de aparecer en las consultas y no puede usarse para
                    crear nuevas reservas.

                    Requiere el rol ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Disponibilidad desactivada correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT ausente o inválido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario autenticado no tiene rol ADMIN",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Disponibilidad no encontrada",
                    content = @Content
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(
            @Parameter(
                    description = "Identificador de la disponibilidad que se desactivará",
                    example = "1",
                    required = true,
                    in = ParameterIn.PATH
            )
            @PathVariable Long id
    ) {
        disponibilidadService.desactivar(id);
    }
}
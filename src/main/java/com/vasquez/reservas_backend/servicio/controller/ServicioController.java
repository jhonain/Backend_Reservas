package com.vasquez.reservas_backend.servicio.controller;

import com.vasquez.reservas_backend.servicio.dto.ActualizarServicioRequest;
import com.vasquez.reservas_backend.servicio.dto.CrearServicioRequest;
import com.vasquez.reservas_backend.servicio.dto.ServicioResponse;
import com.vasquez.reservas_backend.servicio.service.ServicioService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Servicios",
        description = """
                Gestión del catálogo de servicios disponibles para reserva.

                Los usuarios autenticados pueden consultar servicios activos.
                Solo los usuarios con rol ADMIN pueden crear, editar o
                desactivar servicios.
                """
)
@RestController
@RequestMapping("/api/servicios")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @Operation(
            summary = "Crear un servicio",
            description = """
                    Crea un servicio disponible para reserva.

                    Requiere rol ADMIN. El nombre debe ser único y la duración
                    debe respetar las validaciones del sistema.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Servicio creado correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o nombre de servicio ya registrado",
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
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicioResponse crear(
            @Valid @RequestBody CrearServicioRequest request
    ) {
        return servicioService.crear(request);
    }

    @Operation(
            summary = "Actualizar un servicio",
            description = """
                    Modifica los datos de un servicio existente.

                    Requiere rol ADMIN. El servicio debe existir y el nuevo nombre,
                    si cambia, no puede coincidir con otro servicio registrado.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Servicio actualizado correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o nombre de servicio duplicado",
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
                    description = "Servicio no encontrado",
                    content = @Content
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ServicioResponse actualizar(
            @Parameter(
                    description = "Identificador del servicio que se actualizará",
                    example = "1",
                    required = true,
                    in = ParameterIn.PATH
            )
            @PathVariable Long id,

            @Valid @RequestBody ActualizarServicioRequest request
    ) {
        return servicioService.actualizar(id, request);
    }

    @Operation(
            summary = "Obtener servicio por ID",
            description = """
                    Obtiene el detalle de un servicio.

                    Requiere que el usuario esté autenticado mediante un JWT válido.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Servicio encontrado"
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
    @GetMapping("/{id}")
    public ServicioResponse buscarPorId(
            @Parameter(
                    description = "Identificador del servicio",
                    example = "1",
                    required = true,
                    in = ParameterIn.PATH
            )
            @PathVariable Long id
    ) {
        return servicioService.buscarPorId(id);
    }

    @Operation(
            summary = "Listar servicios activos",
            description = """
                    Obtiene una lista paginada de servicios activos disponibles
                    para reservar.

                    Requiere autenticación mediante JWT.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Servicios activos obtenidos correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT ausente o inválido",
                    content = @Content
            )
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public Page<ServicioResponse> listarActivos(
            @ParameterObject Pageable pageable
    ) {
        return servicioService.listarActivos(pageable);
    }

    @Operation(
            summary = "Desactivar un servicio",
            description = """
                    Desactiva un servicio mediante borrado lógico.

                    El servicio no se elimina físicamente de la base de datos,
                    pero deja de aparecer en la lista de servicios activos y no
                    puede utilizarse para nuevas reservas.

                    Requiere rol ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Servicio desactivado correctamente"
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
                    description = "Servicio no encontrado",
                    content = @Content
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(
            @Parameter(
                    description = "Identificador del servicio que se desactivará",
                    example = "1",
                    required = true,
                    in = ParameterIn.PATH
            )
            @PathVariable Long id
    ) {
        servicioService.desactivar(id);
    }
}
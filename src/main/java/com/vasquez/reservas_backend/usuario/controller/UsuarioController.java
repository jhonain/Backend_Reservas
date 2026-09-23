package com.vasquez.reservas_backend.usuario.controller;

import com.vasquez.reservas_backend.usuario.dto.ActualizarUsuarioRequest;
import com.vasquez.reservas_backend.usuario.dto.RegistrarUsuarioRequest;
import com.vasquez.reservas_backend.usuario.dto.UsuarioResponse;
import com.vasquez.reservas_backend.usuario.service.UsuarioService;
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
        name = "Usuarios",
        description = """
                Registro y gestión de usuarios.

                El registro es público. Un usuario autenticado puede consultar y
                editar solamente su propio perfil. Los usuarios con rol ADMIN pueden
                consultar, editar, listar y desactivar cualquier usuario.
                """
)
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(
            summary = "Registrar usuario",
            description = """
                    Registra un nuevo usuario con rol CLIENTE.

                    Es un endpoint público: no requiere JWT. El correo debe tener
                    formato válido y no puede estar registrado previamente. La
                    contraseña se almacena cifrada, nunca en texto plano.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario registrado correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o correo ya registrado",
                    content = @Content
            )
    })
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(
            @Valid @RequestBody RegistrarUsuarioRequest request
    ) {
        return usuarioService.registrar(request);
    }

    @Operation(
            summary = "Actualizar usuario",
            description = """
                    Actualiza los datos permitidos de un usuario.

                    Un CLIENTE solo puede actualizar su propio perfil. Un ADMIN puede
                    actualizar cualquier usuario.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario actualizado correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT ausente o inválido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario intenta modificar un perfil ajeno",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content
            )
    })
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(
            @Parameter(
                    description = "Identificador del usuario a actualizar",
                    example = "1",
                    required = true,
                    in = ParameterIn.PATH
            )
            @PathVariable Long id,

            @Valid @RequestBody ActualizarUsuarioRequest request
    ) {
        return usuarioService.actualizar(id, request);
    }

    @Operation(
            summary = "Obtener usuario por ID",
            description = """
                    Obtiene los datos de un usuario.

                    Un CLIENTE solo puede consultar su propio perfil. Un ADMIN puede
                    consultar cualquier usuario.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario encontrado"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT ausente o inválido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario intenta consultar un perfil ajeno",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content
            )
    })
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @GetMapping("/{id}")
    public UsuarioResponse buscarPorId(
            @Parameter(
                    description = "Identificador del usuario",
                    example = "1",
                    required = true,
                    in = ParameterIn.PATH
            )
            @PathVariable Long id
    ) {
        return usuarioService.buscarPorId(id);
    }

    @Operation(
            summary = "Listar usuarios activos",
            description = """
                    Devuelve una lista paginada de los usuarios activos registrados
                    en el sistema.

                    Operación exclusiva para usuarios con rol ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuarios activos obtenidos correctamente"
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
    public Page<UsuarioResponse> listar(
            @ParameterObject Pageable pageable
    ) {
        return usuarioService.listarActivos(pageable);
    }

    @Operation(
            summary = "Desactivar usuario",
            description = """
                    Desactiva un usuario mediante borrado lógico.

                    El usuario no se elimina físicamente, pero no podrá iniciar
                    sesión ni ser utilizado para nuevas operaciones. Esta acción
                    requiere el rol ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Usuario desactivado correctamente"
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
                    description = "Usuario no encontrado",
                    content = @Content
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(
            @Parameter(
                    description = "Identificador del usuario que se desactivará",
                    example = "1",
                    required = true,
                    in = ParameterIn.PATH
            )
            @PathVariable Long id
    ) {
        usuarioService.desactivar(id);
    }
}
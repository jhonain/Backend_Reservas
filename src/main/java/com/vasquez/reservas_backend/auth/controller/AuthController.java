package com.vasquez.reservas_backend.auth.controller;


import com.vasquez.reservas_backend.auth.dto.LoginRequest;
import com.vasquez.reservas_backend.auth.dto.LoginResponse;
import com.vasquez.reservas_backend.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
        name = "Autenticación",
        description = "Login, renovación de sesión y cierre de sesión"
)

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Iniciar sesión",
            description = """
                Valida correo y contraseña.

                Retorna un access token JWT en el cuerpo de respuesta y envía
                un refresh token mediante una cookie httpOnly.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inicio de sesión exitoso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Correo o contraseña inválidos",
                    content = @Content
            )
    })
    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        return authService.login(request, response);
    }


    @Operation(
            summary = "Renovar access token",
            description = """
                    Genera un nuevo access token JWT utilizando la cookie httpOnly
                    `refreshToken` enviada automáticamente por el navegador.

                    No debes enviar el refresh token en el JSON. En Swagger UI,
                    primero ejecuta el login para que el navegador guarde la cookie.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Access token renovado correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cookie ausente, token inválido, expirado o revocado",
                    content = @Content
            )
    })
    @PostMapping("/refresh")
    public LoginResponse refrescar(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        return authService.refrescar(refreshToken, response);
    }


    @Operation(
            summary = "Cerrar sesión",
            description = """
                    Revoca el refresh token de la cookie `refreshToken` y ordena al
                    navegador eliminar dicha cookie. El access token actual seguirá
                    siendo válido hasta que expire.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sesión cerrada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cookie de sesión ausente o inválida",
                    content = @Content
            )
    })
    @PostMapping("/logout")
    public void logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        authService.logout(refreshToken, response);
    }
}
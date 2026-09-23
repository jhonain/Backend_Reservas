package com.vasquez.reservas_backend.auth.service;

import com.vasquez.reservas_backend.auth.dto.LoginRequest;
import com.vasquez.reservas_backend.auth.dto.LoginResponse;
import com.vasquez.reservas_backend.auth.entity.RefreshToken;
import com.vasquez.reservas_backend.auth.repository.RefreshTokenRepository;
import com.vasquez.reservas_backend.auth.security.JwtService;
import com.vasquez.reservas_backend.auth.service.Impl.AuthServiceImpl;
import com.vasquez.reservas_backend.shared.exception.BusinessException;
import com.vasquez.reservas_backend.usuario.entity.RolUsuario;
import com.vasquez.reservas_backend.usuario.entity.Usuario;
import com.vasquez.reservas_backend.usuario.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Nota: el refresh token viaja como cookie httpOnly (ver
 * AuthServiceImpl.agregarCookieRefreshToken), nunca en el cuerpo JSON.
 * Por eso estas pruebas verifican response.addCookie(...) en vez de
 * inspeccionar un campo "refreshToken" en LoginResponse, que no existe.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletResponse response;

    private AuthServiceImpl authService;

    private Usuario usuario;

    @BeforeEach
    void setUp() throws Exception {
        authService = new AuthServiceImpl(
                usuarioRepository,
                refreshTokenRepository,
                passwordEncoder,
                jwtService,
                7L // refreshExpirationDays
        );

        usuario = new Usuario(
                "Juan Pérez",
                "juan@test.com",
                "hash-almacenado",
                RolUsuario.CLIENTE
        );
        asignarId(usuario, 1L);
    }

    @Test
    void deberiaLoguearConCredencialesValidas() {
        LoginRequest request = new LoginRequest("juan@test.com", "password123");

        when(usuarioRepository.findByCorreoIgnoreCase("juan@test.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password123", "hash-almacenado"))
                .thenReturn(true);
        when(jwtService.generarAccessToken(1L, "juan@test.com", "CLIENTE"))
                .thenReturn("access-token-simulado");
        when(jwtService.getAccessExpirationSeconds())
                .thenReturn(1800L);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LoginResponse loginResponse = authService.login(request, response);

        assertEquals("access-token-simulado", loginResponse.accessToken());
        assertEquals("Bearer", loginResponse.tokenType());
        assertEquals(1800L, loginResponse.expiresInSeconds());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(response).addCookie(any());
    }

    @Test
    void noDeberiaLoguearSiCorreoNoExiste() {
        LoginRequest request = new LoginRequest("noexiste@test.com", "password123");

        when(usuarioRepository.findByCorreoIgnoreCase("noexiste@test.com"))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.login(request, response)
        );

        assertEquals("Credenciales inválidas", exception.getMessage());
        verify(refreshTokenRepository, never()).save(any());
        verify(response, never()).addCookie(any());
    }

    @Test
    void noDeberiaLoguearSiPasswordEsIncorrecto() {
        LoginRequest request = new LoginRequest("juan@test.com", "password-incorrecto");

        when(usuarioRepository.findByCorreoIgnoreCase("juan@test.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password-incorrecto", "hash-almacenado"))
                .thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.login(request, response)
        );

        assertEquals("Credenciales inválidas", exception.getMessage());
    }

    @Test
    void noDeberiaLoguearSiCuentaEstaInactiva() throws Exception {
        Usuario inactivo = new Usuario(
                "Inactivo",
                "inactivo@test.com",
                "hash",
                RolUsuario.CLIENTE
        );
        inactivo.desactivar();

        LoginRequest request = new LoginRequest("inactivo@test.com", "password123");

        when(usuarioRepository.findByCorreoIgnoreCase("inactivo@test.com"))
                .thenReturn(Optional.of(inactivo));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.login(request, response)
        );

        assertEquals("Credenciales inválidas", exception.getMessage());
        // No debe llegar a verificar password si la cuenta ya está inactiva.
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void deberiaRefrescarConTokenValido() {
        RefreshToken tokenGuardado = new RefreshToken(
                usuario,
                "hash-cualquiera",
                Instant.now().plusSeconds(3600)
        );

        when(refreshTokenRepository.findByTokenHash(any()))
                .thenReturn(Optional.of(tokenGuardado));
        when(jwtService.generarAccessToken(1L, "juan@test.com", "CLIENTE"))
                .thenReturn("nuevo-access-token");
        when(jwtService.getAccessExpirationSeconds())
                .thenReturn(1800L);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LoginResponse loginResponse = authService.refrescar("token-plano-cualquiera", response);

        assertEquals("nuevo-access-token", loginResponse.accessToken());
        assertTrue(tokenGuardado.isRevocado());
        verify(response).addCookie(any());
    }

    @Test
    void noDeberiaRefrescarSiTokenEsNulo() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.refrescar(null, response)
        );

        assertEquals("Sesión inválida, inicia sesión nuevamente", exception.getMessage());
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void noDeberiaRefrescarSiTokenNoExisteEnBd() {
        when(refreshTokenRepository.findByTokenHash(any()))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.refrescar("token-desconocido", response)
        );

        assertEquals("Sesión inválida, inicia sesión nuevamente", exception.getMessage());
    }

    @Test
    void noDeberiaRefrescarSiTokenEstaExpirado() {
        RefreshToken tokenExpirado = new RefreshToken(
                usuario,
                "hash-cualquiera",
                Instant.now().minusSeconds(60) // ya expiró
        );

        when(refreshTokenRepository.findByTokenHash(any()))
                .thenReturn(Optional.of(tokenExpirado));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.refrescar("token-expirado", response)
        );

        assertEquals("Sesión expirada, inicia sesión nuevamente", exception.getMessage());
    }

    @Test
    void noDeberiaRefrescarSiTokenYaFueRevocado() {
        RefreshToken tokenRevocado = new RefreshToken(
                usuario,
                "hash-cualquiera",
                Instant.now().plusSeconds(3600)
        );
        tokenRevocado.revocar();

        when(refreshTokenRepository.findByTokenHash(any()))
                .thenReturn(Optional.of(tokenRevocado));

        assertThrows(
                BusinessException.class,
                () -> authService.refrescar("token-revocado", response)
        );
    }

    @Test
    void logoutDeberiaRevocarTokenYLimpiarCookieCuandoExiste() {
        RefreshToken token = new RefreshToken(
                usuario,
                "hash-cualquiera",
                Instant.now().plusSeconds(3600)
        );

        when(refreshTokenRepository.findByTokenHash(any()))
                .thenReturn(Optional.of(token));

        authService.logout("token-plano", response);

        assertTrue(token.isRevocado());
        verify(response).addCookie(any());
    }

    @Test
    void logoutSinTokenSoloDeberiaLimpiarCookie() {
        authService.logout(null, response);

        verifyNoInteractions(refreshTokenRepository);
        verify(response).addCookie(any());
    }

    private void asignarId(Usuario usuario, Long id) throws Exception {
        Field campoId = Usuario.class.getDeclaredField("id");
        campoId.setAccessible(true);
        campoId.set(usuario, id);
    }
}

package com.vasquez.reservas_backend.auth.service.Impl;

import com.vasquez.reservas_backend.auth.dto.LoginRequest;
import com.vasquez.reservas_backend.auth.dto.LoginResponse;
import com.vasquez.reservas_backend.auth.entity.RefreshToken;
import com.vasquez.reservas_backend.auth.repository.RefreshTokenRepository;
import com.vasquez.reservas_backend.auth.security.JwtService;
import com.vasquez.reservas_backend.auth.service.AuthService;
import com.vasquez.reservas_backend.shared.exception.BusinessException;
import com.vasquez.reservas_backend.usuario.entity.Usuario;
import com.vasquez.reservas_backend.usuario.repository.UsuarioRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final String COOKIE_REFRESH_TOKEN = "refreshToken";

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long refreshExpirationDays;

    public AuthServiceImpl(
            UsuarioRepository usuarioRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${app.jwt.refresh-expiration-days}") long refreshExpirationDays
    ) {
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshExpirationDays = refreshExpirationDays;
    }

    @Override
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        logger.info("Intento de login: correo={}", request.correo());

        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(request.correo())
                .orElseThrow(() -> {
                    logger.warn("Login fallido, correo no registrado: {}", request.correo());
                    return new BusinessException("Credenciales inválidas");
                });

        if (!usuario.isActivo()) {
            logger.warn("Login fallido, cuenta inactiva: correo={}", request.correo());
            throw new BusinessException("Credenciales inválidas");
        }

        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            logger.warn("Login fallido, password incorrecto: correo={}", request.correo());
            throw new BusinessException("Credenciales inválidas");
        }

        String accessToken = jwtService.generarAccessToken(
                usuario.getId(),
                usuario.getCorreo(),
                usuario.getRol().name()
        );

        String refreshTokenPlano = generarRefreshTokenSeguro();
        String refreshTokenHash = hashSha256(refreshTokenPlano);

        Instant expiraEn = Instant.now().plusSeconds(refreshExpirationDays * 24 * 3600);

        RefreshToken refreshToken = new RefreshToken(
                usuario,
                refreshTokenHash,
                expiraEn
        );

        refreshTokenRepository.save(refreshToken);

        agregarCookieRefreshToken(response, refreshTokenPlano, refreshExpirationDays);

        logger.info("Login exitoso: correo={}, usuarioId={}", usuario.getCorreo(), usuario.getId());

        return LoginResponse.de(accessToken, jwtService.getAccessExpirationSeconds());
    }

    @Override
    public LoginResponse refrescar(String refreshTokenPlano, HttpServletResponse response) {
        if (refreshTokenPlano == null || refreshTokenPlano.isBlank()) {
            logger.warn("Intento de refresh sin token");
            throw new BusinessException("Sesión inválida, inicia sesión nuevamente");
        }

        String hash = hashSha256(refreshTokenPlano);

        RefreshToken tokenGuardado = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> {
                    logger.warn("Intento de refresh con token no reconocido");
                    return new BusinessException("Sesión inválida, inicia sesión nuevamente");
                });

        if (!tokenGuardado.esValido()) {
            logger.warn("Intento de refresh con token expirado/revocado: usuarioId={}",
                    tokenGuardado.getUsuario().getId());
            throw new BusinessException("Sesión expirada, inicia sesión nuevamente");
        }

        Usuario usuario = tokenGuardado.getUsuario();

        tokenGuardado.revocar();

        String nuevoRefreshTokenPlano = generarRefreshTokenSeguro();
        String nuevoHash = hashSha256(nuevoRefreshTokenPlano);
        Instant expiraEn = Instant.now().plusSeconds(refreshExpirationDays * 24 * 3600);

        RefreshToken nuevoRefreshToken = new RefreshToken(usuario, nuevoHash, expiraEn);
        refreshTokenRepository.save(nuevoRefreshToken);

        agregarCookieRefreshToken(response, nuevoRefreshTokenPlano, refreshExpirationDays);

        logger.info("Refresh exitoso: usuarioId={}", usuario.getId());

        String nuevoAccessToken = jwtService.generarAccessToken(
                usuario.getId(),
                usuario.getCorreo(),
                usuario.getRol().name()
        );

        return LoginResponse.de(nuevoAccessToken, jwtService.getAccessExpirationSeconds());
    }

    @Override
    public void logout(String refreshTokenPlano, HttpServletResponse response) {
        if (refreshTokenPlano != null && !refreshTokenPlano.isBlank()) {
            String hash = hashSha256(refreshTokenPlano);
            refreshTokenRepository.findByTokenHash(hash)
                    .ifPresent(token -> {
                        logger.info("Logout: usuarioId={}", token.getUsuario().getId());
                        token.revocar();
                    });
        }

        eliminarCookieRefreshToken(response);
    }

    private String generarRefreshTokenSeguro() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashSha256(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(valor.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Error al generar hash", exception);
        }
    }

    private void agregarCookieRefreshToken(
            HttpServletResponse response,
            String refreshTokenPlano,
            long expirationDays
    ) {
        Cookie cookie = new Cookie(COOKIE_REFRESH_TOKEN, refreshTokenPlano);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/api/auth");
        cookie.setMaxAge((int) (expirationDays * 24 * 3600));
        response.addCookie(cookie);
    }

    private void eliminarCookieRefreshToken(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_REFRESH_TOKEN, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
package com.vasquez.reservas_backend.auth.controller;

import com.vasquez.reservas_backend.auth.TestDatabaseCleaner;
import com.vasquez.reservas_backend.auth.dto.LoginRequest;
import com.vasquez.reservas_backend.usuario.entity.RolUsuario;
import com.vasquez.reservas_backend.usuario.entity.Usuario;
import com.vasquez.reservas_backend.usuario.repository.UsuarioRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIT {

    private static final String PASSWORD_PLANO = "SecurePassword123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestDatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        // Limpiar base de datos
        databaseCleaner.limpiar();

        // Usar correo único para evitar violaciones de unicidad
        String correoUnico = "test-" + UUID.randomUUID() + "@test.com";

        Usuario usuario = new Usuario(
                "Usuario de prueba",
                correoUnico,
                passwordEncoder.encode(PASSWORD_PLANO),
                RolUsuario.CLIENTE
        );
        usuarioRepository.save(usuario);
    }

    @Test
    void deberiaLoguearConCredencialesValidas() throws Exception {
        // Obtener el usuario creado en setUp
        var usuario = usuarioRepository.findAll().iterator().next();

        LoginRequest request = new LoginRequest(usuario.getCorreo(), PASSWORD_PLANO);

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("refreshToken", true));
    }

    @Test
    void noDeberiaLoguearConPasswordIncorrecto() throws Exception {
        var usuario = usuarioRepository.findAll().iterator().next();
        LoginRequest request = new LoginRequest(usuario.getCorreo(), "password-equivocado");

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }

    @Test
    void noDeberiaLoguearSiUsuarioNoExiste() throws Exception {
        LoginRequest request = new LoginRequest("noexiste@test.com", PASSWORD_PLANO);

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void noDeberiaLoguearConCorreoMalFormado() throws Exception {
        String jsonInvalido = """
                {"correo":"no-es-un-correo", "password":"cualquiera"}
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonInvalido)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void deberiaRefrescarUsandoLaCookieDeLogin() throws Exception {
        var usuario = usuarioRepository.findAll().iterator().next();
        LoginRequest loginRequest = new LoginRequest(usuario.getCorreo(), PASSWORD_PLANO);

        MvcResult loginResult = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isOk())
                .andReturn();

        Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");
        assertRefreshCookiePresente(refreshCookie);

        mockMvc.perform(post("/api/auth/refresh").cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(cookie().exists("refreshToken"));
    }

    @Test
    void noDeberiaRefrescarSinCookie() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Sesión inválida, inicia sesión nuevamente"));
    }

    @Test
    void noDeberiaRefrescarConCookieInvalida() throws Exception {
        mockMvc.perform(
                        post("/api/auth/refresh")
                                .cookie(new Cookie("refreshToken", "valor-inventado"))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void deberiaHacerLogoutYRevocarLaSesion() throws Exception {
        var usuario = usuarioRepository.findAll().iterator().next();
        LoginRequest loginRequest = new LoginRequest(usuario.getCorreo(), PASSWORD_PLANO);

        MvcResult loginResult = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isOk())
                .andReturn();

        Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");
        assertRefreshCookiePresente(refreshCookie);

        mockMvc.perform(post("/api/auth/logout").cookie(refreshCookie))
                .andExpect(status().isOk());

        // El token ya revocado no debe servir para refrescar.
        mockMvc.perform(post("/api/auth/refresh").cookie(refreshCookie))
                .andExpect(status().isBadRequest());
    }

    private void assertRefreshCookiePresente(Cookie cookie) {
        if (cookie == null) {
            throw new AssertionError("Se esperaba la cookie refreshToken tras el login");
        }
    }
}
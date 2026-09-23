package com.vasquez.reservas_backend.usuario.controller;

import com.vasquez.reservas_backend.auth.TestDatabaseCleaner;
import com.vasquez.reservas_backend.auth.dto.LoginRequest;
import com.vasquez.reservas_backend.usuario.dto.ActualizarUsuarioRequest;
import com.vasquez.reservas_backend.usuario.dto.RegistrarUsuarioRequest;
import com.vasquez.reservas_backend.usuario.entity.RolUsuario;
import com.vasquez.reservas_backend.usuario.entity.Usuario;
import com.vasquez.reservas_backend.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UsuarioControllerIT {

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
        databaseCleaner.limpiar();
    }

    @Test
    void deberiaRegistrarUsuarioConDatosValidos() throws Exception {
        String correoUnico = "juan-" + UUID.randomUUID() + "@test.com";

        RegistrarUsuarioRequest request = new RegistrarUsuarioRequest(
                "Juan Pérez",
                correoUnico,
                PASSWORD_PLANO
        );

        mockMvc.perform(
                        post("/api/usuarios/registro")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.correo").value(correoUnico))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    void noDeberiaRegistrarConCorreoDuplicado() throws Exception {
        String correoUnico = "juan-" + UUID.randomUUID() + "@test.com";

        RegistrarUsuarioRequest request = new RegistrarUsuarioRequest(
                "Juan Pérez",
                correoUnico,
                PASSWORD_PLANO
        );

        mockMvc.perform(
                        post("/api/usuarios/registro")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/usuarios/registro")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Ya existe un usuario registrado con ese correo"));
    }

    @Test
    void noDeberiaRegistrarSinNombre() throws Exception {
        String jsonInvalido = """
                {"correo":"test@test.com", "password":"SecurePassword123!"}
                """;

        mockMvc.perform(
                        post("/api/usuarios/registro")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonInvalido)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void noDeberiaRegistrarConPasswordCorto() throws Exception {
        String correoUnico = "juan-" + UUID.randomUUID() + "@test.com";

        RegistrarUsuarioRequest request = new RegistrarUsuarioRequest(
                "Juan Pérez",
                correoUnico,
                "1234567"
        );

        mockMvc.perform(
                        post("/api/usuarios/registro")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void deberiaRetornar401SinTokenAlConsultarUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteNoDeberiaPoderListarUsuarios() throws Exception {
        String correoUnico = "cliente-" + UUID.randomUUID() + "@test.com";
        String tokenCliente = registrarYObtenerAccessToken(
                "Cliente Uno", correoUnico
        );

        mockMvc.perform(
                        get("/api/usuarios")
                                .header("Authorization", "Bearer " + tokenCliente)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void clienteDeberiaPoderVerYActualizarSuPropioPerfil() throws Exception {
        String correoUnico = "propio-" + UUID.randomUUID() + "@test.com";
        String tokenCliente = registrarYObtenerAccessToken("Nombre Original", correoUnico);
        Long id = usuarioRepository.findByCorreoIgnoreCase(correoUnico).orElseThrow().getId();

        mockMvc.perform(
                        get("/api/usuarios/" + id)
                                .header("Authorization", "Bearer " + tokenCliente)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nombre Original"));

        mockMvc.perform(
                        put("/api/usuarios/" + id)
                                .header("Authorization", "Bearer " + tokenCliente)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new ActualizarUsuarioRequest("Nombre Actualizado")
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nombre Actualizado"));
    }

    @Test
    void clienteNoDeberiaPoderVerPerfilDeOtroUsuario() throws Exception {
        String correoA = "clienteA-" + UUID.randomUUID() + "@test.com";
        String correoB = "clienteB-" + UUID.randomUUID() + "@test.com";

        String tokenClienteA = registrarYObtenerAccessToken("Cliente A", correoA);
        registrarYObtenerAccessToken("Cliente B", correoB);
        Long idClienteB = usuarioRepository
                .findByCorreoIgnoreCase(correoB)
                .orElseThrow()
                .getId();

        mockMvc.perform(
                        get("/api/usuarios/" + idClienteB)
                                .header("Authorization", "Bearer " + tokenClienteA)
                )
                .andExpect(status().isForbidden());
    }

    private String registrarYObtenerAccessToken(String nombre, String correo) throws Exception {
        usuarioRepository.save(new Usuario(
                nombre,
                correo,
                passwordEncoder.encode(PASSWORD_PLANO),
                RolUsuario.CLIENTE
        ));

        var loginResult = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new LoginRequest(correo, PASSWORD_PLANO)
                                ))
                )
                .andExpect(status().isOk())
                .andReturn();

        String body = loginResult.getResponse().getContentAsString();
        return objectMapper.readTree(body).get("accessToken").asText();
    }
}
package com.vasquez.reservas_backend.reserva.controller;

import com.vasquez.reservas_backend.auth.TestDatabaseCleaner;
import com.vasquez.reservas_backend.auth.dto.LoginRequest;
import com.vasquez.reservas_backend.disponibilidad.entity.Disponibilidad;
import com.vasquez.reservas_backend.disponibilidad.repository.DisponibilidadRepository;
import com.vasquez.reservas_backend.reserva.dto.ActualizarEstadoReservaRequest;
import com.vasquez.reservas_backend.reserva.dto.CrearReservaRequest;
import com.vasquez.reservas_backend.reserva.entity.EstadoReserva;
import com.vasquez.reservas_backend.reserva.entity.Reserva;
import com.vasquez.reservas_backend.reserva.repository.ReservaRepository;
import com.vasquez.reservas_backend.servicio.entity.Servicio;
import com.vasquez.reservas_backend.servicio.repository.ServicioRepository;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservaControllerIT {

    private static final String PASSWORD_PLANO = "SecurePassword123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private DisponibilidadRepository disponibilidadRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestDatabaseCleaner databaseCleaner;

    private Servicio servicio;

    @BeforeEach
    void setUp() {
        // Limpiar toda la base de datos en orden correcto
        databaseCleaner.limpiar();

        servicio = servicioRepository.save(
                new Servicio(
                        "Corte de cabello"+UUID.randomUUID(),
                        "Corte clásico",
                        30)
        );

        disponibilidadRepository.save(new Disponibilidad(
                servicio,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(18, 0)
        ));
    }

    @Test
    void deberiaRetornar401CuandoNoSeEnviaToken() throws Exception {
        mockMvc.perform(get("/api/reservas/mis-reservas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message")
                        .value("Debes iniciar sesión para acceder a este recurso"));
    }

    @Test
    void deberiaRetornar401CuandoTokenEsInvalido() throws Exception {
        mockMvc.perform(
                        get("/api/reservas/mis-reservas")
                                .header("Authorization", "Bearer token-invalido-123")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteDeberiaPoderCrearReservaEnHorarioDisponible() throws Exception {
        String correoUnico = "cliente1-" + UUID.randomUUID() + "@test.com";
        String token = registrarYObtenerAccessToken(
                "Cliente Uno", correoUnico, RolUsuario.CLIENTE
        );

        LocalDate proximoLunes = proximoDiaSemana(DayOfWeek.MONDAY);
        CrearReservaRequest request = new CrearReservaRequest(
                servicio.getId(), proximoLunes, LocalTime.of(10, 0)
        );

        mockMvc.perform(
                        post("/api/reservas")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.servicioId").value(servicio.getId()));
    }

    @Test
    void noDeberiaCrearReservaFueraDeDisponibilidad() throws Exception {
        String correoUnico = "cliente2-" + UUID.randomUUID() + "@test.com";
        String token = registrarYObtenerAccessToken(
                "Cliente Dos", correoUnico, RolUsuario.CLIENTE
        );

        LocalDate proximoMartes = proximoDiaSemana(DayOfWeek.TUESDAY);
        CrearReservaRequest request = new CrearReservaRequest(
                servicio.getId(), proximoMartes, LocalTime.of(10, 0)
        );

        mockMvc.perform(
                        post("/api/reservas")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("El horario seleccionado está fuera de la disponibilidad del servicio"));
    }

    @Test
    void noDeberiaCrearReservaConServicioInexistente() throws Exception {
        String correoUnico = "cliente3-" + UUID.randomUUID() + "@test.com";
        String token = registrarYObtenerAccessToken(
                "Cliente Tres", correoUnico, RolUsuario.CLIENTE
        );

        CrearReservaRequest request = new CrearReservaRequest(
                999L, proximoDiaSemana(DayOfWeek.MONDAY), LocalTime.of(10, 0)
        );

        mockMvc.perform(
                        post("/api/reservas")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void clienteNoDeberiaVerListaCompletaDeReservas() throws Exception {
        String correoUnico = "cliente4-" + UUID.randomUUID() + "@test.com";
        String token = registrarYObtenerAccessToken(
                "Cliente Cuatro", correoUnico, RolUsuario.CLIENTE
        );

        mockMvc.perform(
                        get("/api/reservas")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminDeberiaPoderVerListaCompletaDeReservas() throws Exception {
        String correoUnico = "admin1-" + UUID.randomUUID() + "@test.com";
        String token = registrarYObtenerAccessToken(
                "Admin Uno", correoUnico, RolUsuario.ADMIN
        );

        mockMvc.perform(
                        get("/api/reservas")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk());
    }

    @Test
    void clienteNoDeberiaVerReservaDeOtroCliente() throws Exception {
        String correoPropietario = "propietario-" + UUID.randomUUID() + "@test.com";
        String correoOtroCliente = "otro-" + UUID.randomUUID() + "@test.com";

        Usuario propietario = usuarioRepository.save(new Usuario(
                "Propietario", correoPropietario,
                passwordEncoder.encode(PASSWORD_PLANO), RolUsuario.CLIENTE
        ));
        Reserva reservaAjena = reservaRepository.save(new Reserva(
                propietario, servicio,
                proximoDiaSemana(DayOfWeek.MONDAY),
                LocalTime.of(11, 0), LocalTime.of(11, 30)
        ));

        String tokenOtroCliente = registrarYObtenerAccessToken(
                "Otro Cliente", correoOtroCliente, RolUsuario.CLIENTE
        );

        mockMvc.perform(
                        get("/api/reservas/" + reservaAjena.getId())
                                .header("Authorization", "Bearer " + tokenOtroCliente)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void clienteDeberiaPoderCancelarSuPropiaReserva() throws Exception {
        String correoUnico = "cliente5-" + UUID.randomUUID() + "@test.com";
        String token = registrarYObtenerAccessToken(
                "Cliente Cinco", correoUnico, RolUsuario.CLIENTE
        );

        LocalDate proximoLunes = proximoDiaSemana(DayOfWeek.MONDAY);
        CrearReservaRequest request = new CrearReservaRequest(
                servicio.getId(), proximoLunes, LocalTime.of(12, 0)
        );

        String bodyCreacion = mockMvc.perform(
                        post("/api/reservas")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long reservaId = objectMapper.readTree(bodyCreacion).get("id").asLong();

        mockMvc.perform(
                        patch("/api/reservas/" + reservaId + "/cancelar")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void clienteNoDeberiaPoderActualizarEstadoDeReserva() throws Exception {
        String correoUnico = "cliente6-" + UUID.randomUUID() + "@test.com";
        String token = registrarYObtenerAccessToken(
                "Cliente Seis", correoUnico, RolUsuario.CLIENTE
        );

        Usuario cliente = usuarioRepository.findByCorreoIgnoreCase(correoUnico)
                .orElseThrow();
        Reserva reserva = reservaRepository.save(new Reserva(
                cliente, servicio,
                proximoDiaSemana(DayOfWeek.MONDAY),
                LocalTime.of(13, 0), LocalTime.of(13, 30)
        ));

        mockMvc.perform(
                        patch("/api/reservas/" + reserva.getId() + "/estado")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new ActualizarEstadoReservaRequest(EstadoReserva.CONFIRMADA)
                                ))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminDeberiaPoderConfirmarReserva() throws Exception {
        String correoCliente = "cliente7-" + UUID.randomUUID() + "@test.com";
        String correoAdmin = "admin2-" + UUID.randomUUID() + "@test.com";

        Usuario cliente = usuarioRepository.save(new Usuario(
                "Cliente Siete", correoCliente,
                passwordEncoder.encode(PASSWORD_PLANO), RolUsuario.CLIENTE
        ));
        Reserva reserva = reservaRepository.save(new Reserva(
                cliente, servicio,
                proximoDiaSemana(DayOfWeek.MONDAY),
                LocalTime.of(14, 0), LocalTime.of(14, 30)
        ));

        String tokenAdmin = registrarYObtenerAccessToken(
                "Admin Dos", correoAdmin, RolUsuario.ADMIN
        );

        mockMvc.perform(
                        patch("/api/reservas/" + reserva.getId() + "/estado")
                                .header("Authorization", "Bearer " + tokenAdmin)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new ActualizarEstadoReservaRequest(EstadoReserva.CONFIRMADA)
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));
    }

    private LocalDate proximoDiaSemana(DayOfWeek diaSemana) {
        LocalDate fecha = LocalDate.now().plusDays(1);
        while (fecha.getDayOfWeek() != diaSemana) {
            fecha = fecha.plusDays(1);
        }
        return fecha;
    }

    private String registrarYObtenerAccessToken(
            String nombre, String correo, RolUsuario rol
    ) throws Exception {
        usuarioRepository.save(new Usuario(
                nombre, correo, passwordEncoder.encode(PASSWORD_PLANO), rol
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
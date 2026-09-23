package com.vasquez.reservas_backend.disponibilidad;


import com.vasquez.reservas_backend.auth.security.JwtAuthenticationFilter;
import com.vasquez.reservas_backend.disponibilidad.controller.DisponibilidadController;
import com.vasquez.reservas_backend.disponibilidad.service.DisponibilidadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.*;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DisponibilidadController.class)
@Import(DisponibilidadTestSecurityConfig.class)
// Evita que Spring Boot registre automáticamente JwtAuthenticationFilter
// (un bean Filter) como filtro independiente de MockMvc. La cadena real de
// Spring Security (FilterChainProxy) se sigue instalando por su cuenta via
// SecurityMockMvcConfigurer, así que @PreAuthorize sigue funcionando; solo
// se excluye el filtro suelto que no aporta nada en este slice y que
// interfiere con la propagación del SecurityContext de @WithMockUser.
@AutoConfigureMockMvc(addFilters = false)
class DisponibilidadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DisponibilidadService disponibilidadService;

    // SecurityConfig (real) sigue en el classpath del slice y declara
    // JwtAuthenticationFilter como bean; sin este stub, Spring intenta
    // construir el filtro real, que requiere JwtService y no está
    // disponible en @WebMvcTest.
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @WithMockUser(
            username = "cliente@test.com",
            roles = "CLIENTE"
    )
    void clienteNoPuedeCrearDisponibilidad() throws Exception {
        mockMvc.perform(
                        post("/api/disponibilidades")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "servicioId": 1,
                                          "diaSemana": "MONDAY",
                                          "horaInicio": "09:00:00",
                                          "horaFin": "13:00:00"
                                        }
                                        """)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(
            username = "admin@test.com",
            roles = "ADMIN"
    )
    void adminSiPuedeCrearDisponibilidad() throws Exception {
        mockMvc.perform(
                        post("/api/disponibilidades")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "servicioId": 1,
                                          "diaSemana": "MONDAY",
                                          "horaInicio": "09:00:00",
                                          "horaFin": "13:00:00"
                                        }
                                        """)
                )
                .andExpect(status().isCreated());
    }
}
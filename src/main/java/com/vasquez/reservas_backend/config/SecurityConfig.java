package com.vasquez.reservas_backend.config;

import com.vasquez.reservas_backend.auth.security.JwtAuthenticationFilter;
import com.vasquez.reservas_backend.shared.exception.ApiErrorResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import tools.jackson.databind.ObjectMapper;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;
    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            ObjectMapper objectMapper
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(
                16,      // saltLength: sal única de 128 bits por contraseña
                32,      // hashLength: hash de salida de 256 bits
                1,       // parallelism
                19456,   // memoria: 19 MiB por cálculo
                2        // iterations
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/usuarios/registro",
                                "/actuator/health"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                escribirError(
                                        response,
                                        HttpStatus.UNAUTHORIZED,
                                        "Debes iniciar sesión para acceder a este recurso"
                                )
                        )
                        .accessDeniedHandler((request, response, exception) ->
                                escribirError(
                                        response,
                                        HttpStatus.FORBIDDEN,
                                        "No tienes permiso para realizar esta acción"
                                )
                        )
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }
    private void escribirError(
            jakarta.servlet.http.HttpServletResponse response,
            HttpStatus status,
            String mensaje
    ) throws IOException {

        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ApiErrorResponse error = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                mensaje,
                Map.of()
        );

        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
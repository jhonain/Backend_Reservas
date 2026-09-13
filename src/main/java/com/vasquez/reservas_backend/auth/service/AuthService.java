package com.vasquez.reservas_backend.auth.service;

import com.vasquez.reservas_backend.auth.dto.LoginRequest;
import com.vasquez.reservas_backend.auth.dto.LoginResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request, HttpServletResponse response);

    LoginResponse refrescar(String refreshTokenPlano, HttpServletResponse response);

    void logout(String refreshTokenPlano, HttpServletResponse response);
}
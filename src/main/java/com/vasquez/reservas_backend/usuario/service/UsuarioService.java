package com.vasquez.reservas_backend.usuario.service;


import com.vasquez.reservas_backend.usuario.dto.RegistrarUsuarioRequest;
import com.vasquez.reservas_backend.usuario.dto.UsuarioResponse;
import com.vasquez.reservas_backend.usuario.dto.ActualizarUsuarioRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsuarioService {

    UsuarioResponse registrar(RegistrarUsuarioRequest request);

    UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest request);

    UsuarioResponse buscarPorId(Long id);

    Page<UsuarioResponse> listarActivos(Pageable pageable);

    void desactivar(Long id);
}
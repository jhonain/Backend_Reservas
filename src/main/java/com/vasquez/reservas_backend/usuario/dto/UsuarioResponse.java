package com.vasquez.reservas_backend.usuario.dto;


import com.vasquez.reservas_backend.usuario.entity.Usuario;
import com.vasquez.reservas_backend.usuario.entity.RolUsuario;

import java.time.Instant;

public record UsuarioResponse(
        Long id,
        String nombre,
        String correo,
        RolUsuario rol,
        boolean activo,
        Instant createdAt,
        Instant updatedAt
) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.isActivo(),
                usuario.getCreatedAt(),
                usuario.getUpdatedAt()
        );
    }
}
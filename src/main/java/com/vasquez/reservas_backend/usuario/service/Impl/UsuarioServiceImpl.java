package com.vasquez.reservas_backend.usuario.service.Impl;

import com.vasquez.reservas_backend.shared.exception.BusinessException;
import com.vasquez.reservas_backend.shared.exception.ResourceNotFoundException;
import com.vasquez.reservas_backend.usuario.dto.ActualizarUsuarioRequest;
import com.vasquez.reservas_backend.usuario.dto.RegistrarUsuarioRequest;
import com.vasquez.reservas_backend.usuario.dto.UsuarioResponse;
import com.vasquez.reservas_backend.usuario.entity.RolUsuario;
import com.vasquez.reservas_backend.usuario.entity.Usuario;
import com.vasquez.reservas_backend.usuario.repository.UsuarioRepository;
import com.vasquez.reservas_backend.usuario.service.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UsuarioResponse registrar(RegistrarUsuarioRequest request) {
        if (usuarioRepository.existsByCorreoIgnoreCase(request.correo())) {
            throw new BusinessException(
                    "Ya existe un usuario registrado con ese correo"
            );
        }

        String passwordHash = passwordEncoder.encode(request.password());

        Usuario usuario = new Usuario(
                request.nombre(),
                request.correo(),
                passwordHash,
                RolUsuario.CLIENTE
        );

        Usuario guardado = usuarioRepository.save(usuario);

        return UsuarioResponse.desde(guardado);
    }

    @Override
    public UsuarioResponse actualizar(
            Long id,
            ActualizarUsuarioRequest request
    ) {
        Usuario usuario = obtenerOFallar(id);
        usuario.actualizarDatos(request.nombre());

        return UsuarioResponse.desde(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return UsuarioResponse.desde(obtenerOFallar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listarActivos(Pageable pageable) {
        return usuarioRepository.findAll(pageable)
                .map(UsuarioResponse::desde);
    }

    @Override
    public void desactivar(Long id) {
        Usuario usuario = obtenerOFallar(id);
        usuario.desactivar();
    }

    private Usuario obtenerOFallar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con id " + id
                ));
    }
}
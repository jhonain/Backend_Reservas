package com.vasquez.reservas_backend.usuario.service;

import com.vasquez.reservas_backend.shared.exception.BusinessException;
import com.vasquez.reservas_backend.shared.exception.ResourceNotFoundException;
import com.vasquez.reservas_backend.usuario.dto.ActualizarUsuarioRequest;
import com.vasquez.reservas_backend.usuario.dto.RegistrarUsuarioRequest;
import com.vasquez.reservas_backend.usuario.dto.UsuarioResponse;
import com.vasquez.reservas_backend.usuario.entity.RolUsuario;
import com.vasquez.reservas_backend.usuario.entity.Usuario;
import com.vasquez.reservas_backend.usuario.repository.UsuarioRepository;
import com.vasquez.reservas_backend.usuario.service.Impl.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioServiceImpl(usuarioRepository, passwordEncoder);
    }

    @Test
    void deberiaRegistrarUsuarioConDatosValidos() {
        RegistrarUsuarioRequest request = new RegistrarUsuarioRequest(
                "Juan Pérez",
                "juan@example.com",
                "password123"
        );

        when(usuarioRepository.existsByCorreoIgnoreCase("juan@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hash-seguro");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioResponse response = usuarioService.registrar(request);

        assertEquals("Juan Pérez", response.nombre());
        assertEquals("juan@example.com", response.correo());
        assertEquals(RolUsuario.CLIENTE, response.rol());
        assertTrue(response.activo());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void noDeberiaRegistrarSiCorreoYaExiste() {
        RegistrarUsuarioRequest request = new RegistrarUsuarioRequest(
                "Juan Pérez",
                "juan@example.com",
                "password123"
        );

        when(usuarioRepository.existsByCorreoIgnoreCase("juan@example.com"))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> usuarioService.registrar(request)
        );

        assertEquals(
                "Ya existe un usuario registrado con ese correo",
                exception.getMessage()
        );
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void deberiaActualizarNombreUsuario() throws Exception {
        Usuario usuario = new Usuario(
                "Juan Pérez",
                "juan@example.com",
                "hash",
                RolUsuario.CLIENTE
        );
        asignarId(usuario, 1L);

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        UsuarioResponse response = usuarioService.actualizar(
                1L,
                new ActualizarUsuarioRequest("Juan Carlos Pérez")
        );

        assertEquals("Juan Carlos Pérez", response.nombre());
    }

    @Test
    void noDeberiaActualizarSiUsuarioNoExiste() {
        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> usuarioService.actualizar(
                        99L,
                        new ActualizarUsuarioRequest("Nombre Nuevo")
                )
        );
    }

    @Test
    void deberiaDesactivarUsuario() throws Exception {
        Usuario usuario = new Usuario(
                "Juan Pérez",
                "juan@example.com",
                "hash",
                RolUsuario.CLIENTE
        );
        asignarId(usuario, 1L);

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        usuarioService.desactivar(1L);

        assertFalse(usuario.isActivo());
    }

    @Test
    void deberiaBuscarPorIdCuandoExiste() throws Exception {
        Usuario usuario = new Usuario(
                "Juan Pérez",
                "juan@example.com",
                "hash",
                RolUsuario.CLIENTE
        );
        asignarId(usuario, 5L);

        when(usuarioRepository.findById(5L))
                .thenReturn(Optional.of(usuario));

        UsuarioResponse response = usuarioService.buscarPorId(5L);

        assertEquals(5L, response.id());
        assertEquals("juan@example.com", response.correo());
    }

    @Test
    void noDeberiaBuscarPorIdSiNoExiste() {
        when(usuarioRepository.findById(404L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> usuarioService.buscarPorId(404L)
        );
    }

    // Usuario.id es autogenerado y no tiene setter público; se asigna por
    // reflexión para simular una entidad persistida en pruebas unitarias.
    private void asignarId(Usuario usuario, Long id) throws Exception {
        Field campoId = Usuario.class.getDeclaredField("id");
        campoId.setAccessible(true);
        campoId.set(usuario, id);
    }
}

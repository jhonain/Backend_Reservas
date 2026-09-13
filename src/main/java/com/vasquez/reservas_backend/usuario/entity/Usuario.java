package com.vasquez.reservas_backend.usuario.entity;

import com.vasquez.reservas_backend.shared.entity.Auditable;
import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RolUsuario rol;

    @Column(nullable = false)
    private boolean activo;

    protected Usuario() {
    }

    public Usuario(
            String nombre,
            String correo,
            String password,
            RolUsuario rol
    ) {
        this.nombre = nombre;
        this.correo = correo;
        this.password = password;
        this.rol = rol;
        this.activo = true;
    }

    public void actualizarDatos(String nombre) {
        this.nombre = nombre;
    }

    public void cambiarPassword(String nuevoPasswordHash) {
        this.password = nuevoPasswordHash;
    }

    public void desactivar() {
        this.activo = false;
    }

    public void activar() {
        this.activo = true;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getPassword() {
        return password;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }
}
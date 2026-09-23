package com.vasquez.reservas_backend.servicio.entity;

import com.vasquez.reservas_backend.shared.entity.Auditable;
import jakarta.persistence.*;

@Entity
@Table(name = "servicios")
public class Servicio extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @Column(nullable = false)
    private boolean activo;

    protected Servicio() {
    }

    public Servicio(
            String nombre,
            String descripcion,
            Integer duracionMinutos
    ) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.duracionMinutos = duracionMinutos;
        this.activo = true;
    }

    public void actualizar(
            String nombre,
            String descripcion,
            Integer duracionMinutos
    ) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.duracionMinutos = duracionMinutos;
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

    public String getDescripcion() {
        return descripcion;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public boolean isActivo() {
        return activo;
    }

    public Long getVersion() {
        return version;
    }
}
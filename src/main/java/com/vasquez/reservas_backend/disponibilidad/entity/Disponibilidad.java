package com.vasquez.reservas_backend.disponibilidad.entity;

import com.vasquez.reservas_backend.servicio.entity.Servicio;
import com.vasquez.reservas_backend.shared.entity.Auditable;
import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "disponibilidades")
public class Disponibilidad extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false, length = 20)
    private DayOfWeek diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    private boolean activo;

    protected Disponibilidad() {
    }

    public Disponibilidad(
            Servicio servicio,
            DayOfWeek diaSemana,
            LocalTime horaInicio,
            LocalTime horaFin
    ) {
        validarHorario(horaInicio, horaFin);

        this.servicio = servicio;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.activo = true;
    }

    public void actualizar(
            DayOfWeek diaSemana,
            LocalTime horaInicio,
            LocalTime horaFin
    ) {
        validarHorario(horaInicio, horaFin);

        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public void desactivar() {
        this.activo = false;
    }

    public void activar() {
        this.activo = true;
    }

    public boolean seSuperponeCon(LocalTime otraInicio, LocalTime otraFin) {
        return horaInicio.isBefore(otraFin) && otraInicio.isBefore(horaFin);
    }

    private void validarHorario(LocalTime horaInicio, LocalTime horaFin) {
        if (horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException(
                    "La hora de inicio y fin son obligatorias"
            );
        }

        if (!horaInicio.isBefore(horaFin)) {
            throw new IllegalArgumentException(
                    "La hora de inicio debe ser anterior a la hora de fin"
            );
        }
    }

    public Long getId() {
        return id;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public DayOfWeek getDiaSemana() {
        return diaSemana;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public boolean isActivo() {
        return activo;
    }
}
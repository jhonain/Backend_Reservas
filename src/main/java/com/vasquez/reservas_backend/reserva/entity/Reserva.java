package com.vasquez.reservas_backend.reserva.entity;

import com.vasquez.reservas_backend.servicio.entity.Servicio;
import com.vasquez.reservas_backend.shared.entity.Auditable;
import com.vasquez.reservas_backend.usuario.entity.Usuario;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
        name = "reservas",
        indexes = {
                @Index(
                        name = "idx_reserva_servicio_fecha",
                        columnList = "servicio_id, fecha"
                ),
                @Index(
                        name = "idx_reserva_usuario_fecha",
                        columnList = "usuario_id, fecha"
                )
        }
)
public class Reserva extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado;

    protected Reserva() {
    }

    public Reserva(
            Usuario usuario,
            Servicio servicio,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin
    ) {
        this.usuario = usuario;
        this.servicio = servicio;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.estado = EstadoReserva.PENDIENTE;
    }

    public void confirmar() {
        validarNoCancelada();
        this.estado = EstadoReserva.CONFIRMADA;
    }

    public void completar() {
        if (this.estado != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException(
                    "Solo una reserva confirmada puede marcarse como completada"
            );
        }

        this.estado = EstadoReserva.COMPLETADA;
    }

    public void cancelar() {
        if (this.estado == EstadoReserva.COMPLETADA) {
            throw new IllegalStateException(
                    "No se puede cancelar una reserva completada"
            );
        }

        if (this.estado == EstadoReserva.CANCELADA) {
            throw new IllegalStateException(
                    "La reserva ya está cancelada"
            );
        }

        this.estado = EstadoReserva.CANCELADA;
    }

    private void validarNoCancelada() {
        if (this.estado == EstadoReserva.CANCELADA) {
            throw new IllegalStateException(
                    "No se puede confirmar una reserva cancelada"
            );
        }
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public EstadoReserva getEstado() {
        return estado;
    }
}
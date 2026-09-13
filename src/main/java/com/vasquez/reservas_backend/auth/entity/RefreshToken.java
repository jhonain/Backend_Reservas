package com.vasquez.reservas_backend.auth.entity;


import com.vasquez.reservas_backend.shared.entity.Auditable;
import com.vasquez.reservas_backend.usuario.entity.Usuario;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "expira_en", nullable = false)
    private Instant expiraEn;

    @Column(nullable = false)
    private boolean revocado;

    protected RefreshToken() {
    }

    public RefreshToken(
            Usuario usuario,
            String tokenHash,
            Instant expiraEn
    ) {
        this.usuario = usuario;
        this.tokenHash = tokenHash;
        this.expiraEn = expiraEn;
        this.revocado = false;
    }

    public void revocar() {
        this.revocado = true;
    }

    public boolean estaExpirado() {
        return Instant.now().isAfter(expiraEn);
    }

    public boolean esValido() {
        return !revocado && !estaExpirado();
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiraEn() {
        return expiraEn;
    }

    public boolean isRevocado() {
        return revocado;
    }
}

package com.example.safeip.safeip.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "codigos")
public class CodigoCaptchaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String tipo;

    @Column(name = "valido", nullable = false)
    private Boolean ativo;

    public boolean isAtivo() {
        return ativo != null && ativo;
    }

}

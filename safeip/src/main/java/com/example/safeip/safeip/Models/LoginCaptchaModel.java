package com.example.safeip.safeip.Models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "credenciais_seguranca")
@Data
public class LoginCaptchaModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "cpf_usuario", referencedColumnName = "cpf")
    private UsuarioModel usuario;

    @Column(name = "senha_seguranca")
    private String senha_seguranca;
}

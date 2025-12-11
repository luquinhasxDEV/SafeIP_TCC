package com.example.safeip.safeip.Models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "usuarios")
public class UsuarioModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;

    private String cargo;

    @Column(unique = true)
    private String cpf;

    private Double salario;

    private String senha;

    @Column(name = "ip_permitido")
    private String ipPermitido;

    private Boolean ativo = true;
}

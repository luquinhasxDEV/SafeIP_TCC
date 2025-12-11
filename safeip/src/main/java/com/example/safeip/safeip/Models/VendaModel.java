package com.example.safeip.safeip.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
@Data
@Entity(name = "registroVendas")
@Table(name = "registroVendas")
public class VendaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private UsuarioModel usuario;
    private String produto;
    private int quantidade;
    private String pagamento;
    private LocalDate data_venda;
    private double total;
}

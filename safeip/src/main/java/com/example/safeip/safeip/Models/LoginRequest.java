package com.example.safeip.safeip.Models;

import lombok.Data;

@Data
public class LoginRequest {

    private String cpf;
    private String senha;

    public LoginRequest(String cpf, String senha) {
        this.cpf = cpf;
        this.senha = senha;
    }
}

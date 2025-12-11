package com.example.safeip.safeip.Dtos;

import lombok.Data;

@Data
public class LoginCaptchaRequest {
    private String cpf;
    private String senha_seguranca;
}

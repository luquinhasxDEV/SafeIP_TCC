package com.example.safeip.safeip.Repositories;

import com.example.safeip.safeip.Models.LoginCaptchaModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaptchaRepository extends JpaRepository<LoginCaptchaModel, String> {
    LoginCaptchaModel findByUsuario_Cpf(String cpf);
}

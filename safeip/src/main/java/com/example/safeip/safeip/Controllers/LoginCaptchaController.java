package com.example.safeip.safeip.Controllers;

import com.example.safeip.safeip.Dtos.LoginCaptchaRequest;
import com.example.safeip.safeip.Models.LogModel;
import com.example.safeip.safeip.Models.LoginCaptchaModel;
import com.example.safeip.safeip.Models.UsuarioModel;
import com.example.safeip.safeip.Repositories.CaptchaRepository;
import com.example.safeip.safeip.Repositories.LogRepository;
import com.example.safeip.safeip.Repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/safeip")
public class LoginCaptchaController {

    private static final Logger logger = LoggerFactory.getLogger(LoginCaptchaController.class);

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CaptchaRepository captchaRepository;

    @PostMapping("/loginCaptcha")
    public ResponseEntity<?> loginCaptcha(@RequestBody LoginCaptchaRequest loginCaptchaRequest) {
        try {
            String cpf = loginCaptchaRequest.getCpf().trim();
            String senhaDigitada = loginCaptchaRequest.getSenha_seguranca().trim();

            UsuarioModel usuario = usuarioRepository.findByCpf(cpf);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não encontrado!");
            }

            LoginCaptchaModel credenciais = captchaRepository.findByUsuario_Cpf(cpf);
            if (credenciais == null || credenciais.getSenha_seguranca() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Senha de segurança não cadastrada!");
            }


            if (!senhaDigitada.equals(credenciais.getSenha_seguranca().trim())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("CPF ou senha de segurança incorretos.");
            }

            String mensagem = String.format("O Funcionário %s (CPF: %s) ACESSOU o CAPTCHA.", usuario.getNome(), usuario.getCpf());
            logger.info(mensagem);


            LogModel log = new LogModel();
            log.setUsuario(usuario.getCpf());
            log.setAcao(mensagem);
            log.setDataHora(LocalDateTime.now());
            logRepository.save(log);
            return ResponseEntity.ok("Acesso autorizado!");

        } catch (Exception e) {
            logger.error("Erro ao processar login de segurança: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno no servidor.");
        }
    }
}

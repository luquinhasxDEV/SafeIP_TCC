package com.example.safeip.safeip.Controllers;

import com.example.safeip.safeip.Models.LogModel;
import com.example.safeip.safeip.Models.SairRequest;
import com.example.safeip.safeip.Models.UsuarioModel;
import com.example.safeip.safeip.Repositories.LogRepository;
import com.example.safeip.safeip.Repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/safeip/sair")
public class SairController {
    private static final Logger logger = LoggerFactory.getLogger(SairController.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LogRepository logRepository;

    @PostMapping
    public ResponseEntity<String> registrarSaida(@RequestBody SairRequest sairRequest) {
        UsuarioModel usuario = usuarioRepository.findByCpf(sairRequest.getCpf());
        if (usuario == null) {
            return ResponseEntity.badRequest().body("Usuário não encontrado!");
        }
        String mensagem = String.format("O Funcionário %s (CPF: %s) SAIU do sistema.", usuario.getNome(), usuario.getCpf());
        LogModel log = new LogModel();
        log.setUsuario(usuario.getCpf());
        log.setAcao(mensagem);
        log.setDataHora(LocalDateTime.now());
        logRepository.save(log);
        logger.info(mensagem);

        return ResponseEntity.ok("Saída registrada com sucesso!");
    }
}


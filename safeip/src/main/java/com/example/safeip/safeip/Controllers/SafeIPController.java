package com.example.safeip.safeip.Controllers;

import com.example.safeip.safeip.Models.LogModel;
import com.example.safeip.safeip.Models.UsuarioModel;
import com.example.safeip.safeip.Repositories.LogRepository;
import com.example.safeip.safeip.Repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/safeip/controle")
@CrossOrigin(origins = "http://localhost:5173")
public class SafeIPController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LogRepository logRepository;

    private static final Logger logger = LoggerFactory.getLogger(SafeIPController.class);

    @GetMapping("/bloqueados")
    public List<UsuarioModel> listarUsuariosComIpCadastrado() {
        // Filtra e retorna apenas usuários que possuem um IP permitido cadastrado.
        List<UsuarioModel> todos = usuarioRepository.findAll();

        return todos.stream()
                .filter(u -> u.getIpPermitido() != null && !u.getIpPermitido().isEmpty())
                .collect(Collectors.toList());
    }

    @Transactional
    @PostMapping("/liberarAcesso/{cpf}")
    // O CPF do gerente logado é passado no cabeçalho para registro no log
    public ResponseEntity<String> liberarAcesso(@PathVariable String cpf, @RequestHeader("cpf-logado") String cpfGerente) {

        UsuarioModel usuario = usuarioRepository.findByCpf(cpf);
        UsuarioModel gerente = usuarioRepository.findByCpf(cpfGerente);

        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
        }

        //Limpa o IP permitido
        String ipAnterior = usuario.getIpPermitido();
        usuario.setIpPermitido(null);
        usuarioRepository.save(usuario);

        String acao = String.format("Acesso do usuário %s (CPF: %s) foi LIBERADO. IP anterior (%s) resetado.",
                usuario.getNome(), usuario.getCpf(), ipAnterior);

        String logMsg = String.format("Gerente %s (CPF: %s) executou: %s",
                gerente != null ? gerente.getNome() : "Desconhecido", cpfGerente, acao);

        logger.warn(logMsg);

        LogModel log = new LogModel();
        log.setUsuario(cpfGerente);
        log.setAcao(acao);
        log.setDataHora(LocalDateTime.now());
        logRepository.save(log);

        return ResponseEntity.ok("Acesso liberado com sucesso para o CPF: " + cpf);
    }
}
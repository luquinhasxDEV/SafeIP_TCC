package com.example.safeip.safeip.Controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.example.safeip.safeip.Models.LogModel;
import com.example.safeip.safeip.Models.LoginRequest;
import com.example.safeip.safeip.Models.UsuarioModel;
import com.example.safeip.safeip.Repositories.LogRepository;
import com.example.safeip.safeip.Repositories.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/safeip")
public class LoginController {

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private String getClientIp(HttpServletRequest request) {
        // Isso aqui é para caso o projeto estivesse na web, ele pega o IP real do usuario
        //Como nossa Aplicação é localhost (neste momento), ele vem nulo, assim, este if nao entra em acao
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0].trim();
        }
        // Como a aplicacao é localhost, vai retornar o IP da maquina que está rodando o código
        return request.getRemoteAddr();
    }

    @Transactional // Permite salvar o IP no primeiro login
    @PostMapping("/login")
    public ResponseEntity<?> login (@RequestBody LoginRequest loginRequest, HttpServletRequest request) {

        UsuarioModel usuario = usuarioRepository.findByCpf(loginRequest.getCpf());

        // Pega o IP
        String clientIp = getClientIp(request);

        // Verifica as credenciais
        if (usuario == null) {
            return ResponseEntity.status(401).body("Usuário Inválido!");
        }

        BCrypt.Result result = BCrypt.verifyer().verify(loginRequest.getSenha().toCharArray(), usuario.getSenha());
        if (!result.verified) {
            return ResponseEntity.status(401).body("Senha Inválida!");
        }

        if (usuario.getAtivo() == null || !usuario.getAtivo()) {
            return ResponseEntity.status(403).body("Usuário Inativo!");
        }
        // Aqui, é se o IP do usuário não estiver cadastrado (Primeiro Acesso)
        if (usuario.getIpPermitido() == null || usuario.getIpPermitido().isEmpty()) {
            // Aqui salva o IP atual como o permitido
            usuario.setIpPermitido(clientIp);
            usuarioRepository.save(usuario);

            String logPermitido = String.format("Acesso liberado. IP (%s) cadastrado como permitido para %s.", clientIp, usuario.getNome());
            logger.warn(logPermitido);

        } else {
            // verifica se o IP já está cadastrado, vai checar se o IP atual corresponde
            if (!usuario.getIpPermitido().equals(clientIp)) {

                String logBloqueio = String.format("BLOQUEIO DE IP! Usuário %s tentou acessar do IP não autorizado: %s. IP Permitido: %s",
                        usuario.getNome(), clientIp, usuario.getIpPermitido());
                logger.error(logBloqueio);
                return ResponseEntity.status(403).body("ACESSO BLOQUEADO! IP não autorizado.");
            }
        }
        String mensagem = String.format("O Funcionário %s (CPF: %s) ENTROU no Sistema. (IP: %s)", usuario.getNome(), usuario.getCpf(), clientIp);
        logger.info(mensagem);

        LogModel log = new LogModel();
        log.setUsuario(usuario.getCpf());
        log.setAcao(mensagem);
        log.setDataHora(LocalDateTime.now());
        logRepository.save(log);

        return ResponseEntity.ok(usuario);
    }
}
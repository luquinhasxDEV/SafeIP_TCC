package com.example.safeip.safeip.Controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.example.safeip.safeip.Models.LogModel;
import com.example.safeip.safeip.Models.UsuarioModel;
// Certifique-se de que a classe StatusUpdateDto esteja no local correto, ou importe-a
// Se não quiser criar a classe separada, pode usar um Map<String, Boolean> no @RequestBody.
// Para fins de demonstração, vou assumir que você criou a classe StatusUpdateDto.
// import com.example.safeip.safeip.Dto.StatusUpdateDto;
import com.example.safeip.safeip.Repositories.LogRepository;
import com.example.safeip.safeip.Repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("safeip/equipe")
public class UsuarioController {

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    // Dto simples para Receber Apenas o Status
    public static class StatusUpdateDto {
        private Boolean ativo; // Deve ser um Boolean para evitar erros com valores nulos

        // Getters and Setters
        public Boolean getAtivo() {
            return ativo;
        }

        public void setAtivo(Boolean ativo) {
            this.ativo = ativo;
        }
    }
    @PostMapping
    public ResponseEntity<?> adicionarFuncionario(@RequestBody UsuarioModel usuarioModel, @RequestHeader("cpf-logado") String cpfLogado) {
        UsuarioModel usuario = usuarioRepository.findByCpf(cpfLogado);

        try {
            if (usuarioModel.getSenha().length() >= 8 && (usuarioModel.getSenha().contains("!") || usuarioModel.getSenha().contains("@") || usuarioModel.getSenha().contains("#") || usuarioModel.getSenha().contains("$") || usuarioModel.getSenha().contains("%"))) {

                String passwordHashed = BCrypt.withDefaults().hashToString(12, usuarioModel.getSenha().toCharArray());
                usuarioModel.setSenha(passwordHashed);

                usuarioRepository.save(usuarioModel);

                String mensagem = String.format("O Funcionário %s (CPF: %s) REGISTROU um novo Funcionário — Nome: %s | CPF: %s | Cargo: %s | Salário: %.2f",
                        usuario.getNome(),cpfLogado, usuarioModel.getNome(), usuarioModel.getCpf(), usuarioModel.getCargo(), usuarioModel.getSalario());

                LogModel log = new LogModel();
                log.setUsuario(cpfLogado);
                log.setAcao(mensagem);
                log.setDataHora(LocalDateTime.now());
                logRepository.save(log);

                logger.info(mensagem);
                return ResponseEntity.ok("Funcionário Cadastrado com Sucesso!");

            } else {
                return ResponseEntity.badRequest()
                        .body("Verifique se a senha digitada atende os requisitos para ser uma senha segura.");
            }
        } catch (RuntimeException e) {
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<UsuarioModel>> listarUsuarios() {
        List<UsuarioModel> usuarios = usuarioRepository.findAll();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable int id) {
        UsuarioModel usuario = usuarioRepository.findById(id).orElse(null);

        if (usuario == null) {
            return ResponseEntity.badRequest().body("Funcionário não encontrado");
        }

        return ResponseEntity.ok(usuario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletar(@PathVariable int id, @RequestHeader("cpf-logado") String cpfLogado) {
        UsuarioModel usuario = usuarioRepository.findByCpf(cpfLogado);
        UsuarioModel usuarioModel = usuarioRepository.findById(id).orElse(null);
        if (usuarioModel == null) {
            return ResponseEntity.badRequest().body("Funcionário não encontrado!");
        }

        usuarioRepository.deleteById(id);

        String mensagem = String.format(
                "O Funcionário %s (CPF: %s) DELETOU um Funcionário — Nome: %s | CPF: %s | Cargo: %s | Salário: %.2f",
                usuario.getNome(),cpfLogado, usuarioModel.getNome(), usuarioModel.getCpf(),
                usuarioModel.getCargo(), usuarioModel.getSalario()
        );

        LogModel log = new LogModel();
        log.setUsuario(cpfLogado);
        log.setAcao(mensagem);
        log.setDataHora(LocalDateTime.now());
        logRepository.save(log);

        logger.info(mensagem);
        return ResponseEntity.ok("Funcionário excluído com sucesso!");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarFuncionario(@PathVariable int id, @RequestBody UsuarioModel dadosAtualizados, @RequestHeader("cpf-logado") String cpfLogado) {

        UsuarioModel usuario = usuarioRepository.findByCpf(cpfLogado);

        return usuarioRepository.findById(id).map(usuarioExistente -> {
            usuarioExistente.setNome(dadosAtualizados.getNome());
            usuarioExistente.setCargo(dadosAtualizados.getCargo());
            usuarioExistente.setSalario(dadosAtualizados.getSalario());

            if (dadosAtualizados.getSenha() != null && !dadosAtualizados.getSenha().isEmpty()) {
                String hashed = BCrypt.withDefaults().hashToString(12, dadosAtualizados.getSenha().toCharArray());
                usuarioExistente.setSenha(hashed);
            }
            if (dadosAtualizados.getAtivo() != null) {
                usuarioExistente.setAtivo(dadosAtualizados.getAtivo());
            }

            usuarioRepository.save(usuarioExistente);

            String mensagem = String.format(
                    "O Funcionário %s (CPF: %s) ATUALIZOU o Funcionário - Nome: %s | Cargo: %s | Salário: %.2f | Ativo: %s.",
                    usuario.getNome(),
                    cpfLogado,
                    usuarioExistente.getNome(),
                    usuarioExistente.getCargo(),
                    usuarioExistente.getSalario(),
                    usuarioExistente.getAtivo()
            );

            logger.info(mensagem);
            LogModel log = new LogModel();
            log.setUsuario(cpfLogado);
            log.setAcao(mensagem);
            log.setDataHora(LocalDateTime.now());
            logRepository.save(log);

            return ResponseEntity.ok("Funcionário atualizado com sucesso!");
        }).orElseGet(() -> ResponseEntity.badRequest().body("Funcionário não encontrado."));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<String> alterarStatus(@PathVariable int id, @RequestBody StatusUpdateDto statusDto, @RequestHeader("cpf-logado") String cpfLogado) {

        UsuarioModel gerente = usuarioRepository.findByCpf(cpfLogado);
        if (gerente == null) {
            return ResponseEntity.badRequest().body("Usuário logado não encontrado.");
        }

        return usuarioRepository.findById(id).map(funcionario -> {

            // Verifica se o status foi enviado no corpo da requisição
            if (statusDto.getAtivo() == null) {
                return ResponseEntity.badRequest().body("O campo 'ativo' é obrigatório.");
            }

            // Atualiza o status do funcionário
            funcionario.setAtivo(statusDto.getAtivo());
            usuarioRepository.save(funcionario);

            String statusMsg = funcionario.getAtivo() ? "ATIVOU" : "INATIVOU";

            String mensagem = String.format(
                    "O Funcionário %s (CPF: %s) %s o status do Funcionário — Nome: %s | CPF: %s | Novo Status: %s.",
                    gerente.getNome(),
                    cpfLogado,
                    statusMsg,
                    funcionario.getNome(),
                    funcionario.getCpf(),
                    funcionario.getAtivo() ? "Ativo (1)" : "Inativo (0)"
            );

            logger.info(mensagem);
            LogModel log = new LogModel();
            log.setUsuario(cpfLogado);
            log.setAcao(mensagem);
            log.setDataHora(LocalDateTime.now());
            logRepository.save(log);

            return ResponseEntity.ok("Status do funcionário alterado com sucesso.");
        }).orElseGet(() -> ResponseEntity.badRequest().body("Funcionário não encontrado."));
    }
}
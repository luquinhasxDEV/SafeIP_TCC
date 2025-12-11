package com.example.safeip.safeip.Controllers;

import com.example.safeip.safeip.Dtos.CodigoVerificacaoDTO;
import com.example.safeip.safeip.Models.CodigoCaptchaModel;
import com.example.safeip.safeip.Models.LogModel;
import com.example.safeip.safeip.Models.UsuarioModel;
import com.example.safeip.safeip.Repositories.CodigoCaptchaRepository;
import com.example.safeip.safeip.Repositories.LogRepository;
import com.example.safeip.safeip.Repositories.UsuarioRepository;
import com.example.safeip.safeip.Dtos.CodigoVerificacaoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/safeip")
@CrossOrigin(origins = "http://localhost:5173")
public class CodigoCaptchaController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private LogRepository logRepository;

    private static final Logger logger = LoggerFactory.getLogger(CodigoCaptchaController.class);

    @Autowired
    private CodigoCaptchaRepository codigoRepository;

    @PostMapping("/codigoCaptcha")
    public CodigoCaptchaModel salvarCodigo(@RequestBody CodigoCaptchaModel codigoCaptchaModel, @RequestHeader("cpf-logado") String cpfLogado) {

        UsuarioModel usuario = usuarioRepository.findByCpf(cpfLogado);
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não encontrado para CPF: " + cpfLogado);
        }

        // Nesse caso, ele percorre todos os codigos e seta como inativo assim que gerar um novo
        List<CodigoCaptchaModel> codigosDoMesmoTipo = codigoRepository.findByTipoAndAtivoTrue(codigoCaptchaModel.getTipo());
        for (CodigoCaptchaModel c : codigosDoMesmoTipo) {
            c.setAtivo(false);
            codigoRepository.save(c);
        }


        Optional<CodigoCaptchaModel> codigoExistente = codigoRepository.findByCodigo(codigoCaptchaModel.getCodigo());

        if (codigoExistente.isPresent()) {
            // Vai verificar se ja existe e se ja existir, apenas ativa (coloca true)
            CodigoCaptchaModel c = codigoExistente.get();
            c.setAtivo(true);
            return codigoRepository.save(c);
        } else {
            // Se o codigo for novo, vai salvar no captcha
            codigoCaptchaModel.setAtivo(true);
            String mensagem = String.format("O Funcionário %s (CPF: %s) Gerou um Código Captcha.", usuario.getNome(), cpfLogado);
            logger.info(mensagem);


            LogModel log = new LogModel();
            log.setUsuario(usuario.getCpf());
            log.setAcao(mensagem);
            log.setDataHora(LocalDateTime.now());
            logRepository.save(log);
            return codigoRepository.save(codigoCaptchaModel);
        }
    }
    @PostMapping("/verificarCodigo")
    public Map<String, Boolean> verificarCodigo(@RequestBody CodigoVerificacaoDTO dto, @RequestHeader("cpf-logado") String cpfLogado) {
        Map<String, Boolean> resultado = new HashMap<>();
        resultado.put("valido", false);

        Optional<CodigoCaptchaModel> codigoOpt = codigoRepository.findByCodigoAndTipoAndAtivoTrue(dto.getCodigo(), dto.getTipo());

        if (codigoOpt.isPresent()) {
            UsuarioModel usuario = usuarioRepository.findByCpf(cpfLogado);
            String nomeUsuario = usuario != null ? usuario.getNome() : "Desconhecido";

            String mensagem = String.format("O Funcionário %s (CPF: %s) Utilizou o Código Captcha de %s com sucesso.",
                    nomeUsuario,
                    cpfLogado,
                    dto.getTipo());
            logger.info(mensagem);

            LogModel log = new LogModel();
            log.setUsuario(cpfLogado);
            log.setAcao(mensagem);
            log.setDataHora(LocalDateTime.now());
            logRepository.save(log);

            resultado.put("valido", true);
        }

        return resultado;
    }
}
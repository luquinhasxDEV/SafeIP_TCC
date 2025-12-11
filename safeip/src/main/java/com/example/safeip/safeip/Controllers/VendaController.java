package com.example.safeip.safeip.Controllers;

import com.example.safeip.safeip.Models.*;
import com.example.safeip.safeip.Repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import com.example.safeip.safeip.Models.CodigoCaptchaModel;
import org.springframework.http.ResponseEntity;
import com.example.safeip.safeip.Repositories.CodigoCaptchaRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/safeip/vendas")
public class VendaController {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    ProdutoRepository produtoRepository;

    @Autowired
    CodigoCaptchaRepository codigoCaptchaRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private VendaRepository vendaRepository;

    private static final Logger logger = LoggerFactory.getLogger(VendaController.class);

    @PostMapping
    public ResponseEntity<?> registrarVenda(@RequestBody VendaModel vendaModel, @RequestHeader("cpf-logado") String cpfLogado, @RequestParam(value = "autorizada", required = false) String vendaAutorizada) {

        UsuarioModel usuario = usuarioRepository.findByCpf(cpfLogado);
        if (usuario == null) {
            return ResponseEntity.badRequest().body("Usuário inválido!");
        }

        vendaModel.setUsuario(usuario);

        ProdutoModel produto = produtoRepository.findByNome(vendaModel.getProduto());
        if (produto == null) {
            return ResponseEntity.badRequest().body("Produto inválido!");
        }
        double totalCalculado = produto.getPreco() * vendaModel.getQuantidade();
        vendaModel.setTotal(totalCalculado);

        if (!"Gerente".equalsIgnoreCase(usuario.getCargo()) && vendaModel.getTotal() > 500000) {
            if (vendaAutorizada != null && vendaAutorizada.equalsIgnoreCase("true")) {
                String mensagemAutorizacao = String.format("O Funcionário %s (CPF: %s) REGISTROU UMA VENDA DE ALTO VALOR VIA CAPTCHA — Total: R$ %.2f", usuario.getNome(), cpfLogado, vendaModel.getTotal());
                logger.info(mensagemAutorizacao);

                LogModel logAutorizacao = new LogModel();
                logAutorizacao.setUsuario(cpfLogado);
                logAutorizacao.setAcao(mensagemAutorizacao);
                logAutorizacao.setDataHora(LocalDateTime.now());
                logRepository.save(logAutorizacao);
            } else {
                String mensagemBloqueio = String.format("O Funcionário %s (CPF: %s) tentou registrar uma venda acima do LIMITE — Total: R$ %.2f", usuario.getNome(), cpfLogado, vendaModel.getTotal());
                logger.warn(mensagemBloqueio);

                LogModel logBloqueio = new LogModel();
                logBloqueio.setUsuario(cpfLogado);
                logBloqueio.setAcao(mensagemBloqueio);
                logBloqueio.setDataHora(LocalDateTime.now());
                logRepository.save(logBloqueio);

                return ResponseEntity.status(403).body("Venda acima do limite! Código do gerente necessário.");
            }
        }

        if (vendaModel.getProduto().length() > 0 &&
                vendaModel.getQuantidade() > 0 &&
                vendaModel.getPagamento().length() > 0 &&
                vendaModel.getTotal() > 0) {

            vendaRepository.save(vendaModel);

            String mensagem = String.format("O Funcionário %s (CPF: %s) REGISTROU uma venda — Produto: %s | Quantidade: %d | Forma de Pagamento: %s | Total: R$ %.2f", usuario.getNome(), cpfLogado, vendaModel.getProduto(), vendaModel.getQuantidade(), vendaModel.getPagamento(), vendaModel.getTotal());

            logger.info(mensagem);

            LogModel log = new LogModel();
            log.setUsuario(cpfLogado);
            log.setAcao(mensagem);
            log.setDataHora(LocalDateTime.now());
            logRepository.save(log);

            return ResponseEntity.ok("Venda Registrada com Sucesso!");
        } else {
            return ResponseEntity.badRequest().body("Verifique se todos os campos foram preenchidos!");
        }
    }
        @GetMapping
        public ResponseEntity<List<VendaModel>> listarVendas () {
            List<VendaModel> vendas = vendaRepository.findAll();
            return ResponseEntity.ok(vendas);
        }

    @GetMapping("/codigoCaptcha/{codigo}")
    public ResponseEntity<?> verificarCodigo(@PathVariable String codigo) {
        try {

            CodigoCaptchaModel codigoModel = codigoCaptchaRepository.findByCodigo(codigo).orElse(null);

            if (codigoModel == null || !codigoModel.isAtivo()) {
                return ResponseEntity.notFound().build();
            }

            // Se passar vai desativar o codigo
            codigoModel.setAtivo(false);
            codigoCaptchaRepository.save(codigoModel);

            return ResponseEntity.ok(java.util.Map.of("status", "VALIDO")); // 200
        } catch (Exception e) {
            logger.error("Erro interno do servidor ao validar código Captcha.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor ao validar código."); // 500
        }
    }
}

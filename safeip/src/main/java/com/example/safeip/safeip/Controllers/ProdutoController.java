package com.example.safeip.safeip.Controllers;

import com.example.safeip.safeip.Models.LogModel;
import com.example.safeip.safeip.Models.ProdutoModel;
import com.example.safeip.safeip.Models.UsuarioModel;
import com.example.safeip.safeip.Repositories.LogRepository;
import com.example.safeip.safeip.Repositories.ProdutoRepository;
import com.example.safeip.safeip.Repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/safeip/produtos")
public class ProdutoController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    ProdutoRepository produtoRepository;

    private static final Logger logger = LoggerFactory.getLogger(ProdutoController.class);

    @PostMapping
    public ResponseEntity<?> registrarProduto(@RequestBody ProdutoModel produtoModel, @RequestHeader("cpf-logado") String cpfLogado) {
        UsuarioModel usuario = usuarioRepository.findByCpf(cpfLogado);


        if (produtoModel.getNome().length() > 0 && produtoModel.getPreco() > 0 && produtoModel.getDescricao().length() > 0 && produtoModel.getEstoque() > 0 && produtoModel.getTamanhos_disponiveis().length() > 0){
            produtoRepository.save(produtoModel);
            String mensagem = String.format("O Funcionário %s (CPF: %s) REGISTROU um novo Produto - Nome: %s | Preço: %.2f | Descrição: %s | Estoque: %d | Tamanhos Disponíveis: %s | Produto Ativo: %s.",
                   usuario.getCpf(),cpfLogado,produtoModel.getNome(),produtoModel.getPreco(),produtoModel.getDescricao(),produtoModel.getEstoque(),produtoModel.getTamanhos_disponiveis(), produtoModel.isAtivo());
            logger.info(mensagem);
            LogModel log = new LogModel();
            log.setUsuario(cpfLogado);
            log.setAcao(mensagem);
            log.setDataHora(LocalDateTime.now());
            logRepository.save(log);
        }else {
            return ResponseEntity.badRequest().body("Verifique se todos os campos foram preenchidos!");
        }

        return ResponseEntity.ok("Produto Cadastrado com Sucesso!");
    }

    @GetMapping
    public ResponseEntity<List<ProdutoModel>> listarProdutos() {
        List<ProdutoModel> produtos = produtoRepository.findAll();
        return ResponseEntity.ok(produtos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarProduto(@PathVariable int id, @RequestBody ProdutoModel dadosAtualizados, @RequestHeader("cpf-logado") String cpfLogado) {
        UsuarioModel usuario = usuarioRepository.findByCpf(cpfLogado);

        return produtoRepository.findById(id).map(produtoExistente -> {
                    produtoExistente.setNome(dadosAtualizados.getNome());
                    produtoExistente.setPreco(dadosAtualizados.getPreco());
                    produtoExistente.setDescricao(dadosAtualizados.getDescricao());
                    produtoExistente.setEstoque(dadosAtualizados.getEstoque());
                    produtoExistente.setTamanhos_disponiveis(dadosAtualizados.getTamanhos_disponiveis());
                    produtoExistente.setAtivo(dadosAtualizados.isAtivo());

                    produtoRepository.save(produtoExistente);

                    String mensagem = String.format(
                            "O Funcionário %s (CPF: %s) ATUALIZOU o Produto - Nome: %s | Preço: %.2f | Descrição: %s | Estoque: %d | Tamanhos Disponíveis: %s | Produto Ativo: %s.",
                            usuario.getNome(),cpfLogado, produtoExistente.getNome(), produtoExistente.getPreco(), produtoExistente.getDescricao(), produtoExistente.getEstoque(), produtoExistente.getTamanhos_disponiveis(), produtoExistente.isAtivo());
                    logger.info(mensagem);
                    LogModel log = new LogModel();
                    log.setUsuario(cpfLogado);
                    log.setAcao(mensagem);
                    log.setDataHora(LocalDateTime.now());
                    logRepository.save(log);

                    return ResponseEntity.ok("Produto atualizado com sucesso!");
        })
                .orElseGet(() -> ResponseEntity.badRequest().body("Produto não encontrado."));
    }

}

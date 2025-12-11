package com.example.safeip.safeip.Repositories;

import com.example.safeip.safeip.Models.ProdutoModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<ProdutoModel, Integer> {
    ProdutoModel findByNome(String nome);
}

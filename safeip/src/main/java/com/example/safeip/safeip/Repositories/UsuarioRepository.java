package com.example.safeip.safeip.Repositories;

import com.example.safeip.safeip.Models.UsuarioModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<UsuarioModel, Integer> {
    UsuarioModel findByCpf(String cpf);
}

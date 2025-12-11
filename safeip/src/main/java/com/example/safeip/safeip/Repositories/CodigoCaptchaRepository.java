package com.example.safeip.safeip.Repositories;

import com.example.safeip.safeip.Models.CodigoCaptchaModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CodigoCaptchaRepository extends JpaRepository<CodigoCaptchaModel, Long> {

    Optional<CodigoCaptchaModel> findByCodigoAndTipoAndAtivoTrue(String codigo, String tipo);

    Optional<CodigoCaptchaModel> findByCodigo(String codigo);

    List<CodigoCaptchaModel> findByTipoAndAtivoTrue(String tipo);

    void deleteByTipoAndAtivoFalse(String tipo);

}

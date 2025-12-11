package com.example.safeip.safeip.Controllers;

import com.example.safeip.safeip.Models.LogModel;
import com.example.safeip.safeip.Repositories.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/safeip/logs")
public class LogController {
    private final List<String> logs = Collections.synchronizedList(new ArrayList<>());

    @Autowired
    private LogRepository logRepository;

    @PostMapping
    public ResponseEntity<?> adicionarLog(@RequestBody LogModel log) {
        log.setDataHora(LocalDateTime.now());
        logRepository.save(log);
        return ResponseEntity.ok("Log registrado com sucesso!");
    }

    public void adicionarLogs(String mensagem) {
        logs.add(LocalDateTime.now() + " - " + mensagem);
    }


    @GetMapping
    public ResponseEntity<List<LogModel>> getLogs() {
        List<LogModel> logs = logRepository.findAll();
        return ResponseEntity.ok(logs);
    }
}

package com.example.safeip.safeip.Repositories;

import com.example.safeip.safeip.Models.LogModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<LogModel,Long> {
}

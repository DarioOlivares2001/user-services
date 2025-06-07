// src/main/java/com/matchwork/user_service/repository/PerfilExperienciaRepository.java
package com.matchwork.user_service.repository;

import com.matchwork.user_service.model.PerfilExperiencia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PerfilExperienciaRepository extends JpaRepository<PerfilExperiencia, Long> {
  List<PerfilExperiencia> findByPerfilProfesionalId(Long perfilId);
}

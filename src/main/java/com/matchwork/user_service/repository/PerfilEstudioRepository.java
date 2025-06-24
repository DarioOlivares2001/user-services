package com.matchwork.user_service.repository;

import com.matchwork.user_service.model.PerfilEstudio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PerfilEstudioRepository
    extends JpaRepository<PerfilEstudio, Long> {
  List<PerfilEstudio> findByPerfilProfesionalId(Long perfilId);
}

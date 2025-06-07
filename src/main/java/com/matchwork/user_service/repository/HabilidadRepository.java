package com.matchwork.user_service.repository;

import com.matchwork.user_service.model.Habilidad;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HabilidadRepository extends JpaRepository<Habilidad, Long> {
    Optional<Habilidad> findByNombreIgnoreCase(String nombre);
    List<Habilidad> findByNombreContainingIgnoreCase(String nombre);

}

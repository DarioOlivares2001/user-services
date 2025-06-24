package com.matchwork.user_service.repository;

import com.matchwork.user_service.model.PerfilProfesional;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PerfilProfesionalRepository extends JpaRepository<PerfilProfesional, Long> { 



     @Query("""
        SELECT p 
        FROM PerfilProfesional p
        JOIN FETCH p.usuario
        WHERE p.id = :id
        """)
    Optional<PerfilProfesional> findByIdWithUsuario(@Param("id") Long id);

}

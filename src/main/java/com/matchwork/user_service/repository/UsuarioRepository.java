package com.matchwork.user_service.repository;

import com.matchwork.user_service.model.Habilidad;
import com.matchwork.user_service.model.Usuario;
import com.matchwork.user_service.model.UsuarioHabilidad;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;



public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findById(Long id); 

    
    
}
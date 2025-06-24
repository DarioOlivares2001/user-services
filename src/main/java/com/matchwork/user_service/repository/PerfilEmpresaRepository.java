package com.matchwork.user_service.repository;

import com.matchwork.user_service.model.PerfilEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PerfilEmpresaRepository 
        extends JpaRepository<PerfilEmpresa, Long> { }

// src/main/java/com/matchwork/user_service/repository/UsuarioHabilidadRepository.java
package com.matchwork.user_service.repository;

import com.matchwork.user_service.model.UsuarioHabilidad;
import com.matchwork.user_service.model.Habilidad;
import com.matchwork.user_service.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioHabilidadRepository extends JpaRepository<UsuarioHabilidad, Long> {
    List<UsuarioHabilidad> findByUsuario(Usuario usuario);

     boolean existsByUsuarioAndHabilidad(Usuario usuario, Habilidad habilidad);
     void deleteByUsuarioCorreo(String correo);

     void deleteByUsuarioCorreoAndHabilidadId(String correo, Long habilidadId);

}


package com.matchwork.user_service.service;

import com.matchwork.user_service.dto.UsuarioHabilidadDTO;
import com.matchwork.user_service.model.Habilidad;
import com.matchwork.user_service.model.Usuario;
import com.matchwork.user_service.model.UsuarioHabilidad;
import com.matchwork.user_service.repository.HabilidadRepository;
import com.matchwork.user_service.repository.UsuarioHabilidadRepository;
import com.matchwork.user_service.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioHabilidadService {

    private final UsuarioRepository usuarioRepository;
    private final HabilidadRepository habilidadRepository;

    private final UsuarioHabilidadRepository usuarioHabilidadRepository;

    public UsuarioHabilidadService(UsuarioHabilidadRepository usuarioHabilidadRepository,
        UsuarioRepository usuarioRepository,
        HabilidadRepository habilidadRepository) {
        this.usuarioHabilidadRepository = usuarioHabilidadRepository;
        this.usuarioRepository = usuarioRepository;
        this.habilidadRepository = habilidadRepository;
    }

    public UsuarioHabilidad agregarHabilidad(Usuario usuario, Habilidad habilidad) {
        return usuarioHabilidadRepository.save(
                UsuarioHabilidad.builder()
                        .usuario(usuario)
                        .habilidad(habilidad)
                        .build()
        );
    }

    public List<UsuarioHabilidad> obtenerHabilidadesDeUsuario(Usuario usuario) {
        return usuarioHabilidadRepository.findByUsuario(usuario);
    }


    public UsuarioHabilidad asociarHabilidad(Long usuarioId, Long habilidadId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    
        Habilidad habilidad = habilidadRepository.findById(habilidadId)
                .orElseThrow(() -> new RuntimeException("Habilidad no encontrada"));

        boolean yaExiste = usuarioHabilidadRepository.existsByUsuarioAndHabilidad(usuario, habilidad);
        if (yaExiste) {
            throw new RuntimeException("Esta habilidad ya está asociada al usuario.");
        }
    
        return usuarioHabilidadRepository.save(
                UsuarioHabilidad.builder()
                        .usuario(usuario)
                        .habilidad(habilidad)
                        .build()
        );
    }


    public List<UsuarioHabilidad> asociarMultiplesHabilidades(Long usuarioId, List<Long> habilidadIds) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<UsuarioHabilidad> nuevas = new ArrayList<>();

        for (Long habilidadId : habilidadIds) {
            Habilidad habilidad = habilidadRepository.findById(habilidadId)
                    .orElseThrow(() -> new RuntimeException("Habilidad con ID " + habilidadId + " no encontrada"));

            boolean yaExiste = usuarioHabilidadRepository.existsByUsuarioAndHabilidad(usuario, habilidad);
            if (!yaExiste) {
                UsuarioHabilidad nueva = UsuarioHabilidad.builder()
                        .usuario(usuario)
                        .habilidad(habilidad)
                        .build();
                nuevas.add(nueva);
            }
        }

        return usuarioHabilidadRepository.saveAll(nuevas);
    }


    @Transactional    
    public void eliminarHabilidadesPorUsuarioId(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuarioHabilidadRepository.deleteByUsuario(usuario);
    }

    @Transactional
    public void eliminarHabilidad(Long usuarioId, Long habilidadId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Habilidad habilidad = habilidadRepository.findById(habilidadId)
                .orElseThrow(() -> new RuntimeException("Habilidad no encontrada"));
        usuarioHabilidadRepository.deleteByUsuarioAndHabilidad(usuario, habilidad);
    }


    

    @Transactional
    public void eliminarHabilidadPorId(Long usuarioHabilidadId) {
        usuarioHabilidadRepository.deleteById(usuarioHabilidadId);
    }

    public List<String> obtenerHabilidadesPorUsuarioId(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return usuarioHabilidadRepository.findByUsuario(usuario).stream()
                .map(uh -> uh.getHabilidad().getNombre())
                .collect(Collectors.toList());
    }

     public List<UsuarioHabilidadDTO> obtenerHabilidadesPorUsuarioIdConId(Long userId) {

        Usuario usuario = usuarioRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

  
        List<UsuarioHabilidad> listaEntidades = usuarioHabilidadRepository.findByUsuario(usuario);

   
        return listaEntidades.stream()
                .map(uh -> new UsuarioHabilidadDTO(
                        uh.getId(),
                        uh.getHabilidad().getNombre()
                ))
                .toList();
    }
}


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


    public UsuarioHabilidad asociarHabilidad(String correoUsuario, Long habilidadId) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
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


    public List<UsuarioHabilidad> asociarMultiplesHabilidades(String correoUsuario, List<Long> habilidadIds) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
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
    public void eliminarHabilidadesPorCorreo(String correo) {
        usuarioHabilidadRepository.deleteByUsuarioCorreo(correo);
    }


    @Transactional
    public void eliminarHabilidad(String correo, Long habilidadId) {
        usuarioHabilidadRepository.deleteByUsuarioCorreoAndHabilidadId(correo, habilidadId);
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
        // 1) Obtener la entidad Usuario (lanzar excepción si no existe)
        Usuario usuario = usuarioRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2) Traer TODAS las filas de la tabla usuario_habilidad para ese usuario
        List<UsuarioHabilidad> listaEntidades = usuarioHabilidadRepository.findByUsuario(usuario);

        // 3) Mapear cada entidad a un DTO (id de la relación, nombre de la habilidad)
        return listaEntidades.stream()
                .map(uh -> new UsuarioHabilidadDTO(
                        uh.getId(),
                        uh.getHabilidad().getNombre()
                ))
                .toList();
    }
}

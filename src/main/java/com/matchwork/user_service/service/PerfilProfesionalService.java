package com.matchwork.user_service.service;

import com.matchwork.user_service.dto.EstudioDTO;
import com.matchwork.user_service.dto.ExperienciaDTO;
import com.matchwork.user_service.dto.PerfilProfesionalDTO;
import com.matchwork.user_service.dto.UsuarioDTO;
import com.matchwork.user_service.model.PerfilEstudio;
import com.matchwork.user_service.model.PerfilExperiencia;
import com.matchwork.user_service.model.PerfilProfesional;
import com.matchwork.user_service.model.Usuario;
import com.matchwork.user_service.repository.PerfilProfesionalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PerfilProfesionalService {

    private final PerfilProfesionalRepository perfilRepo;

    public PerfilProfesionalService(PerfilProfesionalRepository perfilRepo) {
        this.perfilRepo = perfilRepo;
    }

    /**
     * Recupera el PerfilProfesional completo (con usuario, experiencias, estudios)
     * y lo mapea a PerfilProfesionalDTO.
     */
    public PerfilProfesionalDTO getPerfilCompleto(Long userId) {
        PerfilProfesional entidad = perfilRepo.findByIdWithUsuario(userId)
                .orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil profesional no encontrado para userId=" + userId)
                );

        // 1) Mapeamos Usuario -> UsuarioDTO
        Usuario usuario = entidad.getUsuario();
        UsuarioDTO usuarioDTO = new UsuarioDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.getComuna()
        );

        // 2) Mapeamos lista de experiencias
        List<ExperienciaDTO> expDTOs = entidad.getExperiencias().stream()
                .map(this::mapToExperienciaDTO)
                .collect(Collectors.toList());

        // 3) Mapeamos lista de estudios
        List<EstudioDTO> estDTOs = entidad.getEstudios().stream()
                .map(this::mapToEstudioDTO)
                .collect(Collectors.toList());

        // 4) Construimos y retornamos el PerfilProfesionalDTO completo
        return new PerfilProfesionalDTO(
                entidad.getId(),
                usuarioDTO,
                entidad.getTitulo(),
                entidad.getFotoUrl(),
                entidad.getCvUrl(),
                entidad.getPresentacion(),
                entidad.getDisponibilidad(),
                entidad.getModoTrabajo(),
                expDTOs,
                estDTOs
        );
    }

    // ─── Métodos auxiliares de mapeo ───

    private ExperienciaDTO mapToExperienciaDTO(PerfilExperiencia exp) {
        return new ExperienciaDTO(
                exp.getId(),
                exp.getCargo(),
                exp.getEmpresa(),
                exp.getDescripcion(),
                exp.getFechaDesde(),
                exp.getFechaHasta()
        );
    }

    private EstudioDTO mapToEstudioDTO(PerfilEstudio est) {
        return new EstudioDTO(
                est.getId(),
                est.getInstitucion(),
                est.getGrado(),
                est.getFechaDesde(),
                est.getFechaHasta()
        );
    }
}

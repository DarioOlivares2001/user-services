package com.matchwork.user_service.controller;

import com.matchwork.user_service.dto.PerfilEmpresaDTO;
import com.matchwork.user_service.model.PerfilEmpresa;
import com.matchwork.user_service.model.Usuario;
import com.matchwork.user_service.repository.PerfilEmpresaRepository;
import com.matchwork.user_service.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/usuarios/{userId}/perfil-empresa")
@CrossOrigin(origins = "*")
public class PerfilEmpresaController {

    private final PerfilEmpresaRepository perfilRepo;
    private final UsuarioRepository usuarioRepo;

    public PerfilEmpresaController(PerfilEmpresaRepository p,
                                   UsuarioRepository u) {
        this.perfilRepo  = p;
        this.usuarioRepo = u;
    }

   @GetMapping
    public ResponseEntity<PerfilEmpresaDTO> getPerfil(@PathVariable Long userId) {
        return perfilRepo.findById(userId)
              .map(this::toDTO)                                // convertimos la entidad a DTO
              .map(ResponseEntity::ok)
              .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PerfilEmpresaDTO> crearOActualizar(
            @PathVariable Long userId,
            @RequestBody PerfilEmpresa datos) {

        Usuario u = usuarioRepo.findById(userId)
                .orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")
                );

        PerfilEmpresa perfilExistente = perfilRepo.findById(userId).orElse(null);

        if (perfilExistente == null) {
            datos.setUsuario(u);
            PerfilEmpresa creado = perfilRepo.save(datos);

            PerfilEmpresaDTO dto = toDTO(creado);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);

        } else {
            perfilExistente.setNombreFantasia(datos.getNombreFantasia());
            perfilExistente.setLogoUrl(datos.getLogoUrl());
            perfilExistente.setDescripcion(datos.getDescripcion());
            perfilExistente.setIndustria(datos.getIndustria());
            perfilExistente.setUbicacion(datos.getUbicacion());

            PerfilEmpresa actualizado = perfilRepo.save(perfilExistente);

            PerfilEmpresaDTO dto = toDTO(actualizado);
            return ResponseEntity.ok(dto);
        }
    }

    private PerfilEmpresaDTO toDTO(PerfilEmpresa entidad) {
        PerfilEmpresaDTO dto = new PerfilEmpresaDTO();
        dto.setId(entidad.getId());
        dto.setNombreFantasia(entidad.getNombreFantasia());
        dto.setLogoUrl(entidad.getLogoUrl());
        dto.setDescripcion(entidad.getDescripcion());
        dto.setIndustria(entidad.getIndustria());
        dto.setUbicacion(entidad.getUbicacion());
        return dto;
    }

}

package com.matchwork.user_service.controller;

import com.matchwork.user_service.dto.PerfilEmpresaDTO;
import com.matchwork.user_service.model.PerfilEmpresa;
import com.matchwork.user_service.model.Usuario;
import com.matchwork.user_service.repository.PerfilEmpresaRepository;
import com.matchwork.user_service.repository.UsuarioRepository;
import com.matchwork.user_service.service.S3Service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios/{userId}/perfil-empresa")

public class PerfilEmpresaController {

    private final PerfilEmpresaRepository perfilRepo;
    private final UsuarioRepository usuarioRepo;
    private final S3Service s3Service;           // <--- inyectar S3Service
    private static final Logger log = LoggerFactory.getLogger(PerfilEmpresaController.class);


    public PerfilEmpresaController(PerfilEmpresaRepository p,
                                   UsuarioRepository u,
                                   S3Service s3Service) {
        this.perfilRepo  = p;
        this.usuarioRepo = u;
        this.s3Service   = s3Service;
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


 @PostMapping("/logo")
    public ResponseEntity<Map<String, String>> uploadLogo(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {
        try {
            // 1) Verificar usuario existe
            Usuario usr = usuarioRepo.findById(userId)
                .orElseThrow(() ->
                   new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")
                );

            // 2) Cargar o crear perfilEmpresa
            PerfilEmpresa perfil = perfilRepo.findById(userId)
                .orElseGet(() -> {
                    PerfilEmpresa pe = new PerfilEmpresa();
                    pe.setUsuario(usr);
                    return pe;
                });

            // 3) Si ya tenía logo, bórralo de S3
            if (perfil.getLogoUrl() != null) {
                s3Service.deleteFile(perfil.getLogoUrl());
            }

            // 4) Subir nuevo logo
            String logoUrl = s3Service.uploadCompanyLogo(userId, file);

            // 5) Actualizar entidad y guardar
            perfil.setLogoUrl(logoUrl);
            perfilRepo.save(perfil);

            // 6) Responder con la URL
            Map<String, String> resp = new HashMap<>();
            resp.put("logoUrl", logoUrl);
            resp.put("message", "Logo subido exitosamente");
            return ResponseEntity.ok(resp);

        } catch (IOException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Error al procesar el archivo: " + e.getMessage()
            );
        } catch (Exception e) {
            // guardamos la traza completa en el log
            log.error("Fallo subiendo logo de empresa para userId=" + userId, e);
            // re-lanzamos incluyendo la causa
            throw new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Error interno del servidor: " + e.getMessage(),
            e
            );
        }
    }

    // ------------------------------------------------------------
    // 🌟 NUEVO: borrar logo de empresa
    //    DELETE /api/usuarios/{userId}/perfil-empresa/logo
    // ------------------------------------------------------------
    @DeleteMapping("/logo")
    public ResponseEntity<Map<String, String>> deleteLogo(@PathVariable Long userId) {
        try {
            PerfilEmpresa perfil = perfilRepo.findById(userId)
                .orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil no encontrado")
                );

            if (perfil.getLogoUrl() != null) {
                s3Service.deleteFile(perfil.getLogoUrl());
                perfil.setLogoUrl(null);
                perfilRepo.save(perfil);
            }

            Map<String, String> resp = new HashMap<>();
            resp.put("message", "Logo eliminado exitosamente");
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error al eliminar el logo"
            );
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

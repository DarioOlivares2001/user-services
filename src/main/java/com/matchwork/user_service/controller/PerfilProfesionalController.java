// src/main/java/com/matchwork/user_service/controller/PerfilProfesionalController.java
package com.matchwork.user_service.controller;

import com.matchwork.user_service.dto.PerfilProfesionalDTO;
import com.matchwork.user_service.model.PerfilEstudio;
import com.matchwork.user_service.model.PerfilExperiencia;
import com.matchwork.user_service.model.PerfilProfesional;
import com.matchwork.user_service.model.Usuario;
import com.matchwork.user_service.repository.PerfilProfesionalRepository;
import com.matchwork.user_service.repository.UsuarioRepository;
import com.matchwork.user_service.service.PerfilProfesionalService;
import com.matchwork.user_service.service.S3Service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/usuarios/{userId}/perfil-profesional")
@CrossOrigin(origins = "*")
public class PerfilProfesionalController {
    private final PerfilProfesionalRepository perfilRepo;
    private final UsuarioRepository usuarioRepo;
    private final PerfilProfesionalService perfilService;
    private final S3Service s3Service;

    public PerfilProfesionalController(PerfilProfesionalRepository p, UsuarioRepository u, PerfilProfesionalService perfilService,  S3Service s3Service) {
        this.perfilRepo = p;
        this.usuarioRepo = u;
        this.perfilService = perfilService;
        this.s3Service = s3Service;

    }

    @GetMapping
    public ResponseEntity<PerfilProfesional> getPerfil(@PathVariable Long userId) {
        return perfilRepo.findById(userId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PerfilProfesional> crearOActualizar(
        @PathVariable Long userId,
        @RequestBody PerfilProfesional datos
    ) {
        Usuario u = usuarioRepo.findById(userId)
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")
            );

        return perfilRepo.findById(userId)
            .map(existing -> {
                // 1) SI YA EXISTE: actualizo los campos cambiados
                existing.setTitulo(datos.getTitulo());
                existing.setFotoUrl(datos.getFotoUrl());
                existing.setPresentacion(datos.getPresentacion());
                existing.setDisponibilidad(datos.getDisponibilidad());
                existing.setModoTrabajo(datos.getModoTrabajo());

                // Sustituyo las colecciones: EXPERIENCIAS
                existing.getExperiencias().clear();
                for (PerfilExperiencia exp : datos.getExperiencias()) {
                    exp.setPerfilProfesional(existing);
                    existing.getExperiencias().add(exp);
                }

                // SUSTITUYO las colecciones: ESTUDIOS
                existing.getEstudios().clear();
                for (PerfilEstudio est : datos.getEstudios()) {
                    est.setPerfilProfesional(existing);
                    existing.getEstudios().add(est);
                }

                // Hacemos save → Hibernate hará MERGE sobre la fila existente
                return ResponseEntity.ok(perfilRepo.save(existing));
            })
            .orElseGet(() -> {
                // 2) SI NO EXISTE: crearlo de cero
                datos.setUsuario(u);
                // NO usemos datos.setId(userId) porque @MapsId se encarga de eso

                // Asegurar que cada hijo tenga referencia al padre
                for (PerfilExperiencia exp : datos.getExperiencias()) {
                    exp.setPerfilProfesional(datos);
                }
                for (PerfilEstudio est : datos.getEstudios()) {
                    est.setPerfilProfesional(datos);
                }

                // Al hacer save con datos.getId()==null, Hibernate hará PERSIST (INSERT)
                return ResponseEntity.ok(perfilRepo.save(datos));
            });
    }

    /**
     * NUEVO ENDPOINT:
     * GET /api/usuarios/{userId}/perfil-profesional/completo
     * → Devuelve PerfilProfesionalDTO con datos de usuario anidado + experiencias + estudios.
     */
    @GetMapping("/completo")
    public ResponseEntity<PerfilProfesionalDTO> getPerfilCompleto(@PathVariable Long userId) {
        PerfilProfesionalDTO dto = perfilService.getPerfilCompleto(userId);
        return ResponseEntity.ok(dto);
    }

/**
     * NUEVO ENDPOINT: Subir foto de perfil
     * POST /api/usuarios/{userId}/perfil-profesional/foto
     */
    @PostMapping("/foto")
    public ResponseEntity<Map<String, String>> uploadProfilePhoto(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {
        
        try {
            // Verificar que el usuario existe
            usuarioRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

            // Obtener el perfil profesional si existe
            PerfilProfesional perfil = perfilRepo.findById(userId).orElse(null);
            
            // Si ya tiene foto, eliminar la anterior
            if (perfil != null && perfil.getFotoUrl() != null) {
                s3Service.deleteFile(perfil.getFotoUrl());
            }

            // Subir nueva foto
            String fotoUrl = s3Service.uploadProfilePhoto(userId, file);

            // Actualizar en base de datos
            if (perfil != null) {
                perfil.setFotoUrl(fotoUrl);
                perfilRepo.save(perfil);
            }

            Map<String, String> response = new HashMap<>();
            response.put("fotoUrl", fotoUrl);
            response.put("message", "Foto de perfil subida exitosamente");
            
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error al procesar la imagen: " + e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    /**
     * NUEVO ENDPOINT: Subir CV
     * POST /api/usuarios/{userId}/perfil-profesional/cv
     */
    @PostMapping("/cv")
    public ResponseEntity<Map<String, String>> uploadCV(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {
        
        try {
            // Verificar que el usuario existe
            usuarioRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

            // Obtener el perfil profesional si existe
            PerfilProfesional perfil = perfilRepo.findById(userId).orElse(null);
            
            // Si ya tiene CV, eliminar el anterior
            if (perfil != null && perfil.getCvUrl() != null) {
                s3Service.deleteFile(perfil.getCvUrl());
            }

            // Subir nuevo CV
            String cvUrl = s3Service.uploadCV(userId, file);

            // Actualizar en base de datos
            if (perfil != null) {
                perfil.setCvUrl(cvUrl);
                perfilRepo.save(perfil);
            }

            Map<String, String> response = new HashMap<>();
            response.put("cvUrl", cvUrl);
            response.put("message", "CV subido exitosamente");
            
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error al procesar el archivo: " + e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    /**
     * NUEVO ENDPOINT: Eliminar foto de perfil
     * DELETE /api/usuarios/{userId}/perfil-profesional/foto
     */
    @DeleteMapping("/foto")
    public ResponseEntity<Map<String, String>> deleteProfilePhoto(@PathVariable Long userId) {
        try {
            PerfilProfesional perfil = perfilRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil no encontrado"));

            if (perfil.getFotoUrl() != null) {
                s3Service.deleteFile(perfil.getFotoUrl());
                perfil.setFotoUrl(null);
                perfilRepo.save(perfil);
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Foto de perfil eliminada exitosamente");
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al eliminar la foto");
        }
    }

    /**
     * NUEVO ENDPOINT: Eliminar CV
     * DELETE /api/usuarios/{userId}/perfil-profesional/cv
     */
    @DeleteMapping("/cv")
    public ResponseEntity<Map<String, String>> deleteCV(@PathVariable Long userId) {
        try {
            PerfilProfesional perfil = perfilRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil no encontrado"));

            if (perfil.getCvUrl() != null) {
                s3Service.deleteFile(perfil.getCvUrl());
                perfil.setCvUrl(null);
                perfilRepo.save(perfil);
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "CV eliminado exitosamente");
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al eliminar el CV");
        }
    }


}

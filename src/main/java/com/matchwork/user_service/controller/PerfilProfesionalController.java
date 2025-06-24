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
          
                existing.setTitulo(datos.getTitulo());
                existing.setFotoUrl(datos.getFotoUrl());
                existing.setPresentacion(datos.getPresentacion());
                existing.setDisponibilidad(datos.getDisponibilidad());
                existing.setModoTrabajo(datos.getModoTrabajo());

         
                existing.getExperiencias().clear();
                for (PerfilExperiencia exp : datos.getExperiencias()) {
                    exp.setPerfilProfesional(existing);
                    existing.getExperiencias().add(exp);
                }

        
                existing.getEstudios().clear();
                for (PerfilEstudio est : datos.getEstudios()) {
                    est.setPerfilProfesional(existing);
                    existing.getEstudios().add(est);
                }

               
                return ResponseEntity.ok(perfilRepo.save(existing));
            })
            .orElseGet(() -> {
              
                datos.setUsuario(u);
             
                for (PerfilExperiencia exp : datos.getExperiencias()) {
                    exp.setPerfilProfesional(datos);
                }
                for (PerfilEstudio est : datos.getEstudios()) {
                    est.setPerfilProfesional(datos);
                }

         
                return ResponseEntity.ok(perfilRepo.save(datos));
            });
    }

    
    @GetMapping("/completo")
    public ResponseEntity<PerfilProfesionalDTO> getPerfilCompleto(@PathVariable Long userId) {
        PerfilProfesionalDTO dto = perfilService.getPerfilCompleto(userId);
        return ResponseEntity.ok(dto);
    }


    @PostMapping("/foto")
    public ResponseEntity<Map<String, String>> uploadProfilePhoto(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {
        
        try {
       
            usuarioRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

      
            PerfilProfesional perfil = perfilRepo.findById(userId).orElse(null);
            
        
            if (perfil != null && perfil.getFotoUrl() != null) {
                s3Service.deleteFile(perfil.getFotoUrl());
            }

       
            String fotoUrl = s3Service.uploadProfilePhoto(userId, file);

          
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

   
    @PostMapping("/cv")
    public ResponseEntity<Map<String, String>> uploadCV(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {
        
        try {
        
            usuarioRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

       
            PerfilProfesional perfil = perfilRepo.findById(userId).orElse(null);
            
           
            if (perfil != null && perfil.getCvUrl() != null) {
                s3Service.deleteFile(perfil.getCvUrl());
            }

        
            String cvUrl = s3Service.uploadCV(userId, file);

       
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

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

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/usuarios/{userId}/perfil-profesional")
@CrossOrigin(origins = "*")
public class PerfilProfesionalController {
    private final PerfilProfesionalRepository perfilRepo;
    private final UsuarioRepository usuarioRepo;
    private final PerfilProfesionalService perfilService;

    public PerfilProfesionalController(PerfilProfesionalRepository p, UsuarioRepository u, PerfilProfesionalService perfilService) {
        this.perfilRepo = p;
        this.usuarioRepo = u;
        this.perfilService = perfilService;
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
}

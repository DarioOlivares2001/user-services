// src/main/java/com/matchwork/user_service/controller/PerfilExperienciaController.java
package com.matchwork.user_service.controller;

import com.matchwork.user_service.model.PerfilExperiencia;
import com.matchwork.user_service.model.PerfilProfesional;
import com.matchwork.user_service.repository.PerfilExperienciaRepository;
import com.matchwork.user_service.repository.PerfilProfesionalRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios/{userId}/perfil-profesional/experiencias")
@CrossOrigin(origins = "*")
public class PerfilExperienciaController {

  private final PerfilProfesionalRepository perfilRepo;
  private final PerfilExperienciaRepository expRepo;

  public PerfilExperienciaController(
      PerfilProfesionalRepository perfilRepo,
      PerfilExperienciaRepository expRepo
  ) {
    this.perfilRepo = perfilRepo;
    this.expRepo    = expRepo;
  }

  @GetMapping
  public List<PerfilExperiencia> listar(@PathVariable Long userId) {
    return expRepo.findByPerfilProfesionalId(userId);
  }

  @PostMapping
  public ResponseEntity<PerfilExperiencia> crear(
      @PathVariable Long userId,
      @RequestBody PerfilExperiencia nueva
  ) {
    PerfilProfesional perfil = perfilRepo.findById(userId)
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil no existe")
      );
    nueva.setPerfilProfesional(perfil);
    PerfilExperiencia guardada = expRepo.save(nueva);
    return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
  }

  @PutMapping("/{expId}")
  public ResponseEntity<PerfilExperiencia> actualizar(
      @PathVariable Long userId,
      @PathVariable Long expId,
      @RequestBody PerfilExperiencia datos
  ) {
    PerfilExperiencia existente = expRepo.findById(expId)
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Experiencia no existe")
      );
    // opcional: verificar que existente.getPerfilProfesional().getId().equals(userId)
    existente.setEmpresa(datos.getEmpresa());
    existente.setDescripcion(datos.getDescripcion());
    existente.setFechaDesde(datos.getFechaDesde());
    existente.setFechaHasta(datos.getFechaHasta());
    PerfilExperiencia updated = expRepo.save(existente);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{expId}")
  public ResponseEntity<Void> borrar(
      @PathVariable Long userId,
      @PathVariable Long expId
  ) {
    PerfilExperiencia existente = expRepo.findById(expId)
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Experiencia no existe")
      );
    expRepo.delete(existente);
    return ResponseEntity.noContent().build();
  }
}

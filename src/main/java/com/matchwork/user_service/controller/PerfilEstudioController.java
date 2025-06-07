// src/main/java/com/matchwork/user_service/controller/PerfilEstudioController.java
package com.matchwork.user_service.controller;

import com.matchwork.user_service.model.PerfilEstudio;
import com.matchwork.user_service.model.PerfilProfesional;
import com.matchwork.user_service.repository.PerfilEstudioRepository;
import com.matchwork.user_service.repository.PerfilProfesionalRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios/{userId}/perfil-profesional/estudios")
@CrossOrigin(origins = "*")
public class PerfilEstudioController {

  private final PerfilProfesionalRepository perfilRepo;
  private final PerfilEstudioRepository estudioRepo;

  public PerfilEstudioController(
      PerfilProfesionalRepository perfilRepo,
      PerfilEstudioRepository estudioRepo
  ) {
    this.perfilRepo   = perfilRepo;
    this.estudioRepo  = estudioRepo;
  }

  @GetMapping
  public List<PerfilEstudio> listar(@PathVariable Long userId) {
    return estudioRepo.findByPerfilProfesionalId(userId);
  }

  @PostMapping
  public ResponseEntity<PerfilEstudio> crear(
      @PathVariable Long userId,
      @RequestBody PerfilEstudio nuevo
  ) {
    PerfilProfesional perfil = perfilRepo.findById(userId)
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil no existe")
      );
    nuevo.setPerfilProfesional(perfil);
    PerfilEstudio guardado = estudioRepo.save(nuevo);
    return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
  }

  @PutMapping("/{estId}")
  public ResponseEntity<PerfilEstudio> actualizar(
      @PathVariable Long userId,
      @PathVariable Long estId,
      @RequestBody PerfilEstudio datos
  ) {
    PerfilEstudio existente = estudioRepo.findById(estId)
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudio no existe")
      );
    existente.setTitulo(datos.getTitulo());
    existente.setInstitucion(datos.getInstitucion());
    existente.setDescripcion(datos.getDescripcion());
    existente.setGrado(datos.getGrado());
    existente.setFechaDesde(datos.getFechaDesde());
    existente.setFechaHasta(datos.getFechaHasta());
    PerfilEstudio updated = estudioRepo.save(existente);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{estId}")
  public ResponseEntity<Void> borrar(
      @PathVariable Long userId,
      @PathVariable Long estId
  ) {
    PerfilEstudio existente = estudioRepo.findById(estId)
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudio no existe")
      );
    estudioRepo.delete(existente);
    return ResponseEntity.noContent().build();
  }
}

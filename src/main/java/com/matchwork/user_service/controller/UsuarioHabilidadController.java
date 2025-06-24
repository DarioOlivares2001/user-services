package com.matchwork.user_service.controller;

import com.matchwork.user_service.dto.UsuarioHabilidadDTO;
import com.matchwork.user_service.dto.UsuarioHabilidadRequest;
import com.matchwork.user_service.dto.UsuarioHabilidadSingleRequest;
import com.matchwork.user_service.model.UsuarioHabilidad;
import com.matchwork.user_service.service.UsuarioHabilidadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuario-habilidades")
public class UsuarioHabilidadController {

    private final UsuarioHabilidadService usuarioHabilidadService;

    public UsuarioHabilidadController(UsuarioHabilidadService usuarioHabilidadService) {
        this.usuarioHabilidadService = usuarioHabilidadService;
    }

    @PostMapping("/usuario/{userId}")
    public ResponseEntity<UsuarioHabilidad> asociarHabilidad(
            @PathVariable Long userId,
            @RequestBody UsuarioHabilidadSingleRequest request) {
        return ResponseEntity.ok(usuarioHabilidadService.asociarHabilidad(userId, request.habilidadId()));
    }

    @PostMapping("/usuario/{userId}/masivo")
    public ResponseEntity<?> asociarMultiples(
            @PathVariable Long userId,
            @RequestBody UsuarioHabilidadRequest request) {
        return ResponseEntity.ok(usuarioHabilidadService.asociarMultiplesHabilidades(userId, request.habilidadIds()));
    }

    @DeleteMapping("/usuario/{userId}")
    public ResponseEntity<?> eliminarHabilidades(@PathVariable Long userId) {
        usuarioHabilidadService.eliminarHabilidadesPorUsuarioId(userId);
        return ResponseEntity.ok("Habilidades eliminadas correctamente para el usuario con ID: " + userId);
    }

    @DeleteMapping("/{usuarioHabilidadId}")
    public ResponseEntity<?> eliminarHabilidadUsuario(@PathVariable Long usuarioHabilidadId) {
        usuarioHabilidadService.eliminarHabilidadPorId(usuarioHabilidadId);
        return ResponseEntity.ok("Habilidad eliminada correctamente.");
    }

    @GetMapping("/usuario/{userId}")
    public ResponseEntity<List<UsuarioHabilidadDTO>> obtenerHabilidadesPorUsuarioId(@PathVariable Long userId) {
        List<UsuarioHabilidadDTO> lista = usuarioHabilidadService.obtenerHabilidadesPorUsuarioIdConId(userId);
        return ResponseEntity.ok(lista);
    }
}
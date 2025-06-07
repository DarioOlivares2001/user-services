package com.matchwork.user_service.controller;

import com.matchwork.user_service.model.Habilidad;
import com.matchwork.user_service.service.HabilidadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habilidades")
public class HabilidadController {

    private final HabilidadService habilidadService;

    public HabilidadController(HabilidadService habilidadService) {
        this.habilidadService = habilidadService;
    }

    @GetMapping
    public ResponseEntity<List<Habilidad>> listar() {
        return ResponseEntity.ok(habilidadService.listarHabilidades());
    }

    @PostMapping
    public ResponseEntity<Habilidad> crear(@RequestBody Habilidad habilidad) {
        return ResponseEntity.ok(habilidadService.crear(habilidad));
    }

    @PostMapping("/masivo")
    public ResponseEntity<List<Habilidad>> crearMasivo(@RequestBody List<Habilidad> habilidades) {
        return ResponseEntity.ok(habilidadService.crearMasivo(habilidades));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Habilidad>> buscar(@RequestParam("q") String texto) {
        List<Habilidad> resultado = habilidadService.buscarPorNombre(texto);
        return ResponseEntity.ok(resultado);
    }
}

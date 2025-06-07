// src/main/java/com/matchwork/user_service/controller/UsuarioHabilidadController.java
package com.matchwork.user_service.controller;

import com.matchwork.user_service.dto.UsuarioHabilidadDTO;
import com.matchwork.user_service.dto.UsuarioHabilidadRequest;
import com.matchwork.user_service.dto.UsuarioHabilidadSingleRequest;
import com.matchwork.user_service.model.UsuarioHabilidad;
import com.matchwork.user_service.service.UsuarioHabilidadService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuario-habilidades")
public class UsuarioHabilidadController {

    private final UsuarioHabilidadService usuarioHabilidadService;

    public UsuarioHabilidadController(UsuarioHabilidadService usuarioHabilidadService) {
        this.usuarioHabilidadService = usuarioHabilidadService;
    }

    @PostMapping
    public ResponseEntity<UsuarioHabilidad> asociarHabilidad(@RequestBody UsuarioHabilidadSingleRequest request,
                                                            HttpServletRequest httpRequest) {
        String correo = (String) httpRequest.getAttribute("correo");
        return ResponseEntity.ok(usuarioHabilidadService.asociarHabilidad(correo, request.habilidadId()));
    }

    @PostMapping("/masivo")
    public ResponseEntity<?> asociarMultiples(@RequestBody UsuarioHabilidadRequest request,
                                            HttpServletRequest httpRequest) {
        String correo = (String) httpRequest.getAttribute("correo");
        return ResponseEntity.ok(usuarioHabilidadService.asociarMultiplesHabilidades(correo, request.habilidadIds()));
    }


    @DeleteMapping
    public ResponseEntity<?> eliminarHabilidades(HttpServletRequest httpRequest) {
        String correo = (String) httpRequest.getAttribute("correo");
        usuarioHabilidadService.eliminarHabilidadesPorCorreo(correo);
        return ResponseEntity.ok("Habilidades eliminadas correctamente para el usuario con correo: " + correo);
    }

    @DeleteMapping("/{usuarioHabilidadId}")
    public ResponseEntity<?> eliminarHabilidadUsuario(@PathVariable Long usuarioHabilidadId) {
        // Nota: no necesitamos inyectar correo ni buscar al usuario. Borramos la fila por su ID.
        usuarioHabilidadService.eliminarHabilidadPorId(usuarioHabilidadId);
        return ResponseEntity.ok("Habilidad eliminada correctamente.");
    }




    @GetMapping("/usuario/{id}")
    public ResponseEntity<List<UsuarioHabilidadDTO>> obtenerHabilidadesPorUsuarioId(@PathVariable Long id) {
         // Aquí el service debe regresar List<UsuarioHabilidadDTO> en lugar de List<String>.
        List<UsuarioHabilidadDTO> lista = usuarioHabilidadService.obtenerHabilidadesPorUsuarioIdConId(id);
        return ResponseEntity.ok(lista);
    }

}

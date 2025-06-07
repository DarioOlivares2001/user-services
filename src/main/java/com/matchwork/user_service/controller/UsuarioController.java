package com.matchwork.user_service.controller;

import com.matchwork.user_service.model.Usuario;
import com.matchwork.user_service.repository.UsuarioRepository;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @PostMapping("/register")
    public Usuario registrar(@RequestBody Usuario usuario) {
        usuario.setContrasena(new BCryptPasswordEncoder().encode(usuario.getContrasena()));
        return usuarioRepository.save(usuario);
    }

   @GetMapping("/{id}")
    public Usuario obtenerUsuarioPorId(@PathVariable Long id) {
    return usuarioRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

}
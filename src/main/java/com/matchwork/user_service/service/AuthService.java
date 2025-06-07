package com.matchwork.user_service.service;

import com.matchwork.user_service.model.Usuario;
import com.matchwork.user_service.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    public String login(String correo, String contrasena) throws Exception {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new Exception("Usuario no encontrado"));

        if (!new BCryptPasswordEncoder().matches(contrasena, usuario.getContrasena())) {
            throw new Exception("Credenciales inválidas");
        }

        return jwtService.generarToken(usuario.getId(), usuario.getCorreo(), usuario.getRol());
    }
}
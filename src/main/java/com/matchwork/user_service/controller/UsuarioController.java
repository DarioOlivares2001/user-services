package com.matchwork.user_service.controller;

import com.matchwork.user_service.model.Usuario;
import com.matchwork.user_service.dto.ConfirmRequest;
import com.matchwork.user_service.model.LoginRequest;
import com.matchwork.user_service.model.LoginResponse;
import com.matchwork.user_service.service.AuthHybridService;
import com.matchwork.user_service.service.CognitoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.matchwork.user_service.repository.UsuarioRepository;


import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")

public class UsuarioController {

    private final AuthHybridService authService;
    private final CognitoService cognitoService;
     private final UsuarioRepository usuarioRepository;
    

    public UsuarioController(AuthHybridService authService, CognitoService cognitoService, UsuarioRepository usuarioRepository) {
        this.authService = authService;
        this.cognitoService = cognitoService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = authService.registrar(
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getContrasena(),
                usuario.getRol()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("usuario", nuevoUsuario);
            response.put("message", "Usuario registrado. Revisa tu correo para confirmar la cuenta.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        
        String token = authService.login(request.getCorreo(), request.getContrasena());

        Usuario usuario = usuarioRepository
        .findByCorreo(request.getCorreo())
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        // Por seguridad: limpia la contraseña
        usuario.setContrasena(null);

        // 3) Devuelve ambos
        return ResponseEntity.ok(new LoginResponse(token, usuario));
       
    }
    
     @PostMapping("/confirm")
    public ResponseEntity<?> confirmarRegistro(@RequestBody ConfirmRequest req) {
        try {
            cognitoService.confirmSignUp(req.getEmail(), req.getCode());
            return ResponseEntity.ok(Map.of("message", "Cuenta confirmada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }



    // al final de UsuarioController.java
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> findById(@PathVariable Long id) {
        return usuarioRepository.findById(id)
            .map(u -> ResponseEntity.ok(u))
            .orElse(ResponseEntity.notFound().build());
    }


}
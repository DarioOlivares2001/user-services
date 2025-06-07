package com.matchwork.user_service.controller;

import com.matchwork.user_service.model.LoginRequest;
import com.matchwork.user_service.model.LoginResponse;
import com.matchwork.user_service.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) throws Exception {
        String token = authService.login(request.getCorreo(), request.getContrasena());
        return new LoginResponse(token);
    }
}
package com.matchwork.user_service.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.matchwork.user_service.model.Usuario;
import com.matchwork.user_service.repository.UsuarioRepository;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Service
public class AuthHybridService {
    
    private final UsuarioRepository usuarioRepository;
    private final CognitoService cognitoService;
    private final JwtService jwtService; // Tu servicio JWT existente
    private final BCryptPasswordEncoder passwordEncoder;
    
    public AuthHybridService(
        UsuarioRepository usuarioRepository,
        CognitoService cognitoService,
        JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.cognitoService = cognitoService;
        this.jwtService = jwtService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }
    
    // Registro híbrido
    public Usuario registrar(String nombre, String correo, String contrasena, String rol) {
        // 1. Verificar si el usuario ya existe
        if (usuarioRepository.findByCorreo(correo).isPresent()) {
            throw new RuntimeException("El usuario ya existe");
        }
        
        // 2. Registrar en Cognito
        cognitoService.signUp(correo, contrasena, nombre, rol);
        
        // 3. Crear usuario en tu base de datos
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setRol(rol);
        usuario.setUsaCognito(true);
        // No guardamos la contraseña cuando usa Cognito
        
        return usuarioRepository.save(usuario);
    }
    
    // Login híbrido
    public String login(String correo, String contrasena) {
        // 1) Busca al usuario en tu BD
        Usuario usuario = usuarioRepository.findByCorreo(correo)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2) Si está marcado como Cognito, pide el token
        if (Boolean.TRUE.equals(usuario.getUsaCognito())) {
            // Llama a Cognito
            AdminInitiateAuthResponse resp = cognitoService.signIn(correo, contrasena);

            // Coges el ID Token (RS-256) y el Access Token si quisieras
            String idToken     = resp.authenticationResult().idToken();
            // String accessToken = resp.authenticationResult().accessToken();

            // Parseas con Nimbus para extraer el "sub"
            try {
                SignedJWT jwt    = SignedJWT.parse(idToken);
                JWTClaimsSet cs  = jwt.getJWTClaimsSet();
                String sub       = cs.getSubject();

                // Guarda el sub en tu BD la primera vez
                if (usuario.getCognitoSub() == null) {
                    usuario.setCognitoSub(sub);
                    usuarioRepository.save(usuario);
                }
            } catch (Exception ex) {
                throw new RuntimeException("Error parsing ID token: " + ex.getMessage(), ex);
            }

            // Devuelve el ID Token de Cognito (RS-256)
            return idToken;
        }

        // 3) Si no usa Cognito, error (o implementa tu legacy aquí)
        throw new RuntimeException("Este usuario no utiliza Cognito");
    }
    
    private String loginCognito(String correo, String contrasena, Usuario usuario) {
        try {
            AdminInitiateAuthResponse response = cognitoService.signIn(correo, contrasena);
            
            // Extraer información del token de Cognito
            String accessToken = response.authenticationResult().accessToken();
            SignedJWT signedJWT = SignedJWT.parse(accessToken);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            
            // Actualizar cognito_sub si no existe
            if (usuario.getCognitoSub() == null) {
                usuario.setCognitoSub(claims.getSubject());
                usuarioRepository.save(usuario);
            }
            
            // Generar tu propio JWT con la información de tu BD
            return jwtService.generarToken(usuario.getId(), usuario.getCorreo(), usuario.getRol());
            
        } catch (Exception e) {
            throw new RuntimeException("Error en login con Cognito: " + e.getMessage());
        }
    }
    
    private String loginLegacy(String correo, String contrasena, Usuario usuario) {
        if (!passwordEncoder.matches(contrasena, usuario.getContrasena())) {
            throw new RuntimeException("Contraseña incorrecta");
        }
        
        return jwtService.generarToken(usuario.getId(), usuario.getCorreo(), usuario.getRol());
    }
}
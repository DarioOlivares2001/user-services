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
    private final JwtService jwtService; 
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
    
    
    public Usuario registrar(String nombre, String correo, String contrasena, String rol) {
        
        if (usuarioRepository.findByCorreo(correo).isPresent()) {
            throw new RuntimeException("El usuario ya existe");
        }
        
       
        cognitoService.signUp(correo, contrasena, nombre, rol);
        
       
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setRol(rol);
        usuario.setUsaCognito(true);
        
        
        return usuarioRepository.save(usuario);
    }
    
   
    public String login(String correo, String contrasena) {
      
        Usuario usuario = usuarioRepository.findByCorreo(correo)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        
        if (Boolean.TRUE.equals(usuario.getUsaCognito())) {
        
            AdminInitiateAuthResponse resp = cognitoService.signIn(correo, contrasena);

           
            String idToken     = resp.authenticationResult().idToken();
           

         
            try {
                SignedJWT jwt    = SignedJWT.parse(idToken);
                JWTClaimsSet cs  = jwt.getJWTClaimsSet();
                String sub       = cs.getSubject();

               
                if (usuario.getCognitoSub() == null) {
                    usuario.setCognitoSub(sub);
                    usuarioRepository.save(usuario);
                }
            } catch (Exception ex) {
                throw new RuntimeException("Error parsing ID token: " + ex.getMessage(), ex);
            }

  
            return idToken;
        }

        // 3) Si no usa Cognito, error (o implementa tu legacy aquí)
        throw new RuntimeException("Este usuario no utiliza Cognito");
    }
    
    private String loginCognito(String correo, String contrasena, Usuario usuario) {
        try {
            AdminInitiateAuthResponse response = cognitoService.signIn(correo, contrasena);
            
       
            String accessToken = response.authenticationResult().accessToken();
            SignedJWT signedJWT = SignedJWT.parse(accessToken);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            
       
            if (usuario.getCognitoSub() == null) {
                usuario.setCognitoSub(claims.getSubject());
                usuarioRepository.save(usuario);
            }
            
        
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
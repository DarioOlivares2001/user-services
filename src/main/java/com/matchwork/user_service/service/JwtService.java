package com.matchwork.user_service.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private final String SECRET = "clave-super-secreta-256-bits-para-todo-el-sistema";
    private final long EXPIRACION = 1000 * 60 * 60 * 10; // 10 horas

    public Key getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generarToken(Long id, String correo, String rol) {
        return Jwts.builder()
                .setSubject(correo)
                .claim("id", id) 
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRACION))
                .signWith(getKey())
                .compact();
    }
}

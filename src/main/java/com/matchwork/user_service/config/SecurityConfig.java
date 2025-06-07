// src/main/java/com/matchwork/user_service/config/SecurityConfig.java
package com.matchwork.user_service.config;

import com.matchwork.user_service.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          // 1) Aplica configuración CORS (solo orígenes/headers/methods necesarios)
          .cors(cors -> cors.configurationSource(corsConfigurationSource()))
          .csrf(csrf -> csrf.disable())
          .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

          // 2) ACL de las rutas
          .authorizeHttpRequests(auth -> auth
              // 2.1) Rutas públicas de login/registro
              .requestMatchers("/api/usuarios/register", "/api/usuarios/login").permitAll()

              // 2.2) **EXPICÍTAMENTE** abrir el GET de “perfil-profesional/completo”
              //       con wildcard para el {userId}
              .requestMatchers(HttpMethod.GET, "/api/usuarios/*/perfil-profesional/completo").permitAll()

              // 2.2) Permitir todo GET en /api/usuarios/** (para leer perfil de empresa o profesional)
              .requestMatchers(HttpMethod.GET, "/api/usuarios/**").permitAll()

              // 2.3) PERMITIR *TODOS* LOS OPTIONS bajo /api/** (para que CORS preflight no se bloquee jamás)
              .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()

              // 2.4) Perfil Profesional (ejemplo si luego lo necesitas):
              .requestMatchers(HttpMethod.POST,   "/api/usuarios/*/perfil-profesional").permitAll()
              .requestMatchers(HttpMethod.PUT,    "/api/usuarios/*/perfil-profesional").permitAll()
              .requestMatchers(HttpMethod.DELETE, "/api/usuarios/*/perfil-profesional").permitAll()

              // 2.5) Perfil Empresa: permitimos POST y PUT para /api/usuarios/{id}/perfil-empresa
              .requestMatchers(HttpMethod.POST,  "/api/usuarios/*/perfil-empresa").permitAll()
              .requestMatchers(HttpMethod.PUT,   "/api/usuarios/*/perfil-empresa").permitAll()

              // 2.6) Cualquier otra ruta requiere autenticación
              .anyRequest().authenticated()
          )

          // 3) Antes de entrar a cualquier endpoint protegido, aplicamos el filtro JWT
          .addFilterBefore(new JwtAuthorizationFilter(jwtService),
                           BasicAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    static class JwtAuthorizationFilter extends BasicAuthenticationFilter {
        private final JwtService jwtService;

        public JwtAuthorizationFilter(JwtService jwtService) {
            super(authentication -> authentication);
            this.jwtService = jwtService;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain chain)
                                        throws IOException, ServletException {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.replace("Bearer ", "");
                    Jws<Claims> claimsJws = Jwts.parserBuilder()
                            .setSigningKey(jwtService.getKey())
                            .build()
                            .parseClaimsJws(token);

                    Long id   = claimsJws.getBody().get("id", Integer.class).longValue();
                    String correo = claimsJws.getBody().getSubject();
                    String rol    = claimsJws.getBody().get("rol", String.class);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                correo,
                                null,
                                Collections.singleton(() -> "ROLE_" + rol.toUpperCase())
                            );
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                } catch (Exception e) {
                    // Si el token es inválido o expiró, devolvemos 401
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
                    return;
                }
            }
            chain.doFilter(request, response);
        }
    }
}

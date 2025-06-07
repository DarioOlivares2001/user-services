// src/main/java/com/matchwork/user_service/model/UsuarioHabilidad.java
package com.matchwork.user_service.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuario_habilidades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioHabilidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @JsonBackReference
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "habilidad_id")
    
    private Habilidad habilidad;


}

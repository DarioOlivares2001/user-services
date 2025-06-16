// src/main/java/com/matchwork/user_service/model/PerfilProfesional.java
package com.matchwork.user_service.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.Fetch;         // ← IMPORT CORRECTO
import org.hibernate.annotations.FetchMode;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "perfil_profesional")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerfilProfesional {

    
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Usuario usuario;

    private String titulo;           
    private String fotoUrl;
    private String cvUrl;

    @Column(length = 2000)
    private String presentacion;

    private String disponibilidad;    
    private String modoTrabajo;       
   
    @OneToMany(
      mappedBy = "perfilProfesional",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER
    )
    @JsonManagedReference
    @Fetch(FetchMode.SUBSELECT)
    private List<PerfilExperiencia> experiencias = new ArrayList<>();

    
    @OneToMany(
      mappedBy = "perfilProfesional",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER
    )
    @JsonManagedReference
    @Fetch(FetchMode.SUBSELECT)
    private List<PerfilEstudio> estudios = new ArrayList<>();
}

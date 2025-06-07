// src/main/java/com/matchwork/user_service/service/HabilidadService.java
package com.matchwork.user_service.service;

import com.matchwork.user_service.model.Habilidad;
import com.matchwork.user_service.repository.HabilidadRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HabilidadService {

    private final HabilidadRepository habilidadRepository;

    public HabilidadService(HabilidadRepository habilidadRepository) {
        this.habilidadRepository = habilidadRepository;
    }

    public List<Habilidad> listarHabilidades() {
        return habilidadRepository.findAll();
    }

    public Habilidad crear(Habilidad habilidad) {
        return habilidadRepository.save(habilidad);
    }

    public List<Habilidad> crearMasivo(List<Habilidad> habilidades) {
        List<String> nombresExistentes = habilidadRepository.findAll()
                .stream()
                .map(h -> h.getNombre().toLowerCase())
                .toList();
    
        List<Habilidad> nuevas = habilidades.stream()
                .filter(h -> !nombresExistentes.contains(h.getNombre().toLowerCase()))
                .toList();
    
        return habilidadRepository.saveAll(nuevas);
    }

    public List<Habilidad> buscarPorNombre(String query) {
        return habilidadRepository.findByNombreContainingIgnoreCase(query);
    }
    
}

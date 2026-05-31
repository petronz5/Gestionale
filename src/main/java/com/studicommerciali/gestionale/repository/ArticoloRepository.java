package com.studicommerciali.gestionale.repository;

import com.studicommerciali.gestionale.entity.Articolo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ArticoloRepository extends JpaRepository<Articolo, Long> {
    // Trova solo gli articoli attivi, ordinati per descrizione
    List<Articolo> findByAttivoTrueOrderByDescrizioneAsc();
}
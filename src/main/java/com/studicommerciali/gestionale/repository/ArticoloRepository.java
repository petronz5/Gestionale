package com.studicommerciali.gestionale.repository;

import com.studicommerciali.gestionale.entity.Articolo;
import com.studicommerciali.gestionale.entity.Azienda;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ArticoloRepository extends JpaRepository<Articolo, Long> {

    // Metodo SaaS
    List<Articolo> findByAziendaAndAttivoTrueOrderByDescrizioneAsc(Azienda azienda);

    // Metodo vecchio di sicurezza
    List<Articolo> findByAttivoTrueOrderByDescrizioneAsc();
}
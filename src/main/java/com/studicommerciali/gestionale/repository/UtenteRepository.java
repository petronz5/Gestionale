package com.studicommerciali.gestionale.repository;

import com.studicommerciali.gestionale.entity.Utente; // Assicurati che l'entità si chiami Utente o adatta il nome
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UtenteRepository extends JpaRepository<Utente, Long> {
    Optional<Utente> findByUsername(String username);
}
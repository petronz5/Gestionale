package com.studicommerciali.gestionale.repository;

import com.studicommerciali.gestionale.entity.Azienda;
import com.studicommerciali.gestionale.entity.Scadenza;
import com.studicommerciali.gestionale.entity.Scadenza.StatoScadenza;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ScadenzaRepository extends JpaRepository<Scadenza, Long> {

    // --- METODI SAAS (Per i Controller, filtrano in base all'azienda dell'utente) ---
    List<Scadenza> findByAziendaAndStatoOrderByDataScadenzaAsc(Azienda azienda, StatoScadenza stato);

    List<Scadenza> findByAziendaAndDataScadenzaBetweenAndStatoOrderByDataScadenzaAsc(
            Azienda azienda, LocalDate da, LocalDate a, StatoScadenza stato);

    List<Scadenza> findByAziendaAndDataScadenzaAndStato(Azienda azienda, LocalDate dataScadenza, StatoScadenza stato);

    List<Scadenza> findByStatoOrderByDataScadenzaAsc(StatoScadenza stato);

    List<Scadenza> findByDataScadenzaBetweenAndStatoOrderByDataScadenzaAsc(
            LocalDate da, LocalDate a, StatoScadenza stato);

    List<Scadenza> findByDataScadenzaAndStato(LocalDate dataScadenza, StatoScadenza stato);
}
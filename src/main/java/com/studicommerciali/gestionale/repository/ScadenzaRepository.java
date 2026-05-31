package com.studicommerciali.gestionale.repository;

import com.studicommerciali.gestionale.entity.Scadenza;
import com.studicommerciali.gestionale.entity.Scadenza.StatoScadenza;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ScadenzaRepository extends JpaRepository<Scadenza, Long> {

    List<Scadenza> findByStatoOrderByDataScadenzaAsc(StatoScadenza stato);

    List<Scadenza> findByDataScadenzaBetweenAndStatoOrderByDataScadenzaAsc(
            LocalDate da, LocalDate a, StatoScadenza stato);
}
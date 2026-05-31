package com.studicommerciali.gestionale.repository;

import com.studicommerciali.gestionale.entity.Fornitore;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FornitoreRepository extends JpaRepository<Fornitore, Long> {
    List<Fornitore> findByAttivoTrueOrderByRagioneSocialeAsc();
}
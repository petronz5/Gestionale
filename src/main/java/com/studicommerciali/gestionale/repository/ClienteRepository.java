package com.studicommerciali.gestionale.repository;

import com.studicommerciali.gestionale.entity.Azienda;
import com.studicommerciali.gestionale.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByAziendaAndAttivoTrueOrderByRagioneSocialeAsc(Azienda azienda);
    List<Cliente> findByAziendaAndRagioneSocialeContainingIgnoreCaseAndAttivoTrue(Azienda azienda, String q);
    List<Cliente> findByAttivoTrueOrderByRagioneSocialeAsc();

    @Query("""
        SELECT c FROM Cliente c
        WHERE c.attivo = true
          AND (LOWER(c.ragioneSociale) LIKE LOWER(CONCAT('%',:q,'%'))
           OR LOWER(c.codiceFiscale)  LIKE LOWER(CONCAT('%',:q,'%'))
           OR LOWER(c.partitaIva)     LIKE LOWER(CONCAT('%',:q,'%')))
        ORDER BY c.ragioneSociale
        """)
    List<Cliente> cerca(@Param("q") String query);
}
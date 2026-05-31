package com.studicommerciali.gestionale.repository;

import com.studicommerciali.gestionale.entity.Cliente;
import com.studicommerciali.gestionale.entity.Ddt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface DdtRepository extends JpaRepository<Ddt, Long> {

    List<Ddt> findByAnnoOrderByNumeroDesc(int anno);

    @Query("SELECT MAX(CAST(d.numero AS int)) FROM Ddt d WHERE d.anno = :anno")
    Integer ultimoNumero(int anno);

    // --- NUOVI METODI PER LA FATTURAZIONE DIFFERITA ---

    // Trova i clienti che hanno almeno un DDT da fatturare
    @Query("SELECT DISTINCT d.cliente FROM Ddt d WHERE d.stato = 'DA_FATTURARE'")
    List<Cliente> findClientiDaFatturare();

    // Trova i DDT da fatturare per un cliente specifico
    List<Ddt> findByClienteIdAndStatoOrderByDataDocumentoAsc(Long clienteId, Ddt.StatoDdt stato);
}
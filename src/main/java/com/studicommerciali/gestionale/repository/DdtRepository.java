package com.studicommerciali.gestionale.repository;

import com.studicommerciali.gestionale.entity.Azienda;
import com.studicommerciali.gestionale.entity.Ddt;
import com.studicommerciali.gestionale.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DdtRepository extends JpaRepository<Ddt, Long> {

    // ==========================================
    // METODI SAAS (Filtrati per Azienda loggata)
    // ==========================================

    List<Ddt> findByAziendaAndAnnoOrderByNumeroDesc(Azienda azienda, int anno);

    @Query("SELECT MAX(CAST(d.numero AS int)) FROM Ddt d WHERE d.azienda = :azienda AND d.anno = :anno")
    Integer ultimoNumeroAzienda(@Param("azienda") Azienda azienda, @Param("anno") int anno);

    // Passiamo lo StatoDdt come parametro per evitare errori di battitura nelle query
    @Query("SELECT DISTINCT d.cliente FROM Ddt d WHERE d.azienda = :azienda AND d.stato = :stato ORDER BY d.cliente.ragioneSociale ASC")
    List<Cliente> findClientiDaFatturareAzienda(@Param("azienda") Azienda azienda, @Param("stato") Ddt.StatoDdt stato);

    // Spring Data genera in automatico la query corretta usando il tipo Enum
    List<Ddt> findByAziendaAndClienteIdAndStatoOrderByDataDocumentoAsc(Azienda azienda, Long clienteId, Ddt.StatoDdt stato);


    // ==========================================
    // METODI GLOBALI
    // ==========================================

    List<Ddt> findByAnnoOrderByNumeroDesc(int anno);

    @Query("SELECT MAX(CAST(d.numero AS int)) FROM Ddt d WHERE d.anno = :anno")
    Integer ultimoNumero(@Param("anno") int anno);

    @Query("SELECT DISTINCT d.cliente FROM Ddt d WHERE d.stato = :stato")
    List<Cliente> findClientiDaFatturare(@Param("stato") Ddt.StatoDdt stato);

    // Spring Data genera in automatico la query corretta usando il tipo Enum
    List<Ddt> findByClienteIdAndStatoOrderByDataDocumentoAsc(Long clienteId, Ddt.StatoDdt stato);
}
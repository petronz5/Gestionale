package com.studicommerciali.gestionale.repository;

import com.studicommerciali.gestionale.entity.Azienda;
import com.studicommerciali.gestionale.entity.Fattura;
import com.studicommerciali.gestionale.entity.Fattura.StatoFattura;
import com.studicommerciali.gestionale.entity.Fattura.TipoFattura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface FatturaRepository extends JpaRepository<Fattura, Long> {

    List<Fattura> findByAnnoOrderByNumeroDesc(int anno);

    List<Fattura> findByStatoOrderByDataScadenzaAsc(StatoFattura stato);

    List<Fattura> findByAziendaAndAnnoOrderByNumeroDesc(Azienda azienda, int anno);

    // Calcolo del prossimo numero isolato per Azienda
    @Query("SELECT MAX(CAST(f.numero AS int)) FROM Fattura f WHERE f.azienda = :azienda AND f.anno = :anno AND f.tipo = :tipo")
    Integer ultimoNumero(@Param("azienda") Azienda azienda, @Param("anno") int anno, @Param("tipo") Fattura.TipoFattura tipo);

    // Calcolo totali cruscotto isolato per Azienda
    @Query("SELECT SUM(f.totale) FROM Fattura f WHERE f.azienda = :azienda AND f.tipo = :tipo AND f.anno = :anno AND (f.stato = 'EMESSA' OR f.stato = 'PAGATA')")
    java.math.BigDecimal totalePerTipoAnno(@Param("azienda") Azienda azienda, @Param("tipo") Fattura.TipoFattura tipo, @Param("anno") int anno);

    @Query("SELECT COALESCE(SUM(f.totale),0) FROM Fattura f " +
            "WHERE f.tipo = :tipo AND f.anno = :anno AND f.stato != 'ANNULLATA'")
    BigDecimal totalePerTipoAnno(TipoFattura tipo, int anno);

    @Query("SELECT MAX(CAST(f.numero AS int)) FROM Fattura f " +
            "WHERE f.anno = :anno AND f.tipo = :tipo")
    Integer ultimoNumero(int anno, TipoFattura tipo);
}
package com.studicommerciali.gestionale.repository;

import com.studicommerciali.gestionale.entity.Fattura;
import com.studicommerciali.gestionale.entity.Fattura.StatoFattura;
import com.studicommerciali.gestionale.entity.Fattura.TipoFattura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;

public interface FatturaRepository extends JpaRepository<Fattura, Long> {

    List<Fattura> findByAnnoOrderByNumeroDesc(int anno);

    List<Fattura> findByStatoOrderByDataScadenzaAsc(StatoFattura stato);

    @Query("SELECT COALESCE(SUM(f.totale),0) FROM Fattura f " +
            "WHERE f.tipo = :tipo AND f.anno = :anno AND f.stato != 'ANNULLATA'")
    BigDecimal totalePerTipoAnno(TipoFattura tipo, int anno);

    @Query("SELECT MAX(CAST(f.numero AS int)) FROM Fattura f " +
            "WHERE f.anno = :anno AND f.tipo = :tipo")
    Integer ultimoNumero(int anno, TipoFattura tipo);
}
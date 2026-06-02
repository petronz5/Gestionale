package com.studicommerciali.gestionale.repository;

import com.studicommerciali.gestionale.entity.Azienda;
import com.studicommerciali.gestionale.entity.MovimentoCassa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MovimentoCassaRepository extends JpaRepository<MovimentoCassa, Long> {

    List<MovimentoCassa> findByAziendaOrderByDataMovimentoDescIdDesc(Azienda azienda);

    // Calcolo saldo totale dell'azienda (Entrate - Uscite)
    @Query("SELECT COALESCE(SUM(CASE WHEN m.tipo = 'ENTRATA' THEN m.importo ELSE -m.importo END), 0) " +
            "FROM MovimentoCassa m WHERE m.azienda = :azienda")
    java.math.BigDecimal getSaldoAttualeAzienda(@Param("azienda") Azienda azienda);
}
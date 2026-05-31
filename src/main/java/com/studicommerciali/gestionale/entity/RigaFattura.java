package com.studicommerciali.gestionale.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "righe_fattura")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RigaFattura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fattura_id", nullable = false)
    private Fattura fattura;

    @NotBlank
    @Column(nullable = false, length = 500)
    private String descrizione;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantita = BigDecimal.ONE;

    @Column(name = "unita_misura", length = 20)
    private String unitaMisura;

    @Column(name = "prezzo_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal prezzoUnitario = BigDecimal.ZERO;

    @Column(name = "aliquota_iva", nullable = false, precision = 5, scale = 2)
    private BigDecimal aliquotaIva = new BigDecimal("22.00");

    @Column(name = "importo_netto",  nullable = false, precision = 15, scale = 2)
    private BigDecimal importoNetto  = BigDecimal.ZERO;

    @Column(name = "importo_iva",    nullable = false, precision = 15, scale = 2)
    private BigDecimal importoIva    = BigDecimal.ZERO;

    @Column(name = "importo_totale", nullable = false, precision = 15, scale = 2)
    private BigDecimal importoTotale = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer ordine = 1;

    public void ricalcola() {
        this.importoNetto  = quantita.multiply(prezzoUnitario)
                .setScale(2, RoundingMode.HALF_UP);
        this.importoIva    = importoNetto.multiply(aliquotaIva)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        this.importoTotale = importoNetto.add(importoIva);
    }
}
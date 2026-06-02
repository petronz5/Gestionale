package com.studicommerciali.gestionale.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fatture",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"numero", "anno", "tipo"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Fattura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 20)
    private String numero;

    @NotNull
    @Column(nullable = false)
    private Integer anno;

    @NotNull
    @Column(name = "data_emissione", nullable = false)
    private LocalDate dataEmissione;

    @Column(name = "data_scadenza")
    private LocalDate dataScadenza;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoFattura tipo = TipoFattura.ATTIVA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoFattura stato = StatoFattura.BOZZA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornitore_id")
    private Fornitore fornitore;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal imponibile = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal iva = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totale = BigDecimal.ZERO;

    @Column(name = "aliquota_iva", nullable = false, precision = 5, scale = 2)
    private BigDecimal aliquotaIva = new BigDecimal("22.00");

    @Column(name = "metodo_pagamento", length = 30)
    private String metodoPagamento;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "azienda_id", nullable = false)
    private Azienda azienda;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "fattura",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @OrderBy("ordine ASC")
    @Builder.Default
    private List<RigaFattura> righe = new ArrayList<>();

    public void ricalcolaTotali() {
        this.imponibile = righe.stream()
                .map(RigaFattura::getImportoNetto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.iva = righe.stream()
                .map(RigaFattura::getImportoIva)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totale = imponibile.add(iva);
    }

    public String getIntestazione() {
        if (tipo == TipoFattura.ATTIVA && cliente != null)
            return cliente.getDisplayName();
        if (tipo == TipoFattura.PASSIVA && fornitore != null)
            return fornitore.getRagioneSociale();
        return "N/D";
    }


    public enum TipoFattura  { ATTIVA, PASSIVA, NOTA_CREDITO, PREVENTIVO }
    public enum StatoFattura { BOZZA, EMESSA, PAGATA, SCADUTA, ANNULLATA, ACCETTATA, RIFIUTATA }

}
package com.studicommerciali.gestionale.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "scadenze")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Scadenza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String titolo;

    @Column(columnDefinition = "TEXT")
    private String descrizione;

    @NotNull
    @Column(name = "data_scadenza", nullable = false)
    private LocalDate dataScadenza;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoScadenza tipo = TipoScadenza.GENERICO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoScadenza stato = StatoScadenza.APERTA;

    @Column(precision = 15, scale = 2)
    private BigDecimal importo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornitore_id")
    private Fornitore fornitore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fattura_id")
    private Fattura fattura;

    @Column(columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isScaduta() {
        return stato == StatoScadenza.APERTA
                && dataScadenza.isBefore(LocalDate.now());
    }

    public enum TipoScadenza  { FISCALE, PAGAMENTO, INCASSO, ADEMPIMENTO, GENERICO }
    public enum StatoScadenza { APERTA, COMPLETATA, SCADUTA, ANNULLATA }
}
package com.studicommerciali.gestionale.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimenti_cassa")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MovimentoCassa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "data_movimento", nullable = false)
    private LocalDate dataMovimento;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String descrizione;

    @NotNull
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal importo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimento tipo;

    @Column(length = 100)
    private String metodoPagamento = "BONIFICO"; // BONIFICO, CONTANTI, CARTA

    @Column(columnDefinition = "TEXT")
    private String note;

    // Isolamento SaaS Multi-Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "azienda_id", nullable = false)
    private Azienda azienda;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum TipoMovimento { ENTRATA, USCITA }
}
package com.studicommerciali.gestionale.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimenti_magazzino")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MovimentoMagazzino {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articolo_id", nullable = false)
    private Articolo articolo;

    @Column(nullable = false)
    private LocalDateTime dataMovimento = LocalDateTime.now();

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantita; // Positiva se carico, Negativa se scarico

    private String causale;
    private String riferimentoDocumento; // es. "DDT n. 5"
}
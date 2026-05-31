package com.studicommerciali.gestionale.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "articoli")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Articolo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false, length = 50)
    private String codice;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String descrizione;

    @NotNull
    @Column(name = "prezzo_base", nullable = false, precision = 15, scale = 2)
    private BigDecimal prezzoBase = BigDecimal.ZERO;

    @NotNull
    @Column(name = "aliquota_iva", nullable = false, precision = 5, scale = 2)
    private BigDecimal aliquotaIva = new BigDecimal("22.00");

    @Column(name = "unita_misura", length = 10)
    private String unitaMisura = "PZ";

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false)
    private Boolean attivo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
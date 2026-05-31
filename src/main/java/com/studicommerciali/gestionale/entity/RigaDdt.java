package com.studicommerciali.gestionale.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "righe_ddt")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RigaDdt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ddt_id", nullable = false)
    private Ddt ddt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articolo_id")
    private Articolo articolo;

    @NotBlank
    @Column(nullable = false, length = 500)
    private String descrizione;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantita = BigDecimal.ONE;

    @Column(nullable = false)
    private Integer ordine = 1;
}
package com.studicommerciali.gestionale.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ddt", uniqueConstraints = @UniqueConstraint(columnNames = {"numero", "anno"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ddt {

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
    @Column(name = "data_documento", nullable = false)
    private LocalDate dataDocumento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // Campi specifici per il trasporto
    @Column(name = "causale_trasporto")
    private String causaleTrasporto = "Vendita";

    @Column(name = "aspetto_beni")
    private String aspettoBeni = "Scatole";

    @Column(name = "numero_colli")
    private Integer numeroColli = 1;

    private String vettore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoDdt stato = StatoDdt.DA_FATTURARE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "ddt", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordine ASC")
    @Builder.Default
    private List<RigaDdt> righe = new ArrayList<>();

    public enum StatoDdt { DA_FATTURARE, FATTURATO, ANNULLATO }
}
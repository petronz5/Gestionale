package com.studicommerciali.gestionale.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "aziende") // Questo è il Tenant
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Azienda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ragioneSociale;

    @Column(nullable = false, unique = true, length = 11)
    private String partitaIva;

    private String indirizzo;
    private String cap;
    private String citta;
    private String provincia;

    private String email;
    private String telefono;

    // Utile per sapere se il cliente ti sta pagando l'abbonamento o se bloccargli l'accesso
    @Column(nullable = false)
    private boolean abbonamentoAttivo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
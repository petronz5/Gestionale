package com.studicommerciali.gestionale.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "fornitori")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Fornitore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codice_fiscale", length = 16, unique = true)
    private String codiceFiscale;

    @Column(name = "partita_iva", length = 11, unique = true)
    private String partitaIva;

    @NotBlank(message = "La ragione sociale è obbligatoria")
    @Column(name = "ragione_sociale", nullable = false)
    private String ragioneSociale;

    private String indirizzo;
    private String cap;
    private String citta;
    private String provincia;
    private String paese = "IT";
    private String telefono;

    @Email
    private String email;

    @Email
    private String pec;

    @Column(name = "codice_sdi", length = 7)
    private String codiceSdi;

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
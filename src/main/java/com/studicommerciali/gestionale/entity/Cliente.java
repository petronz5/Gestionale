package com.studicommerciali.gestionale.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "clienti")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cliente {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCliente tipo = TipoCliente.PERSONA_FISICA;

    private String nome;
    private String cognome;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "azienda_id", nullable = false)
    private Azienda azienda;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TipoCliente { PERSONA_FISICA, PERSONA_GIURIDICA }

    public String getDisplayName() {
        if (tipo == TipoCliente.PERSONA_FISICA
                && nome != null && cognome != null) {
            return cognome + " " + nome;
        }
        return ragioneSociale;
    }
}
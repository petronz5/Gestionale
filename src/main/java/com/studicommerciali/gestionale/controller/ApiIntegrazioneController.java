package com.studicommerciali.gestionale.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/integrazioni")
public class ApiIntegrazioneController {

    // Questo endpoint funge da "Proxy". In un caso reale qui faresti
    // una richiesta HTTP (RestTemplate/WebClient) al VIES o all'Agenzia delle Entrate.
    @GetMapping("/piva/{partitaIva}")
    public ResponseEntity<Map<String, String>> cercaDatiAzienda(@PathVariable String partitaIva) {

        Map<String, String> dati = new HashMap<>();

        // Simulo una risposta da un server esterno
        if (partitaIva.length() == 11) {
            dati.put("ragioneSociale", "Azienda Trovata da API S.p.A.");
            dati.put("indirizzo", "Via dell'Innovazione 42");
            dati.put("cap", "20100");
            dati.put("citta", "Milano");
            dati.put("provincia", "MI");
            return ResponseEntity.ok(dati);
        }

        return ResponseEntity.notFound().build(); // Restituisce 404 se PIVA non valida
    }
}
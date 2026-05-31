package com.studicommerciali.gestionale.service;

import com.studicommerciali.gestionale.entity.Utente;
import com.studicommerciali.gestionale.repository.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtenteRepository utenteRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("\n\n=== 🚦 INIZIO CONTROLLO LOGIN PER: " + username + " 🚦 ===");

        Utente utente = utenteRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("❌ ERRORE: L'utente '" + username + "' NON esiste nel database!");
                    return new UsernameNotFoundException("Utente non trovato");
                });

        System.out.println("✅ Utente trovato nel database!");
        System.out.println("👉 Hash memorizzato: " + utente.getPassword());
        System.out.println("👉 Lunghezza Hash: " + utente.getPassword().length() + " caratteri (ATTENZIONE: DEVE essere 60!)");
        System.out.println("👉 Stato Attivo: " + utente.isAttivo());

        String ruolo = utente.getRuolo().startsWith("ROLE_") ? utente.getRuolo() : "ROLE_" + utente.getRuolo();
        System.out.println("👉 Ruolo Assegnato: " + ruolo);
        System.out.println("=== 🚦 FINE PREPARAZIONE UTENTE 🚦 ===\n\n");

        return User.builder()
                .username(utente.getUsername())
                .password(utente.getPassword())
                .authorities(ruolo)
                .disabled(!utente.isAttivo())
                .build();
    }
}
package com.studicommerciali.gestionale.service;

import com.studicommerciali.gestionale.entity.Utente;
import com.studicommerciali.gestionale.repository.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importazione fondamentale

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtenteRepository utenteRepo;

    @Override
    @Transactional // <-- LA MAGIA CHE RISOLVE L'ERRORE "NO SESSION"
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        System.out.println("======> TENTATIVO DI LOGIN PER: " + usernameOrEmail);

        Utente utente = utenteRepo.findByUsername(usernameOrEmail).orElse(null);

        if (utente == null) {
            utente = utenteRepo.findByEmail(usernameOrEmail)
                    .orElseThrow(() -> {
                        System.out.println("======> ERRORE: Utente non trovato nel Database!");
                        return new UsernameNotFoundException("Utente non trovato");
                    });
        }

        if (!utente.isAttivo()) {
            System.out.println("======> ERRORE: L'utente è disattivato!");
            throw new RuntimeException("Utente disattivato dall'amministratore");
        }

        // Ora che c'è @Transactional, Hibernate riesce a leggere l'Azienda senza crashare
        if (utente.getAzienda() != null && !utente.getAzienda().isAbbonamentoAttivo()) {
            System.out.println("======> ERRORE: Abbonamento Azienda scaduto!");
            throw new RuntimeException("L'abbonamento della tua Azienda è sospeso.");
        }

        System.out.println("======> UTENTE TROVATO CON SUCCESSO: " + utente.getUsername());

        return new User(
                utente.getUsername(),
                utente.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + utente.getRuolo()))
        );
    }
}
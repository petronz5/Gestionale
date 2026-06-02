package com.studicommerciali.gestionale.config;

import com.studicommerciali.gestionale.entity.Azienda;
import com.studicommerciali.gestionale.entity.Utente;
import com.studicommerciali.gestionale.repository.AziendaRepository;
import com.studicommerciali.gestionale.repository.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AziendaRepository aziendaRepo;
    private final UtenteRepository utenteRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Se non ci sono aziende, creo l'ambiente di test iniziale
        if (aziendaRepo.count() == 0) {

            Azienda aziendaTest = new Azienda();
            aziendaTest.setRagioneSociale("Azienda Beta S.p.A.");
            aziendaTest.setPartitaIva("11111111111");
            aziendaTest.setAbbonamentoAttivo(true);
            aziendaRepo.save(aziendaTest);

            Utente admin = new Utente();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin")); // La password è: admin
            admin.setEmail("admin@aziendabeta.it");
            admin.setRuolo("ADMIN");
            admin.setAttivo(true);
            admin.setAzienda(aziendaTest);
            utenteRepo.save(admin);

            System.out.println("✅ Database inizializzato!");
            System.out.println("👉 Usa Username: admin | Password: admin");
        }
    }
}
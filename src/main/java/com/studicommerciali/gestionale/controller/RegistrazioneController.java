package com.studicommerciali.gestionale.controller;

import com.studicommerciali.gestionale.entity.Azienda;
import com.studicommerciali.gestionale.entity.Utente;
import com.studicommerciali.gestionale.repository.AziendaRepository;
import com.studicommerciali.gestionale.repository.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/registrati")
@RequiredArgsConstructor
public class RegistrazioneController {

    private final AziendaRepository aziendaRepo;
    private final UtenteRepository utenteRepo;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String mostraForm(Model model) {
        return "registrati";
    }

    @PostMapping
    public String eseguiRegistrazione(
            @RequestParam String ragioneSociale,
            @RequestParam String partitaIva,
            @RequestParam String indirizzo,
            @RequestParam String cap,
            @RequestParam String citta,
            @RequestParam String provincia,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            RedirectAttributes ra) {

        // 1. Controllo di sicurezza: la P.IVA o l'username esistono già?
        if (utenteRepo.findByUsername(username).isPresent()) {
            ra.addFlashAttribute("errore", "Questo username è già registrato.");
            return "redirect:/registrati";
        }
        if (utenteRepo.findByEmail(email).isPresent()) {
            ra.addFlashAttribute("errore", "Questa email è già associata a un abbonamento.");
            return "redirect:/registrati";
        }

        // 2. Creazione dell'Azienda (Tenant)
        Azienda nuovaAzienda = Azienda.builder()
                .ragioneSociale(ragioneSociale)
                .partitaIva(partitaIva)
                .indirizzo(indirizzo)
                .cap(cap)
                .citta(citta)
                .provincia(provincia)
                .abbonamentoAttivo(true) // Attivo di default, in futuro legato a Stripe
                .build();
        Azienda aziendaSalvata = aziendaRepo.save(nuovaAzienda);

        // 3. Creazione dell'Utente Amministratore per quell'azienda
        Utente utenteAdmin = Utente.builder()
                .username(username)
                .password(passwordEncoder.encode(password)) // Criptiamo la password obbligatoriamente
                .email(email)
                .ruolo("ADMIN")
                .attivo(true)
                .azienda(aziendaSalvata) // Colleghiamo l'utente al suo Tenant isolato
                .build();
        utenteRepo.save(utenteAdmin);

        ra.addFlashAttribute("successo", "Registrazione completata con successo! Ora puoi accedere col tuo account aziendale.");
        return "redirect:/login";
    }

    @GetMapping("/forza-creazione-admin")
    @ResponseBody
    public String forzaCreazione() {
        if (utenteRepo.findByUsername("admin").isPresent()) {
            return "Utente admin già esistente! Vai su /login.";
        }

        // Creiamo il Tenant
        Azienda az = new Azienda();
        az.setRagioneSociale("Azienda Test SaaS");
        az.setPartitaIva("00000000000");
        az.setAbbonamentoAttivo(true);
        aziendaRepo.save(az);

        // Creiamo l'Amministratore
        Utente admin = new Utente();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin")); // La password "admin" viene criptata!
        admin.setEmail("admin@testsaas.it");
        admin.setRuolo("ADMIN");
        admin.setAttivo(true);
        admin.setAzienda(az);
        utenteRepo.save(admin);

        return "Utente creato con successo! Ora vai su http://localhost:8080/login e usa admin / admin";
    }
}
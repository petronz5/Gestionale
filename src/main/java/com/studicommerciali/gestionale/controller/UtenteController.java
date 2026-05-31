package com.studicommerciali.gestionale.controller;

import com.studicommerciali.gestionale.entity.Utente;
import com.studicommerciali.gestionale.repository.UtenteRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/utenti")
@RequiredArgsConstructor
public class UtenteController {

    private final UtenteRepository repo;
    private final PasswordEncoder passwordEncoder; // Ci serve per criptare le nuove password

    @GetMapping
    public String lista(Model model) {
        List<Utente> utenti = repo.findAll(); // Mostriamo tutti, sia attivi che disattivati
        model.addAttribute("utenti", utenti);
        return "utenti/lista";
    }

    @GetMapping("/nuovo")
    public String nuovo(Model model) {
        Utente utente = new Utente();
        utente.setAttivo(true); // Di default un nuovo utente è attivo
        model.addAttribute("utente", utente);
        return "utenti/form";
    }

    @GetMapping("/{id}/modifica")
    public String modifica(@PathVariable Long id, Model model) {
        model.addAttribute("utente", repo.findById(id).orElseThrow());
        return "utenti/form";
    }

    @PostMapping("/salva")
    public String salva(@Valid @ModelAttribute Utente utente, BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) return "utenti/form";

        if (utente.getId() == null) {
            // È un NUOVO utente: dobbiamo criptare la password inserita nel form
            utente.setPassword(passwordEncoder.encode(utente.getPassword()));
        } else {
            // È una MODIFICA: gestiamo il caso in cui la password venga lasciata vuota
            Utente esistente = repo.findById(utente.getId()).orElseThrow();
            if (utente.getPassword() == null || utente.getPassword().isBlank()) {
                utente.setPassword(esistente.getPassword()); // Mantiene la vecchia password
            } else {
                utente.setPassword(passwordEncoder.encode(utente.getPassword())); // Aggiorna con la nuova
            }
            // Manteniamo la data di creazione originale
            utente.setCreatedAt(esistente.getCreatedAt());
        }

        repo.save(utente);
        ra.addFlashAttribute("successo", "Utente salvato correttamente.");
        return "redirect:/utenti";
    }

    // Rotta speciale per bloccare o sbloccare l'accesso di un utente
    @PostMapping("/{id}/toggle-stato")
    public String toggleStato(@PathVariable Long id, RedirectAttributes ra) {
        Utente u = repo.findById(id).orElseThrow();

        // Evitiamo che l'admin disattivi l'unico account admin base, causando un "lockout"
        if ("admin".equals(u.getUsername()) && u.isAttivo()) {
            ra.addFlashAttribute("errore", "Impossibile disattivare l'account amministratore principale.");
            return "redirect:/utenti";
        }

        u.setAttivo(!u.isAttivo()); // Inverte lo stato logico (da true a false o viceversa)
        repo.save(u);

        String msg = u.isAttivo() ? "Utente abilitato all'accesso." : "Accesso bloccato per l'utente.";
        ra.addFlashAttribute("successo", msg);
        return "redirect:/utenti";
    }
}
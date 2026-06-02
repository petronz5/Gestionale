package com.studicommerciali.gestionale.controller;

import com.studicommerciali.gestionale.entity.Azienda;
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

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/utenti")
@RequiredArgsConstructor
public class UtenteController {

    private final UtenteRepository repo;
    private final PasswordEncoder passwordEncoder;

    // Metodo di supporto SaaS per isolare i dati
    private Azienda getAziendaLoggata(Principal principal) {
        Utente utente = repo.findByUsername(principal.getName()).orElseThrow();
        return utente.getAzienda();
    }

    @GetMapping
    public String lista(Model model, Principal principal) {
        Azienda miaAzienda = getAziendaLoggata(principal);

        // Mostriamo solo gli utenti della PROPRIA azienda
        List<Utente> utenti = repo.findAll().stream()
                .filter(u -> u.getAzienda() != null && u.getAzienda().getId().equals(miaAzienda.getId()))
                .toList();

        model.addAttribute("utenti", utenti);
        return "utenti/lista";
    }

    @GetMapping("/nuovo")
    public String nuovo(Model model) {
        Utente utente = new Utente();
        utente.setAttivo(true);
        model.addAttribute("utente", utente);
        return "utenti/form";
    }

    @GetMapping("/{id}/modifica")
    public String modifica(@PathVariable Long id, Model model, Principal principal) {
        Utente utente = repo.findById(id).orElseThrow();

        // Controllo di sicurezza SaaS
        if (!utente.getAzienda().getId().equals(getAziendaLoggata(principal).getId())) {
            throw new SecurityException("Accesso Negato");
        }

        model.addAttribute("utente", utente);
        return "utenti/form";
    }

    @PostMapping("/salva")
    public String salva(@Valid @ModelAttribute Utente utente, BindingResult result, Principal principal, RedirectAttributes ra) {
        Azienda miaAzienda = getAziendaLoggata(principal);
        if (result.hasErrors()) return "utenti/form";

        utente.setAzienda(miaAzienda); // Forza l'assegnazione al Tenant SaaS corretto

        if (utente.getId() == null) {
            utente.setPassword(passwordEncoder.encode(utente.getPassword()));
        } else {
            Utente esistente = repo.findById(utente.getId()).orElseThrow();
            if (utente.getPassword() == null || utente.getPassword().isBlank()) {
                utente.setPassword(esistente.getPassword());
            } else {
                utente.setPassword(passwordEncoder.encode(utente.getPassword()));
            }
            utente.setCreatedAt(esistente.getCreatedAt());
        }

        repo.save(utente);
        ra.addFlashAttribute("successo", "Utente salvato correttamente.");
        return "redirect:/utenti";
    }

    @PostMapping("/{id}/toggle-stato")
    public String toggleStato(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        Utente u = repo.findById(id).orElseThrow();

        // Controllo di sicurezza SaaS
        if (!u.getAzienda().getId().equals(getAziendaLoggata(principal).getId())) {
            throw new SecurityException("Accesso Negato");
        }

        if ("admin".equals(u.getUsername()) && u.isAttivo()) {
            ra.addFlashAttribute("errore", "Impossibile disattivare l'account amministratore principale.");
            return "redirect:/utenti";
        }

        u.setAttivo(!u.isAttivo());
        repo.save(u);

        String msg = u.isAttivo() ? "Utente abilitato all'accesso." : "Accesso bloccato per l'utente.";
        ra.addFlashAttribute("successo", msg);
        return "redirect:/utenti";
    }
}
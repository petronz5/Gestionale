package com.studicommerciali.gestionale.controller;

import com.studicommerciali.gestionale.entity.Articolo;
import com.studicommerciali.gestionale.entity.Azienda;
import com.studicommerciali.gestionale.entity.Utente;
import com.studicommerciali.gestionale.repository.ArticoloRepository;
import com.studicommerciali.gestionale.repository.UtenteRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/articoli")
@RequiredArgsConstructor
public class ArticoloController {

    private final ArticoloRepository repo;
    private final UtenteRepository utenteRepo;

    private Azienda getAziendaLoggata(Principal principal) {
        Utente u = utenteRepo.findByUsername(principal.getName()).orElseThrow();
        return u.getAzienda();
    }

    @GetMapping
    public String lista(Model model, Principal principal) {
        Azienda miaAzienda = getAziendaLoggata(principal);
        model.addAttribute("articoli", repo.findByAziendaAndAttivoTrueOrderByDescrizioneAsc(miaAzienda));
        return "articoli/lista";
    }

    @GetMapping("/nuovo")
    public String nuovo(Model model) {
        model.addAttribute("articolo", new Articolo());
        return "articoli/form";
    }

    @GetMapping("/{id}/modifica")
    public String modifica(@PathVariable Long id, Model model, Principal principal) {
        Articolo articolo = repo.findById(id).orElseThrow();
        if (!articolo.getAzienda().getId().equals(getAziendaLoggata(principal).getId())) {
            throw new SecurityException("Accesso Negato");
        }
        model.addAttribute("articolo", articolo);
        return "articoli/form";
    }

    @PostMapping("/salva")
    public String salva(@Valid @ModelAttribute Articolo articolo, BindingResult result, Principal principal, RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "articoli/form";
        }

        // Assegnazione SaaS
        articolo.setAzienda(getAziendaLoggata(principal));

        repo.save(articolo);
        ra.addFlashAttribute("successo", "Articolo salvato correttamente.");
        return "redirect:/articoli";
    }

    @PostMapping("/{id}/elimina")
    public String elimina(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        Articolo articolo = repo.findById(id).orElseThrow();
        if (articolo.getAzienda().getId().equals(getAziendaLoggata(principal).getId())) {
            articolo.setAttivo(false);
            repo.save(articolo);
            ra.addFlashAttribute("successo", "Articolo disattivato.");
        }
        return "redirect:/articoli";
    }
}
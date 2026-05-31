package com.studicommerciali.gestionale.controller;

import com.studicommerciali.gestionale.entity.Articolo;
import com.studicommerciali.gestionale.repository.ArticoloRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/articoli")
@RequiredArgsConstructor
public class ArticoloController {

    private final ArticoloRepository repo;

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("articoli", repo.findByAttivoTrueOrderByDescrizioneAsc());
        return "articoli/lista";
    }

    @GetMapping("/nuovo")
    public String nuovo(Model model) {
        model.addAttribute("articolo", new Articolo());
        return "articoli/form";
    }

    @GetMapping("/{id}/modifica")
    public String modifica(@PathVariable Long id, Model model) {
        model.addAttribute("articolo", repo.findById(id).orElseThrow());
        return "articoli/form";
    }

    @PostMapping("/salva")
    public String salva(@Valid @ModelAttribute Articolo articolo, BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) return "articoli/form";
        repo.save(articolo);
        ra.addFlashAttribute("successo", "Articolo salvato nel catalogo.");
        return "redirect:/articoli";
    }

    @PostMapping("/{id}/elimina")
    public String elimina(@PathVariable Long id, RedirectAttributes ra) {
        Articolo a = repo.findById(id).orElseThrow();
        a.setAttivo(false); // Soft delete
        repo.save(a);
        ra.addFlashAttribute("successo", "Articolo disattivato.");
        return "redirect:/articoli";
    }
}
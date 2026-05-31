package com.studicommerciali.gestionale.controller;

import com.studicommerciali.gestionale.entity.Fornitore;
import com.studicommerciali.gestionale.repository.FornitoreRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/fornitori")
@RequiredArgsConstructor
public class FornitoreController {

    private final FornitoreRepository repo;

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("fornitori",
                repo.findByAttivoTrueOrderByRagioneSocialeAsc());
        return "fornitori/lista";
    }

    @GetMapping("/nuovo")
    public String nuovo(Model model) {
        model.addAttribute("fornitore", new Fornitore());
        return "fornitori/form";
    }

    @GetMapping("/{id}/modifica")
    public String modifica(@PathVariable Long id, Model model) {
        model.addAttribute("fornitore",
                repo.findById(id).orElseThrow());
        return "fornitori/form";
    }

    @PostMapping("/salva")
    public String salva(@Valid @ModelAttribute Fornitore fornitore,
                        BindingResult result,
                        RedirectAttributes ra) {
        if (result.hasErrors()) return "fornitori/form";
        repo.save(fornitore);
        ra.addFlashAttribute("successo", "Fornitore salvato.");
        return "redirect:/fornitori";
    }

    @PostMapping("/{id}/elimina")
    public String elimina(@PathVariable Long id, RedirectAttributes ra) {
        Fornitore f = repo.findById(id).orElseThrow();
        f.setAttivo(false);
        repo.save(f);
        ra.addFlashAttribute("successo", "Fornitore disattivato.");
        return "redirect:/fornitori";
    }
}
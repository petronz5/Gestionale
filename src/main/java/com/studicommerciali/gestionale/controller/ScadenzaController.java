package com.studicommerciali.gestionale.controller;

import com.studicommerciali.gestionale.entity.Scadenza;
import com.studicommerciali.gestionale.entity.Scadenza.StatoScadenza;
import com.studicommerciali.gestionale.repository.ClienteRepository;
import com.studicommerciali.gestionale.repository.ScadenzaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/scadenze")
@RequiredArgsConstructor
public class ScadenzaController {

    private final ScadenzaRepository repo;
    private final ClienteRepository  clienteRepo;

    @GetMapping
    public String lista(@RequestParam(required = false) String stato, Model model) {
        var scadenze = stato != null && !stato.isBlank()
                ? repo.findByStatoOrderByDataScadenzaAsc(StatoScadenza.valueOf(stato))
                : repo.findByStatoOrderByDataScadenzaAsc(StatoScadenza.APERTA);

        model.addAttribute("scadenze", scadenze);
        model.addAttribute("stati",    StatoScadenza.values());
        model.addAttribute("filtroStato", stato != null ? stato : "APERTA");
        return "scadenze/lista";
    }

    @GetMapping("/nuova")
    public String nuova(Model model) {
        model.addAttribute("scadenza", new Scadenza());
        model.addAttribute("tipi",     Scadenza.TipoScadenza.values());
        model.addAttribute("clienti",  clienteRepo.findByAttivoTrueOrderByRagioneSocialeAsc());
        return "scadenze/form";
    }

    @GetMapping("/{id}/modifica")
    public String modifica(@PathVariable Long id, Model model) {
        model.addAttribute("scadenza", repo.findById(id).orElseThrow());
        model.addAttribute("tipi",     Scadenza.TipoScadenza.values());
        model.addAttribute("clienti",  clienteRepo.findByAttivoTrueOrderByRagioneSocialeAsc());
        return "scadenze/form";
    }

    @PostMapping("/salva")
    public String salva(@Valid @ModelAttribute Scadenza scadenza,
                        BindingResult result,
                        @RequestParam(required = false) Long clienteId,
                        Model model,
                        RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("tipi",    Scadenza.TipoScadenza.values());
            model.addAttribute("clienti", clienteRepo.findByAttivoTrueOrderByRagioneSocialeAsc());
            return "scadenze/form";
        }
        if (clienteId != null)
            scadenza.setCliente(clienteRepo.findById(clienteId).orElse(null));
        repo.save(scadenza);
        ra.addFlashAttribute("successo", "Scadenza salvata.");
        return "redirect:/scadenze";
    }

    @PostMapping("/{id}/completa")
    public String completa(@PathVariable Long id, RedirectAttributes ra) {
        Scadenza s = repo.findById(id).orElseThrow();
        s.setStato(StatoScadenza.COMPLETATA);
        repo.save(s);
        ra.addFlashAttribute("successo", "Scadenza completata.");
        return "redirect:/scadenze";
    }

    @PostMapping("/{id}/elimina")
    public String elimina(@PathVariable Long id, RedirectAttributes ra) {
        repo.deleteById(id);
        ra.addFlashAttribute("successo", "Scadenza eliminata.");
        return "redirect:/scadenze";
    }
}
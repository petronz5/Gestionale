package com.studicommerciali.gestionale.controller;

import com.studicommerciali.gestionale.entity.Fattura;
import com.studicommerciali.gestionale.entity.Fattura.TipoFattura;
import com.studicommerciali.gestionale.entity.Fattura.StatoFattura;
import com.studicommerciali.gestionale.repository.ClienteRepository;
import com.studicommerciali.gestionale.repository.FatturaRepository;
import com.studicommerciali.gestionale.repository.FornitoreRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Year;
import java.util.List;

@Controller
@RequestMapping("/fatture")
@RequiredArgsConstructor
public class FatturaController {

    private final FatturaRepository fatturaRepo;
    private final ClienteRepository clienteRepo;
    private final FornitoreRepository fornitoreRepo;

    @GetMapping
    public String lista(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String stato,
            @RequestParam(required = false, defaultValue = "0") int anno,
            Model model) {

        if (anno == 0) anno = Year.now().getValue();

        List<Fattura> fatture = fatturaRepo.findByAnnoOrderByNumeroDesc(anno);

        if (tipo != null && !tipo.isBlank())
            fatture = fatture.stream()
                    .filter(f -> f.getTipo().name().equals(tipo)).toList();
        if (stato != null && !stato.isBlank())
            fatture = fatture.stream()
                    .filter(f -> f.getStato().name().equals(stato)).toList();

        model.addAttribute("fatture", fatture);
        model.addAttribute("tipi",   TipoFattura.values());
        model.addAttribute("stati",  StatoFattura.values());
        model.addAttribute("anno",   anno);
        model.addAttribute("filtroTipo",  tipo);
        model.addAttribute("filtroStato", stato);

        // Totali
        model.addAttribute("totFatturato",
                fatturaRepo.totalePerTipoAnno(TipoFattura.ATTIVA,  anno));
        model.addAttribute("totCosti",
                fatturaRepo.totalePerTipoAnno(TipoFattura.PASSIVA, anno));

        return "fatture/lista";
    }

    @GetMapping("/nuova")
    public String nuova(@RequestParam(defaultValue = "ATTIVA") String tipo, Model model) {
        Fattura f = new Fattura();
        f.setTipo(TipoFattura.valueOf(tipo));
        f.setAnno(Year.now().getValue());
        f.setAliquotaIva(new java.math.BigDecimal("22.00"));

        // Prossimo numero
        Integer ultimo = fatturaRepo.ultimoNumero(
                Year.now().getValue(), TipoFattura.valueOf(tipo));
        f.setNumero(String.valueOf(ultimo != null ? ultimo + 1 : 1));

        model.addAttribute("fattura", f);
        model.addAttribute("clienti",   clienteRepo.findByAttivoTrueOrderByRagioneSocialeAsc());
        model.addAttribute("fornitori", fornitoreRepo.findByAttivoTrueOrderByRagioneSocialeAsc());
        model.addAttribute("stati",     StatoFattura.values());
        return "fatture/form";
    }

    @GetMapping("/{id}")
    public String dettaglio(@PathVariable Long id, Model model) {
        model.addAttribute("fattura",
                fatturaRepo.findById(id)
                        .orElseThrow(() -> new RuntimeException("Fattura non trovata")));
        return "fatture/dettaglio";
    }

    @GetMapping("/{id}/modifica")
    public String modifica(@PathVariable Long id, Model model) {
        model.addAttribute("fattura",   fatturaRepo.findById(id).orElseThrow());
        model.addAttribute("clienti",   clienteRepo.findByAttivoTrueOrderByRagioneSocialeAsc());
        model.addAttribute("fornitori", fornitoreRepo.findByAttivoTrueOrderByRagioneSocialeAsc());
        model.addAttribute("stati",     StatoFattura.values());
        return "fatture/form";
    }

    @PostMapping("/salva")
    public String salva(@Valid @ModelAttribute Fattura fattura,
                        BindingResult result,
                        @RequestParam(required = false) Long clienteId,
                        @RequestParam(required = false) Long fornitoreId,
                        Model model,
                        RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("clienti",   clienteRepo.findByAttivoTrueOrderByRagioneSocialeAsc());
            model.addAttribute("fornitori", fornitoreRepo.findByAttivoTrueOrderByRagioneSocialeAsc());
            model.addAttribute("stati",     StatoFattura.values());
            return "fatture/form";
        }
        if (clienteId   != null) fattura.setCliente(clienteRepo.findById(clienteId).orElse(null));
        if (fornitoreId != null) fattura.setFornitore(fornitoreRepo.findById(fornitoreId).orElse(null));

        fatturaRepo.save(fattura);
        ra.addFlashAttribute("successo", "Fattura salvata correttamente.");
        return "redirect:/fatture";
    }

    @PostMapping("/{id}/stato")
    public String cambiaStato(@PathVariable Long id,
                              @RequestParam String stato,
                              RedirectAttributes ra) {
        Fattura f = fatturaRepo.findById(id).orElseThrow();
        f.setStato(StatoFattura.valueOf(stato));
        fatturaRepo.save(f);
        ra.addFlashAttribute("successo", "Stato aggiornato.");
        return "redirect:/fatture/" + id;
    }
}
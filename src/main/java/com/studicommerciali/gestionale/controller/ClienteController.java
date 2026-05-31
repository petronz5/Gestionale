package com.studicommerciali.gestionale.controller;

import com.studicommerciali.gestionale.entity.Cliente;
import com.studicommerciali.gestionale.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clienti")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService service;

    @GetMapping
    public String lista(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("clienti", service.cerca(q));
        model.addAttribute("q", q);
        return "clienti/lista";
    }

    @GetMapping("/nuovo")
    public String nuovo(Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("tipi", Cliente.TipoCliente.values());
        return "clienti/form";
    }

    @GetMapping("/{id}")
    public String dettaglio(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", service.trovaPerId(id));
        return "clienti/dettaglio";
    }

    @GetMapping("/{id}/modifica")
    public String modifica(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", service.trovaPerId(id));
        model.addAttribute("tipi", Cliente.TipoCliente.values());
        return "clienti/form";
    }

    @PostMapping("/salva")
    public String salva(@Valid @ModelAttribute Cliente cliente,
                        BindingResult result,
                        Model model,
                        RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("tipi", Cliente.TipoCliente.values());
            return "clienti/form";
        }
        service.salva(cliente);
        ra.addFlashAttribute("successo", "Cliente salvato correttamente.");
        return "redirect:/clienti";
    }

    @PostMapping("/{id}/elimina")
    public String elimina(@PathVariable Long id, RedirectAttributes ra) {
        service.elimina(id);
        ra.addFlashAttribute("successo", "Cliente disattivato.");
        return "redirect:/clienti";
    }
}
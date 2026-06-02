package com.studicommerciali.gestionale.controller;

import com.studicommerciali.gestionale.entity.Azienda;
import com.studicommerciali.gestionale.entity.Cliente;
import com.studicommerciali.gestionale.entity.Utente;
import com.studicommerciali.gestionale.repository.ClienteRepository;
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
@RequestMapping("/clienti")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteRepository clienteRepo;
    private final UtenteRepository utenteRepo;

    // Metodo di supporto per isolare i dati SaaS
    private Azienda getAziendaLoggata(Principal principal) {
        Utente utente = utenteRepo.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        return utente.getAzienda();
    }

    @GetMapping
    public String lista(@RequestParam(required = false) String q, Model model, Principal principal) {
        Azienda miaAzienda = getAziendaLoggata(principal);

        var clienti = (q != null && !q.isBlank())
                // NOTA: Nel ClienteRepository dovrai creare questo metodo che accetta l'Azienda e la stringa Q
                ? clienteRepo.findByAziendaAndRagioneSocialeContainingIgnoreCaseAndAttivoTrue(miaAzienda, q)
                : clienteRepo.findByAziendaAndAttivoTrueOrderByRagioneSocialeAsc(miaAzienda);

        model.addAttribute("clienti", clienti);
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
    public String dettaglio(@PathVariable Long id, Model model, Principal principal) {
        Azienda miaAzienda = getAziendaLoggata(principal);
        Cliente cliente = clienteRepo.findById(id).orElseThrow();

        // Controllo di sicurezza SaaS: L'utente sta provando a leggere un cliente di un'altra azienda?
        if (!cliente.getAzienda().getId().equals(miaAzienda.getId())) {
            throw new SecurityException("Accesso Negato: Questo cliente appartiene a un'altra azienda.");
        }

        model.addAttribute("cliente", cliente);
        return "clienti/dettaglio";
    }

    @GetMapping("/{id}/modifica")
    public String modifica(@PathVariable Long id, Model model, Principal principal) {
        Azienda miaAzienda = getAziendaLoggata(principal);
        Cliente cliente = clienteRepo.findById(id).orElseThrow();

        if (!cliente.getAzienda().getId().equals(miaAzienda.getId())) {
            throw new SecurityException("Accesso Negato");
        }

        model.addAttribute("cliente", cliente);
        model.addAttribute("tipi", Cliente.TipoCliente.values());
        return "clienti/form";
    }

    @PostMapping("/salva")
    public String salva(@Valid @ModelAttribute Cliente cliente, BindingResult result, Model model, Principal principal, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("tipi", Cliente.TipoCliente.values());
            return "clienti/form";
        }

        // SaaS: Forza l'azienda dell'utente loggato sul cliente salvato
        Azienda miaAzienda = getAziendaLoggata(principal);
        cliente.setAzienda(miaAzienda);

        clienteRepo.save(cliente);
        ra.addFlashAttribute("successo", "Anagrafica cliente salvata correttamente.");
        return "redirect:/clienti";
    }

    @PostMapping("/{id}/elimina")
    public String elimina(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        Azienda miaAzienda = getAziendaLoggata(principal);
        Cliente cliente = clienteRepo.findById(id).orElseThrow();

        if (!cliente.getAzienda().getId().equals(miaAzienda.getId())) {
            throw new SecurityException("Accesso Negato");
        }

        cliente.setAttivo(false);
        clienteRepo.save(cliente);
        ra.addFlashAttribute("successo", "Cliente disattivato correttamente.");
        return "redirect:/clienti";
    }
}
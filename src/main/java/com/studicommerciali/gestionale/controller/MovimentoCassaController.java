package com.studicommerciali.gestionale.controller;

import com.studicommerciali.gestionale.entity.Azienda;
import com.studicommerciali.gestionale.entity.MovimentoCassa;
import com.studicommerciali.gestionale.entity.Utente;
import com.studicommerciali.gestionale.repository.MovimentoCassaRepository;
import com.studicommerciali.gestionale.repository.UtenteRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;

@Controller
@RequestMapping("/cassa")
@RequiredArgsConstructor
public class MovimentoCassaController {

    private final MovimentoCassaRepository movimentoRepo;
    private final UtenteRepository utenteRepo;

    private Azienda getAziendaLoggata(Principal principal) {
        Utente u = utenteRepo.findByUsername(principal.getName()).orElseThrow();
        return u.getAzienda();
    }

    @GetMapping
    public String lista(Model model, Principal principal) {
        Azienda miaAzienda = getAziendaLoggata(principal);

        model.addAttribute("movimenti", movimentoRepo.findByAziendaOrderByDataMovimentoDescIdDesc(miaAzienda));
        model.addAttribute("saldo", movimentoRepo.getSaldoAttualeAzienda(miaAzienda));

        // Prepariamo l'oggetto vuoto per il form di inserimento rapido (modale o inline)
        MovimentoCassa nuovo = new MovimentoCassa();
        nuovo.setDataMovimento(LocalDate.now());
        model.addAttribute("movimento", nuovo);
        model.addAttribute("tipi", MovimentoCassa.TipoMovimento.values());

        return "cassa/lista";
    }

    @PostMapping("/salva")
    public String salva(@Valid @ModelAttribute MovimentoCassa movimento, BindingResult result, Principal principal, RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("errore", "Impossibile salvare il movimento, dati non validi.");
            return "redirect:/cassa";
        }

        movimento.setAzienda(getAziendaLoggata(principal));
        movimentoRepo.save(movimento);
        ra.addFlashAttribute("successo", "Movimento di cassa registrato.");
        return "redirect:/cassa";
    }

    @PostMapping("/{id}/elimina")
    public String elimina(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        MovimentoCassa m = movimentoRepo.findById(id).orElseThrow();
        if (m.getAzienda().getId().equals(getAziendaLoggata(principal).getId())) {
            movimentoRepo.delete(m);
            ra.addFlashAttribute("successo", "Movimento eliminato con successo.");
        }
        return "redirect:/cassa";
    }
}
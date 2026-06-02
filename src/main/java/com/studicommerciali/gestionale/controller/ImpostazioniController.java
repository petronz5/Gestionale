package com.studicommerciali.gestionale.controller;

import com.studicommerciali.gestionale.entity.Azienda;
import com.studicommerciali.gestionale.entity.Utente;
import com.studicommerciali.gestionale.repository.AziendaRepository;
import com.studicommerciali.gestionale.repository.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/impostazioni")
@RequiredArgsConstructor
public class ImpostazioniController {

    private final UtenteRepository utenteRepo;
    private final AziendaRepository aziendaRepo;

    @GetMapping
    public String mostraImpostazioni(Model model, Principal principal) {
        Utente u = utenteRepo.findByUsername(principal.getName()).orElseThrow();
        model.addAttribute("azienda", u.getAzienda());
        return "aziende/impostazioni";
    }

    @PostMapping("/salva")
    public String salvaImpostazioni(@ModelAttribute Azienda datiAggiornati, Principal principal, RedirectAttributes ra) {
        Utente u = utenteRepo.findByUsername(principal.getName()).orElseThrow();
        Azienda miaAzienda = u.getAzienda();

        // Sovrascriviamo i dati del profilo aziendale corrente
        miaAzienda.setRagioneSociale(datiAggiornati.getRagioneSociale());
        miaAzienda.setIndirizzo(datiAggiornati.getIndirizzo());
        miaAzienda.setCap(datiAggiornati.getCap());
        miaAzienda.setCitta(datiAggiornati.getCitta());
        miaAzienda.setProvincia(datiAggiornati.getProvincia());
        miaAzienda.setEmail(datiAggiornati.getEmail());
        miaAzienda.setTelefono(datiAggiornati.getTelefono());
        miaAzienda.setIban(datiAggiornati.getIban());

        aziendaRepo.save(miaAzienda);
        ra.addFlashAttribute("successo", "Profilo Aziendale aggiornato correttamente.");
        return "redirect:/impostazioni";
    }
}
package com.studicommerciali.gestionale.controller;

import com.studicommerciali.gestionale.entity.Azienda;
import com.studicommerciali.gestionale.entity.Fattura.TipoFattura;
import com.studicommerciali.gestionale.entity.Scadenza.StatoScadenza;
import com.studicommerciali.gestionale.entity.Utente;
import com.studicommerciali.gestionale.repository.ClienteRepository;
import com.studicommerciali.gestionale.repository.FatturaRepository;
import com.studicommerciali.gestionale.repository.ScadenzaRepository;
import com.studicommerciali.gestionale.repository.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.time.Year;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ClienteRepository clienteRepo;
    private final FatturaRepository fatturaRepo;
    private final ScadenzaRepository scadenzaRepo;
    private final UtenteRepository utenteRepo;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model, Principal principal) {

        // Recuperiamo il Tenant loggato
        Utente utente = utenteRepo.findByUsername(principal.getName()).orElseThrow();
        Azienda miaAzienda = utente.getAzienda();

        int anno = Year.now().getValue();

        // Tutti i calcoli ora sono isolati sulla singola azienda
        model.addAttribute("totaleClienti",
                clienteRepo.findByAziendaAndAttivoTrueOrderByRagioneSocialeAsc(miaAzienda).size());

        model.addAttribute("fatturatoAnno",
                fatturaRepo.totalePerTipoAnno(miaAzienda, TipoFattura.ATTIVA, anno));

        model.addAttribute("costiAnno",
                fatturaRepo.totalePerTipoAnno(miaAzienda, TipoFattura.PASSIVA, anno));

        model.addAttribute("scadenzeAperte",
                scadenzaRepo.findByAziendaAndStatoOrderByDataScadenzaAsc(miaAzienda, StatoScadenza.APERTA));

        model.addAttribute("anno", anno);

        return "dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // Deve corrispondere a templates/login.html
    }
}
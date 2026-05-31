package com.studicommerciali.gestionale.controller;

import com.studicommerciali.gestionale.entity.Fattura.TipoFattura;
import com.studicommerciali.gestionale.entity.Scadenza.StatoScadenza;
import com.studicommerciali.gestionale.repository.ClienteRepository;
import com.studicommerciali.gestionale.repository.FatturaRepository;
import com.studicommerciali.gestionale.repository.ScadenzaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.time.LocalDate;
import java.time.Year;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ClienteRepository clienteRepo;
    private final FatturaRepository fatturaRepo;
    private final ScadenzaRepository scadenzaRepo;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        int anno = Year.now().getValue();
        model.addAttribute("totaleClienti",
                clienteRepo.findByAttivoTrueOrderByRagioneSocialeAsc().size());
        model.addAttribute("fatturatoAnno",
                fatturaRepo.totalePerTipoAnno(TipoFattura.ATTIVA, anno));
        model.addAttribute("costiAnno",
                fatturaRepo.totalePerTipoAnno(TipoFattura.PASSIVA, anno));
        model.addAttribute("scadenzeAperte",
                scadenzaRepo.findByStatoOrderByDataScadenzaAsc(StatoScadenza.APERTA));
        model.addAttribute("anno", anno);
        return "dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // Deve corrispondere a templates/login.html
    }
}
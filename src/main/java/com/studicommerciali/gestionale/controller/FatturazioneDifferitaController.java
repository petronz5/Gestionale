package com.studicommerciali.gestionale.controller;

import com.studicommerciali.gestionale.entity.*;
import com.studicommerciali.gestionale.repository.ClienteRepository;
import com.studicommerciali.gestionale.repository.DdtRepository;
import com.studicommerciali.gestionale.repository.FatturaRepository;
import com.studicommerciali.gestionale.repository.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

@Controller
@RequestMapping("/fatturazione-differita")
@RequiredArgsConstructor
public class FatturazioneDifferitaController {

    private final DdtRepository ddtRepo;
    private final ClienteRepository clienteRepo;
    private final FatturaRepository fatturaRepo;
    private final UtenteRepository utenteRepo; // Aggiunto per il SaaS

    // Metodo di supporto SaaS
    private Azienda getAziendaLoggata(Principal principal) {
        Utente utente = utenteRepo.findByUsername(principal.getName()).orElseThrow();
        return utente.getAzienda();
    }

    @GetMapping
    public String index(Model model, Principal principal) {
        Azienda miaAzienda = getAziendaLoggata(principal);

        // Cerca i clienti da fatturare limitatamente alla PROPRIA azienda
        model.addAttribute("clienti", ddtRepo.findClientiDaFatturareAzienda(miaAzienda, Ddt.StatoDdt.DA_FATTURARE));
        return "differita/index";
    }

    @GetMapping("/cliente/{id}")
    public String dettaglioCliente(@PathVariable Long id, Model model, Principal principal) {
        Azienda miaAzienda = getAziendaLoggata(principal);
        Cliente cliente = clienteRepo.findById(id).orElseThrow();

        // Controllo di sicurezza SaaS
        if (!cliente.getAzienda().getId().equals(miaAzienda.getId())) {
            throw new SecurityException("Accesso Negato");
        }

        List<Ddt> ddts = ddtRepo.findByAziendaAndClienteIdAndStatoOrderByDataDocumentoAsc(miaAzienda, id, Ddt.StatoDdt.DA_FATTURARE);

        model.addAttribute("cliente", cliente);
        model.addAttribute("ddts", ddts);
        return "differita/ddt-cliente";
    }

    @PostMapping("/genera")
    @Transactional
    public String generaFattura(@RequestParam Long clienteId,
                                @RequestParam List<Long> ddtIds,
                                Principal principal,
                                RedirectAttributes ra) {

        Azienda miaAzienda = getAziendaLoggata(principal);
        Cliente cliente = clienteRepo.findById(clienteId).orElseThrow();

        // Controllo di sicurezza SaaS
        if (!cliente.getAzienda().getId().equals(miaAzienda.getId())) {
            throw new SecurityException("Accesso Negato");
        }

        int annoCorrente = Year.now().getValue();

        // 1. Inizializza la nuova Fattura
        Fattura nuovaFattura = new Fattura();
        nuovaFattura.setAzienda(miaAzienda); // Impostiamo il tenant SaaS
        nuovaFattura.setTipo(Fattura.TipoFattura.ATTIVA);
        nuovaFattura.setStato(Fattura.StatoFattura.BOZZA);
        nuovaFattura.setCliente(cliente);
        nuovaFattura.setAnno(annoCorrente);
        nuovaFattura.setDataEmissione(LocalDate.now());
        nuovaFattura.setAliquotaIva(new BigDecimal("22.00"));

        Integer ultimoNum = fatturaRepo.ultimoNumero(miaAzienda, annoCorrente, Fattura.TipoFattura.ATTIVA);
        nuovaFattura.setNumero(String.valueOf(ultimoNum != null ? ultimoNum + 1 : 1));

        StringBuilder riferimenti = new StringBuilder("Fatturazione differita DDT: ");

        int ordine = 1;
        for (Long ddtId : ddtIds) {
            Ddt ddt = ddtRepo.findById(ddtId).orElseThrow();

            // Verifica di sicurezza aggiuntiva sulle singole righe
            if(!ddt.getAzienda().getId().equals(miaAzienda.getId())) continue;

            riferimenti.append("n.").append(ddt.getNumero()).append(" del ").append(ddt.getDataDocumento()).append("; ");

            for (RigaDdt rDdt : ddt.getRighe()) {
                RigaFattura rFatt = new RigaFattura();
                rFatt.setFattura(nuovaFattura);
                rFatt.setDescrizione(rDdt.getDescrizione());
                rFatt.setQuantita(rDdt.getQuantita());
                rFatt.setOrdine(ordine++);

                if (rDdt.getArticolo() != null) {
                    Articolo art = rDdt.getArticolo();
                    rFatt.setPrezzoUnitario(art.getPrezzoBase());
                    rFatt.setAliquotaIva(art.getAliquotaIva());
                    rFatt.setUnitaMisura(art.getUnitaMisura());
                } else {
                    rFatt.setPrezzoUnitario(BigDecimal.ZERO);
                }

                rFatt.ricalcola();
                nuovaFattura.getRighe().add(rFatt);
            }

            ddt.setStato(Ddt.StatoDdt.FATTURATO);
            ddtRepo.save(ddt);
        }

        nuovaFattura.setNote(riferimenti.toString());
        nuovaFattura.ricalcolaTotali();

        Fattura fatturaSalvata = fatturaRepo.save(nuovaFattura);

        ra.addFlashAttribute("successo", "Fattura differita generata con successo!");
        return "redirect:/fatture/" + fatturaSalvata.getId();
    }
}
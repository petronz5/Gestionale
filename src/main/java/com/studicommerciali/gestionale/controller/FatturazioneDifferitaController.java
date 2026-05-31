package com.studicommerciali.gestionale.controller;

import com.studicommerciali.gestionale.entity.*;
import com.studicommerciali.gestionale.repository.ClienteRepository;
import com.studicommerciali.gestionale.repository.DdtRepository;
import com.studicommerciali.gestionale.repository.FatturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
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

    @GetMapping
    public String index(Model model) {
        // Mostra solo i clienti che aspettano di essere fatturati
        model.addAttribute("clienti", ddtRepo.findClientiDaFatturare());
        return "differita/index";
    }

    @GetMapping("/cliente/{id}")
    public String dettaglioCliente(@PathVariable Long id, Model model) {
        Cliente cliente = clienteRepo.findById(id).orElseThrow();
        List<Ddt> ddts = ddtRepo.findByClienteIdAndStatoOrderByDataDocumentoAsc(id, Ddt.StatoDdt.DA_FATTURARE);

        model.addAttribute("cliente", cliente);
        model.addAttribute("ddts", ddts);
        return "differita/ddt-cliente";
    }

    @PostMapping("/genera")
    @Transactional // Fondamentale: se qualcosa va storto, annulla tutte le modifiche al database
    public String generaFattura(@RequestParam Long clienteId,
                                @RequestParam List<Long> ddtIds,
                                RedirectAttributes ra) {

        Cliente cliente = clienteRepo.findById(clienteId).orElseThrow();
        int annoCorrente = Year.now().getValue();

        // 1. Inizializza la nuova Fattura
        Fattura nuovaFattura = new Fattura();
        nuovaFattura.setTipo(Fattura.TipoFattura.ATTIVA);
        nuovaFattura.setStato(Fattura.StatoFattura.BOZZA);
        nuovaFattura.setCliente(cliente);
        nuovaFattura.setAnno(annoCorrente);
        nuovaFattura.setDataEmissione(LocalDate.now());
        nuovaFattura.setAliquotaIva(new BigDecimal("22.00"));

        Integer ultimoNum = fatturaRepo.ultimoNumero(annoCorrente, Fattura.TipoFattura.ATTIVA);
        nuovaFattura.setNumero(String.valueOf(ultimoNum != null ? ultimoNum + 1 : 1));

        // Note per indicare i riferimenti
        StringBuilder riferimenti = new StringBuilder("Fatturazione differita DDT: ");

        // 2. Trasforma le righe dei DDT in righe della Fattura
        int ordine = 1;
        for (Long ddtId : ddtIds) {
            Ddt ddt = ddtRepo.findById(ddtId).orElseThrow();

            riferimenti.append("n.").append(ddt.getNumero()).append(" del ").append(ddt.getDataDocumento()).append("; ");

            for (RigaDdt rDdt : ddt.getRighe()) {
                RigaFattura rFatt = new RigaFattura();
                rFatt.setFattura(nuovaFattura);
                rFatt.setDescrizione(rDdt.getDescrizione());
                rFatt.setQuantita(rDdt.getQuantita());
                rFatt.setOrdine(ordine++);

                // Se c'è un articolo collegato, recuperiamo il prezzo e l'IVA per calcolare i totali
                if (rDdt.getArticolo() != null) {
                    Articolo art = rDdt.getArticolo();
                    rFatt.setPrezzoUnitario(art.getPrezzoBase());
                    rFatt.setAliquotaIva(art.getAliquotaIva());
                    rFatt.setUnitaMisura(art.getUnitaMisura());
                } else {
                    rFatt.setPrezzoUnitario(BigDecimal.ZERO);
                }

                // Ricalcolo degli importi della singola riga
                rFatt.ricalcola();
                nuovaFattura.getRighe().add(rFatt);
            }

            // 3. Segniamo il DDT come FATTURATO
            ddt.setStato(Ddt.StatoDdt.FATTURATO);
            ddtRepo.save(ddt);
        }

        nuovaFattura.setNote(riferimenti.toString());

        // 4. Ricalcolo i totali globali della fattura
        nuovaFattura.ricalcolaTotali();
        Fattura fatturaSalvata = fatturaRepo.save(nuovaFattura);

        ra.addFlashAttribute("successo", "Fattura differita generata con successo!");
        // Rimanda direttamente alla fattura appena generata
        return "redirect:/fatture/" + fatturaSalvata.getId();
    }
}
package com.studicommerciali.gestionale.controller;

import com.studicommerciali.gestionale.entity.Fattura;
import com.studicommerciali.gestionale.entity.Fattura.TipoFattura;
import com.studicommerciali.gestionale.entity.Fattura.StatoFattura;
import com.studicommerciali.gestionale.repository.ClienteRepository;
import com.studicommerciali.gestionale.repository.FatturaRepository;
import com.studicommerciali.gestionale.repository.FornitoreRepository;
import com.studicommerciali.gestionale.service.FatturaElettronicaService;
import com.studicommerciali.gestionale.service.FatturaPdfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

@Controller
@RequestMapping("/fatture")
@RequiredArgsConstructor
public class FatturaController {

    private final FatturaRepository fatturaRepo;
    private final ClienteRepository clienteRepo;
    private final FornitoreRepository fornitoreRepo;
    private final FatturaElettronicaService xmlService;
    private final FatturaPdfService pdfService;

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
        model.addAttribute("totFatturato", fatturaRepo.totalePerTipoAnno(TipoFattura.ATTIVA,  anno));
        model.addAttribute("totCosti", fatturaRepo.totalePerTipoAnno(TipoFattura.PASSIVA, anno));

        return "fatture/lista";
    }

    @GetMapping("/nuova")
    public String nuova(@RequestParam(defaultValue = "ATTIVA") String tipo, Model model) {
        Fattura f = new Fattura();
        f.setTipo(TipoFattura.valueOf(tipo));
        f.setAnno(Year.now().getValue());
        f.setAliquotaIva(new java.math.BigDecimal("22.00"));

        // Calcolo del prossimo numero in base al tipo (Attiva, Passiva, Preventivo)
        Integer ultimo = fatturaRepo.ultimoNumero(Year.now().getValue(), TipoFattura.valueOf(tipo));
        f.setNumero(String.valueOf(ultimo != null ? ultimo + 1 : 1));

        model.addAttribute("fattura", f);
        model.addAttribute("clienti",   clienteRepo.findByAttivoTrueOrderByRagioneSocialeAsc());
        model.addAttribute("fornitori", fornitoreRepo.findByAttivoTrueOrderByRagioneSocialeAsc());
        model.addAttribute("stati",     StatoFattura.values());
        return "fatture/form";
    }

    @GetMapping("/{id}")
    public String dettaglio(@PathVariable Long id, Model model) {
        model.addAttribute("fattura", fatturaRepo.findById(id).orElseThrow(() -> new RuntimeException("Fattura non trovata")));
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
        ra.addFlashAttribute("successo", "Documento salvato correttamente.");
        return "redirect:/fatture/" + fattura.getId();
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

    @PostMapping("/{id}/converti-preventivo")
    public String convertiPreventivo(@PathVariable Long id, RedirectAttributes ra) {
        Fattura preventivo = fatturaRepo.findById(id).orElseThrow();

        if (preventivo.getTipo() != TipoFattura.PREVENTIVO) {
            ra.addFlashAttribute("errore", "Solo i preventivi possono essere convertiti!");
            return "redirect:/fatture/" + id;
        }

        // Crea la nuova fattura
        Fattura nuovaFattura = new Fattura();
        nuovaFattura.setTipo(TipoFattura.ATTIVA);
        nuovaFattura.setStato(StatoFattura.BOZZA);
        nuovaFattura.setCliente(preventivo.getCliente());
        nuovaFattura.setAnno(Year.now().getValue());
        nuovaFattura.setDataEmissione(LocalDate.now());
        nuovaFattura.setAliquotaIva(preventivo.getAliquotaIva());
        nuovaFattura.setImponibile(preventivo.getImponibile());
        nuovaFattura.setIva(preventivo.getIva());
        nuovaFattura.setTotale(preventivo.getTotale());
        nuovaFattura.setMetodoPagamento(preventivo.getMetodoPagamento());
        nuovaFattura.setNote("Rif. Preventivo n. " + preventivo.getNumero() + " del " + preventivo.getDataEmissione());

        Integer ultimoNum = fatturaRepo.ultimoNumero(nuovaFattura.getAnno(), TipoFattura.ATTIVA);
        nuovaFattura.setNumero(String.valueOf(ultimoNum != null ? ultimoNum + 1 : 1));

        preventivo.setStato(StatoFattura.ACCETTATA);
        fatturaRepo.save(preventivo);

        Fattura salvata = fatturaRepo.save(nuovaFattura);
        ra.addFlashAttribute("successo", "Preventivo convertito in Fattura con successo!");
        return "redirect:/fatture/" + salvata.getId();
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> scaricaPdf(@PathVariable Long id) {
        try {
            Fattura f = fatturaRepo.findById(id).orElseThrow();
            byte[] pdfContent = pdfService.generaPdfFattura(f);

            String nomeFile = (f.getTipo() == TipoFattura.PREVENTIVO ? "Preventivo_" : "Fattura_")
                    + f.getNumero() + "_" + f.getAnno() + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeFile + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfContent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/invia-email")
    public String inviaEmail(@PathVariable Long id, RedirectAttributes ra) {
        Fattura fattura = fatturaRepo.findById(id).orElseThrow();
        String email = (fattura.getCliente() != null) ? fattura.getCliente().getEmail() : null;

        if (email == null || email.isBlank()) {
            ra.addFlashAttribute("errore", "Il cliente associato non ha un indirizzo email configurato in anagrafica.");
            return "redirect:/fatture/" + id;
        }

        try {
            pdfService.inviaFatturaViaEmail(fattura, email);
            ra.addFlashAttribute("successo", "Documento inviato con successo via email a: " + email);
        } catch (Exception e) {
            ra.addFlashAttribute("errore", "Errore nell'invio dell'email: Controlla le credenziali SMTP in application.properties.");
        }
        return "redirect:/fatture/" + id;
    }

    @GetMapping("/{id}/xml")
    public ResponseEntity<byte[]> scaricaXmlSDI(@PathVariable Long id) {
        try {
            Fattura f = fatturaRepo.findById(id).orElseThrow();
            String xmlContent = xmlService.generaXmlSdi(f);

            // Nome standard per SDI: CodicePaese + P.IVA + _ + Progressivo.xml
            String pIvaAzienda = "01234567890"; // Inserisci la tua VERA p.iva qui
            String nomeFile = "IT" + pIvaAzienda + "_" + f.getNumero() + ".xml";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeFile + "\"")
                    .contentType(MediaType.APPLICATION_XML)
                    .body(xmlContent.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
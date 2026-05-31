package com.studicommerciali.gestionale.controller;

import com.studicommerciali.gestionale.entity.Ddt;
import com.studicommerciali.gestionale.repository.ArticoloRepository;
import com.studicommerciali.gestionale.repository.ClienteRepository;
import com.studicommerciali.gestionale.repository.DdtRepository;
import com.studicommerciali.gestionale.repository.RigaDdtRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.Year;

@Controller
@RequestMapping("/ddt")
@RequiredArgsConstructor
public class DdtController {

    private final DdtRepository ddtRepo;
    private final ClienteRepository clienteRepo;
    private final ArticoloRepository articoloRepo;
    private final RigaDdtRepository rigaDdtRepo;

    @GetMapping
    public String lista(@RequestParam(required = false, defaultValue = "0") int anno, Model model) {
        if (anno == 0) anno = Year.now().getValue();

        model.addAttribute("ddts", ddtRepo.findByAnnoOrderByNumeroDesc(anno));
        model.addAttribute("anno", anno);
        return "ddt/lista";
    }

    @GetMapping("/nuovo")
    public String nuovo(Model model) {
        Ddt ddt = new Ddt();
        int annoCorrente = Year.now().getValue();
        ddt.setAnno(annoCorrente);
        ddt.setDataDocumento(LocalDate.now());

        // Calcolo automatico del prossimo numero DDT
        Integer ultimoNum = ddtRepo.ultimoNumero(annoCorrente);
        ddt.setNumero(String.valueOf(ultimoNum != null ? ultimoNum + 1 : 1));

        model.addAttribute("ddt", ddt);
        model.addAttribute("clienti", clienteRepo.findByAttivoTrueOrderByRagioneSocialeAsc());
        return "ddt/form";
    }

    @GetMapping("/{id}/modifica")
    public String modifica(@PathVariable Long id, Model model) {
        model.addAttribute("ddt", ddtRepo.findById(id).orElseThrow());
        model.addAttribute("clienti", clienteRepo.findByAttivoTrueOrderByRagioneSocialeAsc());
        return "ddt/form";
    }

    @PostMapping("/salva")
    public String salva(@Valid @ModelAttribute Ddt ddt,
                        BindingResult result,
                        @RequestParam Long clienteId,
                        Model model,
                        RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("clienti", clienteRepo.findByAttivoTrueOrderByRagioneSocialeAsc());
            return "ddt/form";
        }

        ddt.setCliente(clienteRepo.findById(clienteId).orElseThrow());
        ddtRepo.save(ddt);

        ra.addFlashAttribute("successo", "DDT salvato correttamente.");
        // Dopo aver salvato la testata, rimandiamo al dettaglio per inserire le righe (articoli)
        return "redirect:/ddt";
    }

    @PostMapping("/{id}/elimina")
    public String elimina(@PathVariable Long id, RedirectAttributes ra) {
        Ddt ddt = ddtRepo.findById(id).orElseThrow();
        ddt.setStato(Ddt.StatoDdt.ANNULLATO);
        ddtRepo.save(ddt);
        ra.addFlashAttribute("successo", "DDT annullato.");
        return "redirect:/ddt";
    }

    // --- GESTIONE RIGHE E DETTAGLIO ---

    @GetMapping("/{id}")
    public String dettaglio(@PathVariable Long id, Model model) {
        Ddt ddt = ddtRepo.findById(id).orElseThrow(() -> new RuntimeException("DDT non trovato"));
        model.addAttribute("ddt", ddt);
        model.addAttribute("nuovaRiga", new com.studicommerciali.gestionale.entity.RigaDdt());
        // Passiamo il catalogo articoli per la tendina
        model.addAttribute("articoli", articoloRepo.findByAttivoTrueOrderByDescrizioneAsc());
        return "ddt/dettaglio";
    }

    @PostMapping("/{id}/righe")
    public String aggiungiRiga(@PathVariable Long id,
                               @ModelAttribute com.studicommerciali.gestionale.entity.RigaDdt riga,
                               @RequestParam(required = false) Long articoloId,
                               RedirectAttributes ra) {
        Ddt ddt = ddtRepo.findById(id).orElseThrow();
        riga.setDdt(ddt);

        // Se l'utente ha selezionato un articolo dal catalogo, preleviamo la descrizione
        if (articoloId != null) {
            com.studicommerciali.gestionale.entity.Articolo art = articoloRepo.findById(articoloId).orElseThrow();
            riga.setArticolo(art);
            if (riga.getDescrizione() == null || riga.getDescrizione().isBlank()) {
                riga.setDescrizione(art.getDescrizione());
            }
        }

        // Calcola l'ordine progressivo della riga
        riga.setOrdine(ddt.getRighe().size() + 1);
        rigaDdtRepo.save(riga);

        ra.addFlashAttribute("successo", "Riga aggiunta al DDT.");
        return "redirect:/ddt/" + id;
    }

    @PostMapping("/righe/{rigaId}/elimina")
    public String eliminaRiga(@PathVariable Long rigaId, @RequestParam Long ddtId, RedirectAttributes ra) {
        rigaDdtRepo.deleteById(rigaId);
        ra.addFlashAttribute("successo", "Riga eliminata.");
        return "redirect:/ddt/" + ddtId;
    }
}
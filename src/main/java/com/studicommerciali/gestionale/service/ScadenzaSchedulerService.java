package com.studicommerciali.gestionale.service;

import com.studicommerciali.gestionale.entity.Scadenza;
import com.studicommerciali.gestionale.repository.ScadenzaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScadenzaSchedulerService {

    private final ScadenzaRepository scadenzaRepo;
    private final JavaMailSender mailSender;

    // Esegue questo metodo ogni giorno alle 08:00 del mattino
    @Scheduled(cron = "0 0 8 * * *")
    public void inviaPromemoriaScadenze() {
        System.out.println("⏳ Esecuzione controllo scadenze giornaliero...");

        // Cerchiamo le scadenze che scadono esattamente tra 3 giorni
        LocalDate traTreGiorni = LocalDate.now().plusDays(3);
        List<Scadenza> scadenzeImminenti = scadenzaRepo.findByDataScadenzaAndStato(traTreGiorni, Scadenza.StatoScadenza.APERTA);

        for (Scadenza s : scadenzeImminenti) {
            // Mandiamo la mail solo se è associata a un cliente con email
            if (s.getCliente() != null && s.getCliente().getEmail() != null && !s.getCliente().getEmail().isBlank()) {
                inviaMail(s.getCliente().getEmail(), s);
            }
        }
    }

    private void inviaMail(String emailDestinatario, Scadenza s) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(emailDestinatario);
            msg.setSubject("Promemoria Scadenza Imminente: " + s.getTitolo());
            msg.setText("Gentile " + s.getCliente().getDisplayName() + ",\n\n" +
                    "Le ricordiamo che in data " + s.getDataScadenza() + " è prevista la seguente scadenza:\n" +
                    "Descrizione: " + s.getTitolo() + "\n" +
                    "Importo: € " + (s.getImporto() != null ? s.getImporto() : "N/D") + "\n\n" +
                    "Cordiali saluti.");
            mailSender.send(msg);
            System.out.println("✅ Promemoria inviato a: " + emailDestinatario);
        } catch (Exception e) {
            System.out.println("❌ Errore invio promemoria a " + emailDestinatario + ": " + e.getMessage());
        }
    }
}
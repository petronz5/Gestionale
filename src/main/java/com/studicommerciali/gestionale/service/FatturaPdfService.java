package com.studicommerciali.gestionale.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.studicommerciali.gestionale.entity.Fattura;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class FatturaPdfService {

    @Autowired
    private JavaMailSender mailSender;

    public byte[] generaPdfFattura(Fattura fattura) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontTitolo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Font fontSottotitolo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font fontDati = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            // Intestazione Azienda (Mittente)
            document.add(new Paragraph("LA TUA AZIENDA S.R.L.", fontTitolo));
            document.add(new Paragraph("Via Roma 1, 00100 Roma (RM) - P.IVA 01234567890", fontDati));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // Dati Cliente (Destinatario)
            document.add(new Paragraph("Spett.le", fontDati));
            document.add(new Paragraph(fattura.getIntestazione(), fontBold));
            if (fattura.getCliente() != null) {
                String idFiscale = fattura.getCliente().getPartitaIva() != null
                        ? "P.IVA: " + fattura.getCliente().getPartitaIva()
                        : "C.F.: " + fattura.getCliente().getCodiceFiscale();
                document.add(new Paragraph(idFiscale, fontDati));
                if (fattura.getCliente().getIndirizzo() != null) {
                    document.add(new Paragraph(fattura.getCliente().getIndirizzo() + ", " +
                            fattura.getCliente().getCitta() + " (" + fattura.getCliente().getProvincia() + ")", fontDati));
                }
            }
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // Titolo Documento
            String tipoDoc = fattura.getTipo().name().equals("PREVENTIVO") ? "PREVENTIVO" : "FATTURA";
            document.add(new Paragraph(tipoDoc + " N. " + fattura.getNumero() + " / " + fattura.getAnno(), fontTitolo));
            document.add(new Paragraph("Data Emissione: " + (fattura.getDataEmissione() != null ? fattura.getDataEmissione().toString() : ""), fontDati));
            document.add(new Paragraph("Metodo Pagamento: " + (fattura.getMetodoPagamento() != null ? fattura.getMetodoPagamento() : "Non specificato"), fontDati));
            document.add(new Paragraph(" "));

            // Dati Economici
            document.add(new Paragraph("RIEPILOGO IMPORTI", fontSottotitolo));
            document.add(new Paragraph("--------------------------------------------------"));
            document.add(new Paragraph("Imponibile: € " + (fattura.getImponibile() != null ? fattura.getImponibile() : "0.00"), fontDati));
            document.add(new Paragraph("Aliquota IVA: " + fattura.getAliquotaIva() + "%", fontDati));
            document.add(new Paragraph("Totale IVA: € " + (fattura.getIva() != null ? fattura.getIva() : "0.00"), fontDati));
            document.add(new Paragraph("--------------------------------------------------"));
            document.add(new Paragraph("TOTALE DOCUMENTO: € " + (fattura.getTotale() != null ? fattura.getTotale() : "0.00"), fontBold));

            if (fattura.getNote() != null && !fattura.getNote().isBlank()) {
                document.add(new Paragraph(" "));
                document.add(new Paragraph("Note:", fontBold));
                document.add(new Paragraph(fattura.getNote(), fontDati));
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Errore durante la generazione del PDF", e);
        }
    }

    public void inviaFatturaViaEmail(Fattura fattura, String emailDestinatario) {
        try {
            byte[] pdfBytes = generaPdfFattura(fattura);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String tipoDoc = fattura.getTipo().name().equals("PREVENTIVO") ? "Preventivo" : "Fattura";

            helper.setTo(emailDestinatario);
            helper.setSubject("Invio " + tipoDoc + " n. " + fattura.getNumero() + "/" + fattura.getAnno());
            helper.setText("Gentile Cliente,\n\nIn allegato le trasmettiamo copia di cortesia in formato PDF del documento in oggetto.\n\nCordiali saluti.");

            String nomeFile = tipoDoc + "_" + fattura.getNumero() + "_" + fattura.getAnno() + ".pdf";
            helper.addAttachment(nomeFile, new ByteArrayResource(pdfBytes));

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'invio dell'email", e);
        }
    }
}
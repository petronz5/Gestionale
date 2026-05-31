package com.studicommerciali.gestionale.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.studicommerciali.gestionale.entity.Fattura;
import com.studicommerciali.gestionale.entity.RigaFattura;
import com.studicommerciali.gestionale.repository.FatturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class FatturaPdfService {

    private final FatturaRepository fatturaRepo;

    public byte[] generaPdfFattura(Long fatturaId) {
        Fattura fattura = fatturaRepo.findById(fatturaId)
                .orElseThrow(() -> new RuntimeException("Fattura non trovata"));

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Font personalizzati
            Font fontTitolo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font fontNormale = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);

            // 1. Intestazione Documento
            document.add(new Paragraph("FATTURA DI CORTESIA", fontTitolo));
            document.add(new Paragraph("Numero: " + fattura.getNumero() + " / " + fattura.getAnno(), fontBold));
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            document.add(new Paragraph("Data Emissione: " + fattura.getDataEmissione().format(dtf), fontNormale));
            document.add(Chunk.NEWLINE);

            // 2. Dati Destinatario
            document.add(new Paragraph("Spett.le", fontNormale));
            document.add(new Paragraph(fattura.getIntestazione(), fontBold));
            if (fattura.getTipo() == Fattura.TipoFattura.ATTIVA && fattura.getCliente() != null) {
                document.add(new Paragraph("P.IVA/CF: " + (fattura.getCliente().getPartitaIva() != null ? fattura.getCliente().getPartitaIva() : fattura.getCliente().getCodiceFiscale()), fontNormale));
            }
            document.add(Chunk.NEWLINE);

            // 3. Tabella delle Righe
            PdfPTable table = new PdfPTable(5); // 5 colonne
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4f, 1f, 2f, 1f, 2f}); // Proporzioni larghezza colonne

            // Intestazione Tabella
            aggiungiCellaIntestazione(table, "Descrizione");
            aggiungiCellaIntestazione(table, "Q.tà");
            aggiungiCellaIntestazione(table, "Prezzo Unit.");
            aggiungiCellaIntestazione(table, "IVA %");
            aggiungiCellaIntestazione(table, "Totale Riga");

            // Righe Fattura
            for (RigaFattura riga : fattura.getRighe()) {
                table.addCell(new Phrase(riga.getDescrizione(), fontNormale));
                table.addCell(new Phrase(riga.getQuantita().toString(), fontNormale));
                table.addCell(new Phrase("E. " + riga.getPrezzoUnitario().toString(), fontNormale));
                table.addCell(new Phrase(riga.getAliquotaIva().toString() + "%", fontNormale));
                table.addCell(new Phrase("E. " + riga.getImportoNetto().toString(), fontNormale));
            }
            document.add(table);
            document.add(Chunk.NEWLINE);

            // 4. Riepilogo Totali
            Paragraph riepilogo = new Paragraph();
            riepilogo.setAlignment(Element.ALIGN_RIGHT);
            riepilogo.add(new Phrase("Imponibile: E. " + fattura.getImponibile() + "\n", fontNormale));
            riepilogo.add(new Phrase("Totale IVA: E. " + fattura.getIva() + "\n", fontNormale));
            riepilogo.add(new Phrase("TOTALE DOCUMENTO: E. " + fattura.getTotale(), fontBold));
            document.add(riepilogo);

        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }

        return out.toByteArray();
    }

    private void aggiungiCellaIntestazione(PdfPTable table, String testo) {
        PdfPCell cell = new PdfPCell(new Phrase(testo, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }
}
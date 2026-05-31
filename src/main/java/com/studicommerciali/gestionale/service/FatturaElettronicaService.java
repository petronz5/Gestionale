package com.studicommerciali.gestionale.service;

import com.studicommerciali.gestionale.entity.Fattura;
import com.studicommerciali.gestionale.entity.RigaFattura;
import com.studicommerciali.gestionale.repository.FatturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class FatturaElettronicaService {

    private final FatturaRepository fatturaRepo;

    public String generaXmlFatturaPA(Long fatturaId) {
        Fattura f = fatturaRepo.findById(fatturaId)
                .orElseThrow(() -> new RuntimeException("Fattura non trovata"));

        if (f.getTipo() != Fattura.TipoFattura.ATTIVA || f.getCliente() == null) {
            throw new IllegalStateException("Solo le fatture attive possono essere esportate in XML.");
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Costruzione manuale dell'XML (Semplificata per il tutorial, ma aderente allo standard SDI)
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<p:FatturaElettronica versione=\"FPR12\" xmlns:p=\"http://ivaservizi.agenziaentrate.gov.it/docs/xsd/fatture/v1.2\">\n");

        // --- 1. INTESTAZIONE (Dati Trasmissione e Azienda Mittente) ---
        xml.append("  <FatturaElettronicaHeader>\n");
        xml.append("    <DatiTrasmissione>\n");
        xml.append("      <IdTrasmittente>\n");
        xml.append("        <IdPaese>IT</IdPaese>\n");
        xml.append("        <IdCodice>01234567890</IdCodice>\n"); // La P.IVA del tuo Studio/Azienda
        xml.append("      </IdTrasmittente>\n");
        xml.append("      <ProgressivoInvio>").append(f.getNumero()).append("</ProgressivoInvio>\n");
        xml.append("      <FormatoTrasmissione>FPR12</FormatoTrasmissione>\n");
        xml.append("      <CodiceDestinatario>").append(f.getCliente().getCodiceSdi() != null ? f.getCliente().getCodiceSdi() : "0000000").append("</CodiceDestinatario>\n");
        xml.append("    </DatiTrasmissione>\n");

        // Dati di chi emette la fattura (Fissi per ora, in futuro prelevabili dal DB)
        xml.append("    <CedentePrestatore>\n");
        xml.append("      <DatiAnagrafici>\n");
        xml.append("        <IdFiscaleIVA><IdPaese>IT</IdPaese><IdCodice>01234567890</IdCodice></IdFiscaleIVA>\n");
        xml.append("        <Anagrafica><Denominazione>LA MIA AZIENDA SRL</Denominazione></Anagrafica>\n");
        xml.append("        <RegimeFiscale>RF01</RegimeFiscale>\n");
        xml.append("      </DatiAnagrafici>\n");
        xml.append("      <Sede>\n");
        xml.append("        <Indirizzo>Via Roma 1</Indirizzo>\n");
        xml.append("        <CAP>00100</CAP>\n");
        xml.append("        <Comune>Roma</Comune>\n");
        xml.append("        <Nazione>IT</Nazione>\n");
        xml.append("      </Sede>\n");
        xml.append("    </CedentePrestatore>\n");

        // Dati del Cliente
        xml.append("    <CessionarioCommittente>\n");
        xml.append("      <DatiAnagrafici>\n");
        if (f.getCliente().getPartitaIva() != null && !f.getCliente().getPartitaIva().isBlank()) {
            xml.append("        <IdFiscaleIVA><IdPaese>IT</IdPaese><IdCodice>").append(f.getCliente().getPartitaIva()).append("</IdCodice></IdFiscaleIVA>\n");
        }
        if (f.getCliente().getCodiceFiscale() != null && !f.getCliente().getCodiceFiscale().isBlank()) {
            xml.append("        <CodiceFiscale>").append(f.getCliente().getCodiceFiscale()).append("</CodiceFiscale>\n");
        }
        xml.append("        <Anagrafica>\n");
        if (f.getCliente().getTipo().name().equals("PERSONA_GIURIDICA")) {
            xml.append("          <Denominazione>").append(f.getCliente().getRagioneSociale()).append("</Denominazione>\n");
        } else {
            xml.append("          <Nome>").append(f.getCliente().getNome()).append("</Nome>\n");
            xml.append("          <Cognome>").append(f.getCliente().getCognome()).append("</Cognome>\n");
        }
        xml.append("        </Anagrafica>\n");
        xml.append("      </DatiAnagrafici>\n");
        xml.append("      <Sede>\n");
        xml.append("        <Indirizzo>").append(f.getCliente().getIndirizzo() != null ? f.getCliente().getIndirizzo() : "Sconosciuto").append("</Indirizzo>\n");
        xml.append("        <CAP>").append(f.getCliente().getCap() != null ? f.getCliente().getCap() : "00000").append("</CAP>\n");
        xml.append("        <Comune>").append(f.getCliente().getCitta() != null ? f.getCliente().getCitta() : "Sconosciuto").append("</Comune>\n");
        xml.append("        <Provincia>").append(f.getCliente().getProvincia() != null ? f.getCliente().getProvincia() : "RM").append("</Provincia>\n");
        xml.append("        <Nazione>IT</Nazione>\n");
        xml.append("      </Sede>\n");
        xml.append("    </CessionarioCommittente>\n");
        xml.append("  </FatturaElettronicaHeader>\n");

        // --- 2. CORPO DELLA FATTURA ---
        xml.append("  <FatturaElettronicaBody>\n");
        xml.append("    <DatiGenerali>\n");
        xml.append("      <DatiGeneraliDocumento>\n");
        xml.append("        <TipoDocumento>TD01</TipoDocumento>\n"); // TD01 = Fattura normale
        xml.append("        <Divisa>EUR</Divisa>\n");
        xml.append("        <Data>").append(f.getDataEmissione().format(dtf)).append("</Data>\n");
        xml.append("        <Numero>").append(f.getNumero()).append("</Numero>\n");
        xml.append("        <ImportoTotaleDocumento>").append(f.getTotale()).append("</ImportoTotaleDocumento>\n");
        xml.append("      </DatiGeneraliDocumento>\n");
        xml.append("    </DatiGenerali>\n");

        xml.append("    <DatiBeniServizi>\n");
        // Ciclo sulle righe della fattura
        for (RigaFattura riga : f.getRighe()) {
            xml.append("      <DettaglioLinee>\n");
            xml.append("        <NumeroLinea>").append(riga.getOrdine()).append("</NumeroLinea>\n");
            xml.append("        <Descrizione>").append(riga.getDescrizione()).append("</Descrizione>\n");
            xml.append("        <Quantita>").append(riga.getQuantita()).append("</Quantita>\n");
            xml.append("        <PrezzoUnitario>").append(riga.getPrezzoUnitario()).append("</PrezzoUnitario>\n");
            xml.append("        <PrezzoTotale>").append(riga.getImportoNetto()).append("</PrezzoTotale>\n");
            xml.append("        <AliquotaIVA>").append(riga.getAliquotaIva()).append("</AliquotaIVA>\n");
            xml.append("      </DettaglioLinee>\n");
        }

        // Blocco di riepilogo IVA (Obbligatorio per SDI)
        xml.append("      <DatiRiepilogo>\n");
        xml.append("        <AliquotaIVA>").append(f.getAliquotaIva()).append("</AliquotaIVA>\n");
        xml.append("        <ImponibileImporto>").append(f.getImponibile()).append("</ImponibileImporto>\n");
        xml.append("        <Imposta>").append(f.getIva()).append("</Imposta>\n");
        xml.append("        <EsigibilitaIVA>I</EsigibilitaIVA>\n"); // I = IVA ad esigibilità immediata
        xml.append("      </DatiRiepilogo>\n");
        xml.append("    </DatiBeniServizi>\n");

        xml.append("  </FatturaElettronicaBody>\n");
        xml.append("</p:FatturaElettronica>");

        return xml.toString();
    }
}
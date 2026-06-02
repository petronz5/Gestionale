package com.studicommerciali.gestionale.service;

import com.studicommerciali.gestionale.entity.Fattura;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class FatturaElettronicaService {

    public String generaXmlSdi(Fattura f) {
        if (f.getTipo() != Fattura.TipoFattura.ATTIVA || f.getCliente() == null) {
            throw new IllegalStateException("Solo le fatture attive verso clienti possono essere esportate in XML.");
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<p:FatturaElettronica versione=\"FPR12\" xmlns:p=\"http://ivaservizi.agenziaentrate.gov.it/docs/xsd/fatture/v1.2\">\n");

        // --- 1. INTESTAZIONE (Dati Trasmissione) ---
        xml.append("  <FatturaElettronicaHeader>\n");
        xml.append("    <DatiTrasmissione>\n");
        xml.append("      <IdTrasmittente>\n");
        xml.append("        <IdPaese>IT</IdPaese>\n");
        xml.append("        <IdCodice>01234567890</IdCodice>\n"); // Inserire vera P.IVA
        xml.append("      </IdTrasmittente>\n");
        xml.append("      <ProgressivoInvio>").append(f.getNumero()).append("</ProgressivoInvio>\n");
        xml.append("      <FormatoTrasmissione>FPR12</FormatoTrasmissione>\n");
        xml.append("      <CodiceDestinatario>").append(f.getCliente().getCodiceSdi() != null && !f.getCliente().getCodiceSdi().isBlank() ? f.getCliente().getCodiceSdi() : "0000000").append("</CodiceDestinatario>\n");

        if (f.getCliente().getPec() != null && !f.getCliente().getPec().isBlank()) {
            xml.append("      <PECDestinatario>").append(f.getCliente().getPec()).append("</PECDestinatario>\n");
        }
        xml.append("    </DatiTrasmissione>\n");

        // Cedente / Prestatore (La tua azienda)
        xml.append("    <CedentePrestatore>\n");
        xml.append("      <DatiAnagrafici>\n");
        xml.append("        <IdFiscaleIVA><IdPaese>IT</IdPaese><IdCodice>01234567890</IdCodice></IdFiscaleIVA>\n");
        xml.append("        <Anagrafica><Denominazione>LA MIA AZIENDA SRL</Denominazione></Anagrafica>\n");
        xml.append("        <RegimeFiscale>RF01</RegimeFiscale>\n"); // RF01 = Ordinario
        xml.append("      </DatiAnagrafici>\n");
        xml.append("      <Sede>\n");
        xml.append("        <Indirizzo>Via Roma 1</Indirizzo>\n");
        xml.append("        <CAP>00100</CAP>\n");
        xml.append("        <Comune>Roma</Comune>\n");
        xml.append("        <Nazione>IT</Nazione>\n");
        xml.append("      </Sede>\n");
        xml.append("    </CedentePrestatore>\n");

        // Cessionario / Committente (Il Cliente)
        xml.append("    <CessionarioCommittente>\n");
        xml.append("      <DatiAnagrafici>\n");
        if (f.getCliente().getPartitaIva() != null && !f.getCliente().getPartitaIva().isBlank()) {
            xml.append("        <IdFiscaleIVA><IdPaese>IT</IdPaese><IdCodice>").append(f.getCliente().getPartitaIva()).append("</IdCodice></IdFiscaleIVA>\n");
        } else if (f.getCliente().getCodiceFiscale() != null) {
            xml.append("        <CodiceFiscale>").append(f.getCliente().getCodiceFiscale()).append("</CodiceFiscale>\n");
        }

        xml.append("        <Anagrafica>\n");
        if (f.getCliente().getTipo() != null && f.getCliente().getTipo().name().equals("PERSONA_FISICA")) {
            xml.append("          <Nome>").append(f.getCliente().getRagioneSociale()).append("</Nome>\n");
            xml.append("          <Cognome>").append(f.getCliente().getRagioneSociale()).append("</Cognome>\n");
        } else {
            xml.append("          <Denominazione>").append(f.getCliente().getRagioneSociale()).append("</Denominazione>\n");
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
        xml.append("        <TipoDocumento>TD01</TipoDocumento>\n"); // TD01 = Fattura
        xml.append("        <Divisa>EUR</Divisa>\n");
        xml.append("        <Data>").append(f.getDataEmissione().format(dtf)).append("</Data>\n");
        xml.append("        <Numero>").append(f.getNumero()).append("</Numero>\n");
        xml.append("        <ImportoTotaleDocumento>").append(f.getTotale()).append("</ImportoTotaleDocumento>\n");
        xml.append("      </DatiGeneraliDocumento>\n");
        xml.append("    </DatiGenerali>\n");

        // Riepilogo IVA Obbligatorio
        xml.append("    <DatiBeniServizi>\n");
        xml.append("      <DatiRiepilogo>\n");
        xml.append("        <AliquotaIVA>").append(f.getAliquotaIva()).append("</AliquotaIVA>\n");
        xml.append("        <ImponibileImporto>").append(f.getImponibile()).append("</ImponibileImporto>\n");
        xml.append("        <Imposta>").append(f.getIva()).append("</Imposta>\n");
        xml.append("        <EsigibilitaIVA>I</EsigibilitaIVA>\n");
        xml.append("      </DatiRiepilogo>\n");
        xml.append("    </DatiBeniServizi>\n");

        // Metodi di Pagamento
        xml.append("    <DatiPagamento>\n");
        xml.append("      <CondizioniPagamento>TP02</CondizioniPagamento>\n"); // TP02 = Pagamento Completo
        xml.append("      <DettaglioPagamento>\n");
        xml.append("        <ModalitaPagamento>MP05</ModalitaPagamento>\n"); // MP05 = Bonifico, da rendere dinamico
        xml.append("        <DataScadenzaPagamento>").append(f.getDataScadenza() != null ? f.getDataScadenza().format(dtf) : f.getDataEmissione().format(dtf)).append("</DataScadenzaPagamento>\n");
        xml.append("        <ImportoPagamento>").append(f.getTotale()).append("</ImportoPagamento>\n");
        xml.append("      </DettaglioPagamento>\n");
        xml.append("    </DatiPagamento>\n");

        xml.append("  </FatturaElettronicaBody>\n");
        xml.append("</p:FatturaElettronica>");

        return xml.toString();
    }
}
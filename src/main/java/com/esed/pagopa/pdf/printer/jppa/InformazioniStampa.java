/**
 * 
 */
package com.esed.pagopa.pdf.printer.jppa;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import com.seda.payer.commons.geos.Documento;

import it.maggioli.pagopa.jppa.printer.model.ChiaviDebitoDto;
import it.maggioli.pagopa.jppa.printer.model.ChiaviDebitoDto.CodiceServizioEnum;
import it.maggioli.pagopa.jppa.printer.model.ChiaviDebitoDto.CodiceTipoDebitoEnum;
import it.maggioli.pagopa.jppa.printer.model.DatiEnteAvvisaturaDto;
import it.maggioli.pagopa.jppa.printer.model.PosizioneDebitoriaAvvisaturaDto;
import it.maggioli.pagopa.jppa.printer.model.StampaAvvisaturaRichiesta;
import it.maggioli.pagopa.jppa.printer.model.StampaBollettinoRichiesta;
import it.maggioli.pagopa.jppa.printer.model.StampaAvvisaturaRichiesta.LocaleEnum;

/**
 * 
 */
public class InformazioniStampa {
	
	private StampaAvvisaturaRichiesta stampaAvvisaturaRichiesta;
	private ChiaviDebitoDto chiaviDebitoDto;
	
	
	private DatiEnteAvvisaturaDto avvisaturaDto = null;

	/**
	 * 
	 */
	public InformazioniStampa() {
		this.chiaviDebitoDto = new ChiaviDebitoDto();
	}
	
	private void setChiaveDebito(String codiceIpa,CodiceServizioEnum codice,
			CodiceTipoDebitoEnum codiceTipoDebito,String idDebito,String idPosizione) {
        
		this.chiaviDebitoDto.setCodiceIpaEnte(codiceIpa);
		this.chiaviDebitoDto.setCodiceServizio(codice);
		this.chiaviDebitoDto.setCodiceTipoDebito(codiceTipoDebito);
		this.chiaviDebitoDto.setIdDebito(idDebito);
		this.chiaviDebitoDto.setIdPosizione(idPosizione);
	}
	
	public void setDebito(String codiceIpa,CodiceServizioEnum codice,
			CodiceTipoDebitoEnum codiceTipoDebito,String idDebito,String idPosizione) {
		
		setChiaveDebito(codiceIpa, codice, codiceTipoDebito, idDebito, idPosizione);
		
	}
	
	public StampaAvvisaturaRichiesta setAvvisaturaRisposta(String logoente64,LocaleEnum locale,
			String numeroAvviso) {
        
		stampaAvvisaturaRichiesta.base64FileLogoEnte(logoente64);
        stampaAvvisaturaRichiesta.chiaviDebito(this.chiaviDebitoDto);
        stampaAvvisaturaRichiesta.setLocale(locale);
        if(numeroAvviso==null) {
        	stampaAvvisaturaRichiesta.setNumeroAvviso("");
        }else {
        stampaAvvisaturaRichiesta.setNumeroAvviso(numeroAvviso);
        }
        
        return this.stampaAvvisaturaRichiesta;
	}
	
	//Stampa Bollettino
	public void setAvvisauraDto(Documento doc,String tipostampa,String cutecute) {
		this.avvisaturaDto = new DatiEnteAvvisaturaDto();
		this.avvisaturaDto.setCodiceFiscale(doc.DatiCreditore.get(0).Cf);
		this.avvisaturaDto.setCodiceInterbancario(doc.DatiCreditore.get(0).CodiceInterbancario);
		if(cutecute.equals("000P6")) {
			this.avvisaturaDto.setCpAbilitato(false);
		}
		else {
			this.avvisaturaDto.setCpAbilitato(!(doc.DatiCreditore.get(0).CodiceInterbancario.equals("00000")));
		}
		
		this.avvisaturaDto.cpAutorizzazione(doc.DatiBollettino.get(0).AutorizCcp);
		if(doc.DatiBollettino.get(0).AutorizCcp == null) {
			this.avvisaturaDto.setCpIntestatario("");
			this.avvisaturaDto.setCpIntestatarioDe("");
		}
		else {
				if(tipostampa.equals("jppa")) {
				this.avvisaturaDto.setCpIntestatario(doc.DatiBollettino.get(0).AutorizCcp);
				this.avvisaturaDto.setCpIntestatarioDe("");
			}
			
		}
		
		if(doc.DatiBollettino.get(0).AutorizCcp == null) {
			System.out.println("Numero documento NULL Avvisatura DTO");
			System.out.println("doc.NumeroDocumento = " + doc.DatiBollettino.get(0).AutorizCcp);
			this.avvisaturaDto.setCpNumero("NN");
		}
		else {
			this.avvisaturaDto.setCpNumero(doc.DatiBollettino.get(0).AutorizCcp);
		}
		
		this.avvisaturaDto.setSettore(doc.DatiCreditore.get(0).Denominazione2);
		if(tipostampa.equals("jppa")) {
        this.avvisaturaDto.setNome(doc.DatiCreditore.get(0).Denominazione1);
        this.avvisaturaDto.setNomeDe(doc.DatiCreditore.get(0).Denominazione2);
		}

	}

	private String buildDate(String data) {
		String dataformat[] = new String[3];
		dataformat = data.split("\\/"); //2099-12-31T00:00:00Z
		return new StringBuilder().append(dataformat[2]).append("-").append(dataformat[1])
				.append("-").append(dataformat[0]).append("T").append("00:00:00Z").toString();
	}
	
	public StampaBollettinoRichiesta bollRichiesta(Documento doc, String logo64,String tipoStampa,String cutecute) {
		
		StampaBollettinoRichiesta bollRichiesta = null;
//		if(tipoStampa.equals("jppade")) {
//			return richiestade(doc,logo64);
//		}else {
		
		bollRichiesta = new StampaBollettinoRichiesta();
		PosizioneDebitoriaAvvisaturaDto posDeb  = new PosizioneDebitoriaAvvisaturaDto();
		if(tipoStampa.equals("jppa")) {
			posDeb.setCausaleDebitoriaDe(doc.CausaleDocumento);
			posDeb.setCausaleDebitoria(doc.CausaleDocumento);
			}
		else {
		posDeb.setCausaleDebitoria(doc.CausaleDocumento);
		}
		posDeb.setImporto(Float.valueOf(doc.ImportoDocumento)/100);
		if(doc.DatiBollettino.get(0).AutorizCcp==null) {
			System.out.println("Numero documento NULL PosDebitoria");
			System.out.println("doc.DatiBollettino.get(0).AutorizCcp = " + doc.DatiBollettino.get(0).AutorizCcp);
			posDeb.setNumeroAvviso("NN");
		}else {
			posDeb.setNumeroAvviso(doc.DatiBollettino.get(0).AvvisoPagoPa);
		}
			posDeb.setTitDebitoCapRes(doc.DatiAnagrafici.get(0).Indirizzo);
		    posDeb.setDataScadenza(buildDate(doc.DatiBollettino.get(0).ScadenzaRata));// Data scadenza
			posDeb.setTitDebitoCapSedeLegale("");
			posDeb.setTitDebitoCf(doc.DatiAnagrafici.get(0).Cf);
			posDeb.setTitDebitoCivicoRes(doc.DatiAnagrafici.get(0).Indirizzo);
			posDeb.setTitDebitoCivicoSedeLegale("");
			posDeb.setTitDebitoCognome("");
			posDeb.setTitDebitoComuneRes(doc.DatiAnagrafici.get(0).Citta);
			posDeb.setTitDebitoComuneSedeLegale("");
			posDeb.setTitDebitoIndirizzoRes("");
			posDeb.setTitDebitonazioneRes("");
			posDeb.setTitDebitoNazioneSedeLegale("");
			posDeb.setTitDebitoNome("");
			posDeb.setTitDebitoNominativo(doc.DatiAnagrafici.get(0).Denominazione1);
			posDeb.setTitDebitoprovRes("");
			posDeb.setTitDebitoProvSedeLegale(doc.DatiAnagrafici.get(0).Provincia);
			posDeb.setTitDebitoRagioneSociale("");
			posDeb.setTitDebitoIndirizzoSedeLegale("");
		
		bollRichiesta.addPosizioneDebitoriaItem(posDeb); // causale
		bollRichiesta.datiEnte(this.avvisaturaDto);
		bollRichiesta.setNumeroAvviso(doc.DatiBollettino.get(0).AvvisoPagoPa);
        
		if(cutecute.equals("000P6")) {
			bollRichiesta.setLocale(it.maggioli.pagopa.jppa.printer.model.StampaBollettinoRichiesta.LocaleEnum.DE);
		}else  {
			bollRichiesta.setLocale(it.maggioli.pagopa.jppa.printer.model.StampaBollettinoRichiesta.LocaleEnum.IT);
		}
		bollRichiesta.setBase64FileLogoEnte(logo64);
		
		return bollRichiesta; 
	}
	
	
	private StampaBollettinoRichiesta richiestade(Documento doc, String logo64) {
		
		StampaBollettinoRichiesta bollRichiesta = new StampaBollettinoRichiesta();
		PosizioneDebitoriaAvvisaturaDto posDeb  = new PosizioneDebitoriaAvvisaturaDto();
		posDeb.setCausaleDebitoriaDe(doc.CausaleDocumento);
		posDeb.setImporto(Float.valueOf(doc.ImportoDocumento));
		if(doc.NumeroDocumento==null) {
			System.out.println("Numero documento NULL Posizione Debitoria");
			posDeb.setNumeroAvviso("NN");
		}else {
			posDeb.setNumeroAvviso(doc.NumeroDocumento);
		}
		posDeb.setTitDebitoCapRes(doc.DatiAnagrafici.get(0).Indirizzo);
		posDeb.setTitDebitoCapSedeLegale("");
		posDeb.setTitDebitoCf(doc.DatiAnagrafici.get(0).Cf);
		posDeb.setTitDebitoCivicoRes("");
		posDeb.setTitDebitoCivicoSedeLegale("");
		posDeb.setTitDebitoCognome("");
		posDeb.setTitDebitoComuneRes(doc.DatiAnagrafici.get(0).Citta);
		posDeb.setTitDebitoComuneSedeLegale("");
		posDeb.setTitDebitoIndirizzoRes("");
		posDeb.setTitDebitonazioneRes("");
		posDeb.setTitDebitoNazioneSedeLegale("");
		posDeb.setTitDebitoNome("");
		posDeb.setTitDebitoNominativo(doc.DatiAnagrafici.get(0).Denominazione1);
		posDeb.setTitDebitoprovRes("");
		posDeb.setTitDebitoProvSedeLegale(doc.DatiAnagrafici.get(0).Provincia);
		posDeb.setTitDebitoRagioneSociale("");
		posDeb.setTitDebitoIndirizzoSedeLegale("");
		
		bollRichiesta.addPosizioneDebitoriaItem(posDeb); // causale
		bollRichiesta.datiEnte(this.avvisaturaDto);
		bollRichiesta.setNumeroAvviso(doc.NumeroDocumento);
		bollRichiesta.setLocale(it.maggioli.pagopa.jppa.printer.model.StampaBollettinoRichiesta.LocaleEnum.DE);
		bollRichiesta.setBase64FileLogoEnte(logo64);
		
		return bollRichiesta;

	}


	/*
	 * 
	 *  datiEnte.setCodiceFiscale("");
        datiEnte.setCodiceInterbancario(null);
        datiEnte.setCpAbilitato(null);
        datiEnte.cpAutorizzazione(null);
        datiEnte.setCpIntestatario(null);
        datiEnte.setCpNumero(null);
        datiEnte.setSettore(null);
        datiEnte.setNome(null);
        
        
        stampaBollettinoRichiesta = new StampaBollettinoRichiesta();
        
        stampaBollettinoRichiesta.addPosizioneDebitoriaItem(null); // causale
        stampaBollettinoRichiesta.datiEnte(null);
        stampaBollettinoRichiesta.setNumeroAvviso(null);
        stampaBollettinoRichiesta.setLocale(null);
        stampaBollettinoRichiesta.setBase64FileLogoEnte(null);
	 * 
	 * */

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

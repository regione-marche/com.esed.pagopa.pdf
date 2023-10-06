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
           if(cutecute.equals("000P6")) {
			this.avvisaturaDto.setNomeDe(doc.DatiCreditore.get(0).Denominazione2);
           }
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
		
		String primaparte[] = new String[2];
		primaparte = doc.CausaleDocumento.split("\\n");
		
		if(primaparte[0]==null||primaparte[0]=="") {
			primaparte[0] = "";
		}
		
		String prima[] = new String[2];
		

		prima = primaparte[0].split("\\/");
	
		doc.CausaleDocumento = doc.CausaleDocumento.replaceAll("(\\r|\\n)", " ");
		
		if(prima.length==1) {
			posDeb.setCausaleDebitoriaDe(prima[0].replaceAll("(\\r|\\n)", ""));
			posDeb.setCausaleDebitoria(primaparte[1].replaceAll("(\\r|\\n)", ""));
		} else {
		
		if(tipoStampa.equals("jppa") && cutecute.equals("000P6")) {
			if(prima!=null) {
				if(prima[0]==null||prima[1]==null) {
					posDeb.setCausaleDebitoriaDe("");
				}else {
					posDeb.setCausaleDebitoriaDe(prima[0].replaceAll("(\\r|\\n)", "")+"/"+prima[1].replaceAll("(\\r|\\n)", ""));
				}
		    }
			  if(primaparte[1]==null) {
				posDeb.setCausaleDebitoria(doc.CausaleDocumento);
			  }else {
				  posDeb.setCausaleDebitoria(primaparte[1].replaceAll("(\\r|\\n)", ""));
			  }
			}
		
			else {
				posDeb.setCausaleDebitoriaDe("");
				posDeb.setCausaleDebitoria(doc.CausaleDocumento);
			}
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
		
		if(logo64.equals("")) {
			logo64 = null;
			bollRichiesta.setBase64FileLogoEnte(logo64);
		}
		bollRichiesta.setBase64FileLogoEnte(logo64);
		
		return bollRichiesta; 
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

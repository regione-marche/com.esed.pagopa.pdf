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
import com.seda.payer.commons.geos.Flusso;

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
public class InformazioniStampaBolzano implements InformazioniStampaInterface {
	
	private StampaAvvisaturaRichiesta stampaAvvisaturaRichiesta;
	private ChiaviDebitoDto chiaviDebitoDto;
	
	
	private DatiEnteAvvisaturaDto avvisaturaDto = null;

	/**
	 * 
	 */
	public InformazioniStampaBolzano() {
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
	@Override
	public DatiEnteAvvisaturaDto setAvvisauraDto(Flusso flusso,Documento doc,Boolean tipostampa,String cutecute) {
		this.avvisaturaDto = new DatiEnteAvvisaturaDto();
		this.avvisaturaDto.setCodiceFiscale(doc.DatiCreditore.get(0).Cf);
		this.avvisaturaDto.setCodiceInterbancario(doc.DatiCreditore.get(0).CodiceInterbancario);
		this.avvisaturaDto.setDataMatrix(doc.DatiBollettino.get(0).QRcodePagoPa);
		if(cutecute.equals("000P6")) {
			this.avvisaturaDto.setCpAbilitato(false);
		}
		else {
			this.avvisaturaDto.setCpAbilitato(tipostampa);
		}
		
		this.avvisaturaDto.cpAutorizzazione(doc.DatiBollettino.get(0).AutorizCcp);
		if(doc.DatiBollettino.get(0).AutorizCcp == null || cutecute.equals("000P6")) {
			this.avvisaturaDto.setCpIntestatario("");
			this.avvisaturaDto.setCpIntestatarioDe("");
		}
		else {
				if(tipostampa.equals("jppa")) {
				this.avvisaturaDto.setCpIntestatario(doc.DatiBollettino.get(0).AutorizCcp);
				//this.avvisaturaDto.setCpIntestatarioDe("");
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
			this.avvisaturaDto.setNome(doc.DatiCreditore.get(0).Denominazione1);
           if(cutecute.equals("000P6")) {
			this.avvisaturaDto.setNomeDe(doc.DatiCreditore.get(0).Denominazione2);
           }
		

		return avvisaturaDto;
	}

	public String buildDate(String data) {
		String dataformat[] = new String[3];
		dataformat = data.split("\\/"); //2099-12-31T00:00:00Z
		return new StringBuilder().append(dataformat[2]).append("-").append(dataformat[1])
				.append("-").append(dataformat[0]).append("T").append("00:00:00Z").toString();
	}
	
	@Override
	public StampaBollettinoRichiesta bollRichiesta(Flusso flusso,Documento doc, String logo64,String cutecute) {
		
		StampaBollettinoRichiesta bollRichiesta = null;
		
		bollRichiesta = new StampaBollettinoRichiesta();
		PosizioneDebitoriaAvvisaturaDto posDeb  = new PosizioneDebitoriaAvvisaturaDto();
		
		if(cutecute.equals("000P6")) {
		
		String primaparte[] = new String[2];
		
		if(doc.CausaleDocumento.contains("\n")) {
			primaparte = doc.CausaleDocumento.split("\\n");
		}
		
		primaparte = doc.CausaleDocumento.split("\\n");
		
		if(primaparte[0]==null||primaparte[0].equals("")) {
			primaparte[0] = "";
		}
		
		String prima[] = new String[2];
		

		prima = primaparte[0].split("\\/");
	
		doc.CausaleDocumento = doc.CausaleDocumento.replaceAll("(\\r|\\n)", " ");
		
		if(prima.length==1) {
			 posDeb.setCausaleDebitoriaDe(prima[0].replaceAll("(\\r|\\n)", ""));
			if(primaparte.length==2) {
			 posDeb.setCausaleDebitoria(primaparte[1].replaceAll("(\\r|\\n)", ""));
			}else {
				posDeb.setCausaleDebitoria(doc.CausaleDocumento.replaceAll("(\\r|\\n)", ""));
			}
		} else {
		
		if(cutecute.equals("000P6")) {
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
		}
		
	}else {
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
			posDeb.setTitDebitoCapRes(doc.DatiAnagrafici.get(0).Indirizzo+" "+doc.DatiAnagrafici.get(0).Cap+" ");
		    posDeb.setDataScadenza(buildDate(doc.DatiBollettino.get(0).ScadenzaRata));// Data scadenza
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

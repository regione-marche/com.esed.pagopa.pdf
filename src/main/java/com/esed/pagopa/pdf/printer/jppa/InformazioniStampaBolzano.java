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

import com.seda.payer.commons.geos.Bollettino;
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
	public StampaBollettinoRichiesta bollRichiesta(Flusso flusso,Documento doc, String logo64,String cutecute,boolean daArchivio) {
		
		StampaBollettinoRichiesta bollRichiesta = null;
		
		bollRichiesta = new StampaBollettinoRichiesta();
		PosizioneDebitoriaAvvisaturaDto posDeb  = new PosizioneDebitoriaAvvisaturaDto();
		
		System.out.println("QRcodePagoPa: " + doc.DatiBollettino.get(0).QRcodePagoPa);
		System.out.println("BarCode: " + doc.DatiBollettino.get(0).BarcodePagoPa);
		
		if(!doc.DatiBollettino.get(0).AvvisoPagoPa.contains(" ")) {
			doc.DatiBollettino.get(0).AvvisoPagoPa = doc.DatiBollettino.get(0).AvvisoPagoPa.replaceAll("(.{" + 4 + "})", "$0 ").trim();
			System.out.println("Numero Avviso: " + doc.DatiBollettino.get(0).AvvisoPagoPa);
		}
		
		if(!daArchivio) {
		     posDeb.setDataMatrix(doc.DatiBollettino.get(0).QRcodePagoPa);
		}else {
			System.out.println("Vengo da archivio sono scambiati qr e barcode....");
		    posDeb.setDataMatrix(doc.DatiBollettino.get(0).BarcodePagoPa);
		}
		
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
			logo64 = "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";
			
			bollRichiesta.setBase64FileLogoEnte(logo64);
		}
		bollRichiesta.setBase64FileLogoEnte(logo64);
		
		return bollRichiesta; 
	}
	

	
	
	
	
	@Override
	public DatiEnteAvvisaturaDto avvisaturaDto999(Flusso flusso,Documento doc,Boolean tipostampa,
			String cutecute,Bollettino bollettino999) {
		
		this.avvisaturaDto = new DatiEnteAvvisaturaDto();
		this.avvisaturaDto.setCodiceFiscale(doc.DatiCreditore.get(0).Cf);
		this.avvisaturaDto.setCodiceInterbancario(doc.DatiCreditore.get(0).CodiceInterbancario);
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
    @Override
    public StampaBollettinoRichiesta stampaBoll999(Bollettino bollettino999,Flusso flusso,
			Documento doc, String logo64,String cutecute,boolean daArchivio) {
    	
    	avvisaturaDto999(flusso, doc, false, cutecute, bollettino999);

		StampaBollettinoRichiesta bollRichiesta = null;
		
		bollRichiesta = new StampaBollettinoRichiesta();
		
		PosizioneDebitoriaAvvisaturaDto posDeb  = new PosizioneDebitoriaAvvisaturaDto();
		
		
		if(!doc.DatiBollettino.get(0).AvvisoPagoPa.contains(" ")) {
			bollettino999.AvvisoPagoPa = bollettino999.AvvisoPagoPa.replaceAll("(.{" + 4 + "})", "$0 ").trim();
		    System.out.println("Numero Avviso: " + bollettino999.AvvisoPagoPa);
		}
		
		
		System.out.println("QRcodePagoPa: " + doc.DatiBollettino.get(0).QRcodePagoPa);
		System.out.println("BarCode: " + doc.DatiBollettino.get(0).BarcodePagoPa);
		
		if(!daArchivio) {
		     posDeb.setDataMatrix(bollettino999.QRcodePagoPa);
		}else {
			System.out.println("Vengo da archivio sono scambiati qr e barcode....");
		    posDeb.setDataMatrix(bollettino999.BarcodePagoPa);
		}
		
		
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
		posDeb.setImporto(Float.valueOf(bollettino999.Codeline2Boll)/100);
		if(doc.DatiBollettino.get(0).AutorizCcp==null) {
			System.out.println("Numero documento NULL PosDebitoria");
			System.out.println("doc.DatiBollettino.get(0).AutorizCcp = " + doc.DatiBollettino.get(0).AutorizCcp);
			posDeb.setNumeroAvviso("NN");
		}else {
			posDeb.setNumeroAvviso(bollettino999.AvvisoPagoPa);
		}
			posDeb.setTitDebitoCapRes(doc.DatiAnagrafici.get(0).Indirizzo+" "+doc.DatiAnagrafici.get(0).Cap+" ");
		    posDeb.setDataScadenza(buildDate(bollettino999.ScadenzaRata));// Data scadenza
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
			logo64 = "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";
			
			bollRichiesta.setBase64FileLogoEnte(logo64);
		}
		bollRichiesta.setBase64FileLogoEnte(logo64);
		
		return bollRichiesta; 
    	
    	
	}
	

}















package com.esed.pagopa.pdf.printer.jppa;

import com.seda.payer.commons.geos.Bollettino;
import com.seda.payer.commons.geos.Documento;
import com.seda.payer.commons.geos.Flusso;

import it.maggioli.pagopa.jppa.printer.model.DatiEnteAvvisaturaDto;
import it.maggioli.pagopa.jppa.printer.model.PosizioneDebitoriaAvvisaturaDto;
import it.maggioli.pagopa.jppa.printer.model.StampaBollettinoRichiesta;

public interface InformazioniStampaInterface {
	
	public default DatiEnteAvvisaturaDto setAvvisauraDto(Flusso flusso,Documento doc,Boolean tipostampa,String cutecute) {
		DatiEnteAvvisaturaDto avvisaturaDto = new DatiEnteAvvisaturaDto();
		avvisaturaDto = new DatiEnteAvvisaturaDto();
		avvisaturaDto.setCodiceFiscale(doc.DatiCreditore.get(0).Cf);
		avvisaturaDto.setCodiceInterbancario(doc.DatiCreditore.get(0).CodiceInterbancario);
		if(cutecute.equals("000P6")) {
			avvisaturaDto.setCpAbilitato(false);
		}
		else {
			avvisaturaDto.setCpAbilitato(!(doc.DatiCreditore.get(0).CodiceInterbancario.equals("00000")));
		}
		
		avvisaturaDto.cpAutorizzazione(doc.DatiBollettino.get(0).AutorizCcp);
		if(doc.DatiBollettino.get(0).AutorizCcp == null || cutecute.equals("000P6")) {
			avvisaturaDto.setCpIntestatario("");
			avvisaturaDto.setCpIntestatarioDe("");
		}
		else {
				if(tipostampa.equals("jppa")) {
				avvisaturaDto.setCpIntestatario(doc.DatiBollettino.get(0).AutorizCcp);
				//this.avvisaturaDto.setCpIntestatarioDe("");
			}
			
		}
		
		if(doc.DatiBollettino.get(0).AutorizCcp == null) {
			System.out.println("Numero documento NULL Avvisatura DTO");
			System.out.println("doc.NumeroDocumento = " + doc.DatiBollettino.get(0).AutorizCcp);
			avvisaturaDto.setCpNumero("NN");
		}
		else {
			avvisaturaDto.setCpNumero(doc.DatiBollettino.get(0).AutorizCcp);
		}
		
		avvisaturaDto.setSettore(doc.DatiCreditore.get(0).Denominazione2);
		if(tipostampa.equals("jppa")) {
			avvisaturaDto.setNome(doc.DatiCreditore.get(0).Denominazione1);
           if(cutecute.equals("000P6")) {
			avvisaturaDto.setNomeDe(doc.DatiCreditore.get(0).Denominazione2);
           }
		}

		return avvisaturaDto;
	}
	
	
	public default String buildDate(String data) {
		String dataformat[] = new String[3];
		dataformat = data.split("\\/"); //2099-12-31T00:00:00Z
		return new StringBuilder().append(dataformat[2]).append("-").append(dataformat[1])
				.append("-").append(dataformat[0]).append("T").append("00:00:00Z").toString();
	}
	
	
	
	public default  StampaBollettinoRichiesta bollRichiesta(Flusso flusso,Documento doc, String logo64,String cutecute,boolean daArchivio) {
		
		DatiEnteAvvisaturaDto avvisaturaDto = setAvvisauraDto(flusso,doc, false, cutecute);
		
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
		//posDeb.setCausaleDebitoriaDe("");
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
		bollRichiesta.datiEnte(avvisaturaDto);
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
	
	
	
	public default StampaBollettinoRichiesta stampaBoll999(Bollettino boll999,Flusso flusso,
			Documento doc, String logo64,String cutecute,boolean daArchivio) {
		return null;
	}
	
	/**
	 * Bolzano999
	 * @param boll999
	 * @param flusso
	 * @param doc
	 * @param logo64
	 * @param cutecute
	 * @return
	 */
	public default StampaBollettinoRichiesta stampaBoll999(Flusso flusso,Documento doc,Boolean tipostampa,
			String cutecute,Bollettino bollettino999,boolean daArchivio) {
		return null;
	}


	
	public default DatiEnteAvvisaturaDto avvisaturaDto999(Flusso flusso, Documento doc, Boolean tipostampa, String cutecute,
			Bollettino bollettino999) {
		return null;
	}
	
	
	
	
	
	
	
	
	
	
	
	

}

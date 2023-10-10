/**
 * 
 */
package com.esed.pagopa.pdf.printer.jppa;

import com.seda.payer.commons.geos.Documento;

import it.maggioli.pagopa.jppa.printer.model.DatiEnteAvvisaturaDto;
import it.maggioli.pagopa.jppa.printer.model.PosizioneDebitoriaAvvisaturaDto;
import it.maggioli.pagopa.jppa.printer.model.StampaBollettinoRichiesta;

/**
 * 
 */
public class InformazioniStampaAosta implements InformazioniStampaInterface {

	/**
	 * 
	 */
	public InformazioniStampaAosta() {
	}
	
	@Override
	public DatiEnteAvvisaturaDto setAvvisauraDto(Documento doc,String tipostampa,String cutecute) {
		
		DatiEnteAvvisaturaDto avvisaturaDto = new DatiEnteAvvisaturaDto();
		avvisaturaDto.setCodiceFiscale(doc.DatiCreditore.get(0).Cf);
		avvisaturaDto.setCodiceInterbancario(doc.DatiCreditore.get(0).CodiceInterbancario);
	    avvisaturaDto.setCpAbilitato(!(doc.DatiCreditore.get(0).CodiceInterbancario.equals("00000")));
		
		avvisaturaDto.cpAutorizzazione(doc.DatiBollettino.get(0).AutorizCcp);

		avvisaturaDto.setCpIntestatario(doc.DatiBollettino.get(0).AutorizCcp);
			
	
		if(doc.DatiBollettino.get(0).AutorizCcp == null) {
			System.out.println("Numero documento NULL Avvisatura DTO");
			System.out.println("doc.NumeroDocumento = " + doc.DatiBollettino.get(0).AutorizCcp);
			avvisaturaDto.setCpNumero("NN");
		}
		
		else {
			avvisaturaDto.setCpNumero(doc.DatiBollettino.get(0).AutorizCcp);
		}
		
		
		avvisaturaDto.setNome(doc.DatiCreditore.get(0).Denominazione1);
		
		
		avvisaturaDto.setSettore(doc.DatiCreditore.get(0).Denominazione2);


		return avvisaturaDto;
		
	}
	
	
	@Override
	public StampaBollettinoRichiesta bollRichiesta(Documento doc, String logo64,String tipoStampa,String cutecute) {
		
		DatiEnteAvvisaturaDto avvisaturaDto = setAvvisauraDto(doc, tipoStampa, cutecute);
		
		StampaBollettinoRichiesta bollRichiesta = null;
		
		bollRichiesta = new StampaBollettinoRichiesta();
		PosizioneDebitoriaAvvisaturaDto posDeb  = new PosizioneDebitoriaAvvisaturaDto();

		posDeb.setCausaleDebitoria(doc.CausaleDocumento);
		
		posDeb.setImporto(Float.valueOf(doc.ImportoDocumento)/100);
		if(doc.DatiBollettino.get(0).AutorizCcp==null) {
			System.out.println("Numero documento NULL PosDebitoria");
			System.out.println("doc.DatiBollettino.get(0).AutorizCcp = " + doc.DatiBollettino.get(0).AutorizCcp);
			posDeb.setNumeroAvviso("NN");
		}else {
			posDeb.setNumeroAvviso(doc.DatiBollettino.get(0).AvvisoPagoPa);
		}
			posDeb.setTitDebitoCapRes("");
		    posDeb.setDataScadenza(buildDate(doc.DatiBollettino.get(0).ScadenzaRata));// Data scadenza
			posDeb.setTitDebitoCapSedeLegale("");
			posDeb.setTitDebitoCf(doc.DatiAnagrafici.get(0).Cf);
			posDeb.setTitDebitoCivicoRes("");
			posDeb.setTitDebitoCivicoSedeLegale("");
			posDeb.setTitDebitoCognome("");
			posDeb.setTitDebitoComuneRes(doc.DatiAnagrafici.get(0).Cap +" "+ doc.DatiAnagrafici.get(0).Citta);
			posDeb.setTitDebitoComuneSedeLegale("");
			posDeb.setTitDebitoIndirizzoRes(doc.DatiAnagrafici.get(0).Indirizzo);
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

		bollRichiesta.setLocale(it.maggioli.pagopa.jppa.printer.model.StampaBollettinoRichiesta.LocaleEnum.IT);
		
		if(logo64.equals("")) {
			logo64 = null;
			bollRichiesta.setBase64FileLogoEnte(logo64);
		}
		bollRichiesta.setBase64FileLogoEnte(logo64);
		
		return bollRichiesta; 
	}
	
	
}

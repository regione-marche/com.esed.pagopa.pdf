/**
 * 
 */
package com.esed.pagopa.pdf.printer.jppa;

import com.seda.payer.commons.geos.Bollettino;
import com.seda.payer.commons.geos.Documento;
import com.seda.payer.commons.geos.Flusso;

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
	public DatiEnteAvvisaturaDto setAvvisauraDto(Flusso flusso,Documento doc,Boolean tipostampa,String cutecute) {
		
		System.out.println("TipoStampa postale: " + tipostampa);
		
		DatiEnteAvvisaturaDto avvisaturaDto = new DatiEnteAvvisaturaDto();
		avvisaturaDto.setCodiceFiscale(doc.DatiCreditore.get(0).Cf);
		avvisaturaDto.setCodiceInterbancario(doc.DatiCreditore.get(0).CodiceInterbancario);
	    avvisaturaDto.setCpAbilitato(tipostampa);
	    
		avvisaturaDto.cpAutorizzazione(doc.DatiBollettino.get(0).AutorizCcp);

		System.out.println("Intestatario Descon60Boll: " + doc.DatiBollettino.get(0).Descon60Boll);
		avvisaturaDto.setCpIntestatario(doc.DatiBollettino.get(0).Descon60Boll);
			
	
		if(doc.DatiBollettino.get(0).AutorizCcp == null) {
			System.out.println("Numero documento NULL Avvisatura DTO");
			System.out.println("doc.NumeroDocumento = " + doc.DatiBollettino.get(0).AutorizCcp);
			avvisaturaDto.setCpNumero("NN");
		}
		
		else {
			avvisaturaDto.setCpNumero(doc.DatiBollettino.get(0).Codeline12Boll);
		}
		
		
		avvisaturaDto.setNome(doc.DatiCreditore.get(0).Denominazione1);
		
		
		avvisaturaDto.setSettore(doc.DatiCreditore.get(0).Denominazione2);


		return avvisaturaDto;
		
	}
	
	
	@Override
	public StampaBollettinoRichiesta bollRichiesta(Flusso flusso,Documento doc, String logo64,String cutecute,boolean daArchivio) {
		
		DatiEnteAvvisaturaDto avvisaturaDto = setAvvisauraDto(flusso,doc, flusso.TipoStampa.equals("P"), cutecute);
		
		StampaBollettinoRichiesta bollRichiesta = null;
		
		bollRichiesta = new StampaBollettinoRichiesta();
		PosizioneDebitoriaAvvisaturaDto posDeb  = new PosizioneDebitoriaAvvisaturaDto();

		posDeb.setCausaleDebitoria(doc.CausaleDocumento);
		
		
		if(!doc.DatiBollettino.get(0).AvvisoPagoPa.contains(" ")) {
		doc.DatiBollettino.get(0).AvvisoPagoPa = doc.DatiBollettino.get(0).AvvisoPagoPa.replaceAll("(.{" + 4 + "})", "$0 ").trim();
		System.out.println("Numero Avviso: " + doc.DatiBollettino.get(0).AvvisoPagoPa);
		}
		
		
		String codiceAvvisoOriginalePagoPa = doc.DatiBollettino.get(0).AvvisoPagoPa == null ? "" : doc.DatiBollettino.get(0).AvvisoPagoPa.replace(" ", "");
		String numeroContoCorrente = doc.DatiBollettino.get(0).Codeline12Boll == null ? "" : doc.DatiBollettino.get(0).Codeline12Boll;
		String importo = doc.ImportoDocumento == null ? "" : doc.ImportoDocumento;
		
		System.out.println("codiceAvvisoOriginalePagoPa: " + codiceAvvisoOriginalePagoPa);
		System.out.println("numeroContoCorrente: " + numeroContoCorrente);
		System.out.println("importo: " + importo);
		
		String codeline =   "18"
				+ String.format("%-18.18s", codiceAvvisoOriginalePagoPa)
				+ "12"
				+ String.format("%12.12s", numeroContoCorrente).replace(' ', '0') // numero conto
				+ "10"
				+ String.format("%10.10s", importo).replace(' ', '0')
				+ "3"
				+ "896"; //tipo documento;
		
		System.out.println("Codeline: " + codeline);
		
		String dataMatrix = "codfase=NBPA;" + codeline + "1P1"
				+ String.format("%11.11s", doc.DatiCreditore.get(0).Cf)
				+ String.format("%-16.16s", doc.DatiAnagrafici.get(0).Cf)
				+ String.format("%-40.40s", doc.DatiAnagrafici.get(0).Denominazione1.toUpperCase())
				+ String.format("%-110.110s", doc.CausaleDocumento) + "            "// filler
				+ "A";
		
		System.out.println("DataMatrix: " + dataMatrix);
		
		
		if(!daArchivio) {
			posDeb.setDataMatrix(dataMatrix); //bollettino.QRcodePagoPa
		}else {
			System.out.println("Vengo da archivio");
			posDeb.setDataMatrix(dataMatrix);
		}
		
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
	
	
	
	
	private DatiEnteAvvisaturaDto avvisatura999(Flusso flusso,Documento doc,Boolean tipostampa,String cutecute,Bollettino bollettino999) {
		
		System.out.println("TipoStampa postale: " + tipostampa);
		
		DatiEnteAvvisaturaDto avvisaturaDto = new DatiEnteAvvisaturaDto();
		avvisaturaDto.setCodiceFiscale(doc.DatiCreditore.get(0).Cf);
		avvisaturaDto.setCodiceInterbancario(doc.DatiCreditore.get(0).CodiceInterbancario);
	    avvisaturaDto.setCpAbilitato(tipostampa);
	    
	    System.out.println("QRcodePagoPa: " + bollettino999.BarcodePagoPa);
	    
		avvisaturaDto.cpAutorizzazione(bollettino999.AutorizCcp);

		System.out.println("Intestatario Descon60Boll: " + bollettino999.Descon60Boll);
		avvisaturaDto.setCpIntestatario(bollettino999.Descon60Boll);
			
	
		if(doc.DatiBollettino.get(0).AutorizCcp == null) {
			System.out.println("Numero documento NULL Avvisatura DTO");
			System.out.println("doc.NumeroDocumento = " + doc.DatiBollettino.get(0).AutorizCcp);
			avvisaturaDto.setCpNumero("NN");
		}
		
		else {
			avvisaturaDto.setCpNumero(bollettino999.Codeline12Boll);
		}
		
		
		avvisaturaDto.setNome(doc.DatiCreditore.get(0).Denominazione1);
		
		
		avvisaturaDto.setSettore(doc.DatiCreditore.get(0).Denominazione2);


		return avvisaturaDto;
		
	}
	
	
	
	
	@Override
    public StampaBollettinoRichiesta stampaBoll999(Bollettino bollettino999,Flusso flusso,
			Documento doc, String logo64,String cutecute,boolean daArchivio) {
		
		DatiEnteAvvisaturaDto avvisaturaDto = avvisatura999(flusso, doc, flusso.TipoStampa.equals("P"), cutecute,bollettino999);
		
		StampaBollettinoRichiesta bollRichiesta = null;
		
		bollRichiesta = new StampaBollettinoRichiesta();
		PosizioneDebitoriaAvvisaturaDto posDeb  = new PosizioneDebitoriaAvvisaturaDto();

		posDeb.setCausaleDebitoria(doc.CausaleDocumento);
		
		if(!doc.DatiBollettino.get(0).AvvisoPagoPa.contains(" ")) {
			bollettino999.AvvisoPagoPa = bollettino999.AvvisoPagoPa.replaceAll("(.{" + 4 + "})", "$0 ").trim();
		System.out.println("Numero Avviso: " +bollettino999.AvvisoPagoPa);
		}
		
		
		String codiceAvvisoOriginalePagoPa = bollettino999.AvvisoPagoPa == null ? "" : bollettino999.AvvisoPagoPa.replace(" ", "");
		String numeroContoCorrente = bollettino999.Codeline12Boll == null ? "" : bollettino999.Codeline12Boll;
		String importo = bollettino999.Codeline2Boll == null ? "" : bollettino999.Codeline2Boll;
		
		System.out.println("codiceAvvisoOriginalePagoPa: " + codiceAvvisoOriginalePagoPa);
		System.out.println("numeroContoCorrente: " + numeroContoCorrente);
		System.out.println("importo: " + importo);
		
		String codeline =   "18"
				+ String.format("%-18.18s", codiceAvvisoOriginalePagoPa)
				+ "12"
				+ String.format("%12.12s", numeroContoCorrente).replace(' ', '0') // numero conto
				+ "10"
				+ String.format("%10.10s", importo).replace(' ', '0')
				+ "3"
				+ "896"; //tipo documento;
		
		System.out.println("Codeline: " + codeline);
		
		String dataMatrix = "codfase=NBPA;" + codeline + "1P1"
				+ String.format("%11.11s", doc.DatiCreditore.get(0).Cf)
				+ String.format("%-16.16s", doc.DatiAnagrafici.get(0).Cf)
				+ String.format("%-40.40s", doc.DatiAnagrafici.get(0).Denominazione1.toUpperCase())
				+ String.format("%-110.110s", doc.CausaleDocumento) + "            "// filler
				+ "A";
		
		System.out.println("DataMatrix: " + dataMatrix);
		
		
		if(!daArchivio) {
			posDeb.setDataMatrix(dataMatrix); //bollettino.QRcodePagoPa
		}else {
			System.out.println("Vengo da archivio");
			posDeb.setDataMatrix(dataMatrix);
		}
		
		posDeb.setImporto(Float.valueOf(bollettino999.Codeline2Boll)/100);
		if(doc.DatiBollettino.get(0).AutorizCcp==null) {
			System.out.println("Numero documento NULL PosDebitoria");
			System.out.println("doc.DatiBollettino.get(0).AutorizCcp = " + doc.DatiBollettino.get(0).AutorizCcp);
			posDeb.setNumeroAvviso("NN");
		}else {
			posDeb.setNumeroAvviso(bollettino999.AvvisoPagoPa);
		}
			posDeb.setTitDebitoCapRes("");
		    posDeb.setDataScadenza(buildDate(bollettino999.ScadenzaRata));// Data scadenza
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

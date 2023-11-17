package com.esed.pagopa.pdf;

import com.seda.payer.commons.inviaAvvisiForGeos.AvvisoRata;
import com.seda.payer.commons.inviaAvvisiForGeos.Debitore;
import com.seda.payer.commons.inviaAvvisiForGeos.File512;
import com.seda.payer.commons.inviaAvvisiForGeos.Tributo;

import org.apache.log4j.Logger;

import com.esed.pagopa.pdf.config.PropKeys;
import com.seda.commons.properties.tree.PropertiesTree;
import com.seda.payer.commons.geos.Bollettino;
import com.seda.payer.commons.geos.DatiAnagrafici;
import com.seda.payer.commons.geos.DatiCreditore;
import com.seda.payer.commons.geos.Documento;
import com.seda.payer.commons.geos.Flusso;

public final class ConvertiFile512FlussoGeos {

	private static Logger logger = Logger.getLogger(ConvertiFile512FlussoGeos.class);
	
	public ConvertiFile512FlussoGeos() {
	}

//	public static com.seda.payer.commons.geos.Flusso controllaFlusso(Object flussoDaControllare) throws ValidazioneException{
//		if(flussoDaControllare instanceof com.seda.payer.commons.geos.Flusso) {
//			return (com.seda.payer.commons.geos.Flusso) flussoDaControllare;
//		} else if(flussoDaControllare instanceof com.seda.payer.commons.inviaAvvisiForGeos.File512) {
//			return convertiFlusso((File512)flussoDaControllare);
//		} else throw new ValidazioneException("Flusso di tipo errato");
//	}

	public static com.seda.payer.commons.geos.Flusso convertiFlusso(File512 file512, PropertiesTree propertiesTree) {
		String tipoStampa = "";
		if(file512.tipoTemplate.equals("STANDARD_")) tipoStampa = "B";
		else tipoStampa = "P";
		System.out.println("istanzio il flusso con tipoStampa = " + tipoStampa);
		Flusso flusso = new Flusso(file512.cutecute, file512.ente, tipoStampa, file512.idFlusso);
		
		boolean usaDescTipServ = getUsaDescTipServProp(file512.cutecute, propertiesTree);
		
		file512.listaDocumenti.stream().forEach(doc512 -> flusso.addDocumento(convertiDocumento512(doc512, usaDescTipServ, file512.cutecute)));

		return flusso;
	}
	
	private static Documento convertiDocumento512(com.seda.payer.commons.inviaAvvisiForGeos.Documento doc512, boolean usaDescTipologiaServizio, String cuteCute) {
		logger.debug(doc512.toString());
		Documento documento = new Documento();
		documento.ImpostaServizio = "";
		//PAGONET-303 - inizio
		if (doc512.codiceImpostaServizio!=null && doc512.codiceImpostaServizio.trim().length()>0)
			documento.ImpostaServizio = doc512.codiceImpostaServizio;
		//PAGONET-303 - fine
		documento.NumeroDocumento = doc512.numero;
		documento.CausaleDocumento = cuteCute.equalsIgnoreCase("000P4") ? doc512.oggettoPagamento : doc512.causale;
		documento.ImportoDocumento = doc512.importoTotale;
		documento.CartellaRuoli = "";
		documento.BarcodeDocumento = doc512.codiceBarcode;
		documento.Iban = doc512.iban;
		documento.addDatiAnagrafici(convertiDatiAnagrafici512(doc512.debitore));
		documento.addDatiCreditore(creaDatiCreditore512(doc512, usaDescTipologiaServizio));
		doc512.listaTributi.stream().forEachOrdered(tributo -> documento.addElencoTributi(convertiTributo512(tributo))); 
		doc512.listaAvvisi.stream().forEachOrdered(rata -> documento.addDatiBollettino(convertiRata512(rata, doc512)));
		documento.addDatiBollettino(creaBollettino999(doc512));
		documento.QRcodeDocumento = doc512.codiceQRcode;
		return documento;
	}
	
	private static Bollettino convertiRata512(AvvisoRata avviso, com.seda.payer.commons.inviaAvvisiForGeos.Documento doc512) {
		Bollettino bollettino = new Bollettino(null, 
				doc512.descrizioneEnte, 
				doc512.debitore.flusso.ccp, 
				avviso.codiceIUV, 
				avviso.progressivoBollettino, 
				avviso.numeroAvviso, 
				avviso.dataScadenza, 
				avviso.importo, 
				doc512.debitore.flusso.codiceAutorizzazione, 
				avviso.codiceBarcode, 
				avviso.codiceQRcode);
		//bollettino.ScadenzaRata = convertiScadenza(avviso.dataScadenza);
		logger.debug(bollettino.toString());
		return bollettino;
	}
	
	private static Bollettino creaBollettino999(com.seda.payer.commons.inviaAvvisiForGeos.Documento doc512) {
		Bollettino bollettino = new Bollettino(null, 
				doc512.descrizioneEnte, 
				doc512.debitore.flusso.ccp, 
				doc512.codiceIUV, 
				999, 
				doc512.numAvvisoPagoPa, 
				doc512.listaAvvisi.get(0).dataScadenza, 
				doc512.importoTotale, 
				doc512.debitore.flusso.codiceAutorizzazione, 
				doc512.codiceBarcode, 
				doc512.codiceQRcode);
//		bollettino.ScadenzaRata = convertiScadenza(doc512.listaAvvisi.get(0).dataScadenza);
		return bollettino;
	}

	private static com.seda.payer.commons.geos.Tributo convertiTributo512(Tributo tributo512) {
		com.seda.payer.commons.geos.Tributo tributo = new 
				com.seda.payer.commons.geos.Tributo(Integer.toString(tributo512.anno), 
						tributo512.codiceTributo, 
						tributo512.importo, 
						tributo512.note);
		return tributo;
	}

	private static DatiCreditore creaDatiCreditore512(com.seda.payer.commons.inviaAvvisiForGeos.Documento doc512, boolean usaDescTipologiaServizio) {
		DatiCreditore creditore = new
				DatiCreditore(doc512.debitore.idDominio, //CF ente
						doc512.descrizioneEnte,
						usaDescTipologiaServizio ? doc512.descrizioneServizio: doc512.tipologiaServizio, //ufficio ente //TODO prendere descrizioneServizio in caso il usaDescTipServ sia true
						null, //indirizzo 
						null, //cap
						null, //città
						null, //provincia
						doc512.debitore.flusso.cbill
						);
		creditore.Denominazione3 = ""; // TODO: da valorizzare una volta disponibile il dato
		creditore.LogoEnte = ""; // TODO: da valorizzare una volta disponibile il dato
		return creditore;
	}

	private static DatiAnagrafici convertiDatiAnagrafici512(Debitore debitore512) {
		DatiAnagrafici debitore = new DatiAnagrafici(
				debitore512.nomeCognRagSoc.length() < 81? debitore512.nomeCognRagSoc : debitore512.nomeCognRagSoc.substring(0, 80),	//SPAG-1206 esteso limite da 34 a 80 in analogia al GEOS
				debitore512.codiceFiscale, 
				debitore512.indirizzo.length() < 41 ? debitore512.indirizzo : debitore512.indirizzo.substring(0, 40), 
				debitore512.cap, 
				debitore512.comune, 
				debitore512.provincia
				);

		return debitore;
	}

	
	private static boolean getUsaDescTipServProp(String cuteCute, PropertiesTree propertiesTree) {
		
		String usaDescTipServ = null;
		if (propertiesTree != null) {
			usaDescTipServ = propertiesTree.getProperty(PropKeys.usaDescTipServ.format(cuteCute));
		}
		return usaDescTipServ != null && usaDescTipServ.equals("1");
	}
}

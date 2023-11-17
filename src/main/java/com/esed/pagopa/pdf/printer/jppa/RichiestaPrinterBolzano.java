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
public final class RichiestaPrinterBolzano implements RequestBuilder {

	
	private final StampaBollettinoRichiesta bollRichiesta = 
			new StampaBollettinoRichiesta();
	
	private final PosizioneDebitoriaAvvisaturaDto posDeb  = 
			new PosizioneDebitoriaAvvisaturaDto();
	
	private final DatiEnteAvvisaturaDto datiEnte = 
			new DatiEnteAvvisaturaDto();
	
	private Documento doc;
	
	/**
	 * Richiesta bolzano
	 */
	public RichiestaPrinterBolzano(Documento doc) {
		this.doc = doc;
	}

	@Override
	public int compareTo(StampaBollettinoRichiesta o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setTitDebitoCapRes(String titDeb) {
		posDeb.setTitDebitoCapRes(titDeb);
	}

	@Override
	public void setDataScadenza(String dataScadenza) {
		posDeb.setDataScadenza(dataScadenza);

	}

	@Override
	public void setTitDebitoCapSedeLegale(String sedLeg) {
		posDeb.setTitDebitoCapSedeLegale(sedLeg);

	}

	@Override
	public void setTitDebitoCivicoRes(String s) {
		posDeb.setTitDebitoCivicoRes(s);

	}

	@Override
	public void setTitDebitoCivicoSedeLegale(String s) {
		posDeb.setTitDebitoCivicoSedeLegale(s);

	}

	@Override
	public void setTitDebitoCognome(String s) {
		posDeb.setTitDebitoCognome(s);

	}

	@Override
	public void setTitDebitoComuneRes(String s) {
		posDeb.setTitDebitoComuneRes(s);

	}

	@Override
	public void setTitDebitoComuneSedeLegale(String s) {
		posDeb.setTitDebitoComuneSedeLegale(s);

	}

	@Override
	public void setTitDebitoIndirizzoRes(String s) {
		posDeb.setTitDebitoIndirizzoRes(s);

	}

	@Override
	public void setTitDebitonazioneRes(String s) {
		posDeb.setTitDebitonazioneRes(s);

	}

	@Override
	public void setTitDebitoNazioneSedeLegale(String s) {
		posDeb.setTitDebitoNazioneSedeLegale(s);

	}

	@Override
	public void setTitDebitoNome(String s) {
		posDeb.setTitDebitoNome(s);

	}

	@Override
	public void setTitDebitoNominativo(String s) {
		posDeb.setTitDebitoNominativo(s);

	}

	@Override
	public void setTitDebitoprovRes(String s) {
		posDeb.setTitDebitoprovRes(s);

	}

	@Override
	public void setTitDebitoProvSedeLegale(String s) {
		posDeb.setTitDebitoProvSedeLegale(s);

	}

	@Override
	public void setTitDebitoRagioneSociale(String s) {
		posDeb.setTitDebitoRagioneSociale(s);

	}

	@Override
	public void setTitDebitoIndirizzoSedeLegale(String s) {
		posDeb.setTitDebitoIndirizzoSedeLegale(s);

	}

	@Override
	public void addPosizioneDebitoriaItem(PosizioneDebitoriaAvvisaturaDto posDeb) {
		bollRichiesta.addPosizioneDebitoriaItem(posDeb);

	}
	
	
	@Override
	public void setCodiceFiscale(String s) {
		datiEnte.codiceFiscale(s);
		
	}

	@Override
	public void setCodiceInterbancario(String s) {
		datiEnte.codiceInterbancario(s);
		
	}

	@Override
	public void setCpAbilitato(boolean b) {
		datiEnte.setCpAbilitato(false);
		
	}

	@Override
	public void cpAutorizzazione(String s) {
		datiEnte.cpAutorizzazione(s);
		
	}

	@Override
	public void setCpIntestatario(String s) {
		datiEnte.setCpIntestatario(s);
		
	}

	@Override
	public void setCpIntestatarioDe(String s) {
		datiEnte.setCpIntestatarioDe(s);
		
	}

	@Override
	public void setCpNumero(String s) {
		datiEnte.setCpNumero(s);
		
	}

	@Override
	public void setSettore(String s) {
		datiEnte.setSettore(s);
		
	}

	@Override
	public void setNome(String s) {
		datiEnte.setNome(s);
		
	}
	
	@Override
	public void setNomeDe(String s) {
		datiEnte.setNomeDe(s);
		
	}

	@Override
	public void datiEnte(DatiEnteAvvisaturaDto datiEnte) {
		bollRichiesta.datiEnte(datiEnte);

	}

	@Override
	public void setNumeroAvviso(String s) {
		bollRichiesta.numeroAvviso(s);

	}
	
	
	public StampaBollettinoRichiesta build() {
		return bollRichiesta;
	}

	@Override
	public void setCausaleDebitoria(String s) {
		gestisciCausale(s);
		posDeb.setCausaleDebitoria(s);
	}
	

	private void gestisciCausale(String s) {
		
		String primaparte[] = new String[2];

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
		
				if(prima[0]==null||prima[1]==null) {
					posDeb.setCausaleDebitoriaDe("");
				}else {
					posDeb.setCausaleDebitoriaDe(prima[0].replaceAll("(\\r|\\n)", "")+"/"+prima[1].replaceAll("(\\r|\\n)", ""));
				}
				
			  if(primaparte[1]==null) {
				posDeb.setCausaleDebitoria(doc.CausaleDocumento);
			  }else {
				  posDeb.setCausaleDebitoria(primaparte[1].replaceAll("(\\r|\\n)", ""));
			  }
		}
		
		posDeb.setCausaleDebitoria(doc.CausaleDocumento);
	}
	

	@Override
	public void setCausaleDebitoriaDe(String s) {
		posDeb.setCausaleDebitoriaDe(s);
		
	}

}

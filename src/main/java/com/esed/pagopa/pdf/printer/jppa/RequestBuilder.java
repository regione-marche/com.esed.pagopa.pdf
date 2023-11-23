package com.esed.pagopa.pdf.printer.jppa;

import it.maggioli.pagopa.jppa.printer.model.DatiEnteAvvisaturaDto;
import it.maggioli.pagopa.jppa.printer.model.PosizioneDebitoriaAvvisaturaDto;
import it.maggioli.pagopa.jppa.printer.model.StampaBollettinoRichiesta;

public interface RequestBuilder extends Comparable<StampaBollettinoRichiesta> {

	void setCausaleDebitoria(String s);
	void setCausaleDebitoriaDe(String s);
	void setTitDebitoCapRes(String titDeb);
	void setDataScadenza(String dataScadenza);
	void setTitDebitoCapSedeLegale(String sedLeg);
	void setTitDebitoCivicoRes(String s);
	void setTitDebitoCivicoSedeLegale(String s);
	void setTitDebitoCognome(String s);
	void setTitDebitoComuneRes(String s);
	void setTitDebitoComuneSedeLegale(String s);
	void setTitDebitoIndirizzoRes(String s);
	void setTitDebitonazioneRes(String s);
	void setTitDebitoNazioneSedeLegale(String s);
	void setTitDebitoNome(String s);
	void setTitDebitoNominativo(String s);
	void setTitDebitoprovRes(String s);
	void setTitDebitoProvSedeLegale(String s);
	void setTitDebitoRagioneSociale(String s);
	void setTitDebitoIndirizzoSedeLegale(String s);
	
	
	void setCodiceFiscale(String s);
	void setCodiceInterbancario(String s);
	void setCpAbilitato(boolean b);
	void cpAutorizzazione(String s);
	void setCpIntestatario(String s);
	void setCpIntestatarioDe(String s);
	void setCpNumero(String s);
	void setSettore(String s);
	void setNome(String s);
	void setNomeDe(String s);
	
	
	void addPosizioneDebitoriaItem(PosizioneDebitoriaAvvisaturaDto posDeb);
	
	void datiEnte(DatiEnteAvvisaturaDto datiEnte);
	
	void setNumeroAvviso(String s);
	
}

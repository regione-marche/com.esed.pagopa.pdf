/**
 * 
 */
package com.esed.pagopa.pdf.printer.jppa;

import it.maggioli.pagopa.jppa.printer.model.StampaAvvisaturaRichiesta;
import it.maggioli.pagopa.jppa.printer.model.StampaBollettinoRichiesta;
import it.maggioli.pagopa.jppa.printer.model.StampaBollettinoRisposta;

/**
 * 
 */
public class StampaPdfJppaPagonet implements StampaPdfJppa {
	
	private IconnettoreJppaPrinter connettorePrinter;
	
	public StampaPdfJppaPagonet(String ... strings) {
		this.connettorePrinter = new ConnettorePrinterJppa(strings);
	}

	public IconnettoreJppaPrinter getConnettorePrinter() {
		return this.connettorePrinter;
	}
	
	@Override
	public void stampaPdf(StampaAvvisaturaRichiesta richiesta) {
		this.connettorePrinter.stampaAvvisatura(richiesta);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		StampaPdfJppaPagonet stampa = new StampaPdfJppaPagonet("","");
		System.out.println(stampa.getConnettorePrinter().getToken());
	}

	@Override
	public StampaBollettinoRisposta stampaBolpuntuale(StampaBollettinoRichiesta bolRichiesta) {
		return this.connettorePrinter.stampaBol(bolRichiesta);
	}

}

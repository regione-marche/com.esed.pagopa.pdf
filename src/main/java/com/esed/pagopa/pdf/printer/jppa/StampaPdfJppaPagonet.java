/**
 * 
 */
package com.esed.pagopa.pdf.printer.jppa;

import java.nio.file.Path;

import com.seda.payer.commons.geos.Bollettino;
import com.seda.payer.commons.geos.Documento;
import com.seda.payer.commons.jppa.ConnettorePrinterJppa;
import com.seda.payer.commons.jppa.interfaces.IconnettoreJppaPrinter;

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
		StampaPdfJppaPagonet stampa = new StampaPdfJppaPagonet("admin","password");
		System.out.println(stampa.getConnettorePrinter().getToken());
	}

	@Override
	public StampaBollettinoRisposta stampaBolpuntuale(StampaBollettinoRichiesta bolRichiesta) {
		return this.connettorePrinter.stampaBol(bolRichiesta);
	}

}

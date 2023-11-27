/**
 * 
 */
package com.esed.pagopa.pdf.printer.jppa;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.seda.commons.logger.CustomLoggerManager;
import com.seda.commons.logger.LoggerWrapper;
import com.seda.payer.commons.jppa.interfaces.IconnettoreJppaPrinter;

import it.maggioli.pagopa.jppa.printer.ApiClient;
import it.maggioli.pagopa.jppa.printer.model.StampaAvvisaturaRichiesta;
import it.maggioli.pagopa.jppa.printer.model.StampaAvvisaturaRisposta;
import it.maggioli.pagopa.jppa.printer.model.StampaBollettinoRichiesta;
import it.maggioli.pagopa.jppa.printer.model.StampaBollettinoRisposta;

/**
 * 
 */
public class ConnettorePrinterJppa implements IconnettoreJppaPrinter {

	protected LoggerWrapper logger = CustomLoggerManager.get(getClass());
	private String token = "";
	
	/**
	 * Costruttore che esegue il login al printer
	 */
	public ConnettorePrinterJppa(String ... strings) {
		this.token = this.loginPrinter(strings); 
	}
	
	@Override
	public IconnettoreJppaPrinter build(String ... strings) {
		return new ConnettorePrinterJppa(strings);
	}



	@Override
	public String getToken() {
		return this.token;
	}



	@Override
	public StampaAvvisaturaRisposta stampaAvvisatura(StampaAvvisaturaRichiesta stampaAvvisaturaRichiesta) {
		return this.stampaAvviso(stampaAvvisaturaRichiesta);
	}
	
	
	public static void main(String ... args ) {
		ConnettorePrinterJppa conn = new ConnettorePrinterJppa(args);
	}

	@Override
	public StampaBollettinoRisposta stampaBol(StampaBollettinoRichiesta bolRichiesta) {
		return this.stampaBollettino(bolRichiesta,this.token);
	}

}

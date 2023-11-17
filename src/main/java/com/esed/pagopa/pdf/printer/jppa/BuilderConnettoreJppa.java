/**
 * 
 */
package com.esed.pagopa.pdf.printer.jppa;

import com.seda.payer.commons.jppa.interfaces.IConnettoreJPPA;
import com.seda.payer.commons.jppa.interfaces.IconnettoreJppaPrinter;

/**
 * 
 */
public abstract class BuilderConnettoreJppa {

	public IConnettoreJPPA createConnettoreDate(org.threeten.bp.OffsetDateTime dataInizio,
			org.threeten.bp.OffsetDateTime dataFine,String ... args) {
		IConnettoreJPPA connettore = creaConnettoreDate(dataInizio,dataFine,args);
		return connettore.buildDate();
	}
	
	
	public IconnettoreJppaPrinter createConnettorePrinter(String ... strings) {
		IconnettoreJppaPrinter connettorePrinter = creaConnettorePrinter(strings);
		return connettorePrinter.build();
	}
	
	
	protected abstract IConnettoreJPPA creaConnettoreDate(org.threeten.bp.OffsetDateTime dataInizio,
			org.threeten.bp.OffsetDateTime dataFine,String ... args);
	
	
	protected abstract IconnettoreJppaPrinter creaConnettorePrinter(String ... strings );

}

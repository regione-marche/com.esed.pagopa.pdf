/**
 * 
 */
package com.esed.pagopa.pdf.printer.jppa;

import org.threeten.bp.OffsetDateTime;

import com.seda.payer.commons.jppa.abstractClass.BuilderConnettoreJppa;
import com.seda.payer.commons.jppa.interfaces.IConnettoreJPPA;
import com.seda.payer.commons.jppa.interfaces.IconnettoreJppaPrinter;

/**
 * 
 */
public class BaseClientFactory extends BuilderConnettoreJppa {
	
	@Override
	protected IConnettoreJPPA creaConnettoreDate(OffsetDateTime dataInizio, OffsetDateTime dataFine, String... args) {
		return super.createConnettoreDate(dataInizio, dataFine, args);
	}

	@Override
	protected IconnettoreJppaPrinter creaConnettorePrinter(String... strings) {
		return new ConnettorePrinterJppa(strings);
	}

}

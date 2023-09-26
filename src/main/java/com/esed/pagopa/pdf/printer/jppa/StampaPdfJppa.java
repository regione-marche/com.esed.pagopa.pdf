package com.esed.pagopa.pdf.printer.jppa;

import com.seda.payer.commons.geos.Bollettino;
import com.seda.payer.commons.geos.Documento;

import it.maggioli.pagopa.jppa.printer.model.StampaAvvisaturaRichiesta;
import it.maggioli.pagopa.jppa.printer.model.StampaBollettinoRichiesta;
import it.maggioli.pagopa.jppa.printer.model.StampaBollettinoRisposta;

public interface StampaPdfJppa {
	
	void stampaPdf(StampaAvvisaturaRichiesta richiesta);

	StampaBollettinoRisposta stampaBolpuntuale(StampaBollettinoRichiesta bolRichiesta);

}

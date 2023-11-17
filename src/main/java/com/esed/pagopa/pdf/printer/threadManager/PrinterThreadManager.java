package com.esed.pagopa.pdf.printer.threadManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.esed.pagopa.pdf.printer.jppa.StampaPdfJppaPagonet;
import com.seda.payer.commons.geos.Documento;

import it.maggioli.pagopa.jppa.printer.model.StampaBollettinoRichiesta;
import it.maggioli.pagopa.jppa.printer.model.StampaBollettinoRisposta;

public abstract class PrinterThreadManager {
	
	protected abstract void initilizeMassivePrint();

	protected abstract List<StampaBollettinoRisposta> computePrint() throws InterruptedException, ExecutionException;

}
















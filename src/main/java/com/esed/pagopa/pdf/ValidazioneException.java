package com.esed.pagopa.pdf;

import org.apache.log4j.Logger;

public class ValidazioneException extends Exception {
	 
	private static Logger logger = Logger.getLogger(ValidazioneException.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 5532002375566753262L;

	public ValidazioneException(String messaggiErrore) {
		super(messaggiErrore);
		logger.info(messaggiErrore);
		
	}

}

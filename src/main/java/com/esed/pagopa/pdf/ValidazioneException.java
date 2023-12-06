package com.esed.pagopa.pdf;

import com.seda.commons.logger.CustomLoggerManager;
import com.seda.commons.logger.LoggerWrapper;

public class ValidazioneException extends Exception {
	 
	private static LoggerWrapper logger = CustomLoggerManager.get(ValidazioneException.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 5532002375566753262L;

	public ValidazioneException(String messaggiErrore) {
		super(messaggiErrore);
		logger.info(messaggiErrore);
		
	}

}

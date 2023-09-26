package com.esed.pagopa.pdf.config;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public enum PropKeys {
	tipoStampa,
	usaDescTipServ,
	pathLogoEnte,
	pathLogoEnteSizeX,
	pathLogoEnteSizeY,
	utenteJppa,
	passwordJppa
	;

    private static ResourceBundle rb;

    public String format( Object... args ) {
        synchronized(PropKeys.class) {
            if(rb==null)
            	rb = ResourceBundle.getBundle(PropKeys.class.getName());
            return MessageFormat.format(rb.getString(name()),args);
        }
    }
}
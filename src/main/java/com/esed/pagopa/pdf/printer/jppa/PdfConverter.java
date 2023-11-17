/**
 * 
 */
package com.esed.pagopa.pdf.printer.jppa;

import java.io.File;
import java.nio.file.Path;

/**
 * Converte una stringa base64 in pdf
 */
@FunctionalInterface
public interface PdfConverter {
	
	/**
	 * 
	 * @param base64
	 * @return un pdf convertito da base64
	 */
	String convert(String base64,Path path);

}

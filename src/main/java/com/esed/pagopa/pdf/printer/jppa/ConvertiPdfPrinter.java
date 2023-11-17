/**
 * 
 */
package com.esed.pagopa.pdf.printer.jppa;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Base64;

/**
 * 
 */
public class ConvertiPdfPrinter implements PdfConverter {

	
	private OpenOption[] options = new OpenOption[] { WRITE, CREATE_NEW, APPEND };
	/**
	 * 
	 */
	public ConvertiPdfPrinter() {
	}

	@Override
	public String convert(String base64,Path path) {
		Path ret = null;
		try {
			ret = Files.write(path,Base64.getDecoder().decode(base64.getBytes()),options[1],options[0],options[2]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret.toString();
	}

}

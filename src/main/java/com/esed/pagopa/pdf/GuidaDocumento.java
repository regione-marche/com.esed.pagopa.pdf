/**
 * 
 */
package com.esed.pagopa.pdf;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.io.FileUtils;

import com.seda.commons.logger.CustomLoggerManager;
import com.seda.commons.logger.LoggerWrapper;

/**
 * @apiNote classe per la generazione del file guida, viene restituita in risposta all'aggiunta di un documento al pdf
 *
 */
public class GuidaDocumento {
	
//	private String nomeFileDestinazione;
//	private String nomeFileOrigine;
	private int numeroPaginaIniziale;
	private File file;
	
	private static LoggerWrapper logger = CustomLoggerManager.get(GuidaDocumento.class);
	
	public GuidaDocumento(/*String nomeFile, */File file) {
//		nomeFileDestinazione = nomeFile;
		this.file = file;
		this.numeroPaginaIniziale = 1;
	}
 
	//PAGONET-303 - inizio
	//public void aggiungiRigo(String CFdebitore, int anno, String codiceFile, String nomeFileDestinazione, String nomeFileOrigine/*, int numeroPaginaIniziale, int numeroPaginaFinale*/, int pagineAggiunteDocumento) throws IOException {
	public void aggiungiRigo(String CFdebitore, int anno, String codiceFile, String nomeFileDestinazione, String nomeFileOrigine/*, int numeroPaginaIniziale, int numeroPaginaFinale*/, int pagineAggiunteDocumento, String codiceImpostaServizio) throws IOException {
	//PAGONET-303 - fine
		
		Date dataGenerazione = Calendar.getInstance().getTime();  
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");  
		String strDataGenerazione = dateFormat.format(dataGenerazione);  
		
		// il nomeFileOrigine lo fornisce il metodo getFileName di File512
		
		StringBuilder rigoGuida = new StringBuilder();
		rigoGuida.append(CFdebitore);
		rigoGuida.append(";");
		rigoGuida.append(anno);
		rigoGuida.append(";");
		rigoGuida.append(codiceFile);
		rigoGuida.append(";");
		rigoGuida.append(nomeFileDestinazione);
		rigoGuida.append(";");
		rigoGuida.append(nomeFileOrigine);
		rigoGuida.append(";");
		rigoGuida.append(strDataGenerazione);
		rigoGuida.append(";");
		rigoGuida.append(this.numeroPaginaIniziale);
		rigoGuida.append(";");
		rigoGuida.append(this.numeroPaginaIniziale + pagineAggiunteDocumento - 1);
		//PAGONET-303- inizio
		rigoGuida.append(";");
		rigoGuida.append(codiceImpostaServizio);
		//PAGONET-303 - fine
		rigoGuida.append("\r\n");
		
		FileUtils.writeStringToFile(this.file , rigoGuida.toString(), StandardCharsets.UTF_8, true);
		logger.info("aggiungo rigo al file guida");
		logger.info(rigoGuida.toString());
		
		this.numeroPaginaIniziale += pagineAggiunteDocumento;
	}
	
}

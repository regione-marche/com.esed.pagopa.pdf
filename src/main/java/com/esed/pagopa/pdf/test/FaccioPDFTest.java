package com.esed.pagopa.pdf.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import com.esed.pagopa.pdf.SalvaPDF;
import com.esed.pagopa.pdf.SalvaPDFBolzano;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seda.commons.logger.CustomLoggerManager;
import com.seda.commons.logger.LoggerWrapper;
import com.seda.commons.properties.PropertiesLoader;
import com.seda.commons.properties.tree.PropertiesNodeException;
import com.seda.commons.properties.tree.PropertiesTree;
import com.seda.payer.commons.inviaAvvisiForGeos.File512;
import com.seda.payer.commons.jppa.utils.ConvertiPdfPrinter;
import com.seda.payer.commons.jppa.utils.PdfConverter;

import io.swagger.client.ApiException;

public class FaccioPDFTest {
	
	protected LoggerWrapper logger = CustomLoggerManager.get(getClass());
	
	public static void main(String[] args) throws Exception {
        String rootPath = "C:\\work\\Pagonet\\ConfigFiles\\pagopaPDFws\\pagoPaPdfWsRoot.properties";
		
		PropertiesTree propertiesTree = null;

		
		try {
			propertiesTree = new PropertiesTree(PropertiesLoader.load(rootPath));
		} catch (IOException | PropertiesNodeException e) {
			throw new RuntimeException("Impossibile caricare il file di configurazione", e);
		}
		
		String cuteCute;
//		cuteCute = "000RM";
//		cuteCute = "000TO";
		cuteCute = "000P6";
		
//		puntuale(propertiesTree, cuteCute);

		massivo(propertiesTree);
//		puntuale(propertiesTree,cuteCute);
	}

	static void puntuale(PropertiesTree propertiesTree, String cuteCute) throws Exception {
		
		System.out.println("directory = " + System.getProperty("user.dir"));
		FlussoFinto flusso = new FlussoFinto();
		flusso.TipoStampa = "jppa";
		
		//SalvaPDF salvaPDF = new SalvaPDF(propertiesTree);
		SalvaPDFBolzano salvaPdfBolzano = new SalvaPDFBolzano();
		byte[] ba = null;
//		try {
//			ba = salvaPdfBolzano.SalvaFile(flusso,propertiesTree);
//		} catch (ApiException e) {
//	          System.err.println("Exception when calling BollettinoApi#stampaBollettino");
//	          System.err.println("Status code: " + e.getCode());
//	          System.err.println("Reason: " + e.getResponseBody());
//	          System.err.println("Response headers: " + e.getResponseHeaders());
//	          e.printStackTrace();
//		}
		
		if(flusso.TipoStampa.equals("jppa")) {
			
	        byte[] encode = Base64.getDecoder().decode(Base64.getEncoder().encode(ba));
	        String result = new String(encode);
			
			PdfConverter converter = new ConvertiPdfPrinter();
			converter.convert(result, Paths.get("C:\\work\\Pagonet\\ConfigFiles\\pagopaPDFws\\pagopaPdf\\"+cuteCute+".pdf"));
		}
		else {
		String ran = String.valueOf(System.currentTimeMillis()).substring(0, 10);
		FileUtils.writeByteArrayToFile(new File("C:\\work\\Pagonet\\ConfigFiles\\pagopaPDFws\\pagopaPdf", "avviso" + flusso.CuteCute + ran + ".pdf"), ba);
		}
	}
	
	static void massivo(PropertiesTree propertiesTree) throws JsonParseException, JsonMappingException, IOException {
		System.out.println(System.getProperty("user.dir"));
		ObjectMapper objectMapper = new ObjectMapper();
		List<File512> listaFile512 = objectMapper.readValue(new File("src\\main\\java\\com\\esed\\pagopa\\pdf\\test\\File512rate.json"), new TypeReference<List<File512>>(){} );
		
		UUID uuid = UUID.randomUUID();
		SalvaPDF salvaPDF = new SalvaPDF(propertiesTree);
		salvaPDF.SalvaFileMassivo(uuid, listaFile512, "C:/work/Pagonet/ConfigFiles/pagopaPDFws/pagopaPdf/");
	}
}


// SalvaFileMassivo(UUID uuid, List<File512> listaFile512, String path)
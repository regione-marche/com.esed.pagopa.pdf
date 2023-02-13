package com.esed.pagopa.pdf.test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import com.esed.pagopa.pdf.SalvaPDF;
import com.esed.pagopa.pdf.ValidazioneException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seda.commons.properties.PropertiesLoader;
import com.seda.commons.properties.tree.PropertiesNodeException;
import com.seda.commons.properties.tree.PropertiesTree;
import com.seda.payer.commons.inviaAvvisiForGeos.File512;

public class FaccioPDFTest {
	
	public static void main(String[] args) throws IOException, ValidazioneException {
		String rootPath = "D:/ConfigFiles/Payer/dannibale/payerPagoPaPdfWS/pagoPaPdfWsRoot.properties";
		
		PropertiesTree propertiesTree = null;

		
		try {
			propertiesTree = new PropertiesTree(PropertiesLoader.load(rootPath));
		} catch (IOException | PropertiesNodeException e) {
			throw new RuntimeException("Impossibile caricare il file di configurazione", e);
		}
		
		String cuteCute;
//		cuteCute = "000RM";
//		cuteCute = "000TO";
		cuteCute = "000P4";
		
//		puntuale(propertiesTree, cuteCute);

		massivo(propertiesTree);
	}

	static void puntuale(PropertiesTree propertiesTree, String cuteCute) throws IOException, ValidazioneException {
		
		System.out.println("directory = " + System.getProperty("user.dir"));
		FlussoFinto flusso = new FlussoFinto();
//		
		SalvaPDF salvaPDF = new SalvaPDF(propertiesTree);
		byte[] ba = salvaPDF.SalvaFile(flusso);
		
		String ran = String.valueOf(System.currentTimeMillis()).substring(0, 10);
		FileUtils.writeByteArrayToFile(new File("D:/FileTemporanei/dannibale/pagopaPdf", "avviso" + flusso.CuteCute + ran + ".pdf"), ba);

	}
	
	static void massivo(PropertiesTree propertiesTree) throws JsonParseException, JsonMappingException, IOException {
		System.out.println(System.getProperty("user.dir"));
		ObjectMapper objectMapper = new ObjectMapper();
		List<File512> listaFile512 = objectMapper.readValue(new File("src\\main\\java\\com\\esed\\pagopa\\pdf\\test\\File512rate.json"), new TypeReference<List<File512>>(){} );
		
		UUID uuid = UUID.randomUUID();
		SalvaPDF salvaPDF = new SalvaPDF(propertiesTree);
		salvaPDF.SalvaFileMassivo(uuid, listaFile512, "D:/FileTemporanei/dannibale/pagopaPdf");
	}
}


// SalvaFileMassivo(UUID uuid, List<File512> listaFile512, String path)
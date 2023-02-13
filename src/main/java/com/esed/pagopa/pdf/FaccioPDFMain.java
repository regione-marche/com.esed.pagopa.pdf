package com.esed.pagopa.pdf;

import java.io.File;
import java.io.IOException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.FileUtils;

import com.esed.pagopa.pdf.test.FlussoFinto;

public class FaccioPDFMain {

	public static void main(String[] args) throws IOException, ValidazioneException {
		//inizio LP PG21X007
		String dns = args[0];		
		String porta = args[1];
		if(dns == null || dns.trim().length() == 0) {
			dns = "http://svwsesed02.seda.intra";
		}
		if(porta == null || porta.trim().length() == 0) {
			porta = "8080";
		}
		System.out.println("dns = " + dns);
		System.out.println("porta = " + porta);
		//fine LP PG21X007
		System.out.println("directory = " + System.getProperty("user.dir"));
		FlussoFinto flusso = new FlussoFinto();

		
		
//		flusso = null;
//		System.gc();
//		ObjectMapper objectMapper = new ObjectMapper();
//		List<File512> flusso512 = objectMapper.readValue(new File("src/main/java/com/esed/pagopa/pdf/test/File512corto.json"), new TypeReference<List<File512>>(){});
//		if (SalvaPDF.SalvaFileMassivo(UUID.randomUUID(), flusso512, "pdfExample/") == 0) {
//			logger.info("PDF generato correttamente");
//		} else {
//			logger.info("errore nella generazione del PDF");
//			
//		}
//		
//		byte[] ba = SalvaPDF.SalvaFile(flusso);
//		FileUtils.writeByteArrayToFile(new File("pdfExample", "avviso.pdf"), ba);

		
		Client client = ClientBuilder.newClient();

		//inizio LP PG21X007
		//Response response = client.target("http://svwsesed02.seda.intra:8080/PagoPAPdfService/v1/resource/pdf")
		Response response = client.target(dns + ":" + porta + "/PagoPAPdfService/v1/resource/pdf")
		//fine LP PG21X007
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(flusso, MediaType.APPLICATION_JSON));
		System.out.println("responseWS = " + response.getStatus());
		//inizio LP PG21X007
		System.out.println("responseWS Info = " + response.getStatusInfo());
		if(response.getStatus() == Status.OK.getStatusCode()) {
		//fine LP PG21X007
			byte[] ba = response.readEntity(byte[].class);
			FileUtils.writeByteArrayToFile(new File("pdfExample", "avviso.pdf"), ba);
		//inizio LP PG21X007
		}
		/*
		com.esed.log.req.dati.LogRequest flusso1 = new LogRequest();
		flusso1.setAction("prova.do");
		flusso1.setApplicativo("manager");
		flusso1.setOperatoreUltimoAggiornamento("flussoProva");
		//flusso.setSezioneApplicativa("");
		flusso1.setRequest("login.do?prova=1");
		flusso1.setTipoRequest("GET");
		flusso1.setUrlChiamante("127.0.0.1");
		javax.ws.rs.client.WebTarget target = client.target(dns + ":" + porta + "/PagoPAPdfService/v1/resource/salvareq");
		System.out.println("2v1 target uri= " + target.getUri());
		javax.ws.rs.client.Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON);
		response = builder.post(Entity.entity(flusso1, MediaType.APPLICATION_JSON));
		System.out.println("2v1 responseWS = " + response.getStatus());
		System.out.println("2v1 responseWS info = " + response.getStatusInfo());
		if(response.getStatus() == Status.OK.getStatusCode()) {
			Integer ba = response.readEntity(Integer.class);
			System.out.println("esito = " + ba);
		}
		*/
		//fine LP PG21X007
	}
}

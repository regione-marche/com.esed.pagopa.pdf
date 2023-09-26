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
	}
}

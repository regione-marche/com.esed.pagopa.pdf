/**
 * 
 */
package com.esed.pagopa.pdf.printer.jppa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.threeten.bp.OffsetDateTime;

import com.seda.commons.logger.CustomLoggerManager;
import com.seda.commons.logger.LoggerWrapper;
import com.seda.payer.commons.jppa.interfaces.IConnettoreJPPA;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.Configuration;
import io.swagger.client.api.FlussiDiRendicontazioneApi;
import io.swagger.client.auth.ApiKeyAuth;
import io.swagger.client.model.RichiestaInfoListaFlussiRendicontazioneDto;
import io.swagger.client.model.RichiestaInfoListaFlussiRendicontazioneDto.CodiceServizioEnum;
import io.swagger.client.model.RispostaInfoListaFlussiRendicontazioneDto;
import io.swagger.client.model.RispostaScaricaFlussiAgidDto;

/**
 * Connettore che esegue una chiamata alle api di JPPA per recuperare i flussi
 */
public class ConnettoreJPPA implements IConnettoreJPPA {
	
	protected LoggerWrapper logger = CustomLoggerManager.get(getClass());
	// args[0] user,  args[1] pw,  args[2] cutecute
	private String authToken = "";
	private List<String> retFlussi = null;
	private String codiceIpa = "";
	private String[]args;
	private org.threeten.bp.OffsetDateTime dataInizio = org.threeten.bp.OffsetDateTime.now().minusDays(50);
	private org.threeten.bp.OffsetDateTime dataFine = org.threeten.bp.OffsetDateTime.now();
	
	
	public ConnettoreJPPA(String ... args) {
		this.authToken = loginJPPA(args);
		this.args = args;
	}
	
	/**
	 * Costruttore usato per instanzioare le date e i vari parametri per il login
	 * @param dataInizio
	 * @param dataFine
	 * @param args
	 */
	public ConnettoreJPPA(org.threeten.bp.OffsetDateTime dataInizio, 
			org.threeten.bp.OffsetDateTime dataFine, String ... args) {
			
		if(dataInizio==null || dataFine==null) {
			List<String> dateFormattate = formattaDate();
			
		    this.dataInizio = org.threeten.bp.OffsetDateTime.parse(dateFormattate.get(0));
		    this.dataFine = org.threeten.bp.OffsetDateTime.parse(dateFormattate.get(1));
		    
			this.authToken = loginJPPA(args);
			this.codiceIpa = args[2];
		} else {
			this.authToken = loginJPPA(args);
			this.dataInizio = dataInizio;
			this.dataFine = dataFine;
			this.codiceIpa = args[2];
		}

	}
	
	private List<String> formattaDate() {
		
		  List<String> date = new ArrayList<>();

	      String inizio = this.dataInizio.toString();
		  String fine = this.dataFine.toString();
			
		  inizio = inizio.substring(0,23);
		  inizio+="Z";
			
		  fine = fine.substring(0,23);
		  fine+="Z";
			
		  date.add(inizio);
		  date.add(fine);
		
		return date;
	
  }

	@Override
	public String getToken() {
		return this.authToken;
	}
	
	protected String getAuthString() {
		return this.authToken;
	}
	
	protected String getCodiceIpa() throws Exception {
		return this.codiceIpa;
	}

	
	private String loginJPPA(String ... args) {
		this.authToken = this.loginJppa(args);
		return this.authToken;
	}
	
	private RispostaInfoListaFlussiRendicontazioneDto richiediFlussi(String authToken) throws NullPointerException {
		return this.richiestaInfoListaFlussiRendicontazioneDto(authToken);
	}
	
	@Override
	public List<String> getListaFlussi() throws ApiException, NullPointerException {
		List<String> ret = richiediFlussi(this.authToken).getListaFlussiRendicontazione();
		this.retFlussi = ret;
		return ret;
	}
	
	
	private RispostaInfoListaFlussiRendicontazioneDto richiestaInfoListaFlussiRendicontazioneDto(String authToken) {
		
		ApiClient defaultClient = Configuration.getDefaultApiClient();
		RispostaInfoListaFlussiRendicontazioneDto result = null;
		
        ApiKeyAuth jwtToken = (ApiKeyAuth) defaultClient.getAuthentication("jwtToken");
        jwtToken.setApiKey("Bearer " + authToken);
		
		FlussiDiRendicontazioneApi apiInstance = new FlussiDiRendicontazioneApi();
		RichiestaInfoListaFlussiRendicontazioneDto richiestaInfoListaFlussiRendicontazioneDto = new RichiestaInfoListaFlussiRendicontazioneDto(); // RichiestaInfoListaFlussiRendicontazioneDto | richiestaInfoListaFlussiRendicontazioneDto
		
		try {
			richiestaInfoListaFlussiRendicontazioneDto.setCodIpaRichiedente(this.codiceIpa);
			richiestaInfoListaFlussiRendicontazioneDto.setCodiceIpaBeneficiario(this.codiceIpa);
			richiestaInfoListaFlussiRendicontazioneDto.dataInizioRicerca(this.dataInizio);
			richiestaInfoListaFlussiRendicontazioneDto.dataFineRicerca(this.dataFine);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
		    result = apiInstance.postInfoUsingPOST1(richiestaInfoListaFlussiRendicontazioneDto);
		    System.out.println(result);
		} catch (ApiException | NullPointerException e) {
		    System.err.println("Exception when calling FlussiDiRendicontazioneApi#postInfoUsingPOST1");
		    e.printStackTrace();
		}

		return result;
	}
	
	@Override
	public List<String> getFlussi() {
		return this.retFlussi;
	}
	
	
	
	/**
	 * @param args
	 * @throws JndiProxyException 
	 * @throws ApiException 
	 * @throws NullPointerException 
	 * @throws FacadeException 
	 * @throws QuadraturaException 
	 */
	public static void main(String[] args) throws NullPointerException, ApiException {
		String user = "";
		String pw = "";
		args = new String[3];
		args[0] = user;
		args[1] = pw;
		
		//DbUtiljppa dbUtilJppa = new DbUtiljppa(null,"");
		
		//ConnettoreJPPA connettore = new ConnettoreJPPA(dbUtilJppa,args);
		//connettore.getListaFlussi().forEach(System.out::println);
	}

	@Override
	public IConnettoreJPPA buildDate() {
		return new ConnettoreJPPA(dataInizio, dataFine, this.args);
	}

}


















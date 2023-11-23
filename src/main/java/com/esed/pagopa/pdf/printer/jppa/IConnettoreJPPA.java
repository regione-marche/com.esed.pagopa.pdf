/**
 * 
 */
package com.esed.pagopa.pdf.printer.jppa;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.AutenticazioneApi;
import io.swagger.client.api.FlussiAgidApi;
import io.swagger.client.api.FlussiDiRendicontazioneApi;
import io.swagger.client.auth.ApiKeyAuth;
import io.swagger.client.model.JppaLoginRequest;
import io.swagger.client.model.JppaLoginResponse;
import io.swagger.client.model.RichiestaInfoListaFlussiRendicontazioneDto;
import io.swagger.client.model.RichiestaScaricaFlussiAgidDto;
import io.swagger.client.model.RispostaInfoListaFlussiRendicontazioneDto;
import io.swagger.client.model.RispostaScaricaFlussiAgidDto;
import io.swagger.client.Configuration;

/**
 * 
 */
public interface IConnettoreJPPA {
	
	/**
	 * 
	 * @param args
	 * @return Un token se il login è andato a buon fine
	 * @throws ApiException
	 */
	public default String loginJppa(String ... args) {
		 AutenticazioneApi apiInstance = new AutenticazioneApi();
		 JppaLoginRequest request = new JppaLoginRequest(); // JppaLoginRequest | request
		 request.setUsername(args[0]);
		 request.setPassword(args[1]);
		 request.setIdentificativoEnte("");
		 request.setIdMessaggio(UUID.randomUUID().toString());
		 JppaLoginResponse result = null;
		 try {
		 result = apiInstance.loginUsingPOST(request);
		 System.out.println(result);
		 System.out.println("Login andato a buon fine");
		 System.out.println("Result: " + result);
		 System.out.println("==============================================================================================================================");
		 } catch (ApiException e) {
		 System.err.println("Exception when calling AutenticazioneApi#loginUsingPOST");
		 e.printStackTrace();
		 }
       return result != null ? result.getToken() : "";
	}
	
	/**
	 * Chiamata all'api per scricare i flussi
	 * @param authToken token ricevuto con il login
	 * @return
	 */
	public default RispostaScaricaFlussiAgidDto postScaricaUsingPOST(String authToken) {
		
		ApiClient defaultClient = Configuration.getDefaultApiClient();
		RispostaScaricaFlussiAgidDto result = null;

		// Configure API key authorization: jwtToken
		ApiKeyAuth jwtToken = (ApiKeyAuth) defaultClient.getAuthentication(authToken);
		jwtToken.setApiKey("YOUR API KEY");
		// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
		jwtToken.setApiKeyPrefix("Bearer " + authToken);

		FlussiAgidApi apiInstance = new FlussiAgidApi();
		RichiestaScaricaFlussiAgidDto filterFlussiAgid = new RichiestaScaricaFlussiAgidDto(); // RichiestaScaricaFlussiAgidDto | filterFlussiAgid
		try {
		    result = apiInstance.postScaricaUsingPOST(filterFlussiAgid);
		    System.out.println(result);
		} catch (ApiException e) {
		    System.err.println("Exception when calling FlussiAgidApi#postScaricaUsingPOST");
		    e.printStackTrace();
		}
		
		return result;

	}

	/**
	 * 
	 * @return la lista dei flussi
	 */
	public List<String> getFlussi();
    /**
     * 
     * @return la lista dei flussi
     * @throws ApiException
     */
	public List<String> getListaFlussi() throws ApiException;
	
	/**
	 * 
	 * @return il token assegnato al momento del login
	 */
	public String getToken();
	
	
	public IConnettoreJPPA buildDate();

	
}

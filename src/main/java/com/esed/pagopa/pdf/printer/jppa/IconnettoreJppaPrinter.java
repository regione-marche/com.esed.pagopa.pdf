package com.esed.pagopa.pdf.printer.jppa;


import java.util.stream.Stream;

import it.maggioli.pagopa.jppa.printer.ApiClient;
import it.maggioli.pagopa.jppa.printer.ApiException;
import it.maggioli.pagopa.jppa.printer.Configuration;
import it.maggioli.pagopa.jppa.printer.auth.HttpBearerAuth;
import it.maggioli.pagopa.jppa.printer.model.DatiEnteAvvisaturaDto;
import it.maggioli.pagopa.jppa.printer.model.JwtRequest;
import it.maggioli.pagopa.jppa.printer.model.JwtResponse;
import it.maggioli.pagopa.jppa.printer.model.PosizioneDebitoriaAvvisaturaDto;
import it.maggioli.pagopa.jppa.printer.model.StampaAvvisaturaRichiesta;
import it.maggioli.pagopa.jppa.printer.model.StampaAvvisaturaRisposta;
import it.maggioli.pagopa.jppa.printer.model.StampaBollettinoRichiesta;
import it.maggioli.pagopa.jppa.printer.model.StampaBollettinoRisposta;
import it.maggioli.pagopa.jppa.printer.node.AuthenticateApi;
import it.maggioli.pagopa.jppa.printer.node.AvvisaturaApi;
import it.maggioli.pagopa.jppa.printer.node.BollettinoApi;

public interface IconnettoreJppaPrinter {
	
	/**
	 * @param strings user[0] password[1]
	 * @return un token di autenticazione
	 */
	public default String loginPrinter(String ... strings) {
		
		System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
		
		ApiClient defaultClient = Configuration.getDefaultApiClient();

    	defaultClient.setBasePath(strings[2]);

        AuthenticateApi apiInstance = new AuthenticateApi(defaultClient);
        JwtRequest jwtRequest = new JwtRequest(); // JwtRequest |
        JwtResponse result = null;

        jwtRequest.setUsername(strings[0]);
        jwtRequest.setPassword(strings[1]);
        
         try {
			result = apiInstance.createAuthenticationToken(jwtRequest);
		} catch (ApiException e) {
			e.printStackTrace();
		}
         
        String token = null;
		try {
			token = Stream.of(result)
					 .findFirst()
					 .map(r -> r.getToken())
					 .orElseThrow(ApiException::new);
		} catch (ApiException e) {
			e.printStackTrace();
		}
        
         return token;
 
	}
	
    /**
     * Genera la stampa dell&#39;avviso di pagamento
     *
     * La stampa dell&#39;avvisatura viene generata richiedendo i dati del Debito a jppa
     * @return 
     *
     * @throws ApiException if the Api call fails
     */
    public default StampaAvvisaturaRisposta stampaAvviso(StampaAvvisaturaRichiesta stampaAvvisaturaRichiesta) {
		
		AvvisaturaApi api = new AvvisaturaApi();
		
		StampaAvvisaturaRisposta response = null;
		
		 try {
			response = api.stampaAvvisatura(stampaAvvisaturaRichiesta);
		} catch (ApiException e) {
			e.printStackTrace();
		}
		 
		 return response;
		
	}
    
    /**
     * Genera la stampa del bollettino
     *
     * La stampa del bollettino viene generata con i dati del Debito inseriti, senza richiederli a jppa.    Per i bollettini postali PA (cpAbilitato &#x3D; true) i campi usati per generare il codice a barre a matrice sono: codiceFiscale, cpNumero, numeroAvviso, importo, titDebitoNominativo e causaleDebitoria.    Per singola posizioneDebitoria viene stampato il modello Rata unica.    Con due posizioneDebitoria si ha la variante Violazione CDS dove la prima rappresenta la rata scontata del 30% e la seconda la rata ridotta.    Per tre o più posizioneDebitoria si hanno le varianti del Multirata, dove la prima indica la Soluzione Unica e le successive le rate ordinate per scadenza.
     *
     * @throws ApiException if the Api call fails
     */
    public default StampaBollettinoRisposta stampaBollettino(StampaBollettinoRichiesta stampaBollettinoRichiesta,String token) {
    	
    	System.out.println("Dentro metodo stampaBollettino");
    	ApiClient defaultClient = Configuration.getDefaultApiClient();
    	
        // Configure HTTP bearer authorization: Bearer
        HttpBearerAuth Bearer = (HttpBearerAuth) defaultClient.getAuthentication("Bearer");
        Bearer.setBearerToken(token);
        System.out.println("Token - " + token);

        BollettinoApi apiInstance = new BollettinoApi(defaultClient);
        StampaBollettinoRisposta result = null;
        
        try {
          result = apiInstance.stampaBollettino(stampaBollettinoRichiesta);
          //System.out.println("Result chiamata" + result);
          System.out.println("richiesta.json : ");
          System.out.println(stampaBollettinoRichiesta.toJson());
        } catch (ApiException e) {
          System.err.println("Exception when calling BollettinoApi#stampaBollettino");
          System.err.println("Status code: " + e.getCode());
          System.err.println("Reason: " + e.getResponseBody());
          System.err.println("Response headers: " + e.getResponseHeaders());
          e.printStackTrace();
        }
        
        return result;
      }
	
	
	/**
	 * 
	 * @return un Connettore al printer
	 */
	public IconnettoreJppaPrinter build(String ... strings);
	
	/**
	 * 
	 * @return Il token di autenticazione
	 */
	public String getToken();
	
	/**
	 * 
	 * @param stampaAvvisaturaRichiesta
	 * @return Una stampa di una avviso pagoPA
	 */
	public StampaAvvisaturaRisposta stampaAvvisatura(StampaAvvisaturaRichiesta stampaAvvisaturaRichiesta);
	
	
	
	public StampaBollettinoRisposta stampaBol(StampaBollettinoRichiesta bolRichiesta);

}

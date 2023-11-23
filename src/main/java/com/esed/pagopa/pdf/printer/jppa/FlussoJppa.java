/**
 * 
 */
package com.esed.pagopa.pdf.printer.jppa;

import java.util.ArrayList;
import java.util.List;

import com.seda.payer.commons.geos.Documento;
import com.seda.payer.commons.geos.Flusso;

/**
 * 
 */
public class FlussoJppa extends Flusso {
	
	private String codiceIpa = "";
	private ArrayList<Documento> documenti;
	
	
	
	
	public void setCodiceIpa(String codiceIpa) {
		this.codiceIpa = codiceIpa;
	}

	/**
	 * 
	 */
	public FlussoJppa() {
		
	}

	/**
	 * @param cuteCute
	 * @param ente
	 */
	public FlussoJppa(String cuteCute, String ente) {
		super(cuteCute, ente);
	}
	
	/**
	 * @param cuteCute
	 * @param ente
	 */
	public FlussoJppa(String cuteCute, String ente,ArrayList<Documento> docs) {
		super(cuteCute, ente);
		this.documenti = docs;
	}

	/**
	 * @param cuteCute
	 * @param ente
	 * @param tipoStampa
	 */
	public FlussoJppa(String cuteCute, String ente, String tipoStampa) {
		super(cuteCute, ente, tipoStampa);
		this.TipoStampa = "jppa";
	}

	/**
	 * @param cuteCute
	 * @param ente
	 * @param tipoStampa
	 * @param idFlu
	 */
	public FlussoJppa(String cuteCute, String ente, String tipoStampa, String idFlu) {
		super(cuteCute, ente, tipoStampa, idFlu);
		this.TipoStampa = "jppa";
	}

	/**
	 * @param cuteCute
	 * @param ente
	 * @param tipoStampa
	 * @param idFlu
	 * @param provenienza
	 */
	public FlussoJppa(String cuteCute, String ente, String tipoStampa, String idFlu, String provenienza) {
		super(cuteCute, ente, tipoStampa, idFlu, provenienza);
		this.TipoStampa = "jppa";
	}

}

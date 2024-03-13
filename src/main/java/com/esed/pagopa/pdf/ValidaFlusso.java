package com.esed.pagopa.pdf;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;

import com.seda.commons.logger.CustomLoggerManager;
import com.seda.commons.logger.LoggerWrapper;
import com.seda.payer.commons.geos.Bollettino;
import com.seda.payer.commons.geos.DatiAnagrafici;
import com.seda.payer.commons.geos.Documento;

public class ValidaFlusso {
	
	protected static LoggerWrapper logger = CustomLoggerManager.get(ValidaFlusso.class);
	private static boolean daArchivioCarichi = false; 
	
	
	
	
	public static boolean getDaArchivioCarichi() {
		return daArchivioCarichi;
	}
	
	
	/**
	 * @param documento
	 * @return restituisce l'array della numerazione dei bollettini validati
	 * @throws faccioPDF.ValidazioneException
	 */
	/**
	 * @param documento
	 * @param tipoStampa
	 * @return
	 * @throws ValidazioneException
	 */
	public static int[] validaFlusso(Documento documento, String tipoStampa) throws ValidazioneException {
		int errCounter = 0;
		String messaggiErrore = "";
		int[] elencoBollettini = null;
//		Controllo dei campi vuoti
//		if (flusso.CodiceEnte == null || flusso.CodiceEnte.length() == 0) {
//			messaggiErrore += "-il campo Codice Ente è vuoto \r\n";
//			errCounter++;
//		}
//		if (flusso.CodiceUtente == null || flusso.CodiceUtente.length() == 0) {
//			messaggiErrore += "-il campo CodiceUtente è vuoto \r\n";
//			errCounter++;
//		}
//		if (flusso.CuteCute == null || flusso.CuteCute.length() == 0) {
//			messaggiErrore += "-il campo CuteCute è vuoto \r\n";
//			errCounter++;
//		}
//		if (flusso.DataFornitura == null || flusso.DataFornitura.length() == 0) {
//			messaggiErrore += "-il campo DataFornitura è vuoto \r\n";
//			errCounter++;
//		}
//		if (flusso.idFlusso == null || flusso.idFlusso.length() == 0) {
//			messaggiErrore += "-il campo idFlusso è vuoto \r\n";
//			errCounter++;
//		}
//		if (flusso.ProgZero == null || flusso.ProgZero.length() == 0) {
//			messaggiErrore += "-il campo ProgZero è vuoto \r\n";
//			errCounter++;
//		}
//		if (flusso.Provenienza == null || flusso.Provenienza.length() == 0) {
//			messaggiErrore += "-il campo Provenienza è vuoto \r\n";
//			errCounter++;
//		}
//		if (flusso.TipoStampa == null || flusso.TipoStampa.length() == 0) {
//			messaggiErrore += "-il campo TipoStampa è vuoto \r\n";
//			errCounter++;
//		}
//		if (flusso.VersioneZero == null || flusso.VersioneZero.length() == 0) {
//			messaggiErrore += "-il campo VersioneZero è vuoto \r\n";
//			errCounter++;
//		}
//		if (documento.Documentdata == null || documento.Documentdata.size() == 0) {
//			messaggiErrore += "-non ci sono rate \r\n";
//			errCounter++;
//		}
//		Controllo dei documenti
//		for (int i = 0; i < documento.Documentdata.size(); i++) {
		
		if (documento.CausaleDocumento == null || documento.CausaleDocumento.length() == 0) {
			messaggiErrore += "-manca la causale di pagamento \r\n";
			errCounter++;
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		//} else if (documento.CausaleDocumento.length() > 60) {
		//	//DA FARE qui no 60 ma 120 due righe di 60
		//	documento.CausaleDocumento = documento.CausaleDocumento.substring(0, 60);
////			messaggiErrore += "-la causale di pagamento eccede i 60 caratteri \r\n";
////			errCounter++;
		//}
		} else {
			if (documento.CausaleDocumento != null) {
				int maxDen = 96; //non cambio font e al max 48 ch per riga, 2 righe 
				String appo = documento.CausaleDocumento;
				appo = appo.trim().replaceAll("\\s+", " ");
				if(appo.length() == 0) {
					messaggiErrore += "-manca la causale di pagamento \r\n";
					errCounter++;
				}
				if (appo.length() > maxDen) {
					documento.CausaleDocumento = appo.substring(0, maxDen);
				} else {
					documento.CausaleDocumento = appo;
				}
			}
		}
		//fine LP PG200420 - Errori nel pdf dell'avviso
		
		// controllo di Importo da pagare
		//PAGONET-418 25012023 - inizio
		//Esteso importo a 10 cifre per adeguamento alla versione Avvisi 3.3.0 PagoPA (Valore massimo 999.999.999,99)
		//Si condidera un massimo di 10 cifre anzichhÃ¨ le 11 indicate in quanto il datamatrix poste riserva solo 10 cifre per l'importo
		//BigDecimal max = new BigDecimal("99999999");
		BigDecimal max = new BigDecimal("9999999999");
		//PAGONET-418 25012023 - fine
		if (documento.ImportoDocumento == null) {
			messaggiErrore += "-il campo importo da pagare è vuoto \r\n";
			errCounter++;
		} else if (new BigDecimal(documento.ImportoDocumento).compareTo(max) > 0) {
			//PAGONET-418 25012023 - inizio
			//messaggiErrore += "-il campo importo da pagare eccede il massimo ammesso: â‚¬ 999999,99 \r\n";
			messaggiErrore += "-il campo importo da pagare eccede il massimo ammesso: â‚¬ 99.999.999,99 \r\n";
			//PAGONET-418 25012023 - fine
			errCounter++;
		}

//			TODO INSERIRE IL CONTROLLO SU <DATA> SCADENZA PAGAMENTO

//			Controllo dei Dati Creditore
		for (int j = 0; j < documento.DatiCreditore.size(); j++) {
			
			if (documento.DatiCreditore.get(j).Cf == null || documento.DatiCreditore.get(j).Cf.length() == 0) {
				messaggiErrore += "-il Codice Fiscale dell'Ente Creditore è mancante \r\n";
				errCounter++;
			}else if (documento.DatiCreditore.get(j).Cf.length() > 16) {
				messaggiErrore += "-il Codice Fiscale dell'Ente Creditore eccede i 16 caratteri \r\n";
				errCounter++;
			}
			
			if (documento.DatiCreditore.get(j).Denominazione1 == null
					|| documento.DatiCreditore.get(j).Denominazione1.length() == 0) {
				messaggiErrore += "-la denominazione1 dell'Ente Creditore è mancante \r\n";
				errCounter++;
			//inizio LP PG200420 - Errori nel pdf dell'avviso
			//} else if (documento.DatiCreditore.get(j).Denominazione1.length() > 50) {
			// documento.DatiCreditore.get(j).Denominazione1 = documento.DatiCreditore.get(j).Denominazione1.substring(0,50);
////			messaggiErrore += "-la denominazione1 dell'Ente Creditore eccede i 50 caratteri \r\n";
////			errCounter++;
			//}
			} else {
				String appo = documento.DatiCreditore.get(j).Denominazione1;
				appo = appo.trim().replaceAll("\\s+", " ");
				if(appo.length() == 0) {
					messaggiErrore += "-la denominazione1 dell'Ente Creditore è mancante \r\n";
					errCounter++;
				}
				int maxDen = 100; //al max 50 ch per riga, 2 righe
				if (appo.length() > maxDen) {
					documento.DatiCreditore.get(j).Denominazione1 = appo.substring(0, maxDen);
				} else {
					documento.DatiCreditore.get(j).Denominazione1 = appo;
				}
			}
			//inizio LP PG200420 - Errori nel pdf dell'avviso
			//if (documento.DatiCreditore.get(j).Denominazione2 != null && documento.DatiCreditore.get(j).Denominazione2.length() > 50) {
			//	documento.DatiCreditore.get(j).Denominazione2 = documento.DatiCreditore.get(j).Denominazione2.substring(0,50);
////				messaggiErrore += "-la denominazione2 dell'Ente Creditore eccede i 50 caratteri \r\n";
////				errCounter++;
			//}
			if (documento.DatiCreditore.get(j).Denominazione2 != null) {
				int maxDen = 100; //al max 50 ch per riga, 2 righe
				String appo = documento.DatiCreditore.get(j).Denominazione2;
				appo = appo.trim().replaceAll("\\s+", " ");
				if (appo.length() > maxDen) {
					documento.DatiCreditore.get(j).Denominazione2 = appo.substring(0, maxDen);
				} else {
					documento.DatiCreditore.get(j).Denominazione2 = appo;
				}
			}
			if (documento.DatiCreditore.get(j).Denominazione3 != null) {
				System.out.println("documento.DatiCreditore.get(j).Denominazione3 = " + documento.DatiCreditore.get(j).Denominazione3);
				int maxDen = 300; //al max 100 ch per riga, 3 righe
				String appo = documento.DatiCreditore.get(j).Denominazione3;
				if (appo.length() > maxDen) {
					documento.DatiCreditore.get(j).Denominazione3 = appo.substring(0, maxDen);
				} else {
					documento.DatiCreditore.get(j).Denominazione3 = appo;
				}
				System.out.println("documento.DatiCreditore.get(j).Denominazione3 = " + documento.DatiCreditore.get(j).Denominazione3);
			}
			//fine LP PG200420 - Errori nel pdf dell'avviso
			
			// cbill
			if (documento.DatiCreditore.get(j).CodiceInterbancario == null
					|| documento.DatiCreditore.get(j).CodiceInterbancario.length() == 0) {
				messaggiErrore += "-il Codice Interbancario dell'Ente Creditore è mancante \r\n";
				errCounter++;
			} else if (documento.DatiCreditore.get(j).CodiceInterbancario.length() > 5) {
				messaggiErrore += "-il Codice Interbancario dell'Ente Creditore eccede i 5 caratteri \r\n";
				errCounter++;
			}
			
		}
//			Controllo dati pagatore
		for (int k = 0; k < documento.DatiAnagrafici.size(); k++) {
			
			if (documento.DatiAnagrafici.get(k).Cf == null || documento.DatiAnagrafici.get(k).Cf.length() == 0) {
				messaggiErrore += "-il Codice Fiscale soggetto pagatore è mancante \r\n";
				errCounter++;
			}else if (documento.DatiAnagrafici.get(k).Cf.length() > 16) {
				messaggiErrore += "-il Codice Fiscale soggetto pagatore eccede i 16 caratteri \r\n";
				errCounter++;
			}
			
			if (documento.DatiAnagrafici.get(k).Denominazione1 == null || documento.DatiAnagrafici.get(k).Cf.length() == 0) {
				messaggiErrore += "-la Denominazione del soggetto pagatore è mancante \r\n";
				errCounter++;
			//inizio LP PG200420 - Errori nel pdf dell'avviso
			//} else if (documento.DatiAnagrafici.get(k).Denominazione1.length() > 35) {
			//	documento.DatiAnagrafici.get(k).Denominazione1 = documento.DatiAnagrafici.get(k).Denominazione1.substring(0,35);
////				messaggiErrore += "-la Denominazione del soggetto pagatore eccede i 35 caratteri \r\n";
////				errCounter++;
			//}
			} else {
				int maxDen = 70; //al max 35 ch per riga, 2 righe
				String appo = documento.DatiAnagrafici.get(k).Denominazione1;
				appo = appo.trim().replaceAll("\\s+", " ");
				if(appo.length() == 0) {
					messaggiErrore += "-la Denominazione del soggetto pagatore è mancante \r\n";
					errCounter++;
				}
				if (appo.length() > maxDen) {
					documento.DatiAnagrafici.get(k).Denominazione1 = appo.substring(0, maxDen);
				} else {
					documento.DatiAnagrafici.get(k).Denominazione1 = appo;
				}
			}
			//fine LP PG200420 - Errori nel pdf dell'avviso
			
//			if (documento.DatiAnagrafici.get(k).Indirizzo == null || documento.DatiAnagrafici.get(k).Indirizzo.length() == 0) {
//				messaggiErrore += "-l'Indirizzo del soggetto pagatore è mancante \r\n";
//				errCounter++;
//			//inizio LP PG200420 - Errori nel pdf dell'avviso
////			}else if (documento.DatiAnagrafici.get(k).Indirizzo.length() > 40) {
////				documento.DatiAnagrafici.get(k).Indirizzo = documento.DatiAnagrafici.get(k).Indirizzo.substring(0,40);
//////				messaggiErrore += "-l'Indirizzo del soggetto pagatore eccede i 40 caratteri \r\n";
//////				errCounter++;
////			}
//			} else {
			if (documento.DatiAnagrafici.get(k).Indirizzo != null) {
				int maxDen = 40; //al max 40 ch per riga, 1 riga
				String appo = documento.DatiAnagrafici.get(k).Indirizzo;
				appo = appo.trim().replaceAll("\\s+", " ");
//				if(appo.length() == 0) {
//					messaggiErrore += "-l'Indirizzo del soggetto pagatore è mancante \r\n";
//					errCounter++;
//				}
				if (appo.length() > maxDen) {
					documento.DatiAnagrafici.get(k).Indirizzo = appo.substring(0, maxDen);
				} else {
					documento.DatiAnagrafici.get(k).Indirizzo = appo;
				}
			}
			if(documento.DatiAnagrafici.get(k).Cap != null) {
				String appo = documento.DatiAnagrafici.get(k).Cap;
				appo = appo.trim().replaceAll("\\s+", " ");
//				if(appo.length() == 0) {
//					messaggiErrore += "-il Cap del soggetto pagatore è mancante \r\n";
//					errCounter++;
//				}
				if(appo.length() != 0 && appo.length() != 5) {
					messaggiErrore += "-il Cap del soggetto pagatore Ã¨ errato \r\n";
					errCounter++;
				}
				documento.DatiAnagrafici.get(k).Cap = appo;
//				if(documento.DatiAnagrafici.get(k).Citta == null) {
//					messaggiErrore += "-la CittÃ  del soggetto pagatore è mancante \r\n";
//					errCounter++;
//				} else {
				if(documento.DatiAnagrafici.get(k).Citta != null) {
					appo = documento.DatiAnagrafici.get(k).Citta;
					appo = appo.trim().replaceAll("\\s+", " ");
//					if(appo.length() == 0) {
//						messaggiErrore += "-la CittÃ  del soggetto pagatore è mancante \r\n";
//						errCounter++;
//					}
					documento.DatiAnagrafici.get(k).Citta = appo;
				}
//				if(documento.DatiAnagrafici.get(k).Provincia == null) {
//					messaggiErrore += "-la Provincia del soggetto pagatore è mancante \r\n";
//					errCounter++;
//				} else {
				if (documento.DatiAnagrafici.get(k).Provincia != null) {
					appo = documento.DatiAnagrafici.get(k).Provincia;
					appo = appo.trim().replaceAll("\\s+", " ");
//					if(appo.length() == 0) {
//						messaggiErrore += "-la Provincia del soggetto pagatore è mancante \r\n";
//						errCounter++;
//					}
					if(appo.length() != 0 && appo.length() != 2) {
						messaggiErrore += "-la Provincia del soggetto pagatore Ã¨ errata \r\n";
						errCounter++;
					}
					documento.DatiAnagrafici.get(k).Provincia = appo;
				}
			}
			//fine LP PG200420 - Errori nel pdf dell'avviso
		}
		
//			Controllo dati bollettino
		if (documento.DatiBollettino != null && documento.DatiBollettino.size() > 0) {
			
			elencoBollettini = new int[documento.DatiBollettino.size()];
			
			int contatoreIndiceElencoBollettini = 0;

			for (Bollettino bollettino : documento.DatiBollettino) {
				System.out.println("------------");
				System.out.println("bollettino.ProgressivoBoll = " + bollettino.ProgressivoBoll);
				
				if (bollettino.ProgressivoBoll > 999) {
					messaggiErrore += "-il bollettino n°" + bollettino.ProgressivoBoll
							+ " eccede la numerazione massima consentita: 999 \r\n";
					errCounter++;
				}
//					COSTRUISCE L'ARRAY CON L'ELENCO DEL NUMERO DEI BOLLETTINI
				elencoBollettini[contatoreIndiceElencoBollettini] = bollettino.ProgressivoBoll;
				contatoreIndiceElencoBollettini++;
//					TODO VERIFICARE CHE AVVISOPAGOPA SIA IL CODICE AVVISO
				// controllo di Codice_avviso
				if(bollettino.AvvisoPagoPa != null)
				{
					System.out.println("bollettino.AvvisoPagoPa = " + bollettino.AvvisoPagoPa);	
				}else
					System.out.println("bollettino.AvvisoPagoPa è nullo");
				
				
				
				
				if (bollettino.AvvisoPagoPa != null && bollettino.AvvisoPagoPa.length() > 0) {
					boolean match = controlloAvviso(bollettino.AvvisoPagoPa);
					if (!match) {
//							logger.debug("------------------ codice avviso -- " + bollettino.AvvisoPagoPa);
						messaggiErrore += "-il campo Codice avviso è in formato errato \r\n";
						errCounter++;
					}
				} else {
					messaggiErrore += "-il campo Codice avviso è vuoto \r\n";
					errCounter++;
				}
				
				// qrCode
				
				if (bollettino.BarcodePagoPa != null && bollettino.BarcodePagoPa.matches("\\(415\\).*\\(8020\\).*\\(3902\\).*")) {
					System.out.println("Chiamata da archivioCarichiWS");
					// vengo da archvioCarichi
					// inverto BarcodePagoPa <--> QRcodePagoPa
					ValidaFlusso.daArchivioCarichi = true;
					String barcodePagoPaTemp = bollettino.BarcodePagoPa;
					bollettino.BarcodePagoPa = bollettino.QRcodePagoPa;
					bollettino.QRcodePagoPa = barcodePagoPaTemp;
				}
				
				if (bollettino.BarcodePagoPa != null)
					System.out.println("(bollettino.BarcodePagoPa = " + bollettino.BarcodePagoPa);
				
				if (bollettino.BarcodePagoPa == null || bollettino.BarcodePagoPa.length() == 0) {
//						logger.debug(bollettino.BarcodePagoPa); 
					messaggiErrore += "-il bollettino n°" + bollettino.ProgressivoBoll + " è mancante del QrCode \r\n";
					errCounter++;
				} else if (bollettino.BarcodePagoPa.length() > 52) {
					messaggiErrore += "-il bollettino n°" + bollettino.ProgressivoBoll
							+ " ha il QrCode in formato errato\r\n";
					errCounter++;
				}
				
				// dataMatrix
				if(tipoStampa.equals("P")) {
					
					if (bollettino.QRcodePagoPa != null)
						System.out.println("(bollettino.QRcodePagoPa = " + bollettino.QRcodePagoPa);
					
					if (bollettino.QRcodePagoPa != null && bollettino.QRcodePagoPa.matches("\\(415\\).*\\(8020\\).*\\(3902\\).*")) {

						String codiceAvvisoOriginalePagoPa = bollettino.Codeline1Boll == null ? "" : bollettino.Codeline1Boll.replace(" ", "");
						String numeroContoCorrente = bollettino.Codeline12Boll == null ? "" : bollettino.Codeline12Boll;
						String importo = bollettino.Codeline12Boll == null ? "" : bollettino.Codeline12Boll;
						
						String codeline =   "18"
								+ String.format("%-18.18s", codiceAvvisoOriginalePagoPa)
								+ "12"
								+ String.format("%12.12s", numeroContoCorrente).replace(' ', '0') // numero conto
								+ "10"
								+ String.format("%10.10s", importo).replace(' ', '0')
								+ "3"
								+ "896"; //tipo documento;
						
						String codiceIdentificativoDominio = documento.DatiCreditore != null && documento.DatiCreditore.size() > 0 ? documento.DatiCreditore.get(0).Cf : "";
						DatiAnagrafici ana = documento.DatiAnagrafici != null && documento.DatiAnagrafici.size() > 0 ? documento.DatiAnagrafici.get(0) : null;
						String codiceFiscale = ana != null ? ana.Cf : "";
						String denominazione = ana != null ? ana.Denominazione1 : "";
						String causaleServizio = "";
						
						if (documento.CausaleDocumento != null) {
							if (documento.CausaleDocumento.indexOf('|') > -1) {
								causaleServizio = documento.CausaleDocumento.split("\\|")[0];
							} else {
								causaleServizio = documento.CausaleDocumento;
							}
						}
						
						String dataMatrix = "codfase=NBPA;" + codeline + "1P1"
								+ String.format("%11.11s", codiceIdentificativoDominio)
								+ String.format("%-16.16s", codiceFiscale)
								+ String.format("%-40.40s", denominazione.toUpperCase())
								+ String.format("%-110.110s", causaleServizio) + "            "// filler
								+ "A";
						
						bollettino.QRcodePagoPa = dataMatrix;
					}
					
					if (bollettino.QRcodePagoPa == null || bollettino.QRcodePagoPa.length() == 0) {
						messaggiErrore += "-il bollettino n°" + bollettino.ProgressivoBoll
								+ " è mancante del datamatrix \r\n";
						errCounter++;
					} else if (bollettino.QRcodePagoPa.length() != 256) {
						messaggiErrore += "-il bollettino n°" + bollettino.ProgressivoBoll
								+ " ha il campo datamatrix in formato errato, lunghezza: " + bollettino.QRcodePagoPa.length() + ", deve essere 256\r\n";
						errCounter++;
					}
				}
				// CC Postale
				if (bollettino.Codeline12Boll != null && bollettino.Codeline12Boll.length() > 20) {
					bollettino.Codeline12Boll = bollettino.Codeline12Boll.substring(0,20);
//					messaggiErrore += "-il bollettino n°" + bollettino.ProgressivoBoll
//							+ " ha il campo numero di CC Postale che eccede i 20 caratteri \r\n";
//					errCounter++;
				}
				
				// Intestazione CCCP
				//inizio LP PG210070
//				if (bollettino.Descon60Boll != null && bollettino.Descon60Boll.length() > 50) {
//					bollettino.Descon60Boll = bollettino.Descon60Boll.substring(0,50);
////					messaggiErrore += "-il bollettino n°" + bollettino.ProgressivoBoll
////							+ " ha il campo intestatario del CC Postale che eccede i 50 caratteri \r\n";
////					errCounter++;
//				}
				if (bollettino.Descon60Boll != null) {
					bollettino.Descon60Boll = bollettino.Descon60Boll.trim().replaceAll("\\s+", " ");
					bollettino.Descon60Boll = bollettino.Descon60Boll.substring(0, Math.min(bollettino.Descon60Boll.length(),  50));
				}
				//fine LP PG210070
				
				// controllo di Autorizzazione
				if (bollettino.AutorizCcp != null && bollettino.AutorizCcp.length() != 0
						&& bollettino.AutorizCcp.length() > 64) {
					messaggiErrore += "-il campo Autorizzazione è in formato errato \r\n";
					errCounter++;
				}
				
				// controllo di data_scadenza_rata
				if (bollettino.ScadenzaRata != null && bollettino.ScadenzaRata.length() > 0) {
					
					if (bollettino.ScadenzaRata.length() == 8) {
						bollettino.ScadenzaRata = String.format("%s/%s/%s",
								bollettino.ScadenzaRata.substring(0, 2),
								bollettino.ScadenzaRata.substring(2, 4),
								bollettino.ScadenzaRata.substring(4));
					}
					
					String regex = "^(3[01]|[12][0-9]|0[1-9])/(1[0-2]|0[1-9])/[0-9]{4}$";
					Pattern pattern = Pattern.compile(regex);
					Matcher matcher = pattern.matcher(bollettino.ScadenzaRata);
					if (!matcher.matches()) {
//							System.out.println("------------scadenza rata -- bollettino n° ---" + bollettino.ProgressivoBoll + " " + bollettino.ScadenzaRata );
						messaggiErrore += "-il campo ScadenzaRata relativo al bollettino n°"
								+ bollettino.ProgressivoBoll + " è in un formato errato \r\n";
						errCounter++;
					}
				} else {
					messaggiErrore += "-il campo ScadenzaRata relativo al bollettino n°" + bollettino.ProgressivoBoll
							+ " è vuoto \r\n";
					errCounter++;
				}
				
				if (bollettino.ProgressivoBoll == 666)
					System.out.println("è uscito il bollettino della Bestia");
				
				System.out.println("Codeline2Boll = " + bollettino.Codeline2Boll);
				System.out.println("max = " + max);
				
				// controllo di Importo rata
				if (bollettino.Codeline2Boll == null || bollettino.Codeline2Boll.length() == 0) {
					messaggiErrore += "-il campo importo da pagare relativo alla rata del rigo n°"
							+ bollettino.ProgressivoBoll + " è vuoto \r\n";
					errCounter++;
				} else {
					
					if (bollettino.Codeline2Boll.contains("+")) {
						bollettino.Codeline2Boll = bollettino.Codeline2Boll.replace("+", "");
					}
					
					if (NumberUtils.isCreatable(bollettino.Codeline2Boll) && new BigDecimal(bollettino.Codeline2Boll).compareTo(max) > 0) {
						messaggiErrore += "-il campo importo da pagare relativo alla rata del rigo n°"
								+ bollettino.ProgressivoBoll + " non è un numero o eccede il massimo ammesso: â‚¬ 999999,99 \r\n";
						errCounter++;
					}
				}
			}
		} else {
			messaggiErrore += "-mancano i dati bollettino \r\n";
			errCounter++;
		}
		
		if (elencoBollettini != null) {
//			accerta che il bollettino n° 999  vada alla fine
			Arrays.sort(elencoBollettini);
			if (elencoBollettini[elencoBollettini.length - 1] != 999)
				messaggiErrore += "l'ultimo bollettino non è il 999";
		}

		if (errCounter > 0)
			throw new ValidazioneException(messaggiErrore);

		return elencoBollettini;
	}

	//inizio LP PG210070
	public static int[] validaFlussoBolzano(Documento documento, String tipoStampa) throws ValidazioneException {
		int errCounter = 0;
		String messaggiErrore = "";
		int[] elencoBollettini = null;
		int maxLenCausaleDocumentoLower = 70; //su prima parte causale sono presenti ch minuscoli
		int maxLenCausaleDocumento = 60; //e al max 60 ch per riga, 2 righe
		int maxLenCausaleDocumentoUnicaString = 110;
		int maxLenDenCreditore = 60;
		int maxLenDenCreditoreLower = 70;
		int maxLenDenDebitore = 30; //al max 30 ch per riga, 2 righe
		int maxLenDescIntestatario = 60;
		int maxLenDescIntestatarioLower = 70;
		int maxLenIndirizzoDebitore = 40 * 2; //al max 40 ch per riga, 2 riga
		BigDecimal maxImportoTotale = new BigDecimal("9999999999");
		//int maxDen = 80; //50; //al max 50 ch per riga
		//Controllo CausaleDocumento
		if (documento.CausaleDocumento == null || documento.CausaleDocumento.length() == 0) {
			messaggiErrore += "-manca la causale di pagamento\r\n";
			errCounter++;
		} else {
			if (documento.CausaleDocumento != null) {
				String appo = documento.CausaleDocumento;
				if(appo.indexOf("\r\n") != -1) {
					String[] temp = appo.split("\r\n");
					temp[0] = temp[0].trim().replaceAll("\\s+", " ");
					temp[1] = temp[1].trim().replaceAll("\\s+", " ");
					temp[0] = truncDescAlternativa(temp[0], maxLenCausaleDocumento, maxLenCausaleDocumentoLower);
					temp[1] = truncDesc(temp[1], maxLenCausaleDocumento);
					documento.CausaleDocumento = temp[0] + "\r\n" + temp[1]; 
				} else {
					appo = appo.trim().replaceAll("\\s+", " ");
					if(appo.length() == 0) {
						messaggiErrore += "-manca la causale di pagamento\r\n";
						errCounter++;
					}
					documento.CausaleDocumento = truncDesc(appo, maxLenCausaleDocumentoUnicaString);
				} 
			}
		}
		//Controllo di Importo da pagare
		if (documento.ImportoDocumento == null) {
			messaggiErrore += "-il campo importo da pagare è vuoto\r\n";
			errCounter++;
		} else if (new BigDecimal(documento.ImportoDocumento).compareTo(maxImportoTotale) > 0) {
			messaggiErrore += "-il campo importo da pagare eccede il massimo ammesso: â‚¬ 99.999.999,99\r\n";
			errCounter++;
		}
		//Controllo dei Dati Creditore
		for (int j = 0; j < documento.DatiCreditore.size(); j++) {
			if (documento.DatiCreditore.get(j).Cf == null || documento.DatiCreditore.get(j).Cf.length() == 0) {
				messaggiErrore += "-il Codice Fiscale dell'Ente Creditore è mancante\r\n";
				errCounter++;
			}else if (documento.DatiCreditore.get(j).Cf.length() > 16) {
				messaggiErrore += "-il Codice Fiscale dell'Ente Creditore eccede i 16 caratteri\r\n";
				errCounter++;
			}
			if (documento.DatiCreditore.get(j).Denominazione1 == null || documento.DatiCreditore.get(j).Denominazione1.length() == 0) {
				messaggiErrore += "-la denominazione1 dell'Ente Creditore in lingua tedesca è mancante\r\n";
				errCounter++;
			} else {
				String appo = documento.DatiCreditore.get(j).Denominazione1;
				appo = appo.trim().replaceAll("\\s+", " ");
				if(appo.length() == 0) {
					messaggiErrore += "-la denominazione1 dell'Ente Creditore in lingua tedesca è mancante\r\n";
					errCounter++;
				}
				appo = ValidaFlusso.truncDescAlternativa(appo, maxLenDenCreditore, maxLenDenCreditoreLower);
				documento.DatiCreditore.get(j).Denominazione1 = appo; 
			}
			if (documento.DatiCreditore.get(j).Denominazione2 == null || documento.DatiCreditore.get(j).Denominazione2.length() == 0) {
				messaggiErrore += "-la denominazione2 dell'Ente Creditore in lingua italiana è mancante\r\n";
				errCounter++;
			} else {
				String appo = documento.DatiCreditore.get(j).Denominazione2;
				appo = appo.trim().replaceAll("\\s+", " ");
				if(appo.length() == 0) {
					messaggiErrore += "-la denominazione2 dell'Ente Creditore in lingua italiana è mancante\r\n";
					errCounter++;
				}
				documento.DatiCreditore.get(j).Denominazione2 = truncDesc(appo, maxLenDenCreditore); 
			}
			if (documento.DatiCreditore.get(j).Denominazione3 != null) {
				String appo = documento.DatiCreditore.get(j).Denominazione3;
				if(appo.indexOf("\r\n") != -1) {
					String[] temp = appo.split("\r\n");
					temp[0] = temp[0].trim().replaceAll("\\s+", " ");
					temp[1] = temp[1].trim().replaceAll("\\s+", " ");
					temp[0] = truncDesc(temp[0], maxLenDenCreditore);
					temp[1] = truncDesc(temp[1], maxLenDenCreditore);
					documento.DatiCreditore.get(j).Denominazione3 = temp[0] + "\r\n" + temp[1]; 
				} else {
					documento.DatiCreditore.get(j).Denominazione3 = truncDesc(appo, maxLenDenCreditore * 2);
				}
			}
			// cbill
			if (documento.DatiCreditore.get(j).CodiceInterbancario == null
				|| documento.DatiCreditore.get(j).CodiceInterbancario.length() == 0) {
				messaggiErrore += "-il Codice Interbancario dell'Ente Creditore è mancante\r\n";
				errCounter++;
			} else if (documento.DatiCreditore.get(j).CodiceInterbancario.length() > 5) {
				messaggiErrore += "-il Codice Interbancario dell'Ente Creditore eccede i 5 caratteri\r\n";
				errCounter++;
			}
		}
		//Controllo dati pagatore
		for (int k = 0; k < documento.DatiAnagrafici.size(); k++) {
			if (documento.DatiAnagrafici.get(k).Cf == null || documento.DatiAnagrafici.get(k).Cf.length() == 0) {
				messaggiErrore += "-il Codice Fiscale soggetto pagatore è mancante\r\n";
				errCounter++;
			} else if (documento.DatiAnagrafici.get(k).Cf.length() > 16) {
				messaggiErrore += "-il Codice Fiscale soggetto pagatore eccede i 16 caratteri\r\n";
				errCounter++;
			}
			if (documento.DatiAnagrafici.get(k).Denominazione1 == null || documento.DatiAnagrafici.get(k).Cf.length() == 0) {
				messaggiErrore += "-la Denominazione del soggetto pagatore è mancante\r\n";
				errCounter++;
			} else {
				String appo = documento.DatiAnagrafici.get(k).Denominazione1;
				appo = appo.trim().replaceAll("\\s+", " ");
				if(appo.length() == 0) {
					messaggiErrore += "-la Denominazione del soggetto pagatore è mancante\r\n";
					errCounter++;
				}
				documento.DatiAnagrafici.get(k).Denominazione1 = truncDesc(appo, maxLenDenDebitore * 2);
			}
//			if (documento.DatiAnagrafici.get(k).Indirizzo == null || documento.DatiAnagrafici.get(k).Indirizzo.length() == 0) {
//				messaggiErrore += "-l'Indirizzo del soggetto pagatore è mancante\r\n";
//				errCounter++;
//			} else {
			if (documento.DatiAnagrafici.get(k).Indirizzo != null) {
				String appo = documento.DatiAnagrafici.get(k).Indirizzo;
				appo = appo.trim().replaceAll("\\s+", " ");
//				if(appo.length() == 0) {
//					messaggiErrore += "-l'Indirizzo del soggetto pagatore è mancante\r\n";
//					errCounter++;
//				}
				appo = appo.replaceAll(",", "");
				if (appo.length() > maxLenIndirizzoDebitore) {
					documento.DatiAnagrafici.get(k).Indirizzo = appo.substring(0, maxLenIndirizzoDebitore);
				} else {
					documento.DatiAnagrafici.get(k).Indirizzo = appo;
				}
			}
			String appo = "";
//			if(documento.DatiAnagrafici.get(k).Cap == null) {
//				messaggiErrore += "-il Cap del soggetto pagatore non è valorizzato\r\n";
//				errCounter++;
//			} else {
			if(documento.DatiAnagrafici.get(k).Cap != null) {
				appo = documento.DatiAnagrafici.get(k).Cap;
				appo = appo.trim().replaceAll("\\s+", " ");
//				if(appo.length() == 0) {
//					messaggiErrore += "-il Cap del soggetto pagatore è mancante\r\n";
//					errCounter++;
//				}
				if(appo.length() != 0 && appo.length() != 5) {
					messaggiErrore += "-il Cap del soggetto pagatore è errato\r\n";
					errCounter++;
				}
				documento.DatiAnagrafici.get(k).Cap = appo;
			}
//			if(documento.DatiAnagrafici.get(k).Citta == null) {
//				messaggiErrore += "-la CittÃ  del soggetto pagatore è mancante\r\n";
//				errCounter++;
//			} else {
			if(documento.DatiAnagrafici.get(k).Citta != null) {
				appo = documento.DatiAnagrafici.get(k).Citta;
				appo = appo.trim().replaceAll("\\s+", " ");
//				if(appo.length() == 0) {
//					messaggiErrore += "-la CittÃ  del soggetto pagatore è mancante\r\n";
//					errCounter++;
//				}
				documento.DatiAnagrafici.get(k).Citta = appo;
			}
//			if(documento.DatiAnagrafici.get(k).Provincia == null) {
//				messaggiErrore += "-la Provincia del soggetto pagatore è mancante\r\n";
//				errCounter++;
//			} else {
			if(documento.DatiAnagrafici.get(k).Provincia != null) {
				appo = documento.DatiAnagrafici.get(k).Provincia;
				appo = appo.trim().replaceAll("\\s+", " ");
//				if(appo.length() == 0) {
//					messaggiErrore += "-la Provincia del soggetto pagatore è mancante\r\n";
//					errCounter++;
//				}
				if(appo.length() != 0 && appo.length() != 2) {
					messaggiErrore += "-la Provincia del soggetto pagatore è errata\r\n";
					errCounter++;
				}
				documento.DatiAnagrafici.get(k).Provincia = appo;
			}
		}
		//Controllo dati bollettino
		if (documento.DatiBollettino != null && documento.DatiBollettino.size() > 0) {
			elencoBollettini = new int[documento.DatiBollettino.size()];
			int contatoreIndiceElencoBollettini = 0;
			for (Bollettino bollettino : documento.DatiBollettino) {
				if (bollettino.ProgressivoBoll > 999) {
					messaggiErrore += "-il bollettino n°" + bollettino.ProgressivoBoll
							+ " eccede la numerazione massima consentita: 999\r\n";
					errCounter++;
				}
				//COSTRUISCE L'ARRAY CON L'ELENCO DEL NUMERO DEI BOLLETTINI
				elencoBollettini[contatoreIndiceElencoBollettini] = bollettino.ProgressivoBoll;
				contatoreIndiceElencoBollettini++;
				// controllo di Codice_avviso
				if (bollettino.AvvisoPagoPa != null && bollettino.AvvisoPagoPa.length() > 0) {
					boolean match = controlloAvviso(bollettino.AvvisoPagoPa);
					if (!match) {
						messaggiErrore += "-il campo Codice avviso è in formato errato\r\n";
						errCounter++;
					}
				} else {
					messaggiErrore += "-il campo Codice avviso è vuoto\r\n";
					errCounter++;
				}
				//QRCode
				if (bollettino.BarcodePagoPa == null || bollettino.BarcodePagoPa.length() == 0) {
					messaggiErrore += "-il bollettino n°" + bollettino.ProgressivoBoll + " è mancante del QRCode\r\n";
					errCounter++;
				} else if (bollettino.BarcodePagoPa.length() > 52) {
					messaggiErrore += "-il bollettino n°" + bollettino.ProgressivoBoll
							+ " ha il QRCode in formato errato\r\n";
					errCounter++;
				}
				if(tipoStampa.equals("P")) {
					//DataMatrix
					if (bollettino.QRcodePagoPa == null || bollettino.QRcodePagoPa.length() == 0) {
						messaggiErrore += "-il bollettino n°" + bollettino.ProgressivoBoll
								+ " è mancante del datamatrix\r\n";
						errCounter++;
					} else if (bollettino.QRcodePagoPa.length() != 256) {
						messaggiErrore += "-il bollettino n°" + bollettino.ProgressivoBoll
								+ " ha il campo datamatrix in formato errato, lunghezza: " + bollettino.QRcodePagoPa.length() + "\r\n";
						errCounter++;
					}
					//CC Postale
					if (bollettino.Codeline12Boll != null && bollettino.Codeline12Boll.length() != 12) {
						messaggiErrore += "-il bollettino n°" + bollettino.ProgressivoBoll
								+ " ha il campo numero di CC Postale diverso da 12 caratteri\r\n";
						errCounter++;
					}
					//Intestazione CCCP
					if(bollettino.Descon60Boll == null) {
						messaggiErrore += "-il bollettino n°" + bollettino.ProgressivoBoll
								+ " ha il campo intestatario del CC Postale non valorizzato\r\n";
						errCounter++;
					} else  {
						if(bollettino.Descon60Boll.indexOf("/") != -1) {
							String[] temp = bollettino.Descon60Boll.split("/");
							temp[0] = temp[0].trim().replaceAll("\\s+", " ");
							temp[1] = temp[1].trim().replaceAll("\\s+", " ");
							temp[0] = truncDescAlternativa(temp[0], maxLenDescIntestatario, maxLenDescIntestatarioLower); //ITA
							temp[1] = truncDescAlternativa(temp[1], maxLenDescIntestatario, maxLenDescIntestatarioLower); //DE
							//inverto ITA con DE
							bollettino.Descon60Boll = temp[1] + "\r\n" + temp[0];
						} else {
							maxLenDescIntestatario *= 2;
							bollettino.Descon60Boll = bollettino.Descon60Boll.trim().replaceAll("\\s+", " ");
							bollettino.Descon60Boll = truncDesc(bollettino.Descon60Boll, maxLenDescIntestatario);
						}
						if (bollettino.Descon60Boll.length() == 0) {
							messaggiErrore += "-il bollettino n°" + bollettino.ProgressivoBoll
									+ " ha il campo intestatario del CC Postale mancante\r\n";
							errCounter++;
						}
					}
					// controllo di Autorizzazione
					if (bollettino.AutorizCcp != null
						&& bollettino.AutorizCcp.length() != 0
						&& bollettino.AutorizCcp.length() > 64) {
						messaggiErrore += "-il campo Autorizzazione è in formato errato\r\n";
						errCounter++;
					}
				}
				//Controllo di data_scadenza_rata
				if (bollettino.ScadenzaRata != null && bollettino.ScadenzaRata.length() > 0) {
					String regex = "^(3[01]|[12][0-9]|0[1-9])/(1[0-2]|0[1-9])/[0-9]{4}$";
					Pattern pattern = Pattern.compile(regex);
					Matcher matcher = pattern.matcher(bollettino.ScadenzaRata);
					if (!matcher.matches()) {
						messaggiErrore += "-il campo ScadenzaRata relativo al bollettino n°"
								+ bollettino.ProgressivoBoll + " è in un formato errato\r\n";
						errCounter++;
					}
				} else {
					messaggiErrore += "-il campo ScadenzaRata relativo al bollettino n°" + bollettino.ProgressivoBoll + " è vuoto\r\n";
					errCounter++;
				}
				//Controllo di Importo rata
				if (bollettino.Codeline2Boll == null || bollettino.Codeline2Boll.length() == 0) {
					messaggiErrore += "-il campo importo da pagare relativo al bollettino n°"
							+ bollettino.ProgressivoBoll + " è vuoto\r\n";
					errCounter++;
				} else if (new BigDecimal(bollettino.Codeline2Boll).compareTo(maxImportoTotale) > 0) {
					messaggiErrore += "-il campo importo da pagare relativo al bollettino n°"
							+ bollettino.ProgressivoBoll + " eccede il massimo ammesso: â‚¬ 99.999.999,99\r\n";
					errCounter++;
				}
			}
		} else {
			messaggiErrore += "-mancano i dati bollettino\r\n";
			errCounter++;
		}
		
		if (elencoBollettini != null) {
//			accerta che il bollettino n° 999  vada alla fine
			Arrays.sort(elencoBollettini);
			if (elencoBollettini[elencoBollettini.length - 1] != 999)
				messaggiErrore += "l'ultimo bollettino non è il 999";
		}
		if (errCounter > 0)
			throw new ValidazioneException(messaggiErrore);
		return elencoBollettini;
	}

	public static String truncDesc(String desc, int maxDesc) 
	{
		if(desc.length() > maxDesc) {
			//Nota. Si tronca a maxDesc e si inserisce il ch '.'
			//      se non Ã¨ giÃ  presente e non sostituisce
			//      un ch ' ' un '-' o un '.'
			if(desc.charAt(maxDesc - 1) == ' ') {
				desc = desc.substring(0, maxDesc - 1);
			} else if(desc.charAt(maxDesc - 1) == '-') {
				desc = desc.substring(0, maxDesc - 1);
			} else if(desc.charAt(maxDesc - 1) == '.') {
				desc = desc.substring(0, maxDesc);
			} else {
				if(desc.charAt(maxDesc - 2) == ' ') {
					desc = desc.substring(0, maxDesc - 2);
				} else if(desc.charAt(maxDesc - 2) == '-') {
					desc = desc.substring(0, maxDesc - 2);
				} else if(desc.charAt(maxDesc - 2) == '.') {
					desc = desc.substring(0, maxDesc - 1);
				} else {
					desc = desc.substring(0, maxDesc - 1) + ".";
				}
			}
			desc = desc.trim();
		}
		return desc;
	}
	
	public static String truncDescAlternativa(String appo, int maxDesc, int maxDescLower)
	{
		if(appo.toUpperCase().equals(appo))
			appo = ValidaFlusso.truncDesc(appo, maxDesc);
		else
			appo = ValidaFlusso.truncDesc(appo, maxDescLower);
		return appo;
	}
	//fine LP PG210070
	
	static boolean controlloAvviso(String codice_avviso) {
		
		return true;
		
//		String regex = "^([0-9]{4} ){4}([0-9]{3}|[0-9]{2})$";
//		Pattern pattern = Pattern.compile(regex);
//		Matcher matcher = pattern.matcher(codice_avviso);
//		return matcher.matches();
	}
	
	public static int[] validaFlussoRegMarche(Documento documento, String tipoStampa) throws ValidazioneException {
	int errCounter = 0;
	String messaggiErrore = "";
	int[] elencoBollettini = null;
	if (documento.CausaleDocumento == null || documento.CausaleDocumento.length() == 0) {
		messaggiErrore += "-manca la causale di pagamento \r\n";
		errCounter++;
	} else {
		if (documento.CausaleDocumento != null) {
			int maxDen = 96; //non cambio font e al max 48 ch per riga, 2 righe 
			String appo = documento.CausaleDocumento;
			appo = appo.trim().replaceAll("\\s+", " ");
			if(appo.length() == 0) {
				messaggiErrore += "-manca la causale di pagamento \r\n";
				errCounter++;
			}
			if (appo.length() > maxDen) {
				documento.CausaleDocumento = appo.substring(0, maxDen);
			} else {
				documento.CausaleDocumento = appo;
			}
		}
	}

	
	// controllo di Importo da pagare
	//PAGONET-418 25012023 - inizio
	//Esteso importo a 10 cifre per adeguamento alla versione Avvisi 3.3.0 PagoPA (Valore massimo 999.999.999,99)
	//Si condidera un massimo di 10 cifre anzichhÃ¨ le 11 indicate in quanto il datamatrix poste riserva solo 10 cifre per l'importo
	//BigDecimal max = new BigDecimal("99999999");
	BigDecimal max = new BigDecimal("9999999999");
	//PAGONET-418 25012023 - fine
	if (documento.ImportoDocumento == null) {
		messaggiErrore += "-il campo importo da pagare è vuoto \r\n";
		errCounter++;
	} else if (new BigDecimal(documento.ImportoDocumento).compareTo(max) > 0) {
		//PAGONET-418 25012023 - inizio
		//messaggiErrore += "-il campo importo da pagare eccede il massimo ammesso: â‚¬ 999999,99 \r\n";
		messaggiErrore += "-il campo importo da pagare eccede il massimo ammesso: â‚¬ 99.999.999,99 \r\n";
		//PAGONET-418 25012023 - fine
		errCounter++;
	}

//		Controllo dei Dati Creditore
	for (int j = 0; j < documento.DatiCreditore.size(); j++) {
		
		if (documento.DatiCreditore.get(j).Cf == null || documento.DatiCreditore.get(j).Cf.length() == 0) {
			messaggiErrore += "-il Codice Fiscale dell'Ente Creditore è mancante \r\n";
			errCounter++;
		}else if (documento.DatiCreditore.get(j).Cf.length() > 16) {
			messaggiErrore += "-il Codice Fiscale dell'Ente Creditore eccede i 16 caratteri \r\n";
			errCounter++;
		}
		
		if (documento.DatiCreditore.get(j).Denominazione1 == null
				|| documento.DatiCreditore.get(j).Denominazione1.length() == 0) {
			messaggiErrore += "-la denominazione1 dell'Ente Creditore è mancante \r\n";
			errCounter++;
		} else {
			String appo = documento.DatiCreditore.get(j).Denominazione1;
			appo = appo.trim().replaceAll("\\s+", " ");
			if(appo.length() == 0) {
				messaggiErrore += "-la denominazione1 dell'Ente Creditore è mancante \r\n";
				errCounter++;
			}
			int maxDen = 100; //al max 50 ch per riga, 2 righe
			if (appo.length() > maxDen) {
				documento.DatiCreditore.get(j).Denominazione1 = appo.substring(0, maxDen);
			} else {
				documento.DatiCreditore.get(j).Denominazione1 = appo;
			}
		}
		if (documento.DatiCreditore.get(j).Denominazione2 != null) {
			int maxDen = 100; //al max 50 ch per riga, 2 righe
			String appo = documento.DatiCreditore.get(j).Denominazione2;
			appo = appo.trim().replaceAll("\\s+", " ");
			if (appo.length() > maxDen) {
				documento.DatiCreditore.get(j).Denominazione2 = appo.substring(0, maxDen);
			} else {
				documento.DatiCreditore.get(j).Denominazione2 = appo;
			}
		}
		if (documento.DatiCreditore.get(j).Denominazione3 != null) {
			System.out.println("documento.DatiCreditore.get(j).Denominazione3 = " + documento.DatiCreditore.get(j).Denominazione3);
			int maxDen = 300; //al max 100 ch per riga, 3 righe
			String appo = documento.DatiCreditore.get(j).Denominazione3;
			if (appo.length() > maxDen) {
				documento.DatiCreditore.get(j).Denominazione3 = appo.substring(0, maxDen);
			} else {
				documento.DatiCreditore.get(j).Denominazione3 = appo;
			}
			System.out.println("documento.DatiCreditore.get(j).Denominazione3 = " + documento.DatiCreditore.get(j).Denominazione3);
		}
		
		// cbill
		if (documento.DatiCreditore.get(j).CodiceInterbancario == null
				|| documento.DatiCreditore.get(j).CodiceInterbancario.length() == 0) {
			messaggiErrore += "-il Codice Interbancario dell'Ente Creditore è mancante \r\n";
			errCounter++;
		} else if (documento.DatiCreditore.get(j).CodiceInterbancario.length() > 5) {
			messaggiErrore += "-il Codice Interbancario dell'Ente Creditore eccede i 5 caratteri \r\n";
			errCounter++;
		}
		
	}
//		Controllo dati pagatore
	for (int k = 0; k < documento.DatiAnagrafici.size(); k++) {
		
		if (documento.DatiAnagrafici.get(k).Cf == null || documento.DatiAnagrafici.get(k).Cf.length() == 0) {
			messaggiErrore += "-il Codice Fiscale soggetto pagatore è mancante \r\n";
			errCounter++;
		}else if (documento.DatiAnagrafici.get(k).Cf.length() > 16) {
			messaggiErrore += "-il Codice Fiscale soggetto pagatore eccede i 16 caratteri \r\n";
			errCounter++;
		}
		
		if (documento.DatiAnagrafici.get(k).Denominazione1 == null || documento.DatiAnagrafici.get(k).Cf.length() == 0) {
			messaggiErrore += "-la Denominazione del soggetto pagatore è mancante \r\n";
			errCounter++;
		} else {
			int maxDen = 70; //al max 35 ch per riga, 2 righe
			String appo = documento.DatiAnagrafici.get(k).Denominazione1;
			appo = appo.trim().replaceAll("\\s+", " ");
			if(appo.length() == 0) {
				messaggiErrore += "-la Denominazione del soggetto pagatore è mancante \r\n";
				errCounter++;
			}
			if (appo.length() > maxDen) {
				documento.DatiAnagrafici.get(k).Denominazione1 = appo.substring(0, maxDen);
			} else {
				documento.DatiAnagrafici.get(k).Denominazione1 = appo;
			}
		}
		
		if (documento.DatiAnagrafici.get(k).Indirizzo != null) {
			int maxDen = 40; //al max 40 ch per riga, 1 riga
			String appo = documento.DatiAnagrafici.get(k).Indirizzo;
			appo = appo.trim().replaceAll("\\s+", " ");
			if (appo.length() > maxDen) {
				documento.DatiAnagrafici.get(k).Indirizzo = appo.substring(0, maxDen);
			} else {
				documento.DatiAnagrafici.get(k).Indirizzo = appo;
			}
		}
		if(documento.DatiAnagrafici.get(k).Cap != null) {
			String appo = documento.DatiAnagrafici.get(k).Cap;
			appo = appo.trim().replaceAll("\\s+", " ");
			if(appo.length() != 0 && appo.length() != 5) {
				messaggiErrore += "-il Cap del soggetto pagatore è errato \r\n";
				errCounter++;
			}
			documento.DatiAnagrafici.get(k).Cap = appo;
			if(documento.DatiAnagrafici.get(k).Citta != null) {
				appo = documento.DatiAnagrafici.get(k).Citta;
				appo = appo.trim().replaceAll("\\s+", " ");
				documento.DatiAnagrafici.get(k).Citta = appo;
			}
			if (documento.DatiAnagrafici.get(k).Provincia != null) {
				appo = documento.DatiAnagrafici.get(k).Provincia;
				appo = appo.trim().replaceAll("\\s+", " ");
				if(appo.length() != 0 && appo.length() != 2) {
					messaggiErrore += "-la Provincia del soggetto pagatore è errata \r\n";
					errCounter++;
				}
				documento.DatiAnagrafici.get(k).Provincia = appo;
			}
		}
	}
	
//		Controllo dati bollettino
	if (documento.DatiBollettino != null && documento.DatiBollettino.size() > 0) {
		
		elencoBollettini = new int[documento.DatiBollettino.size()];
		
		int contatoreIndiceElencoBollettini = 0;

		for (Bollettino bollettino : documento.DatiBollettino) {
			System.out.println("------------");
			System.out.println("bollettino.ProgressivoBoll = " + bollettino.ProgressivoBoll);
			
			if (bollettino.ProgressivoBoll > 999) {
				messaggiErrore += "-il bollettino n°" + bollettino.ProgressivoBoll
						+ " eccede la numerazione massima consentita: 999 \r\n";
				errCounter++;
			}
//				COSTRUISCE L'ARRAY CON L'ELENCO DEL NUMERO DEI BOLLETTINI
			elencoBollettini[contatoreIndiceElencoBollettini] = bollettino.ProgressivoBoll;
			contatoreIndiceElencoBollettini++;
			if(bollettino.AvvisoPagoPa != null)
			{
				System.out.println("bollettino.AvvisoPagoPa = " + bollettino.AvvisoPagoPa);	
			}else
				System.out.println("bollettino.AvvisoPagoPa è nullo");
			
			
			
			
			if (bollettino.AvvisoPagoPa != null && bollettino.AvvisoPagoPa.length() > 0) {
				boolean match = controlloAvviso(bollettino.AvvisoPagoPa);
				if (!match) {
//						logger.debug("------------------ codice avviso -- " + bollettino.AvvisoPagoPa);
					messaggiErrore += "-il campo Codice avviso è in formato errato \r\n";
					errCounter++;
				}
			} else {
				messaggiErrore += "-il campo Codice avviso è vuoto \r\n";
				errCounter++;
			}
			
			// qrCode
			
			if (bollettino.BarcodePagoPa != null && bollettino.BarcodePagoPa.matches("\\(415\\).*\\(8020\\).*\\(3902\\).*")) {
				System.out.println("Chiamata da archivioCarichiWS");
				// vengo da archvioCarichi
				// inverto BarcodePagoPa <--> QRcodePagoPa
				String barcodePagoPaTemp = bollettino.BarcodePagoPa;
				bollettino.BarcodePagoPa = bollettino.QRcodePagoPa;
				bollettino.QRcodePagoPa = barcodePagoPaTemp;
			}
			
			if (bollettino.BarcodePagoPa != null)
				System.out.println("(bollettino.BarcodePagoPa = " + bollettino.BarcodePagoPa);
			
			if (bollettino.BarcodePagoPa == null || bollettino.BarcodePagoPa.length() == 0) {
//					logger.debug(bollettino.BarcodePagoPa); 
				messaggiErrore += "-il bollettino n°" + bollettino.ProgressivoBoll + " è mancante del QrCode \r\n";
				errCounter++;
			} else if (bollettino.BarcodePagoPa.length() > 52) {
				messaggiErrore += "-il bollettino n°" + bollettino.ProgressivoBoll
						+ " ha il QrCode in formato errato\r\n";
				errCounter++;
			}
			
			// dataMatrix
			if(tipoStampa.equals("P")) {
				
				if (bollettino.QRcodePagoPa != null)
					System.out.println("(bollettino.QRcodePagoPa = " + bollettino.QRcodePagoPa);
				
				if (bollettino.QRcodePagoPa != null && bollettino.QRcodePagoPa.matches("\\(415\\).*\\(8020\\).*\\(3902\\).*")) {

					String codiceAvvisoOriginalePagoPa = bollettino.Codeline1Boll == null ? "" : bollettino.Codeline1Boll.replace(" ", "");
					String numeroContoCorrente = bollettino.Codeline12Boll == null ? "" : bollettino.Codeline12Boll;
					String importo = bollettino.Codeline12Boll == null ? "" : bollettino.Codeline12Boll;
					
					String codeline =   "18"
							+ String.format("%-18.18s", codiceAvvisoOriginalePagoPa)
							+ "12"
							+ String.format("%12.12s", numeroContoCorrente).replace(' ', '0') // numero conto
							+ "10"
							+ String.format("%10.10s", importo).replace(' ', '0')
							+ "3"
							+ "896"; //tipo documento;
					
					String codiceIdentificativoDominio = documento.DatiCreditore != null && documento.DatiCreditore.size() > 0 ? documento.DatiCreditore.get(0).Cf : "";
					DatiAnagrafici ana = documento.DatiAnagrafici != null && documento.DatiAnagrafici.size() > 0 ? documento.DatiAnagrafici.get(0) : null;
					String codiceFiscale = ana != null ? ana.Cf : "";
					String denominazione = ana != null ? ana.Denominazione1 : "";
					String causaleServizio = "";
					
					if (documento.CausaleDocumento != null) {
						if (documento.CausaleDocumento.indexOf('|') > -1) {
							causaleServizio = documento.CausaleDocumento.split("\\|")[0];
						} else {
							causaleServizio = documento.CausaleDocumento;
						}
					}
					
					String dataMatrix = "codfase=NBPA;" + codeline + "1P1"
							+ String.format("%11.11s", codiceIdentificativoDominio)
							+ String.format("%-16.16s", codiceFiscale)
							+ String.format("%-40.40s", denominazione)
							+ String.format("%-110.110s", causaleServizio) + "            "// filler
							+ "A";
					
					bollettino.QRcodePagoPa = dataMatrix;
				}
				
				if (bollettino.QRcodePagoPa == null || bollettino.QRcodePagoPa.length() == 0) {
					messaggiErrore += "-il bollettino n°" + bollettino.ProgressivoBoll
							+ " è mancante del datamatrix \r\n";
					errCounter++;
				} else if (bollettino.QRcodePagoPa.length() != 256) {
					messaggiErrore += "-il bollettino n°" + bollettino.ProgressivoBoll
							+ " ha il campo datamatrix in formato errato, lunghezza: " + bollettino.QRcodePagoPa.length() + "\r\n";
					errCounter++;
				}
			}
			// CC Postale
			if (bollettino.Codeline12Boll != null && bollettino.Codeline12Boll.length() > 20) {
				bollettino.Codeline12Boll = bollettino.Codeline12Boll.substring(0,20);
			}
			
			
			if (bollettino.Descon60Boll != null) {
				bollettino.Descon60Boll = bollettino.Descon60Boll.trim().replaceAll("\\s+", " ");
				bollettino.Descon60Boll = bollettino.Descon60Boll.substring(0, Math.min(bollettino.Descon60Boll.length(),  50));
			}
			// controllo di Autorizzazione
			if (bollettino.AutorizCcp != null && bollettino.AutorizCcp.length() != 0
					&& bollettino.AutorizCcp.length() > 64) {
				messaggiErrore += "-il campo Autorizzazione è in formato errato \r\n";
				errCounter++;
			}
			
			// controllo di data_scadenza_rata
			
			if (bollettino.ScadenzaRata != null && bollettino.ScadenzaRata.length() > 0) {
				if (bollettino.ScadenzaRata.length() == 8) {
					bollettino.ScadenzaRata = String.format("%s/%s/%s",
							bollettino.ScadenzaRata.substring(0, 2),
							bollettino.ScadenzaRata.substring(2, 4),
							bollettino.ScadenzaRata.substring(4));
				}
			}else{
				messaggiErrore += "-il campo ScadenzaRata relativo al bollettino n°" + bollettino.ProgressivoBoll
						+ " è vuoto \r\n";
				errCounter++;
			}
			
			if (bollettino.ProgressivoBoll == 666)
				System.out.println("è uscito il bollettino della Bestia");
			
			System.out.println("Codeline2Boll = " + bollettino.Codeline2Boll);
			System.out.println("max = " + max);
			
			// controllo di Importo rata
			if (bollettino.Codeline2Boll == null || bollettino.Codeline2Boll.length() == 0) {
				messaggiErrore += "-il campo importo da pagare relativo alla rata del rigo n°"
						+ bollettino.ProgressivoBoll + " è vuoto \r\n";
				errCounter++;
			} else {
				
				if (bollettino.Codeline2Boll.contains("+")) {
					bollettino.Codeline2Boll = bollettino.Codeline2Boll.replace("+", "");
				}
				
				if (NumberUtils.isCreatable(bollettino.Codeline2Boll) && new BigDecimal(bollettino.Codeline2Boll).compareTo(max) > 0) {
					messaggiErrore += "-il campo importo da pagare relativo alla rata del rigo n°"
							+ bollettino.ProgressivoBoll + " non è un numero o eccede il massimo ammesso: € 999999,99 \r\n";
					errCounter++;
				}
			}
		}
	} else {
		messaggiErrore += "-mancano i dati bollettino \r\n";
		errCounter++;
	}
	
	if (elencoBollettini != null) {
//		accerta che il bollettino n° 999  vada alla fine
		Arrays.sort(elencoBollettini);
		if (elencoBollettini[elencoBollettini.length - 1] != 999)
			messaggiErrore += "l'ultimo bollettino non è il 999";
	}

	if (errCounter > 0)
		throw new ValidazioneException(messaggiErrore);

	return elencoBollettini;}

}

package com.esed.pagopa.pdf.test;
import com.seda.payer.commons.geos.Bollettino;
import com.seda.payer.commons.geos.DatiAnagrafici;
import com.seda.payer.commons.geos.DatiCreditore;
import com.seda.payer.commons.geos.Documento;
import com.seda.payer.commons.geos.Flusso;

public class FlussoFinto extends Flusso {

	public FlussoFinto() {
//		esempio su una proprietà semplice
		CodiceEnte = "80009280274";
		TipoStampa = "jppa"; //PG21X007
//		esempio con un oggetto articolato
		Documento documento1 = new Documento("imposta servizio", "29860987", "Oggetto del pagamento");
		documento1.NumeroDocumento = "54321";
//		aggiungiamo l'ente ciucciabocchi
		DatiAnagrafici datiAnagrafici = new DatiAnagrafici();
		datiAnagrafici.Denominazione1= "Mario Rossi";
		datiAnagrafici.Cf = "MRORSS99M22D810Z";
		datiAnagrafici.Indirizzo = "Via Vai a Casa 3";
		datiAnagrafici.Citta = "Frossedi";
		datiAnagrafici.Cap = "10100"; //PG21X007
		datiAnagrafici.Provincia = "TO"; //PG21X007
		
		DatiCreditore datiCreditore = new DatiCreditore();
		datiCreditore.CodiceInterbancario = "12345";//cbill
		datiCreditore.Cf = "CF_ENTE_MAX_16CH";
		datiCreditore.Denominazione1 = "Comune di Vattela Pesca";
		datiCreditore.Denominazione2 = "settore ente = ufficio ente?";
		datiCreditore.Denominazione3 = "Denominazione 3"; //PG21X007
//		TODO INSERIRE INFO ENTE
		
		
		
		Bollettino bollettino1 = new Bollettino();
		bollettino1.ProgressivoBoll = 1;
		bollettino1.ScadenzaRata = "01/08/2084";
		bollettino1.BarcodePagoPa = "1 - PAGOPA|002|123456789012345678|12345678901|";
		bollettino1.QRcodePagoPa = "1 - PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|1 - PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678";
		bollettino1.AvvisoPagoPa = "0001 5678 9012 3456 12";
		bollettino1.AutorizCcp = "1 - AUT. DB/xxxx/xxx xxxxxxx/xx/xxxx";
		bollettino1.Codeline2Boll = "001"; // importo in centesimi
		bollettino1.Codeline12Boll = "378929875499"; // CC
		bollettino1.Codeline1Boll = "1 - 1234 5678 9012 3456 121";
		bollettino1.Descon60Boll = "1 - Comune del cavolo che ti Pare";	

		
		Bollettino bollettino2 = new Bollettino();
		bollettino2.ProgressivoBoll = 2;
		bollettino2.ScadenzaRata = "02/08/2084";
		bollettino2.BarcodePagoPa = "2 - PAGOPA|002|123456789012345678|12345678901|";
		bollettino2.QRcodePagoPa = "2 - PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|1 - PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678";
		bollettino2.AvvisoPagoPa = "0002 5678 9012 3456 12";
		bollettino2.AutorizCcp = "2 - AUT. DB/xxxx/xxx xxxxx\r\nDEL xx/xx/xxxx";
		bollettino2.Codeline2Boll = "001";
		bollettino2.Codeline12Boll = "378929875490"; // CC
		bollettino2.Codeline1Boll = "2 - 1234 5678 9012 3456 122";
		bollettino2.Descon60Boll = "2 - Comune del cavolo che ti Pare";	
		
		Bollettino bollettino3 = new Bollettino();
		bollettino3.ProgressivoBoll = 3;
		bollettino3.ScadenzaRata = "03/08/2084";
		bollettino3.BarcodePagoPa = "3 - PAGOPA|002|123456789012345678|12345678901|";
		bollettino3.QRcodePagoPa = "3 - PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|1 - PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678";
		bollettino3.AvvisoPagoPa = "0003 5678 9012 3456 12";
		bollettino3.AutorizCcp = "3 - AUT. DB/xxxx/xxx xxxxx\r\nDEL xx/xx/xxxx";
		bollettino3.Codeline2Boll = "001";
		bollettino3.Codeline12Boll = "378929875493"; // CC
		bollettino3.Codeline1Boll = "3 - 1234 5678 9012 3456 123";
		bollettino3.Descon60Boll = "3 - Comune del cavolo che ti Pare";	
		
		Bollettino bollettino4 = new Bollettino();
		bollettino4.ProgressivoBoll = 4;
		bollettino4.ScadenzaRata = "04/08/2084";
		bollettino4.BarcodePagoPa = "4 - PAGOPA|002|123456789012345678|12345678901|";
		bollettino4.QRcodePagoPa = "4 - PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|1 - PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678";
		bollettino4.AvvisoPagoPa = "0004 5678 9012 3456 123";
		bollettino4.AutorizCcp = "4 - AUT. DB/xxxx/xxx xxxxx\r\nDEL xx/xx/xxxx";
		bollettino4.Codeline2Boll = "400";
		bollettino4.Codeline12Boll = "4 - 37892987549"; // CC
		bollettino4.Codeline1Boll = "4 - 1234 5678 9012 3456 123";
		bollettino4.Descon60Boll = "4 - Comune del cavolo che ti Pare";	
		
		Bollettino bollettino5 = new Bollettino();
		bollettino5.ProgressivoBoll = 5;
		bollettino5.ScadenzaRata = "05/08/2084";
		bollettino5.BarcodePagoPa = "5 - PAGOPA|002|123456789012345678|12345678901|";
		bollettino5.QRcodePagoPa = "5 - PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|1 - PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678";
		bollettino5.AvvisoPagoPa = "0005 5678 9012 3456 123";
		bollettino5.AutorizCcp = "5 - AUT. DB/xxxx/xxx xxxxx\r\nDEL xx/xx/xxxx";
		bollettino5.Codeline2Boll = "500";
		bollettino5.Codeline12Boll = "5 - 37892987549"; // CC
		bollettino5.Codeline1Boll = "5 - 1234 5678 9012 3456 123";
		bollettino5.Descon60Boll = "5 - Comune del cavolo che ti Pare";	
		
		Bollettino bollettino6 = new Bollettino();
		bollettino6.ProgressivoBoll = 6;
		bollettino6.ScadenzaRata = "06/08/2084";
		bollettino6.BarcodePagoPa = "6 - PAGOPA|002|123456789012345678|12345678901|";
		bollettino6.QRcodePagoPa = "6 - PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|1 - PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678";
		bollettino6.AvvisoPagoPa = "0006 5678 9012 3456 123";
		bollettino6.AutorizCcp = "6 - AUT. DB/xxxx/xxx xxxxx\r\nDEL xx/xx/xxxx";
		bollettino6.Codeline2Boll = "600";
		bollettino6.Codeline12Boll = "6 - 37892987549"; // CC
		bollettino6.Codeline1Boll = "6 - 1234 5678 9012 3456 123";
		bollettino6.Descon60Boll = "6 - Comune del cavolo che ti Pare";	
		
		Bollettino bollettino7 = new Bollettino();
		bollettino7.ProgressivoBoll = 7;
		bollettino7.ScadenzaRata = "07/08/2084";
		bollettino7.BarcodePagoPa = "7 - PAGOPA|002|123456789012345678|12345678901|";
		bollettino7.QRcodePagoPa = "7 - PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|";
		bollettino7.AvvisoPagoPa = "0007 5678 9012 3456 123";
		bollettino7.AutorizCcp = "7 - AUT. DB/xxxx/xxx xxxxx\r\nDEL xx/xx/xxxx";
		bollettino7.Codeline2Boll = "700";
		bollettino7.Codeline12Boll = "7 - 37892987549"; // CC
		bollettino7.Codeline1Boll = "7 - 1234 5678 9012 3456 123";
		bollettino7.Descon60Boll = "7 - Comune del cavolo che ti Pare";	
		
		Bollettino bollettino8 = new Bollettino();
		bollettino8.ProgressivoBoll = 8;
		bollettino8.ScadenzaRata = "08/08/2084";
		bollettino8.BarcodePagoPa = "8 - PAGOPA|002|123456789012345678|12345678901|";
		bollettino8.QRcodePagoPa = "8 - PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|";
		bollettino8.AvvisoPagoPa = "0008 5678 9012 3456 123";
		bollettino8.AutorizCcp = "8 - AUT. DB/xxxx/xxx xxxxx\r\nDEL xx/xx/xxxx";
		bollettino8.Codeline2Boll = "800";
		bollettino8.Codeline12Boll = "8 - 37892987549"; // CC
		bollettino8.Codeline1Boll = "8 - 1234 5678 9012 3456 123";
		bollettino8.Descon60Boll = "8 - Comune del cavolo che ti Pare";	
		
		Bollettino bollettino9 = new Bollettino();
		bollettino9.ProgressivoBoll = 9;
		bollettino9.ScadenzaRata = "09/08/2084";
		bollettino9.BarcodePagoPa = "9 - PAGOPA|002|123456789012345678|12345678901|";
		bollettino9.QRcodePagoPa = "9 - PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|";
		bollettino9.AvvisoPagoPa = "0009 5678 9012 3456 123";
		bollettino9.AutorizCcp = "9 - AUT. DB/xxxx/xxx xxxxx\r\nDEL xx/xx/xxxx";
		bollettino9.Codeline2Boll = "900";
		bollettino9.Codeline12Boll = "9 - 37892987549"; // CC
		bollettino9.Codeline1Boll = "9 - 1234 5678 9012 3456 123";
		bollettino9.Descon60Boll = "9 - Comune del cavolo che ti Pare";	
		
		Bollettino bollettino10 = new Bollettino();
		bollettino10.ProgressivoBoll = 10;
		bollettino10.ScadenzaRata = "10/08/2084";
		bollettino10.BarcodePagoPa = "10 - PAGOPA|002|123456789012345678|12345678901|";
		bollettino10.QRcodePagoPa = "10 - PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|";
		bollettino10.AvvisoPagoPa = "0010 5678 9012 3456 123";
		bollettino10.AutorizCcp = "10 - AUT. DB/xxxx/xxx xxxxx\r\nDEL xx/xx/xxxx";
		bollettino10.Codeline2Boll = "1000";
		bollettino10.Codeline12Boll = "10 - 37892987549"; // CC
		bollettino10.Codeline1Boll = "10 - 1234 5678 9012 3456 123";
		bollettino10.Descon60Boll = "10 - Comune del cavolo che ti Pare";	
		
		Bollettino bollettino11 = new Bollettino();
		bollettino11.ProgressivoBoll = 11;
		bollettino11.ScadenzaRata = "11/08/2084";
		bollettino11.BarcodePagoPa = "11 - PAGOPA|002|123456789012345678|12345678901|";
		bollettino11.QRcodePagoPa = "11 - PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|";
		bollettino11.AvvisoPagoPa = "0011 5678 9012 3456 123";
		bollettino11.AutorizCcp = "11 - AUT. DB/xxxx/xxx xxxxx\r\nDEL xx/xx/xxxx";
		bollettino11.Codeline2Boll = "1100";
		bollettino11.Codeline12Boll = "11 - 37892987549"; // CC
		bollettino11.Codeline1Boll = "11 - 1234 5678 9012 3456 123";
		bollettino11.Descon60Boll = "11 - Comune del cavolo che ti Pare";	
		
		Bollettino bollettino12 = new Bollettino();
		bollettino12.ProgressivoBoll = 12;
		bollettino12.ScadenzaRata = "12/08/2084";
		bollettino12.BarcodePagoPa = "12 - PAGOPA|002|123456789012345678|12345678901|";
		bollettino12.QRcodePagoPa = "12 - PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|";
		bollettino12.AvvisoPagoPa = "0012 5678 9012 3456 123";
		bollettino12.AutorizCcp = "12 - AUT. DB/xxxx/xxx xxxxx\r\nDEL xx/xx/xxxx";
		bollettino12.Codeline2Boll = "1200";
		bollettino12.Codeline12Boll = "12 - 37892987549"; // CC
		bollettino12.Codeline1Boll = "12 - 1234 5678 9012 3456 123";
		bollettino12.Descon60Boll = "12 - Comune del cavolo che ti Pare";	
		
		Bollettino bollettino13 = new Bollettino();
		bollettino13.ProgressivoBoll = 13;
		bollettino13.ScadenzaRata = "13/08/2084";
		bollettino13.BarcodePagoPa = "13 - PAGOPA|002|123456789012345678|12345678901|";
		bollettino13.QRcodePagoPa = "13 - PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|";
		bollettino13.AvvisoPagoPa = "0013 5678 9012 3456 123";
		bollettino13.AutorizCcp = "13 - AUT. DB/xxxx/xxx xxxxx\r\nDEL xx/xx/xxxx";
		bollettino13.Codeline2Boll = "1300";
		bollettino13.Codeline12Boll = "13 - 37892987549"; // CC
		bollettino13.Codeline1Boll = "13 - 1234 5678 9012 3456 123";
		bollettino13.Descon60Boll = "13 - Comune del cavolo che ti Pare";	
		
		Bollettino bollettino999 = new Bollettino();
		bollettino999.ProgressivoBoll = 999;
		bollettino999.ScadenzaRata = "31/08/2084";
		bollettino999.BarcodePagoPa = "999 - PAGOPA|002|123456789012345678|12345678901|";
		bollettino999.QRcodePagoPa = "999 PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|1 - PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678901|PAGOPA|002|123456789012345678|12345678";
		bollettino999.AvvisoPagoPa = "0999 5678 9012 3456 123";
		bollettino999.AutorizCcp = "999 - AUT. DB/xxxx/xxx xxxxx\r\nDEL xx/xx/xxxx";
		bollettino999.Codeline2Boll = "29860987";
		bollettino999.Codeline12Boll = "378929875499"; // CC
		bollettino999.Codeline1Boll = "999 - 1234 5678 9012 3456 123";
		bollettino999.Descon60Boll = "999 - Comune del cavolo che ti Pare";	
		
// 		così per il resto dei dati necessari del gruppo
//		fai la stessa cosa per i DatiAnagrafici (il poro cristo vessato dalle istituzioni)
//		e per Bollettino, magari prova a mettere più di qualche bollettino così proviamo l'uso dei vari modelli 
		
//		aggiungiamo gli elementi del documento a documento
		documento1.addDatiAnagrafici(datiAnagrafici);
		documento1.addDatiCreditore(datiCreditore);
		documento1.addDatiBollettino(bollettino1);
		documento1.addDatiBollettino(bollettino2);
		documento1.addDatiBollettino(bollettino3);
		
//		documento1.addDatiBollettino(bollettino2);
//		documento1.addDatiBollettino(bollettino3);
//		documento1.addDatiBollettino(bollettino4);
//		documento1.addDatiBollettino(bollettino5);
//		documento1.addDatiBollettino(bollettino6);
//		documento1.addDatiBollettino(bollettino7);
//		documento1.addDatiBollettino(bollettino8);
//		documento1.addDatiBollettino(bollettino9);
//		documento1.addDatiBollettino(bollettino10);
//		documento1.addDatiBollettino(bollettino11);
//		documento1.addDatiBollettino(bollettino12);
//		documento1.addDatiBollettino(bollettino13);
		
		documento1.addDatiBollettino(bollettino999);
		
//		poi aggiungiamo il documento fatto a questo oggetto flusso
		this.addDocumento(documento1);
		
		}

}

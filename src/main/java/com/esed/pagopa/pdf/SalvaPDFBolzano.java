package com.esed.pagopa.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.rowset.CachedRowSet;

import org.apache.log4j.Logger;

import com.esed.pagopa.pdf.config.PropKeys;
import com.esed.pagopa.pdf.printer.jppa.InformazioniStampa;
import com.esed.pagopa.pdf.printer.jppa.StampaPdfJppaPagonet;
import com.itextpdf.barcodes.BarcodeDataMatrix;
import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.VerticalAlignment;
import com.seda.commons.properties.tree.PropertiesTree;
import com.seda.commons.string.Convert;
import com.seda.payer.commons.geos.Bollettino;
import com.seda.payer.commons.geos.Documento;
import com.seda.payer.commons.geos.Flusso;
import com.seda.payer.commons.jppa.utils.LogoBollettino;
import com.seda.payer.commons.utility.LogUtility;
import com.seda.payer.pgec.webservice.ente.dati.EnteSearchRequest;
import com.seda.payer.pgec.webservice.ente.dati.EnteSearchResponse;
import com.seda.payer.pgec.webservice.ente.source.EnteImplementationStub;

import it.maggioli.pagopa.jppa.printer.model.StampaAvvisaturaRichiesta.LocaleEnum;
import it.maggioli.pagopa.jppa.printer.model.StampaBollettinoRisposta;

public class SalvaPDFBolzano {
	
	
	
	
	private static final Logger logger = Logger.getLogger(SalvaPDFBolzano.class);
    protected static EnteImplementationStub entePort = null;
	
	
	public static EnteSearchRequest setEnteSearchRequest(String companyCode,String userCode,String chiaveEnte) {
		EnteSearchRequest enteSearchRequest = new EnteSearchRequest();
		enteSearchRequest.setRowsPerPage(0);
		enteSearchRequest.setPageNumber(0);
		enteSearchRequest.setOrder("");
		enteSearchRequest.setCompanyCode(companyCode);
		enteSearchRequest.setUserCode(userCode);
		enteSearchRequest.setChiaveEnte(chiaveEnte);
		enteSearchRequest.setTipoEnte("");
		enteSearchRequest.setStrEnte("");
		enteSearchRequest.setStrDescrSocieta("");
		enteSearchRequest.setStrDescrUtente("");
		return enteSearchRequest;
	}
	
	
	public static ArrayList<String> getEnti(String companyCode,String userCode,String chiaveEnte){
		LogUtility.writeLog("******************************************* inizio NodoSpcServer::getEnti");
		ArrayList <String> arIdCodiceIpa = null;
		EnteSearchRequest enteSearchRequest = setEnteSearchRequest(companyCode,userCode,chiaveEnte);
		EnteSearchResponse res = null;
		try {
			
			try {
				res = entePort.getEntes(enteSearchRequest);
			} catch (Exception e) {
				e.printStackTrace();
			}
			//Devo trattare la lista degli enti...
			String sRes = res.getResponse().getListXml();
			CachedRowSet crsIpa = Convert.stringToWebRowSet(sRes);
			if (crsIpa!=null && crsIpa.size()>0) {
				arIdCodiceIpa = new ArrayList<String>();
				while (crsIpa.next()) {
					String codiceIpa = crsIpa.getString(11).trim();
					if (codiceIpa.trim().length()>0) 
						arIdCodiceIpa.add(codiceIpa);
				}
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		LogUtility.writeLog("******************************************* fine NodoSpcServer::getEnti esito: " + (arIdCodiceIpa != null ? arIdCodiceIpa.size() : 0));
		return arIdCodiceIpa;
	}
	
	
	
	
	
	public static byte[] SalvaFile(Flusso flusso,String tipostampa,PropertiesTree propertiesTree) throws Exception {
		ByteArrayOutputStream baos = null;
		String passwordJppa = propertiesTree.getProperty(PropKeys.passwordJppa.format("000P6"));
		String userJppa = propertiesTree.getProperty(PropKeys.utenteJppa.format("000P6"));
		String urlPrinter = propertiesTree.getProperty(PropKeys.urlprinter.format("000P6"));
		
		if(tipostampa.equals("jppa") || tipostampa.equals("jppade") ) {
			return stampaJppa(flusso,passwordJppa,userJppa,urlPrinter,"").getBytes();
		}
		else {
		for (int i = 0; i < flusso.Documentdata.size(); i++) {
			//ValidaFlusso controlla i dati del flusso, se sono corretti restituisce un array contenente la sequenza
			//dei numeri progressivi dei bollettini, se il numero di bollettini == zero non si esegue la stampa
			int[] elencoBollettini = ValidaFlusso.validaFlussoBolzano(flusso.Documentdata.get(i), flusso.TipoStampa);
			if (elencoBollettini.length < 1) {
				return null;
			}
			//NOTA. Per adesso è consentita la stampa solo per avviso con rataunica ==> 2 bollettini 1 e 999
			if(elencoBollettini.length != 2) {
				throw new ValidazioneException("Stampa abilitata solo per rata unica");
			}
			logger.debug(Arrays.toString(elencoBollettini) + "--------------- ELENCO BOLLETTINI BOLZANO -----------------");
			if (elencoBollettini.length > 0) {
				baos = new ByteArrayOutputStream();
				PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
				//Creato il documento crea il lettore di asset
				LeggoAsset asset = new LeggoAsset(pdf, flusso.TipoStampa, flusso.Documentdata.get(i).DatiCreditore.get(0).LogoEnte);
				//Dimensione e margini del documento
				Document document = new Document(pdf, PageSize.A4);
				
				document.setMargins(0, 0, 0, 0);
				Bollettino bollettino999 = flusso.Documentdata.get(i).DatiBollettino
						.stream()
						.filter(x -> x.ProgressivoBoll == 999)
						.findFirst()
						.orElse(null);
				if (bollettino999 != null && !(flusso.TipoStampa.equals("jppa")))
					paginaUnBollettino(pdf.addNewPage(), asset, flusso.Documentdata.get(i), pdf, bollettino999, flusso.TipoStampa);
				else {
					document.close();
					throw new ValidazioneException("Manca il bollettino rata unica (n 999)");
				}
				//Se i bollettini sono 2 allora non c rateizzazione perche il numero 1 e il 999 entrambi con dati coincidenti
				//se invece i bollettini sono almeno 3 il 999 contiene la rata unica e gli altri la rateizzazione
				logger.info("Numero di bollettini nel documento: " + elencoBollettini.length);
				if (elencoBollettini.length > 2) {
					for (int j = 0; j < elencoBollettini.length - 1; ) {
						if (elencoBollettini.length - 1 - j >= 3 && (elencoBollettini.length - 1 - j) != 4 && !(flusso.TipoStampa.equals("jppa"))) {
							logger.info("chiamato metodo 3 bollettini per pagina");
							paginaTreBollettini(pdf.addNewPage(), asset, flusso.Documentdata.get(i), pdf, j);
							j += 3;
							continue;
						}
						if (elencoBollettini.length - 1 - j >= 2 && (elencoBollettini.length - 1 - j) % 3 != 0 && !(flusso.TipoStampa.equals("jppa"))) {
							logger.info("chiamato metodo 2 bollettini per pagina");
							paginaDueBollettini(pdf.addNewPage(), asset, flusso.Documentdata.get(i), pdf, j);
							j += 2;
							continue;
						}
						if (j < 0)
							break;
					}
				}
				//Close document
				pdf.close();
				document.close();
			} else
				throw new ValidazioneException("Mancano i bollettini");
		}
		logger.info("Sto per restituire il bite array del pdf");
		
		return baos.toByteArray();
	}
}
	
	private static String getCodiceIpa(Flusso flusso) throws Exception {
		String codiceIpa = "";
		try {
		  codiceIpa = getEnti(flusso.CodiceEnte, "", "").get(0);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return codiceIpa;
	}

	
	private static String getlogo64(LeggoAsset asset) {
		byte[] immagine = Base64.getEncoder().encode(asset.getLogo_bolzano().toString().getBytes());
		List<String> base64Image = Stream.of(immagine).map(b -> b.toString()).collect(Collectors.toList());
		return base64Image.stream().collect(Collectors.joining());
	}

	private static String stampaJppa(Flusso flusso,String pass,String user,String urlPrinter, String codiceIpa) throws ValidazioneException {
		StampaPdfJppaPagonet stampa = null;
		try {
		 stampa = new StampaPdfJppaPagonet(user,pass,urlPrinter);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		InformazioniStampa info = new InformazioniStampa();
		
		StampaBollettinoRisposta res = null;

		ByteArrayOutputStream baos = null;
		for (int i = 0; i < flusso.Documentdata.size(); i++) {
			//ValidaFlusso controlla i dati del flusso, se sono corretti restituisce un array contenente la sequenza
			//dei numeri progressivi dei bollettini, se il numero di bollettini == zero non si esegue la stampa
			int[] elencoBollettini = null;
			try {
				elencoBollettini = ValidaFlusso.validaFlussoBolzano(flusso.Documentdata.get(i), flusso.TipoStampa);
			} catch (ValidazioneException e) {
				e.printStackTrace();
			}
			if (elencoBollettini.length < 1) {
				return null;
			}
			//NOTA. Per adesso consentita la stampa solo per avviso con rataunica ==> 2 bollettini 1 e 999
			if(elencoBollettini.length != 2) {
				throw new ValidazioneException("Stampa abilitata solo per rata unica");
			}
			logger.debug(Arrays.toString(elencoBollettini) + "--------------- ELENCO BOLLETTINI BOLZANO -----------------");
			if (elencoBollettini.length > 0) {
				Bollettino bollettino999 = flusso.Documentdata.get(i).DatiBollettino
						.stream()
						.filter(x -> x.ProgressivoBoll == 999)
						.findFirst()
						.orElse(null);
				if (bollettino999 != null) {
					
					info.setAvvisauraDto(flusso.Documentdata.get(i),flusso.TipoStampa); // Inofrmazioni Avvisatura
					res = stampa.stampaBolpuntuale(info.bollRichiesta(flusso.Documentdata.get(i),
							LogoBollettino.getLogoBolzano64(),flusso.TipoStampa));
				}
			
				//Se i bollettini sono 2 allora non c rateizzazione perche il numero 1 e il 999 entrambi con dati coincidenti
				//se invece i bollettini sono almeno 3 il 999 contiene la rata unica e gli altri la rateizzazione
				logger.info("Numero di bollettini nel documento: " + elencoBollettini.length);
				if (elencoBollettini.length > 2) {
					for (int j = 0; j < elencoBollettini.length - 1; ) {
						if (elencoBollettini.length - 1 - j >= 3 && (elencoBollettini.length - 1 - j) != 4 && (flusso.TipoStampa.equals("jppa"))) {
							logger.info("chiamato metodo 3 bollettini per pagina");
							//paginaTreBollettini(pdf.addNewPage(), asset, flusso.Documentdata.get(i), pdf, j);
							info.setAvvisauraDto(flusso.Documentdata.get(i),flusso.TipoStampa); // Inofrmazioni Avvisatura
							res = stampa.stampaBolpuntuale(info.bollRichiesta(flusso.Documentdata.get(i),LogoBollettino.getLogoBolzano64(),flusso.TipoStampa));
							j += 3;
							continue;
						}
						if (elencoBollettini.length - 1 - j >= 2 && (elencoBollettini.length - 1 - j) % 3 != 0 && (flusso.TipoStampa.equals("jppa"))) {
							logger.info("chiamato metodo 2 bollettini per pagina");
							//paginaDueBollettini(pdf.addNewPage(), asset, flusso.Documentdata.get(i), pdf, j);
							info.setAvvisauraDto(flusso.Documentdata.get(i),flusso.TipoStampa); // Inofrmazioni Avvisatura
							res = stampa.stampaBolpuntuale(info.bollRichiesta(flusso.Documentdata.get(i),LogoBollettino.getLogoBolzano64(),flusso.TipoStampa));
							j += 2;
							continue;
						}
						if (j < 0)
							break;
					}
				}
		}
	}
		return res.getFileBase64Encoded();

}

	private static void paginaUnBollettino(PdfPage pageTarget, LeggoAsset asset, Documento documento, PdfDocument pdf, Bollettino bollettino999, String tipoStampa) {
		boolean bDebug = false; //se == true mostra alcune aree con fill colorato per verificare posizione e dimensione 

		PdfCanvas pdfCanvas = new PdfCanvas(pageTarget);

		int marginleft = 20;
		int xt = marginleft;
		int yt = 726;
		int xl0 = xt + 3;
		int yl0 = 773;
		int wl0 = 43;
		int hl0 = 43;
		//Logo PagoPa
		Rectangle logoPagopaRectangle = new Rectangle(xl0, yl0, wl0, hl0);
		Canvas logoPagopaCanvas = new Canvas(pdfCanvas, logoPagopaRectangle);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(222, 222, 222)).rectangle(logoPagopaRectangle).fill();
		logoPagopaCanvas.add(asset.getLogo_pagopa_blu().scaleToFit(wl0, hl0));
		logoPagopaCanvas.close();

		int xr0 = xt + 56;
		int yr0 = 806;
		int wr0 = 310;
		int hr0 = 20;
		int fr0 = 15;
		//Avviso di Pagamento
		Rectangle avvisoPagamentoRectangle = new Rectangle(xr0, yr0, wr0, hr0);
		Canvas avvisoPagamentoCanvas = new Canvas(pdfCanvas, avvisoPagamentoRectangle);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(202, 202, 202)).rectangle(avvisoPagamentoRectangle).fill();
		Text avvisoPagamentoText = new Text("Zahlungsmitteilung - Avviso di pagamento").setFont(asset.getTitillium_bold());
		Paragraph avvisoPagamentoP = new Paragraph().add(avvisoPagamentoText).setFontColor(ColorConstants.BLACK)
				.setFontSize(fr0).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		avvisoPagamentoCanvas.add(avvisoPagamentoP);
		avvisoPagamentoCanvas.close();

		int xl1 = xt + 410;
		int yl1 = 678;
		int wl1 = 145;
		int hl1 = 145;
		//Logo Ente
		Image logoEnte = asset.getLogo_ente();
		if(!asset.bLogoEnteExtern && asset.getLogo_bolzano() != null) {
			if(bDebug) System.out.println("uso logo ente interno");
			logoEnte = asset.getLogo_bolzano();
		}
		Rectangle logoEnteRectangle = new Rectangle(xl1, yl1, wl1, hl1);
		Canvas logoEnteCanvas = new Canvas(pdfCanvas, logoEnteRectangle);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(82, 82, 82)).rectangle(logoEnteRectangle).fill();
		logoEnteCanvas.add(logoEnte.scaleToFit(wl1, hl1));
		logoEnteCanvas.close();

		//Label Causale DE\ITA
		int xlc1 = xt;
		int ylc1 = yt + 17;
		int wlc1 = 82;
		int flc1 = 9;
		int hlc1 = 30;
		String sLabelCausaleDE = "Zahlungsgrund";
		String sLabelCausale = "Causale";
		Rectangle oggettoLabelCausaleRectangle = new Rectangle(xlc1, ylc1, wlc1, hlc1);
		Canvas oggettoLabelCausaleCanvas = new Canvas(pdfCanvas, oggettoLabelCausaleRectangle);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(212, 212, 212)).rectangle(oggettoLabelCausaleRectangle).fill();
		Text oggettoLabelCausaleText = new Text(sLabelCausaleDE + "\r\n" + sLabelCausale).setFont(asset.getTitillium_bold());
		Paragraph oggettoLabelCausaleP = new Paragraph().add(oggettoLabelCausaleText).setFontColor(ColorConstants.BLACK)
				.setFontSize(flc1).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(10);
		oggettoLabelCausaleCanvas.add(oggettoLabelCausaleP);
		oggettoLabelCausaleCanvas.close();

		int xca1 = xt + 82;
		int yca1 = yt + 15;
		int wca1 = 330;
		int hca1 = 37;
		int fca1 = 11;
		/*
		if(sCausaleAppo.indexOf("\\") != -1) {
			String sCausaleAppo = documento.CausaleDocumento;
			String[] temp = sCausaleAppo.split("\\"); 
			temp[0] = temp[0].trim().replaceAll("\\s+", " ");
			temp[1] = temp[1].trim().replaceAll("\\s+", " ");
			//if (temp[0].length() > 40)
			//	temp[0] = temp[0].substring(0, 40);
			//if (temp[1].length() > 40)
			//	temp[1] = temp[1].substring(0, 40);
			temp[0] = truncDesc(temp[0], 40); //ITA
			temp[1] = truncDesc(temp[1], 40); //DE
			String sCausaleITA = temp[0] ;// " IT";
			String sCausaleDE = temp[1]; //"DE";
			sCausaleAppo = temp[0] + "\r\n" + temp[1];
			//Causale DE
			Rectangle oggettoCausaleDERectangle = new Rectangle(xca1, yca1, wca1, hca1);
			Canvas oggettoCausaleDECanvas = new Canvas(pdfCanvas, oggettoCausaleDERectangle);
			if(bDebug) {
				System.out.println("sCausaleDE:\r\n" + sCausaleDE);
				pdfCanvas.saveState().setFillColor(new DeviceRgb(232, 0, 0)).rectangle(oggettoCausaleDERectangle).fill();
			}
			Text oggettoCausaleDEText = new Text(sCausaleDE).setFont(asset.getTitillium_bold());
			Paragraph oggettoCausaleDEP = new Paragraph().add(oggettoCausaleDEText).setFontColor(ColorConstants.BLACK)
					.setFontSize(fca1).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(fca1);
			oggettoCausaleDECanvas.add(oggettoCausaleDEP);
			oggettoCausaleDECanvas.close();
			
			//Causale ITA
			yca1 = yt + 4;
			Rectangle oggettoCausaleRectangle = new Rectangle(xca1, yca1, wca1, hca1);
			Canvas oggettoCausaleCanvas = new Canvas(pdfCanvas, oggettoCausaleRectangle);
			if(bDebug) {
				System.out.println("sCausaleITA:\r\n" + sCausaleITA);
				pdfCanvas.saveState().setFillColor(new DeviceRgb(230, 0, 0)).rectangle(oggettoCausaleRectangle).fill();
			}
			Text oggettoCausaleText = new Text(sCausaleITA).setFont(asset.getTitillium_bold());
			Paragraph oggettoCausaleP = new Paragraph().add(oggettoCausaleText).setFontColor(ColorConstants.BLACK)
					.setFontSize(fca1).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(fca1);
			oggettoCausaleCanvas.add(oggettoCausaleP);
			oggettoCausaleCanvas.close();
		} else {
		}
		*/
		//Causale Unica
		yca1 = yt + 3;
		yca1 += 8;
		xca1 -= 10;
		wca1 += 25;
		Rectangle oggettoCausaleRectangle = new Rectangle(xca1, yca1, wca1, hca1);
		Canvas oggettoCausaleCanvas = new Canvas(pdfCanvas, oggettoCausaleRectangle);
		String sCausaleUnica = documento.CausaleDocumento;
		if(bDebug) {
			System.out.println("CausaleDocumento:\r\n" + sCausaleUnica);
			pdfCanvas.saveState().setFillColor(new DeviceRgb(232, 0, 0)).rectangle(oggettoCausaleRectangle).fill();
		}
		Text oggettoPagamentoText = new Text(sCausaleUnica).setFont(asset.getTitillium_bold());
		Paragraph oggettoCausaleP = new Paragraph().add(oggettoPagamentoText).setFontColor(ColorConstants.BLACK)
				.setFontSize(fca1).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(fca1);
		oggettoCausaleCanvas.add(oggettoCausaleP);
		oggettoCausaleCanvas.close();

		//Label Altre Lingue
		int xa1 = xt + 432;
		int ya1 = yt + 15;
		int wa1 = 124;
		int ha1 = 26;
		int fa1 = 7;
		String sAltriLingue = "Other languages, English & Ladin:";
		String sSitoEnte = "suedtirolereinzugsdienste.it/pagoPA";
		Rectangle oggettoAltreLingueRectangle = new Rectangle(xa1, ya1, wa1, ha1);
		Canvas oggettoAltreLingueCanvas = new Canvas(pdfCanvas, oggettoAltreLingueRectangle);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(233, 232, 232)).rectangle(oggettoAltreLingueRectangle).fill();
		Text oggettoAltreLingueText = new Text(sAltriLingue + "\r\n" + sSitoEnte).setFont(asset.getTitillium_bold());
		Paragraph oggettoAltreLingueP = new Paragraph().add(oggettoAltreLingueText).setFontColor(ColorConstants.BLACK)
				.setFontSize(fa1).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(10);
		oggettoAltreLingueCanvas.add(oggettoAltreLingueP);
		oggettoAltreLingueCanvas.close();
		
		//==========================================================================================================================//
		// Ente Creditore Background
		int wtl = 299;
		int ht = 16;
		Rectangle enteCreditoreGrayRectangle = new Rectangle(xt, yt, wtl, ht);
		pdfCanvas.saveState().setFillColor(LeggoAsset.bolzanoBluBollettino).rectangle(enteCreditoreGrayRectangle).fill();
		Canvas enteCreditoreGrayCanvas = new Canvas(pdfCanvas, enteCreditoreGrayRectangle);
		enteCreditoreGrayCanvas.close();

		int h0 = ht - 1;
		int w0 = 112;
		int mleft = 10;
		int mbot = 4;
		int x0 = xt + mleft;
		int y0 = yt + mbot;
		//Ente Creditore
		Rectangle enteCreditoreRectangle = new Rectangle(x0, y0, w0, h0);
		Canvas enteCreditoreCanvas = new Canvas(pdfCanvas, enteCreditoreRectangle);
		Text enteCreditoreText = new Text("Kï¿½RPERSCHAFT / ENTE:").setFont(asset.getTitillium_bold());
		Paragraph enteCreditoreP = new Paragraph().add(enteCreditoreText).setFontColor(ColorConstants.WHITE)
				.setFontSize(10).setMargin(0).setVerticalAlignment(VerticalAlignment.BOTTOM);
		enteCreditoreCanvas.add(enteCreditoreP);
		enteCreditoreCanvas.close();

		int mcf = 2;
		int xlcf = x0 + w0 + 1;
		int ylcf = y0 - mcf;
		int wlcf = 70;
		//Label "Codice Fiscale"
		Rectangle codiceFiscaleRectangle = new Rectangle(xlcf, ylcf, wlcf, h0);
		Canvas codiceFiscaleCanvas = new Canvas(pdfCanvas, codiceFiscaleRectangle);
		Text codiceFiscaleText = new Text("St.Nr./Cod.Fiscale").setFont(asset.getTitillium_regular());
		Paragraph codiceFiscaleP = new Paragraph().add(codiceFiscaleText).setFontColor(ColorConstants.WHITE)
				.setFontSize(8).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		codiceFiscaleCanvas.add(codiceFiscaleP);
		codiceFiscaleCanvas.close();

		int xcf = xlcf + wlcf + 1;
		int wcf = wtl - mleft - wlcf;
		int ycf = ylcf + mcf;
		//Codice Fiscale Ente
		Rectangle cfEnteRectangle = new Rectangle(xcf, ycf, wcf, h0);
		Canvas cfEnteCanvas = new Canvas(pdfCanvas, cfEnteRectangle);
		Text cfEnteText = new Text(documento.DatiCreditore.get(0).Cf).setFont(asset.getRoboto_regular());
		Paragraph cfEnteP = new Paragraph().add(cfEnteText).setFontColor(ColorConstants.WHITE).setFontSize(8)
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		cfEnteCanvas.add(cfEnteP);
		cfEnteCanvas.close();
		
		int xt0 = wtl;
		int wtr = 277;
		// Destinatario Avviso Background
		Rectangle destinatariAvvisoGrayRectangle = new Rectangle(xt0, yt, wtr, ht);
		pdfCanvas.saveState().setFillColor(LeggoAsset.bolzanoBluBollettino).rectangle(destinatariAvvisoGrayRectangle).fill();
		Canvas destinatariAvvisoGrayCanvas = new Canvas(pdfCanvas, destinatariAvvisoGrayRectangle);
		destinatariAvvisoGrayCanvas.close();

		int wde = 114;
		mleft = 1;
		x0 = xt0 + mleft * 3;
		//Destinatario Avviso
		Rectangle destinatarioAvvisoRectangle = new Rectangle(x0, y0, wde, h0);
		Canvas destinatarioAvvisoCanvas = new Canvas(pdfCanvas, destinatarioAvvisoRectangle);
		Text destinatarioAvvisoText = new Text("SCHULDNER / DEBITORE:").setFont(asset.getTitillium_bold());
		Paragraph destinatarioAvvisoP = new Paragraph().add(destinatarioAvvisoText).setFontColor(ColorConstants.WHITE)
				.setFontSize(10).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		destinatarioAvvisoCanvas.add(destinatarioAvvisoP);
		destinatarioAvvisoCanvas.close();

		x0 += w0 + 1;
		w0 = 70;
		//Codice Fiscale
		Rectangle codiceFiscaleRectangle2 = new Rectangle(x0, ylcf, wlcf, h0);
		Canvas codiceFiscaleCanvas2 = new Canvas(pdfCanvas, codiceFiscaleRectangle2);
		codiceFiscaleCanvas2.add(codiceFiscaleP);
		codiceFiscaleCanvas2.close();

		x0 += w0 + 1;
		w0 = wtr - mleft - w0;
		//Codice Fiscale Destinatario
		Rectangle cfDestinatarioRectangle = new Rectangle(x0, ycf, wcf, h0);
		Canvas cfDestinatarioCanvas = new Canvas(pdfCanvas, cfDestinatarioRectangle);
		Text cfDestinatarioText = new Text(documento.DatiAnagrafici.get(0).Cf).setFont(asset.getRoboto_regular());
		Paragraph cfDestinatarioP = new Paragraph().add(cfDestinatarioText).setFontColor(ColorConstants.WHITE)
				.setFontSize(8).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		cfDestinatarioCanvas.add(cfDestinatarioP);
		cfDestinatarioCanvas.close();

		//Ente Creditore String
		int we0 = 262;
		int xe0 = xt + mleft;
		int ye0 = 681;
		int f12 = 12;
		int he0 = 40;
		//Ente Creditore
		Rectangle enteCreditoreStringRectangle = new Rectangle(xe0, ye0, we0, he0);
		if(bDebug) {
			System.out.println("DatiCreditore Denominazione1:\r\n" + documento.DatiCreditore.get(0).Denominazione1);
			pdfCanvas.saveState().setFillColor(new DeviceRgb(224, 224, 244)).rectangle(enteCreditoreStringRectangle).fill();
		}
		Canvas enteCreditoreStringCanvas = new Canvas(pdfCanvas, enteCreditoreStringRectangle);
		Text enteCreditoreStringText = new Text(documento.DatiCreditore.get(0).Denominazione1).setFont(asset.getTitillium_bold());
		Paragraph enteCreditoreStringP = new Paragraph().add(enteCreditoreStringText).setFontColor(ColorConstants.BLACK).setFontSize(f12)
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(f12);
		enteCreditoreStringCanvas.add(enteCreditoreStringP);
		enteCreditoreStringCanvas.close();
		
		//Nome Cognome Destinatario
		int wa0 = 252; // per allineare DatiAnagrafici
		int hd1 = 40;
		int wd1 = 259;
		int xd1 = xe0 + wa0 + 35;
		int yd1 = ye0;
		Rectangle nomeCognomeDestinatarioRectangle = new Rectangle(xd1, yd1, wd1, hd1);
		if(bDebug) {
			System.out.println("DatiAnagrafici Denominazione1:\r\n" + documento.DatiAnagrafici.get(0).Denominazione1);
			pdfCanvas.saveState().setFillColor(new DeviceRgb(228, 228, 228)).rectangle(nomeCognomeDestinatarioRectangle).fill();
		}
		Canvas nomeCognomeDestinatarioCanvas = new Canvas(pdfCanvas, nomeCognomeDestinatarioRectangle);
		Text nomeCognomeDestinatarioText = new Text(documento.DatiAnagrafici.get(0).Denominazione1).setFont(asset.getTitillium_bold());
		Paragraph nomeCognomeDestinatarioP = new Paragraph().add(nomeCognomeDestinatarioText).setFontColor(ColorConstants.BLACK).setFontSize(12)
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		nomeCognomeDestinatarioP.setFixedLeading(12);
		nomeCognomeDestinatarioCanvas.add(nomeCognomeDestinatarioP);
		nomeCognomeDestinatarioCanvas.close();

		int ydc20 = ye0 - he0 + 10;
		//"Ente Creditore" ITA (in alternativa Settore Ente)
		String appo = documento.DatiCreditore.get(0).Denominazione2;
		Rectangle settoreEnteRectangle2 = new Rectangle(xe0, ydc20, we0, he0);
		if(bDebug) {
			System.out.println("DatiCreditore Denominazione2:\r\n" + documento.DatiCreditore.get(0).Denominazione2);
			pdfCanvas.saveState().setFillColor(new DeviceRgb(220, 220, 220)).rectangle(settoreEnteRectangle2).fill();
		}
		Canvas settoreEnteCanvas2 = new Canvas(pdfCanvas, settoreEnteRectangle2);
		//Text settoreEnteText2 = new Text(appo).setFont(asset.getTitillium_regular());
		//Nota. Se qui va la "Ente Creditore" ITA ==> font deve essere Bold come sopra per "Ente Creditore" DE 
		Text settoreEnteText2 = new Text(appo).setFont(asset.getTitillium_bold());
		Paragraph settoreEnteP2 = new Paragraph().add(settoreEnteText2).setFontColor(ColorConstants.BLACK).setFontSize(12)
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		settoreEnteP2.setFixedLeading(12);
		settoreEnteCanvas2.add(settoreEnteP2);
		settoreEnteCanvas2.close();

		//Indirizzo Destinatario
		int wdc20 = 259;
		Rectangle indirizzoDestinatarioRectangle1 = new Rectangle(xd1, ydc20, wdc20, he0);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(220, 220, 220)).rectangle(indirizzoDestinatarioRectangle1).fill();
		Canvas indirizzoDestinatarioCanvas1 = new Canvas(pdfCanvas, indirizzoDestinatarioRectangle1);
		String indirizzo = documento.DatiAnagrafici.get(0).Indirizzo;
		Text indirizzoDestinatarioText = new Text(indirizzo).setFont(asset.getTitillium_regular());
		Paragraph indirizzoDestinatarioP = new Paragraph().add(indirizzoDestinatarioText)
				.setFontColor(ColorConstants.BLACK).setFontSize(12).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(12.0f);
		indirizzoDestinatarioCanvas1.add(indirizzoDestinatarioP);
		indirizzoDestinatarioCanvas1.close();

		int yid0 = yd1 - hd1 - 20;
		wdc20 += 10;
		Rectangle indirizzoDestinatarioRectangle2 = new Rectangle(xd1, yid0, wdc20, he0);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(230, 230, 230)).rectangle(indirizzoDestinatarioRectangle2).fill();
		Canvas indirizzoDestinatarioCanvas2 = new Canvas(pdfCanvas, indirizzoDestinatarioRectangle2);
		String clocalita = documento.DatiAnagrafici.get(0).Cap + " "; // 5 + 1 = 6
		String plocalita = " (" + documento.DatiAnagrafici .get(0).Provincia + ")"; // 2 + 2 + 1 = 5
		String localita = documento.DatiAnagrafici.get(0).Citta;
		//Nota. Campo indirizzo con 2 righe di 40 ch cadauno (bUnasolaRigaLocalita == false)
		//      Ma che consente su una riga il nome della max localitï¿½ italiana che ï¿½ 34 ch (bUnasolaRigaLocalita == true)
		boolean bUnasolaRigaLocalita = true;
		int maxLoc = 34;
		if(bUnasolaRigaLocalita) {
			//if(localita.length() > maxLoc) {
			//	if(localita.charAt(maxLoc - 1) == ' ') {
			//		localita = localita.substring(0, maxLoc - 1);
			//	} else if(localita.charAt(maxLoc - 1) == '.') {
			//		localita = localita.substring(0, maxLoc);
			//	} else {
			//		localita = localita.substring(0, maxLoc - 1) + ".";
			//	}
			//}
			localita = ValidaFlusso.truncDesc(localita, maxLoc);
		} else {
			maxLoc = 80 - 11;
			//localita = localita.substring(0, Math.min(localita.length(), maxLoc));
			localita = ValidaFlusso.truncDesc(localita, maxLoc);
		}
		localita = clocalita + localita + plocalita;
		Text localitaDestinatarioText = new Text(localita).setFont(asset.getTitillium_regular());
		Paragraph localitaDestinatarioP = new Paragraph().add(localitaDestinatarioText)
				.setFontColor(ColorConstants.BLACK).setFontSize(12).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(12.0f);
		indirizzoDestinatarioCanvas2.add(localitaDestinatarioP);
		indirizzoDestinatarioCanvas2.close();

		// Info Ente
		int xie = xt;
		int yie = 629;
		int wie= 174;
		int hie = 28;
		Rectangle infoEnteRectangle = new Rectangle(xie, yie, wie, hie);
		String appo3 = documento.DatiCreditore.get(0).Denominazione3;
		if(bDebug) {
			System.out.println("DatiCreditore Denominazione3:\r\n" + appo3);
			pdfCanvas.saveState().setFillColor(new DeviceRgb(212, 210, 212)).rectangle(infoEnteRectangle).fill();
		}
		Canvas infoEnteCanvas = new Canvas(pdfCanvas, infoEnteRectangle);
		Text infoEnteText = new Text(appo3).setFont(asset.getTitillium_regular());
		Paragraph infoEnteP = new Paragraph().add(infoEnteText).setFontColor(ColorConstants.BLACK).setFontSize(8)
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(9);
		infoEnteCanvas.add(infoEnteP);
		infoEnteCanvas.close();
		
		//Numero Verde
		int xnv = xie + 180;
		int ynv = yie + 7;
		int wnv = 60;
		int hnv = 18;
		Rectangle logoNumeroVerdeRectangle = new Rectangle(xnv, ynv, wnv, hnv);
		Canvas logoNumeroVerdeCanvas = new Canvas(pdfCanvas, logoNumeroVerdeRectangle);
		logoNumeroVerdeCanvas.add(asset.getLogo_numeroverde_bolzano().scaleToFit(wnv, hnv));
		logoNumeroVerdeCanvas.close();
		
		//====================================================================================================================================================================//
		//                                                           Sezione QUANTO E QUANDO PAGARE
		//====================================================================================================================================================================//
		int offset = 45;
		//====================================================================================================================================================================//
		//                                                           Lato Sinistro Sezione QUANTO E QUANDO PAGARE
		//====================================================================================================================================================================//
		//Background
		int yqqp0 = 565 + offset;
		Rectangle quantoQuandoPagareGrayRectangle = new Rectangle(xt, yqqp0, wtl, ht);
		pdfCanvas.saveState().setFillColor(LeggoAsset.bolzanoBluBollettino).rectangle(quantoQuandoPagareGrayRectangle).fill();
		Canvas quantoQuandoPagareGrayCanvas = new Canvas(pdfCanvas, quantoQuandoPagareGrayRectangle);
		quantoQuandoPagareGrayCanvas.close();

		//QUANTO E QUANDO PAGARE DE
		int xqqp0 = xt + 12;
		int yqqp1 = yqqp0 + 3;
		int wqqp0 = 145;
		Rectangle quantoQuandoPagareRectangle = new Rectangle(xqqp0, yqqp1, wqqp0, h0);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(120, 0, 0)).rectangle(quantoQuandoPagareRectangle).fill();
		Canvas quantoQuandoPagareCanvas = new Canvas(pdfCanvas, quantoQuandoPagareRectangle);
		Text quantoQuandoPagareText = new Text("WIEVIEL UND WANN BEZAHLEN?").setFont(asset.getTitillium_bold());
		Paragraph quantoQuandoPagareP = new Paragraph().add(quantoQuandoPagareText).setFontColor(ColorConstants.WHITE)
				.setFontSize(10).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		quantoQuandoPagareCanvas.add(quantoQuandoPagareP);
		quantoQuandoPagareCanvas.close();

		//Pagamento rateale testo opzionale DE
		int xqqp1a = xqqp0 + wqqp0 + 2;
		int yqqp1a = yqqp1 + 1;
		int wqqp1 = 186;
		int hqqp1 = 12;
		Rectangle pagamentoRatealeRectangle = new Rectangle(xqqp1a, yqqp1a, wqqp1, hqqp1);
		Canvas pagamentoRatealeCanvas = new Canvas(pdfCanvas, pagamentoRatealeRectangle);
		Text pagamentoRatealeText = new Text(documento.DatiBollettino.size() > 2 ? "Sie kï¿½nnen auch in raten zahlen" : "")
				.setFont(asset.getTitillium_regular());
		Paragraph pagamentoRatealeP = new Paragraph().add(pagamentoRatealeText).setFontColor(ColorConstants.WHITE)
				.setFontSize(8).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		pagamentoRatealeCanvas.add(pagamentoRatealeP);
		pagamentoRatealeCanvas.close();

		//====================================================================================================================================================================//
		//                                                           Lato Destro Sezione QUANTO E QUANDO PAGARE
		//====================================================================================================================================================================//
		//Background
		int xqqp1 = 299;
		Rectangle dovePagareGrayRectangle = new Rectangle(xqqp1, yqqp0, wtr, ht);
		pdfCanvas.saveState().setFillColor(LeggoAsset.bolzanoBluBollettino).rectangle(dovePagareGrayRectangle).fill();
		Canvas dovePagareGrayCanvas = new Canvas(pdfCanvas, dovePagareGrayRectangle);
		dovePagareGrayCanvas.close();

		//QUANTO E QUANDO PAGARE ITA
		int xqqp1b = xqqp1 + 1;
		Rectangle dovePagareRectangle = new Rectangle(xqqp1b, yqqp1, wqqp0, h0);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(120, 0, 0)).rectangle(dovePagareRectangle).fill();
		Canvas dovePagareCanvas = new Canvas(pdfCanvas, dovePagareRectangle);
		Text dovePagareText = new Text("QUANTO E QUANDO PAGARE?").setFont(asset.getTitillium_bold());
		Paragraph dovePagareP = new Paragraph().add(dovePagareText).setFontColor(ColorConstants.WHITE).setFontSize(10)
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		dovePagareCanvas.add(dovePagareP);
		dovePagareCanvas.close();

		//Pagamento rateale testo opzionale ITA
		wqqp1 += 18 - marginleft;
		Rectangle listaCanaliPagamentoRectangle = new Rectangle(xqqp1a, yqqp1a, wqqp1, hqqp1);
		Canvas listaCanaliPagamentoCanvas = new Canvas(pdfCanvas, listaCanaliPagamentoRectangle);
		Text listaCanaliPagamentoText1 = new Text(documento.DatiBollettino.size() > 2 ? "Puoi pagare anche a rate" : "")
				.setFont(asset.getTitillium_regular());
		Paragraph listaCanaliPagamentoP = new Paragraph().add(listaCanaliPagamentoText1)
				.setFontColor(ColorConstants.WHITE).setFontSize(8).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		listaCanaliPagamentoCanvas.add(listaCanaliPagamentoP);
		listaCanaliPagamentoCanvas.close();

		//====================================================================================================================================================================//
		//                                                           1ï¿½ Riga Importo/Data Scadenza/Modalita Pagamento
		//====================================================================================================================================================================//
		int offset2 = offset + 13;
		int xqqp2 = 51;
		int yqqp2 = 523 + offset2;
		int wqqp2 = 50;
		int hqqp2 = 23;
		Rectangle euroDataRectangle = new Rectangle(xqqp2, yqqp2, wqqp2, hqqp2);
		Canvas euroDataCanvas = new Canvas(pdfCanvas, euroDataRectangle);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(120, 0, 0)).rectangle(euroDataRectangle).fill();
		Text euroDataText1 = new Text("Euro").setFont(asset.getTitillium_bold()).setFontSize(15);
		Paragraph euroDataP = new Paragraph().add(euroDataText1)
				.setFontColor(ColorConstants.BLACK).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		euroDataCanvas.add(euroDataP);
		euroDataCanvas.close();

		//Importo
		xqqp2 += wqqp2;
		wqqp2 = 99;
		Rectangle importoRectangle = new Rectangle(xqqp2, yqqp2, wqqp2, hqqp2);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(220, 220, 220)).rectangle(importoRectangle).fill();
		Canvas importoCanvas = new Canvas(pdfCanvas, importoRectangle);
		Text importoText = new Text(mettiVirgolaEPuntiAllImportoInCent(bollettino999.Codeline2Boll))
				.setFont(asset.getTitillium_bold());
		Paragraph importoP = new Paragraph().add(importoText).setFontColor(ColorConstants.BLACK).setFontSize(15)
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		importoCanvas.add(importoP);
		importoCanvas.close();

		int f11 = 10;
		//Label Data Scadenza DE
		xqqp2 += wqqp2 + 1;
		wqqp2 = 80;
		euroDataRectangle = new Rectangle(xqqp2, yqqp2 + 6, wqqp2, hqqp2);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(120, 120, 120)).rectangle(euroDataRectangle).fill();
		euroDataCanvas = new Canvas(pdfCanvas, euroDataRectangle);
		Text euroDataText2 = new Text("Fï¿½lligkeitsdatum").setFont(asset.getTitillium_regular()).setFontSize(f11);
		euroDataP = new Paragraph().add(euroDataText2).setFontColor(ColorConstants.BLACK).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		euroDataCanvas.add(euroDataP);
		euroDataCanvas.close();

		//Label Data Scadenza ITA
		euroDataRectangle = new Rectangle(xqqp2, yqqp2 - 6, wqqp2, hqqp2);
		euroDataCanvas = new Canvas(pdfCanvas, euroDataRectangle);
		euroDataText2 = new Text("Data scadenza").setFont(asset.getTitillium_regular()).setFontSize(f11);
		euroDataP = new Paragraph().add(euroDataText2).setFontColor(ColorConstants.BLACK).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		euroDataCanvas.add(euroDataP);
		euroDataCanvas.close();

		//Data
		xqqp2 += wqqp2 + 1;
		wqqp2 = 100;
		euroDataRectangle = new Rectangle(xqqp2, yqqp2, wqqp2, hqqp2);
		euroDataCanvas = new Canvas(pdfCanvas, euroDataRectangle);
		String appoD =  bollettino999.ScadenzaRata.replace("/", ".");
		Text euroDataText3 = new Text(appoD).setFont(asset.getTitillium_bold()).setFontSize(15);
		euroDataP = new Paragraph().add(euroDataText3)
				.setFontColor(ColorConstants.BLACK).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		euroDataCanvas.add(euroDataP);
		euroDataCanvas.close();

		//Modalita Pagamento DE
		xqqp2 += wqqp2 + 21;
		wqqp2 = 170;
		euroDataRectangle = new Rectangle(xqqp2, yqqp2 + 6, wqqp2, hqqp2);
		euroDataCanvas = new Canvas(pdfCanvas, euroDataRectangle);
		euroDataText2 = new Text("Zu bezahlen: ").setFont(asset.getTitillium_regular()).setFontSize(f11);
		Text euroDataText21 = new Text("einzige Rate").setFont(asset.getTitillium_bold()).setFontSize(f11);
		euroDataP = new Paragraph().add(euroDataText2).add(euroDataText21).setFontColor(ColorConstants.BLACK).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		euroDataCanvas.add(euroDataP);
		euroDataCanvas.close();

		//Modalita Pagamento ITa
		euroDataRectangle = new Rectangle(xqqp2, yqqp2 - 6, wqqp2, hqqp2);
		euroDataCanvas = new Canvas(pdfCanvas, euroDataRectangle);
		euroDataText2 = new Text("Da pagare: ").setFont(asset.getTitillium_regular()).setFontSize(f11);
		euroDataText21 = new Text("rata unica").setFont(asset.getTitillium_bold()).setFontSize(f11);
		euroDataP = new Paragraph().add(euroDataText2).add(euroDataText21).setFontColor(ColorConstants.BLACK).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		euroDataCanvas.add(euroDataP);
		euroDataCanvas.close();

		int offset3 = 15;
		offset2 += offset3; 
		//====================================================================================================================================================================//
		//                                                           2 Riga
		//====================================================================================================================================================================//
		//Avvertenza su variabilita importo DE
		int yqqp3 = 430 + offset2;
		int wqqp3 = wtl - 10;
		int hqqp3 = 58;
		Rectangle importoDescriptionRectangle = new Rectangle(xt, yqqp3, wqqp3, hqqp3);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(212, 212, 212)).rectangle(importoDescriptionRectangle).fill();
		Canvas importoDescriptionCanvas = new Canvas(pdfCanvas, importoDescriptionRectangle);
		//inizio LP PG210070 - 20210806
		//Text importoDescriptionText = new Text("Der angefï¿½hrte Betrag kï¿½nnte sich auf Grund von allfï¿½lligen auch teilweisen Annullierungen oder Gutschriften, Verzugsgebï¿½hren, Strafen, Zinsen oder anderen Kosten ï¿½ndern. Der Schalterbeamte, eine App oder Webseite kï¿½nnten folglich einen anderen Betrag einfordern.").setFont(asset.getTitillium_regular());
		//Paragraph importoDescriptionP = new Paragraph().add(importoDescriptionText).setFontColor(ColorConstants.BLACK)
		//        .setFontSize(8).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(10);
		Text importoDescriptionText = new Text("Der angefï¿½hrte Betrag kï¿½nnte sich auf Grund von allfï¿½lligen auch teilweisen Annullierungen oder Gutschriften, Verzugsgebï¿½hren, Strafen, Zinsen oder anderen Kosten ï¿½ndern. Der Schalterbeamte, eine App oder Webseite kï¿½nnten folglich einen anderen Betrag einfordern.\r\nMitteilung erstellt in Zusammenarbeit mit").setFont(asset.getTitillium_regular());
		Text importoDescription2Text = new Text(" Sï¿½dtiroler Einzugsdienste").setFont(asset.getTitillium_bold());
		Text importoDescription3Text = new Text(".").setFont(asset.getTitillium_regular());
		Paragraph importoDescriptionP = new Paragraph().add(importoDescriptionText).add(importoDescription2Text).add(importoDescription3Text).setFontColor(ColorConstants.BLACK)
				.setFontSize(8).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(10);
		//fine LP PG210070 - 20210806
		importoDescriptionCanvas.add(importoDescriptionP);
		importoDescriptionCanvas.close();

		//Avvertenza su variabilita importo ITA
		wqqp3 = wtr - 10;
		importoDescriptionRectangle = new Rectangle(xd1, yqqp3, wqqp3, hqqp3);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(228, 0, 228)).rectangle(importoDescriptionRectangle).fill();
		importoDescriptionCanvas = new Canvas(pdfCanvas, importoDescriptionRectangle);
		//inizio LP PG210070 - 20210806
		//importoDescriptionText = new Text("L'importo ï¿½ aggiornato automaticamente dal sistema e potrebbe subire variazioni per eventuali sgravi, note di credito, indennitï¿½ di mora, sanzioni o interessi, ecc. Un operatore, il sito o l'app che userï¿½ Le potrebbero quindi chiedere una cifra diversa da quella qui indicata.").setFont(asset.getTitillium_regular());
		//importoDescriptionP = new Paragraph().add(importoDescriptionText).setFontColor(ColorConstants.BLACK)
		//		.setFontSize(8).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(10);
		importoDescriptionText = new Text("L'importo ï¿½ aggiornato automaticamente dal sistema e potrebbe subire variazioni per eventuali sgravi, note di credito, indennitï¿½ di mora, sanzioni o interessi, ecc. Un operatore, il sito o l'app che userï¿½ Le potrebbero quindi chiedere una cifra diversa da quella qui indicata.\r\nAvviso predisposto in collaborazione con").setFont(asset.getTitillium_regular());
		importoDescription2Text = new Text(" Alto Adige Riscossioni").setFont(asset.getTitillium_bold());
		importoDescription3Text = new Text(".").setFont(asset.getTitillium_regular());
		importoDescriptionP = new Paragraph().add(importoDescriptionText).add(importoDescription2Text).add(importoDescription3Text).setFontColor(ColorConstants.BLACK)
				.setFontSize(8).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(10);
		//fine LP PG210070 - 20210806
		importoDescriptionCanvas.add(importoDescriptionP);
		importoDescriptionCanvas.close();

		offset = offset - 125 + offset3; 
		//inizio LP PG210070 - 20210806
		offset -= 10;
		//fine LP PG210070 - 20210806
		//====================================================================================================================================================================//
		//                                                           Sezione DOVE PAGARE?
		//====================================================================================================================================================================//
		//Background
		int ydp0 = 565 + offset;
		int wdp0 = 299 - xt + 1;
		Rectangle dovePagareRectangle2 = new Rectangle(xt, ydp0, wdp0, ht);
		pdfCanvas.saveState().setFillColor(LeggoAsset.bolzanoBluBollettino).rectangle(dovePagareRectangle2).fill();
		Canvas dovePagareGrayRectangle2 = new Canvas(pdfCanvas, dovePagareRectangle2);
		dovePagareGrayRectangle2.close();
		
		//WO BEZAHLEN? Liste der Zahlungsdienstleister
		int ydp1 = 568 + offset;
		int wdp1 = 70;
		int hdp1 = 15;
		quantoQuandoPagareRectangle = new Rectangle(xqqp0, ydp1, wdp1, hdp1);
		quantoQuandoPagareCanvas = new Canvas(pdfCanvas, quantoQuandoPagareRectangle);
		quantoQuandoPagareText = new Text("WO BEZAHLEN?").setFont(asset.getTitillium_bold());
		quantoQuandoPagareP = new Paragraph().add(quantoQuandoPagareText).setFontColor(ColorConstants.WHITE)
				.setFontSize(10).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		quantoQuandoPagareCanvas.add(quantoQuandoPagareP);
		quantoQuandoPagareCanvas.close();

		//Liste der Zahlungsdienstleister
		int xdp2 = xqqp0 + wdp1 + 4;
		int ydp2 = 569 + offset;
		int wdp2 = 204 - marginleft;
		int hdp2 = 12;
		listaCanaliPagamentoRectangle = new Rectangle(xdp2, ydp2, wdp2, hdp2);
		listaCanaliPagamentoCanvas = new Canvas(pdfCanvas, listaCanaliPagamentoRectangle);
		listaCanaliPagamentoText1 = new Text("Liste der Zahlungsdienstleister")
				.setFont(asset.getTitillium_regular());
		Text listaCanaliPagamentoText2 = new Text(" " + "www.pagopa.gov.it").setFont(asset.getTitillium_bold());
		listaCanaliPagamentoP = new Paragraph().add(listaCanaliPagamentoText1).add(listaCanaliPagamentoText2)
				.setFontColor(ColorConstants.WHITE).setFontSize(8).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		listaCanaliPagamentoCanvas.add(listaCanaliPagamentoP);
		listaCanaliPagamentoCanvas.close();

		//Background
		int wdp3 = 297 - marginleft;
		dovePagareGrayRectangle = new Rectangle(xqqp1, ydp0, wdp3, ht);
		pdfCanvas.saveState().setFillColor(LeggoAsset.bolzanoBluBollettino).rectangle(dovePagareGrayRectangle).fill();
		dovePagareGrayCanvas = new Canvas(pdfCanvas, dovePagareGrayRectangle);
		dovePagareGrayCanvas.close();

		// Dove Pagare
		dovePagareRectangle = new Rectangle(xqqp1b, ydp1, wdp1, hdp1);
		dovePagareCanvas = new Canvas(pdfCanvas, dovePagareRectangle);
		dovePagareText = new Text("DOVE PAGARE?").setFont(asset.getTitillium_bold());
		dovePagareP = new Paragraph().add(dovePagareText).setFontColor(ColorConstants.WHITE).setFontSize(10)
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		dovePagareCanvas.add(dovePagareP);
		dovePagareCanvas.close();

		//Lista Canali Pagamento
		int xdp5 = xqqp1b + wdp1 + 1;
		listaCanaliPagamentoRectangle = new Rectangle(xdp5, ydp2, wdp2, hdp2);
		listaCanaliPagamentoCanvas = new Canvas(pdfCanvas, listaCanaliPagamentoRectangle);
		listaCanaliPagamentoText1 = new Text("Lista dei canali di pagamento su")
				.setFont(asset.getTitillium_regular());
		listaCanaliPagamentoText2 = new Text(" " + "www.pagopa.gov.it").setFont(asset.getTitillium_bold());
		listaCanaliPagamentoP = new Paragraph().add(listaCanaliPagamentoText1).add(listaCanaliPagamentoText2)
				.setFontColor(ColorConstants.WHITE).setFontSize(8).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		listaCanaliPagamentoCanvas.add(listaCanaliPagamentoP);
		listaCanaliPagamentoCanvas.close();

		offset2 = offset + 16;
		//====================================================================================================================================================================//
		//                                                           Lato Sinistro Sezione DOVE PAGARE?
		//====================================================================================================================================================================//
		int wdpl = 235;
		int wdpr = 235;
		int ft = 16; //10;
		int fd = 9; //8;
		int fu = 9;
		int xsdp0 = xt + 5;
		int wsdp0 = 532 + offset2;
		int hsdp0 = ft + 4;
		int hsdp1 = (fd + 5) * 3;
		int hbdp = fu + 5;
		//MITTELS INTERNET ODER APP
		Rectangle pagaSitoRectangle = new Rectangle(xsdp0, wsdp0, wdpl, hsdp0);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(210, 0, 210)).rectangle(pagaSitoRectangle).fill();
		Canvas pagaSitoCanvas = new Canvas(pdfCanvas, pagaSitoRectangle);
		Text pagaSitoText = new Text("MITTELS INTERNET ODER APP").setFont(asset.getTitillium_bold());
		Paragraph pagaSitoP = new Paragraph().add(pagaSitoText).setFontColor(ColorConstants.BLACK).setFontSize(ft)
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(ft);
		pagaSitoCanvas.add(pagaSitoP);
		pagaSitoCanvas.close();

		//Paga Sito Description
		int wsdp1 = 485 + offset2;
		Rectangle pagaSitoDescriptionRectangle = new Rectangle(xsdp0, wsdp1, wdpl, hsdp1);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(210, 0, 0)).rectangle(pagaSitoDescriptionRectangle).fill();
		Canvas pagaSitoDescriptionCanvas = new Canvas(pdfCanvas, pagaSitoDescriptionRectangle);
		Text pagaSitoDescriptionText = new Text(
				"de.epays.it," + (tipoStampa.equals("P") ? " der Post,": "") + " Ihrer Bank\r\n" + 
				"oder anderer Zahlungsdienstleister. Sie kï¿½nnen mit\r\n" +
				"Kreditkarten, Bankkonto oder mit CBILL bezahlen.")
						.setFont(asset.getTitillium_regular());
		Paragraph pagaSitoDescriptionP = new Paragraph().add(pagaSitoDescriptionText).setFontColor(ColorConstants.BLACK)
				.setFontSize(fd).setFixedLeading(fd).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(fd);
		pagaSitoDescriptionCanvas.add(pagaSitoDescriptionP);
		pagaSitoDescriptionCanvas.close();

		//IN IHRER UMGEBUNG
		int wsdp2 = 480 + offset2;
		Rectangle pagaTerritorioRectangle = new Rectangle(xsdp0, wsdp2, wdpl, hsdp0);
		Canvas pagaTerritorioCanvas = new Canvas(pdfCanvas, pagaTerritorioRectangle);
		Text pagaTerritorioText = new Text("IN IHRER UMGEBUNG").setFont(asset.getTitillium_bold());
		Paragraph pagaTerritorioP = new Paragraph().add(pagaTerritorioText).setFontColor(ColorConstants.BLACK)
				.setFontSize(ft).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(ft);
		pagaTerritorioCanvas.add(pagaTerritorioP);
		pagaTerritorioCanvas.close();
		
		// Paga Territorio Description
		int wsdp3 = 434 + offset2;
		String appoT = "bei allen Postï¿½mtern, in Banken, Lottoannahmestellen,\r\n" + 
				       "Tabaktrafiken, beim Bancomat, im Supermarkt. Sie kï¿½nnen\r\n" + 
				       "mit Bargeld, Karten oder Bankï¿½berweisung bezahlen."; 
		Rectangle pagaTerritorioDescriptionRectangle = new Rectangle(xsdp0, wsdp3, wdpl, hsdp1);
		Canvas pagaTerritorioDescriptionCanvas = new Canvas(pdfCanvas, pagaTerritorioDescriptionRectangle);//
		Text pagaTerritorioDescriptionText = new Text(appoT)
				.setFont(asset.getTitillium_regular());
		Paragraph pagaTerritorioDescriptionP = new Paragraph().add(pagaTerritorioDescriptionText)
				.setFontColor(ColorConstants.BLACK).setFontSize(fd).setFixedLeading(fd).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		pagaTerritorioDescriptionCanvas.add(pagaTerritorioDescriptionP);
		pagaTerritorioDescriptionCanvas.close();

		//====================================================================================================================================================================//
		//                                                           Centro Sezione DOVE PAGARE?
		//====================================================================================================================================================================//
		// Canali Digitali
		int wcd = 58;
		int hcd = 39;
		int hcf = 39;
		int offcd = 35;
		Rectangle canaliDigitaliRectangle = new Rectangle(xd1 - offcd, 500 + offset2, wcd, hcd);
		Canvas canaliDigitaliCanvas = new Canvas(pdfCanvas, canaliDigitaliRectangle);
		canaliDigitaliCanvas.add(asset.getCanali_digitali().scaleToFit(wcd, hcd));
		canaliDigitaliCanvas.close();

		// Canali Fisici
		Rectangle canaliFisiciRectangle = new Rectangle(xd1 - offcd, 450 + offset2, wcd, hcf);
		Canvas canaliFisiciCanvas = new Canvas(pdfCanvas, canaliFisiciRectangle);
		canaliFisiciCanvas.add(asset.getCanali_fisici().scaleToFit(wcd, hcf));
		canaliFisiciCanvas.close();

		//====================================================================================================================================================================//
		//                                                           Lato Destro Sezione DOVE PAGARE?
		//====================================================================================================================================================================//
		//Paga Sito
		int xddp0 = xd1 + wcd / 2;
		pagaSitoRectangle = new Rectangle(xddp0, wsdp0, wdpr, hsdp0);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(210, 150, 0)).rectangle(pagaSitoRectangle).fill();
		pagaSitoCanvas = new Canvas(pdfCanvas, pagaSitoRectangle);
		pagaSitoText = new Text("SUL SITO O CON LE APP").setFont(asset.getTitillium_bold());
		pagaSitoP = new Paragraph().add(pagaSitoText).setFontColor(ColorConstants.BLACK).setFontSize(ft)
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(ft);
		pagaSitoCanvas.add(pagaSitoP);
		pagaSitoCanvas.close();

		// Paga Sito Description
		pagaSitoDescriptionRectangle = new Rectangle(xddp0, wsdp1, wdpr, hsdp1);
		pagaSitoDescriptionCanvas = new Canvas(pdfCanvas, pagaSitoDescriptionRectangle);
		pagaSitoDescriptionText = new Text(
				"it.epays.it," + (tipoStampa.equals("P") ? " di Poste,": "") + " della tua Banca\r\n" +
				"o degli altri canali di pagamento.\r\n" +
				"Puï¿½ pagare con carte, conto corrente o CBILL.")
						.setFont(asset.getTitillium_regular());
		pagaSitoDescriptionP = new Paragraph().add(pagaSitoDescriptionText).setFontColor(ColorConstants.BLACK)
				.setFontSize(fd).setFixedLeading(fd).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		pagaSitoDescriptionCanvas.add(pagaSitoDescriptionP);
		pagaSitoDescriptionCanvas.close();

		// Paga Territorio
		pagaTerritorioRectangle = new Rectangle(xddp0, wsdp2, wdpr, hsdp0);
		pagaTerritorioCanvas = new Canvas(pdfCanvas, pagaTerritorioRectangle);
		pagaTerritorioText = new Text("SUL TERRITORIO").setFont(asset.getTitillium_bold());
		pagaTerritorioP = new Paragraph().add(pagaTerritorioText).setFontColor(ColorConstants.BLACK)
				.setFontSize(ft).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(ft);
		pagaTerritorioCanvas.add(pagaTerritorioP);
		pagaTerritorioCanvas.close();

		// Paga Territorio Description
		appoT = "in tutti gli Uffici Postali, in Banca, in Ricevitoria,\r\n" +
				"dal Tabaccaio, al Bancomat, al Supermercato.\r\n" +
				"Puï¿½ pagare in contanti, con carte o conto corrente.";
		pagaTerritorioDescriptionRectangle = new Rectangle(xddp0, wsdp3, wdpr, hsdp1);
		pagaTerritorioDescriptionCanvas = new Canvas(pdfCanvas, pagaTerritorioDescriptionRectangle);//
		pagaTerritorioDescriptionText = new Text(appoT).setFont(asset.getTitillium_regular());
		pagaTerritorioDescriptionP = new Paragraph().add(pagaTerritorioDescriptionText)
				.setFontColor(ColorConstants.BLACK).setFontSize(fd).setFixedLeading(fd).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		pagaTerritorioDescriptionCanvas.add(pagaTerritorioDescriptionP);
		pagaTerritorioDescriptionCanvas.close();
		
		offset3 = 28;
		offset2 = offset + offset3;
		//====================================================================================================================================================================//
		//                                                           Bottom Sezione DOVE PAGARE? 
		//====================================================================================================================================================================//
		// Utilizza Porzione DE
		int xbdp = 411 + offset2;
		Rectangle utilizzaPorzioneRectangle = new Rectangle(xsdp0, xbdp, wdpl + 25, hbdp);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(210, 0, 210)).rectangle(utilizzaPorzioneRectangle).fill();
		Canvas utilizzaPorzioneCanvas = new Canvas(pdfCanvas, utilizzaPorzioneRectangle);
		String appoU = "Verwenden Sie den Abschnitt des bevorzugten Zahlungskanals";
		//TODO: multi rata !
		if(documento.DatiBollettino.size() > 2) {
			appoU = ""; //testo con anche "alla rata e"
		}
		Text utilizzaPorzioneText = new Text(appoU).setFont(asset.getTitillium_regular());
		Paragraph utilizzaPorzioneP = new Paragraph().add(utilizzaPorzioneText)
				.setFontColor(ColorConstants.BLACK).setFontSize(fu).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		utilizzaPorzioneCanvas.add(utilizzaPorzioneP);
		utilizzaPorzioneCanvas.close();

		// Utilizza Porzione ITA
		utilizzaPorzioneRectangle = new Rectangle(xd1 - 12, xbdp, wdpr + 44, hbdp);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(210, 0, 112)).rectangle(utilizzaPorzioneRectangle).fill();
		utilizzaPorzioneCanvas = new Canvas(pdfCanvas, utilizzaPorzioneRectangle);
		appoU = "Utilizzi la porzione di avviso relativa";
		//TODO: multi rata !
		if(documento.DatiBollettino.size() > 2) {
			appoU = " alla rata e";
		}
		appoU += " al canale di pagamento che preferisce";
		utilizzaPorzioneText = new Text(appoU).setFont(asset.getTitillium_regular());
		utilizzaPorzioneP = new Paragraph().add(utilizzaPorzioneText)
				.setFontColor(ColorConstants.BLACK).setFontSize(fu).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		utilizzaPorzioneCanvas.add(utilizzaPorzioneP);
		utilizzaPorzioneCanvas.close();

		offset3 = 27;
		offset = offset + offset3; 
		//====================================================================================================================================================================//
		//                                                        Sezione BANCHE PAGOPA 
		//====================================================================================================================================================================//
		// Forbici
		int xfb = 548;
		int wfb = 14;
		int hfb = 10;
		int yfb0 = 386 + offset;
		int yfb = yfb0 + ht;
		int wfb0 = 578 - marginleft;
		
		Rectangle forbiciRectangle = new Rectangle(xfb, yfb, wfb, hfb);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(210, 150, 0)).rectangle(forbiciRectangle).fill();
		Canvas forbiciCanvas = new Canvas(pdfCanvas, forbiciRectangle);
		forbiciCanvas.add(asset.getLogo_forbici_grigio().scaleToFit(14, 10));
		forbiciCanvas.close();

		//Riga per taglio forbici
		Rectangle bollettinoPostalePaBorderRectangle = new Rectangle(xt, yfb, wfb0, 1);
		pdfCanvas.saveState().setFillColor(LeggoAsset.bolzanoRigaPerForbici).rectangle(bollettinoPostalePaBorderRectangle).fill();
		Canvas bollettinoPostalePaBorderCanvas = new Canvas(pdfCanvas, bollettinoPostalePaBorderRectangle);
		bollettinoPostalePaBorderCanvas.close();
		
		//Banche e Altri Canali Background
		Rectangle bancheAltriCanaliGrayRectangle = new Rectangle(xt, yfb0, wfb0, ht);
		pdfCanvas.saveState().setFillColor(LeggoAsset.bolzanoBluBollettino).rectangle(bancheAltriCanaliGrayRectangle).fill();
		Canvas bancheAltriCanaliGrayCanvas = new Canvas(pdfCanvas, bancheAltriCanaliGrayRectangle);
		bancheAltriCanaliGrayCanvas.close();

		offset = offset - 1;
		int xapp = marginleft + 9;
		int yapp = 390 + offset;
		int happ = 15;
		// Banche e Altri Canali
		Rectangle bancheAltriCanaliRectangle = new Rectangle(xapp, yapp, wtl, happ);
		Canvas bancheAltriCanaliCanvas = new Canvas(pdfCanvas, bancheAltriCanaliRectangle);
		Text bancheAltriCanaliText = new Text("BANKEN UND ANDERE KANï¿½LE - BANCHE E ALTRI CANALI").setFont(asset.getTitillium_bold());
		Paragraph bancheAltriCanaliP = new Paragraph().add(bancheAltriCanaliText).setFontColor(ColorConstants.WHITE)
				.setFontSize(10).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		bancheAltriCanaliCanvas.add(bancheAltriCanaliP);
		bancheAltriCanaliCanvas.close();

		// Rata Unica
		int xrataunica = 325;
		int wrataunica = 213 - marginleft;
		Rectangle rataUnicaRectangle = new Rectangle(xrataunica, yapp, wrataunica, happ);
		Canvas rataUnicaCanvas = new Canvas(pdfCanvas, rataUnicaRectangle);
		Text rataUnicaText1 = new Text("Einzige Rate fï¿½llig am / Rata unica entro il").setFont(asset.getTitillium_bold());
		Paragraph rataUnicaP = new Paragraph().add(rataUnicaText1)
				.setFontColor(ColorConstants.WHITE).setFontSize(10).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		rataUnicaCanvas.add(rataUnicaP);
		rataUnicaCanvas.close();

		// Rata Unica Data
		int xapp0 = 513;
		int yapp0 = yapp + 3;
		int wapp0 = 83;
		int ftdata = 12; //10;
		Rectangle rataUnicadataRectangle = new Rectangle(xapp0, yapp0, wapp0, happ);
		Canvas rataUnicadataCanvas = new Canvas(pdfCanvas, rataUnicadataRectangle);
		Text rataUnicadataText = new Text(appoD).setFont(asset.getTitillium_bold());
		Paragraph rataUnicadataP = new Paragraph().add(rataUnicadataText).setFontColor(ColorConstants.WHITE)
				.setFontSize(ftdata).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		rataUnicadataCanvas.add(rataUnicadataP);
		rataUnicadataCanvas.close();

		//====================================================================================================================================================================//
		//                                                        1ï¿½ Riga Sezione Banche PagoPA
		//====================================================================================================================================================================//
		offset = offset + 24; 
		//Euro
		int ftlbeuro = 12;
		//int xapp1 = 455;
		int xapp1 = 425;
		int yapp1 = 347 + offset;
		int wapp1 = 25;
		int happ1 = ftlbeuro + 5;
		Rectangle euroRectangle = new Rectangle(xapp1, yapp1, wapp1, happ1);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(210, 210, 210)).rectangle(euroRectangle).fill();
		Canvas euroCanvas = new Canvas(pdfCanvas, euroRectangle);
		Text euroText = new Text("Euro").setFont(asset.getTitillium_bold());
		Paragraph euroP = new Paragraph().add(euroText).setFontSize(ftlbeuro).setFontColor(ColorConstants.BLACK).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		euroCanvas.add(euroP);
		euroCanvas.close();

		//Importo
		int fteuro = 13;
		//int xapp2 = 495;
		int xapp2 = 465;
		//int wapp2 = 80;
		int wapp2 = 110;
		int happ2 = fteuro + 5;
		Rectangle importoRectangle2 = new Rectangle(xapp2, yapp1, wapp2, happ2);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(210, 210, 210)).rectangle(importoRectangle2).fill();
		Canvas importoCanvas2 = new Canvas(pdfCanvas, importoRectangle2);
		//importoText = new Text(mettiVirgolaEPuntiAllImportoInCent(bollettino999.Codeline2Boll)).setFont(asset.getTitillium_bold);
		//Nota. Importo leggibile da ocr si usa qui e su bollettino postale
		importoText = new Text(mettiVirgolaEPuntiAllImportoInCent(bollettino999.Codeline2Boll)).setFont(asset.getRoboto_bold());
		Paragraph importoP2 = new Paragraph().add(importoText).setFontColor(ColorConstants.BLACK).setFontSize(fteuro)
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		importoCanvas2.add(importoP2);
		importoCanvas2.close();
		//====================================================================================================================================================================//
		//                                                           Lato Sinistro Sezione Banche PagoPA
		//====================================================================================================================================================================//
		offset = offset - 16;
		int fq = 7;
		int hq = (fq + 5) * 4;
		int yapp3a = 353;
		int yapp3 = yapp3a + offset;
		int wapp3 = 119;
		//Banche Altri Canali Description DE
		//Hier finden Sie den QR-Code und den CBILL-Code, um ï¿½ber Banken und andere berechtigte Zahlungsdienstleister zu zahlen.
		Rectangle bancheAltriCanaliDescriptionRectangle = new Rectangle(xt, yapp3 - hq, wapp3, hq);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(210, 210, 210)).rectangle(bancheAltriCanaliDescriptionRectangle).fill();
		Canvas bancheAltriCanaliDescriptionCanvas = new Canvas(pdfCanvas, bancheAltriCanaliDescriptionRectangle);
		Text bancheAltriCanaliDescriptionText1 = new Text("Hier finden Sie den ").setFont(asset.getTitillium_regular());
		Text bancheAltriCanaliDescriptionText2 = new Text(" QR-Code ").setFont(asset.getTitillium_bold());
		Text bancheAltriCanaliDescriptionText3 = new Text("und den ").setFont(asset.getTitillium_regular());
		Text bancheAltriCanaliDescriptionText4 = new Text(" CBILL-Code").setFont(asset.getTitillium_bold());
		Text bancheAltriCanaliDescriptionText5 = new Text(", um ï¿½ber Banken und andere berechtigte Zahlungsdienstleister zu zahlen.").setFont(asset.getTitillium_regular());
		Paragraph bancheAltriCanaliDescriptionP = new Paragraph().add(bancheAltriCanaliDescriptionText1)
				.add(bancheAltriCanaliDescriptionText2).add(bancheAltriCanaliDescriptionText3)
				.add(bancheAltriCanaliDescriptionText4).add(bancheAltriCanaliDescriptionText5)
				.setFontColor(ColorConstants.BLACK).setFontSize(fq).setMargin(0).setFixedLeading(fq);
		bancheAltriCanaliDescriptionCanvas.add(bancheAltriCanaliDescriptionP);
		bancheAltriCanaliDescriptionCanvas.close();
		
		int offset5 = offset - hq - (fq + 5) * 3;
		int yapp4 = yapp3a + offset5;
		//Banche Altri Canali Description ITA
		//Qui accanto trova il codice QR e il codice interbancario CBILL per pagare attraverso il circuito bancario e gli altri canali di pagamento abilitati
		bancheAltriCanaliDescriptionRectangle = new Rectangle(xt, yapp4, wapp3, hq);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(220, 220, 220)).rectangle(bancheAltriCanaliDescriptionRectangle).fill();
		bancheAltriCanaliDescriptionCanvas = new Canvas(pdfCanvas, bancheAltriCanaliDescriptionRectangle);
		bancheAltriCanaliDescriptionText1 = new Text("Qui accanto trova il codice").setFont(asset.getTitillium_regular());
		bancheAltriCanaliDescriptionText2 = new Text(" QR ").setFont(asset.getTitillium_bold());
		bancheAltriCanaliDescriptionText3 = new Text("e il codice interbancario").setFont(asset.getTitillium_regular());
		bancheAltriCanaliDescriptionText4 = new Text(" CBILL ").setFont(asset.getTitillium_bold());
		bancheAltriCanaliDescriptionText5 = new Text("per pagare attraverso il circuito bancario e gli altri canali di pagamento abilitati.").setFont(asset.getTitillium_regular());
		bancheAltriCanaliDescriptionP = new Paragraph().add(bancheAltriCanaliDescriptionText1)
				.add(bancheAltriCanaliDescriptionText2).add(bancheAltriCanaliDescriptionText3)
				.add(bancheAltriCanaliDescriptionText4).add(bancheAltriCanaliDescriptionText5)
				.setFontColor(ColorConstants.BLACK).setFontSize(fq).setMargin(0).setFixedLeading(fq);
		bancheAltriCanaliDescriptionCanvas.add(bancheAltriCanaliDescriptionP);
		bancheAltriCanaliDescriptionCanvas.close();

		//====================================================================================================================================================================//
		//                                                                       QR CODE Sezione Banche PagoPA
		//====================================================================================================================================================================//
		int offsetX = 30;
		int xqr0 = 177 - offsetX;
		int yqr0 = 289 + offset;
		int wqr0 = 70;
		//QR CODE 
		Rectangle qRCodeRectangle = new Rectangle(xqr0, yqr0, wqr0, wqr0);
		Canvas qRCodeCanvas = new Canvas(pdfCanvas, qRCodeRectangle);
		qRCodeCanvas.add(generaQRCode(bollettino999.BarcodePagoPa, pdf).scaleToFit(70, 70));
		qRCodeCanvas.close();

		//====================================================================================================================================================================//
		//                                                                       Lato Destro Sezione Banche PagoPA
		//====================================================================================================================================================================//
		offsetX += 7;
		int xdapp0 = 263 - offsetX;
		int wdapp0 = 42;
		int hdapp0 = 12;
		//Destinatario DE
		Rectangle destinatarioRectangle = new Rectangle(xdapp0, yapp3, wdapp0, hdapp0);
		Canvas destinatarioCanvas = new Canvas(pdfCanvas, destinatarioRectangle);
		Text destinatarioText = new Text("Empfï¿½nger").setFont(asset.getTitillium_regular());
		Paragraph destinatarioPDE = new Paragraph().add(destinatarioText).setFontColor(ColorConstants.BLACK)
				.setFontSize(8).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		destinatarioCanvas.add(destinatarioPDE);
		destinatarioCanvas.close();
		
		//Destinatario ITA
		destinatarioRectangle = new Rectangle(xdapp0, yapp3 - 8, wdapp0, hdapp0);
		destinatarioCanvas = new Canvas(pdfCanvas, destinatarioRectangle);
		destinatarioText = new Text("Destinatario").setFont(asset.getTitillium_regular());
		Paragraph destinatarioP = new Paragraph().add(destinatarioText).setFontColor(ColorConstants.BLACK)
				.setFontSize(8).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		destinatarioCanvas.add(destinatarioP);
		destinatarioCanvas.close();

		//Nome Cognome Destinatario
		int xdapp1 = 315 - offsetX;
		int ydapp1 = 346 + offset + 3;//346 + offset;
		int wdapp1 = 246 + offsetX;
		Rectangle nomeCognomeDestinatarioRectangle2 = new Rectangle(xdapp1, ydapp1, wdapp1, hdapp0);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(225, 0, 10)).rectangle(nomeCognomeDestinatarioRectangle2).fill();
		Canvas nomeCognomeDestinatarioCanvas2 = new Canvas(pdfCanvas, nomeCognomeDestinatarioRectangle2);
		Paragraph nomeCognomeDestinatarioP2 = new Paragraph().add(nomeCognomeDestinatarioText)
				.setFontColor(ColorConstants.BLACK).setFontSize(8).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(8);
		nomeCognomeDestinatarioCanvas2.add(nomeCognomeDestinatarioP2);
		nomeCognomeDestinatarioCanvas2.close();

		//Ente Creditore DE
		int xdapp2 = 263 - offsetX;
		int ydapp2 = 330 + offset;
		int wdapp2 = 49;
		Rectangle destinatarioRectangle2 = new Rectangle(xdapp2, ydapp2, wdapp2, hdapp0);
		Canvas destinatarioCanvas2 = new Canvas(pdfCanvas, destinatarioRectangle2);
		Text destinatarioText2 = new Text("Glï¿½ubiger").setFont(asset.getTitillium_regular());
		Paragraph destinatarioP2 = new Paragraph().add(destinatarioText2).setFontColor(ColorConstants.BLACK)
				.setFontSize(8).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		destinatarioCanvas2.add(destinatarioP2);
		destinatarioCanvas2.close();
		//Ente Creditore ITA
		destinatarioRectangle2 = new Rectangle(xdapp2, ydapp2 - 8, wdapp2, hdapp0);
		destinatarioCanvas2 = new Canvas(pdfCanvas, destinatarioRectangle2);
		destinatarioText2 = new Text("Ente Creditore").setFont(asset.getTitillium_regular());
		destinatarioP2 = new Paragraph().add(destinatarioText2).setFontColor(ColorConstants.BLACK)
				.setFontSize(8).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		destinatarioCanvas2.add(destinatarioP2);
		destinatarioCanvas2.close();

		//Ente Creditore String
		int xdapp3 = 315 - offsetX;
		ydapp2 -= 17;
		int wdapp3 = 246 + offsetX;
		int hdapp3 = (8 + 5) * 2;
		enteCreditoreStringText = new Text(documento.DatiCreditore.get(0).Denominazione1 + "\r\n" + documento.DatiCreditore.get(0).Denominazione2).setFont(asset.getTitillium_bold());
		Rectangle enteCreditoreStringRectangle2 = new Rectangle(xdapp3, ydapp2, wdapp3, hdapp3);
		if(bDebug) {
			System.out.println("======================================================================\r\n" + enteCreditoreStringText.getText() + "\r\n======================================================================");
			pdfCanvas.saveState().setFillColor(new DeviceRgb(212, 210, 212)).rectangle(enteCreditoreStringRectangle2).fill();
		}
		Canvas enteCreditoreStringCanvas2 = new Canvas(pdfCanvas, enteCreditoreStringRectangle2);
		Paragraph enteCreditoreStringP2 = new Paragraph().add(enteCreditoreStringText)
				.setFontColor(ColorConstants.BLACK).setFontSize(8).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(8);
		enteCreditoreStringCanvas2.add(enteCreditoreStringP2);
		enteCreditoreStringCanvas2.close();

		int wdapp4 = 96;
		int ydapp4 = 307 + offset;
		//Oggetto Pagamento DE
		Rectangle oggettoPagamentoRectangle2 = new Rectangle(xdapp2, ydapp4, wdapp4, hdapp0);
		Canvas oggettoPagamentoCanvas2 = new Canvas(pdfCanvas, oggettoPagamentoRectangle2);
		Text oggettoPagamentoText2 = new Text("Zahlungsgrund").setFont(asset.getTitillium_regular());
		Paragraph oggettoPagamentoP2 = new Paragraph().add(oggettoPagamentoText2).setFontColor(ColorConstants.BLACK)
				.setFontSize(8).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		oggettoPagamentoCanvas2.add(oggettoPagamentoP2);
		oggettoPagamentoCanvas2.close();

		//Oggetto Pagamento ITA
		int ydapp5 = 299 + offset;
		oggettoPagamentoRectangle2 = new Rectangle(xdapp2, ydapp5, wdapp4, hdapp0);
		oggettoPagamentoCanvas2 = new Canvas(pdfCanvas, oggettoPagamentoRectangle2);
		oggettoPagamentoText2 = new Text("Oggetto pagamento").setFont(asset.getTitillium_regular());
		oggettoPagamentoP2 = new Paragraph().add(oggettoPagamentoText2).setFontColor(ColorConstants.BLACK)
				.setFontSize(8).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		oggettoPagamentoCanvas2.add(oggettoPagamentoP2);
		oggettoPagamentoCanvas2.close();

		//Oggetto Pagamento String
		int xdapp3a = 337 - offsetX;
		int ydapp3a = 277 + offset;
		int wdapp3a = 224  + offsetX;
		int hdapp3a = (8 + 5) * 3;
		Rectangle oggettoPagamentoStringRectangle2 = new Rectangle(xdapp3a, ydapp3a, wdapp3a, hdapp3a); 
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(0, 210, 10)).rectangle(oggettoPagamentoStringRectangle2).fill();
		Canvas oggettoPagamentoStringCanvas2 = new Canvas(pdfCanvas, oggettoPagamentoStringRectangle2);
		Paragraph oggettoPagamentoStringP2 = new Paragraph().add(oggettoPagamentoText)
				.setFontColor(ColorConstants.BLACK).setFontSize(8).setMargin(0).setFixedLeading(8)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		oggettoPagamentoStringCanvas2.add(oggettoPagamentoStringP2);
		oggettoPagamentoStringCanvas2.close();
		
		//Inzio Righe Coordinate Pagamento
		offset -= 4;
		//Codice Cbill DE
		int hcp0 = 10;
		int ycp0 = 283 + offset;
		int wcp0 = 45;
		Rectangle codiceCbillRectangle = new Rectangle(xdapp2, ycp0, wcp0, hcp0);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(210, 210, 210)).rectangle(codiceCbillRectangle).fill();
		Canvas codiceCbillCanvas = new Canvas(pdfCanvas, codiceCbillRectangle);
		Text codiceCbillText = new Text("CBILL-Kodex").setFont(asset.getTitillium_regular());
		Paragraph codiceCbillPDE = new Paragraph().add(codiceCbillText).setFontColor(ColorConstants.BLACK).setFontSize(8)
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		codiceCbillCanvas.add(codiceCbillPDE);
		codiceCbillCanvas.close();

		//Codice Avviso DE
		int xdapp4 = 325 - offsetX;
		int wcp1 = 151;
		Rectangle codiceAvvisoRectangle = new Rectangle(xdapp4, ycp0, wcp1, hcp0);
		Canvas codiceAvvisoCanvas = new Canvas(pdfCanvas, codiceAvvisoRectangle);
		Text codiceAvvisoText = new Text("Zahlungsmitteilungskodex").setFont(asset.getTitillium_regular());
		Paragraph codiceAvvisoPDE = new Paragraph().add(codiceAvvisoText).setFontColor(ColorConstants.BLACK)
				.setFontSize(8).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		codiceAvvisoCanvas.add(codiceAvvisoPDE);
		codiceAvvisoCanvas.close();
		
		//Cod. Fiscale Ente Creditore DE
		int xdapp5 = 475 - offsetX;
		int wcp2 = 151 + offsetX;
		Rectangle cfEnteCreditoreRectangle = new Rectangle(xdapp5, ycp0, wcp2, hcp0);
		Canvas cfEnteCreditoreCanvas = new Canvas(pdfCanvas, cfEnteCreditoreRectangle);
		Text cfEnteCreditoreText = new Text("St.Nr. der Kï¿½rperschaft").setFont(asset.getTitillium_regular());
		Paragraph cfEnteCreditorePDE = new Paragraph().add(cfEnteCreditoreText).setFontColor(ColorConstants.BLACK)
				.setFontSize(8).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		cfEnteCreditoreCanvas.add(cfEnteCreditorePDE);
		cfEnteCreditoreCanvas.close();

		//Codice Cbill ITA
		int ycp1 = ycp0 - 8;
		codiceCbillRectangle = new Rectangle(xdapp2, ycp1, wcp0, hcp0);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(220, 220, 220)).rectangle(codiceCbillRectangle).fill();
		codiceCbillCanvas = new Canvas(pdfCanvas, codiceCbillRectangle);
		codiceCbillText = new Text("Codice CBILL").setFont(asset.getTitillium_regular());
		Paragraph codiceCbillP = new Paragraph().add(codiceCbillText).setFontColor(ColorConstants.BLACK).setFontSize(8)
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		codiceCbillCanvas.add(codiceCbillP);
		codiceCbillCanvas.close();

		//Codice Avviso ITA
		codiceAvvisoRectangle = new Rectangle(xdapp4, ycp1, wcp1, hcp0);
		codiceAvvisoCanvas = new Canvas(pdfCanvas, codiceAvvisoRectangle);
		codiceAvvisoText = new Text("Codice Avviso PagoPA").setFont(asset.getTitillium_regular());
		Paragraph codiceAvvisoP = new Paragraph().add(codiceAvvisoText).setFontColor(ColorConstants.BLACK)
				.setFontSize(8).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		codiceAvvisoCanvas.add(codiceAvvisoP);
		codiceAvvisoCanvas.close();

		//Cod. Fiscale Ente Creditore ITA
		cfEnteCreditoreRectangle = new Rectangle(xdapp5, ycp1, wcp2, hcp0);
		cfEnteCreditoreCanvas = new Canvas(pdfCanvas, cfEnteCreditoreRectangle);
		cfEnteCreditoreText = new Text("Cod. Fiscale Ente Creditore").setFont(asset.getTitillium_regular());
		Paragraph cfEnteCreditoreP = new Paragraph().add(cfEnteCreditoreText).setFontColor(ColorConstants.BLACK)
				.setFontSize(8).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		cfEnteCreditoreCanvas.add(cfEnteCreditoreP);
		cfEnteCreditoreCanvas.close();

		//Codice Cbill String
		int ycp2 = 264 + offset;
		Rectangle codiceCbillStringRectangle = new Rectangle(xdapp2, ycp2, wcp0, hdapp0);
		Canvas codiceCbillStringCanvas = new Canvas(pdfCanvas, codiceCbillStringRectangle);
		Text codiceCbillStringText = new Text(documento.DatiCreditore.get(0).CodiceInterbancario).setFont(asset.getRoboto_bold());
		Paragraph codiceCbillStringP = new Paragraph().add(codiceCbillStringText).setFontColor(ColorConstants.BLACK)
				.setFontSize(10).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		codiceCbillStringCanvas.add(codiceCbillStringP);
		codiceCbillStringCanvas.close();

		//Codice Avviso String
		Rectangle codiceAvvisoStringRectangle = new Rectangle(xdapp4, ycp2, wcp1, hdapp0);
		Canvas codiceAvvisoStringCanvas = new Canvas(pdfCanvas, codiceAvvisoStringRectangle);
		String formatNumAvviso = formatNumAvviso(bollettino999.AvvisoPagoPa);
		Text codiceAvvisoStringText = new Text(formatNumAvviso).setFont(asset.getRoboto_bold());
		Paragraph codiceAvvisoStringP = new Paragraph().add(codiceAvvisoStringText).setFontColor(ColorConstants.BLACK)
				.setFontSize(10).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		codiceAvvisoStringCanvas.add(codiceAvvisoStringP);
		codiceAvvisoStringCanvas.close();

		//Cod. Fiscale Ente Creditore String
		Rectangle cfEnteCreditoreStringRectangle = new Rectangle(xdapp5, ycp2, wcp2, hdapp0);
		Canvas cfEnteCreditoreStringCanvas = new Canvas(pdfCanvas, cfEnteCreditoreStringRectangle);
		Text cfEnteCreditoreStringText = new Text(documento.DatiCreditore.get(0).Cf).setFont(asset.getRoboto_bold());
		Paragraph cfEnteCreditoreStringP = new Paragraph().add(cfEnteCreditoreStringText)
				.setFontColor(ColorConstants.BLACK).setFontSize(10).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		cfEnteCreditoreStringCanvas.add(cfEnteCreditoreStringP);
		cfEnteCreditoreStringCanvas.close();
		
		offset -= 6;
		if(tipoStampa.equals("P")) {
			//====================================================================================================================================================================//
			//                                                   Sezione Bollettino Postale PagoPA
			//====================================================================================================================================================================//
			// Forbici
			int ybpp0 = 243 + offset;
			int yfb1 = ybpp0 + ht;
			forbiciRectangle = new Rectangle(xfb, yfb1, wfb, hfb);
			forbiciCanvas = new Canvas(pdfCanvas, forbiciRectangle);
			forbiciCanvas.add(asset.getLogo_forbici_grigio().scaleToFit(14, 10));
			forbiciCanvas.close();
	
			//Riga per taglio forbici
			bollettinoPostalePaBorderRectangle = new Rectangle(xt, yfb1, wfb0, 1);
			pdfCanvas.saveState().setFillColor(LeggoAsset.bolzanoRigaPerForbici).rectangle(bollettinoPostalePaBorderRectangle).fill();
			bollettinoPostalePaBorderCanvas = new Canvas(pdfCanvas, bollettinoPostalePaBorderRectangle);
			bollettinoPostalePaBorderCanvas.close();
	
			//Bollettino Postale PA Background
			Rectangle bollettinoPostalePaGrayRectangle = new Rectangle(xt, ybpp0, wfb0, ht);
			pdfCanvas.saveState().setFillColor(LeggoAsset.bolzanoBluBollettino).rectangle(bollettinoPostalePaGrayRectangle).fill();
			Canvas bollettinoPostalePaGrayCanvas = new Canvas(pdfCanvas, bollettinoPostalePaGrayRectangle);
			bollettinoPostalePaGrayCanvas.close();
	
			//Bollettino Postale PA
			int xbpp0 = marginleft + 9;
			//int wbpp0 = 115 + 90;
			int wbpp0 = 205;
			int ybpp1 = 247 + offset;
			Rectangle bollettinoPostalePaRectangle = new Rectangle(xbpp0, ybpp1, wbpp0, h0);
			Canvas bollettinoPostalePaCanvas = new Canvas(pdfCanvas, bollettinoPostalePaRectangle);
			//Text bollettinoPostalePaText = new Text("POSTERLAGSCHEIN - BOLLETTINO POSTALE PA").setFont(asset.getTitillium_bold());
			Text bollettinoPostalePaText = new Text("BOLLETTINO POSTALE PA - POSTERLAGSCHEIN").setFont(asset.getTitillium_bold());
			Paragraph bollettinoPostalePaP = new Paragraph().add(bollettinoPostalePaText).setFontColor(ColorConstants.WHITE)
					.setFontSize(10).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
			bollettinoPostalePaCanvas.add(bollettinoPostalePaP);
			bollettinoPostalePaCanvas.close();
	
			int xbpp1 = xbpp0 + wbpp0 + 20;
			int ybpp2 = ybpp1 + 1;
			int wlbp0 = 51;
			int hlbp0 = 7;
			//Logo Banco Posta
			Rectangle logoBancopostaRectangle = new Rectangle(xbpp1, ybpp2, wlbp0, hlbp0);
			Canvas logoBancopostaCanvas = new Canvas(pdfCanvas, logoBancopostaRectangle);
			logoBancopostaCanvas.add(asset.getLogo_bancoposta_grigio().scaleToFit(wlbp0, hlbp0));
			logoBancopostaCanvas.close();
			//Rata Unica
			int xbpp2 = xbpp1 + wlbp0 + 20;
			int wru = 190;
			Rectangle rataUnicaRectangle2 = new Rectangle(xbpp2, ybpp1, wru, h0);
			if(bDebug)
				pdfCanvas.saveState().setFillColor(new DeviceRgb(212, 0, 212)).rectangle(rataUnicaRectangle2).fill();
			Canvas rataUnicaCanvas2 = new Canvas(pdfCanvas, rataUnicaRectangle2);
			rataUnicaCanvas2.add(rataUnicaP);
			rataUnicaCanvas2.close();
	
			//Rata Unica Data
			int xbpp3 = 513;
			int ybpp3 = ybpp2 + 2;
			int wbpp3 = 83;
			Rectangle rataUnicadataRectangle2 = new Rectangle(xbpp3, ybpp3, wbpp3, h0);
			Canvas rataUnicadataCanvas2 = new Canvas(pdfCanvas, rataUnicadataRectangle2);
			rataUnicadataCanvas2.add(rataUnicadataP);
			rataUnicadataCanvas2.close();
	
			offset += 35 - 4;
			//====================================================================================================================================================================//
			//                                                       Lato Sinistro Sezione Bollettino Postale PagoPA
			//====================================================================================================================================================================//
			//
			//Logo Poste Italiane
			int xlp = xt + 9;
			int ylp = 188 + offset;
			int wlp = 100;
			int hlp = 12;
			Rectangle logoPosteitalianeRectangle = new Rectangle(xlp, ylp, wlp, hlp);
			Canvas logoPosteitalianeCanvas = new Canvas(pdfCanvas, logoPosteitalianeRectangle);
			logoPosteitalianeCanvas.add(asset.getLogo_poste_italiane().scaleToFit(wlp, hlp));
			logoPosteitalianeCanvas.close();
	
			int xlbp = xt + 10;
			int ylbp = 154 + offset;
			int wlbp = 68;
			int hlbp = 28;
			//Logo Bollettino Postale
			Rectangle logoBollettinoPostaleRectangle = new Rectangle(xlbp, ylbp, wlbp, hlbp);
			Canvas logoBollettinoPostaleCanvas = new Canvas(pdfCanvas, logoBollettinoPostaleRectangle);
			logoBollettinoPostaleCanvas.add(asset.getLogo_bollettino_postale().scaleToFit(wlbp, hlbp));
			logoBollettinoPostaleCanvas.close();
	
			int fp8 = 6;
			int xp8 = xt + 10;
			int hp8 = (fp8 + 3) * 5;
			int yp8 = 102 + offset;
			int wp8 = 90;
			//Bollettino Postale Descrizione DE
			Rectangle bollettinoPostaleDescrizioneRectangle = new Rectangle(xp8, 102 + offset, wp8, hp8);
			if(bDebug)
				pdfCanvas.saveState().setFillColor(new DeviceRgb(222, 222, 222)).rectangle(bollettinoPostaleDescrizioneRectangle).fill();
			Canvas bollettinoPostaleDescrizioneCanvas = new Canvas(pdfCanvas, bollettinoPostaleDescrizioneRectangle);
			Text bollettinoPostaleDescrizioneText = new Text("Posterlagschein, in allen Postï¿½mtern sowie auf den von Poste Italiane autorisierten physischen oder digitalen Kanï¿½len zahlbar")
					.setFont(asset.getTitillium_regular());
			Paragraph bollettinoPostaleDescrizioneP = new Paragraph().add(bollettinoPostaleDescrizioneText)
					.setFontColor(ColorConstants.BLACK).setFontSize(fp8).setMargin(0)
					.setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(fp8);
			bollettinoPostaleDescrizioneCanvas.add(bollettinoPostaleDescrizioneP);
			bollettinoPostaleDescrizioneCanvas.close();
			
			//Bollettino Postale Descrizione ITA
			bollettinoPostaleDescrizioneRectangle = new Rectangle(xp8, yp8 - 35, wp8, hp8);
			if(bDebug)
				pdfCanvas.saveState().setFillColor(new DeviceRgb(232, 232, 232)).rectangle(bollettinoPostaleDescrizioneRectangle).fill();
			bollettinoPostaleDescrizioneCanvas = new Canvas(pdfCanvas, bollettinoPostaleDescrizioneRectangle);
			bollettinoPostaleDescrizioneText = new Text(LeggoAsset.BOLLETTINO_POSTALE_DESCRIZIONE)
					.setFont(asset.getTitillium_regular());
			bollettinoPostaleDescrizioneP = new Paragraph().add(bollettinoPostaleDescrizioneText)
					.setFontColor(ColorConstants.BLACK).setFontSize(fp8).setMargin(0)
					.setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(fp8);
			bollettinoPostaleDescrizioneCanvas.add(bollettinoPostaleDescrizioneP);
			bollettinoPostaleDescrizioneCanvas.close();
	
			//Autorizzazione
			int yp9  = 96 + offset - hp8;
			int wp9 = 240;
			int hp9 = 9;
			Rectangle autorizzazioneRectangle = new Rectangle(xp8, yp9, wp9, hp9);
			if(bDebug)
				pdfCanvas.saveState().setFillColor(new DeviceRgb(242, 242, 242)).rectangle(autorizzazioneRectangle).fill();
			Canvas autorizzazioneCanvas = new Canvas(pdfCanvas, autorizzazioneRectangle);
			Text autorizzazioneText = new Text(bollettino999.AutorizCcp).setFont(asset.getTitillium_regular());
			Paragraph autorizzazioneP = new Paragraph().add(autorizzazioneText).setFontColor(LeggoAsset.bolzanoRigaPerForbici)
					.setFontSize(6).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
			autorizzazioneCanvas.add(autorizzazioneP);
			autorizzazioneCanvas.close();

			offsetX = 35;
			//====================================================================================================================================================================//
			//                                                       Lato Destro Sezione Bollettino Postale PagoPA
			//====================================================================================================================================================================//
			//====================================================================================================================================================================//
			//                                                       1ï¿½ Riga Sezione Bollettino Postale PagoPA
			//====================================================================================================================================================================//
			//Logo Euro
			int xle = 180 - offsetX;
			int yle = 187 + offset;
			int wle = 19;
			Rectangle logoEuroRectangle = new Rectangle(xle, yle, wle, wle);
			Canvas logoEuroCanvas = new Canvas(pdfCanvas, logoEuroRectangle);
			logoEuroCanvas.add(asset.getLogo_euro_bollettino().scaleToFit(wle, wle));
			logoEuroCanvas.close();
	
			//Sul CC DE
			int ftlbnumcc = 8;
			int xcc0 = 205 - offsetX;
			int ycc0 = 195 + offset;
			int wcc0 = 85;
			Rectangle sulCcRectangle = new Rectangle(xcc0, ycc0, wcc0, h0); 
			if(bDebug)
				pdfCanvas.saveState().setFillColor(new DeviceRgb(232, 232, 232)).rectangle(sulCcRectangle).fill();
			Canvas sulCcCanvas = new Canvas(pdfCanvas, sulCcRectangle);
			Text sulCcText = new Text("auf das K/K Nr").setFont(asset.getTitillium_regular());
			Paragraph sulCcP = new Paragraph().add(sulCcText).setFontColor(ColorConstants.BLACK).setFontSize(ftlbnumcc)
					.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
			sulCcCanvas.add(sulCcP);
			sulCcCanvas.close();
			
			//Sul CC ITA
			sulCcRectangle = new Rectangle(xcc0, ycc0 - 10, wcc0, h0);
			if(bDebug)
				pdfCanvas.saveState().setFillColor(new DeviceRgb(242, 242, 242)).rectangle(sulCcRectangle).fill();
			sulCcCanvas = new Canvas(pdfCanvas, sulCcRectangle);
			sulCcText = new Text(LeggoAsset.SUL_CC).setFont(asset.getTitillium_regular());
			sulCcP = new Paragraph().add(sulCcText).setFontColor(ColorConstants.BLACK).setFontSize(ftlbnumcc)
					.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
			sulCcCanvas.add(sulCcP);
			sulCcCanvas.close();
	
			//Numero CC Postale
			int ftnumcc = 14;
			int xnumcc = xcc0 + 85;
			int wnumcc = 156;
			Rectangle numeroCcPostaleRectangle = new Rectangle(xnumcc, ycc0, wnumcc, h0);
			Canvas numeroCcPostaleCanvas = new Canvas(pdfCanvas, numeroCcPostaleRectangle);
			Text numeroCcPostaleText = new Text(bollettino999.Codeline12Boll).setFont(asset.getRoboto_bold());
			Paragraph numeroCcPostaleP = new Paragraph().add(numeroCcPostaleText).setFontColor(ColorConstants.BLACK)
					.setFontSize(ftnumcc).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
			numeroCcPostaleCanvas.add(numeroCcPostaleP);
			numeroCcPostaleCanvas.close();
	
			//Euro
			//int xeur = 455; 
			int xeur = 425;
			ycc0 -= 1;
			int heur = 25;
			Rectangle euroRectangle2 = new Rectangle(xeur, ycc0, heur,  ftlbeuro + 5);
			if(bDebug)
				pdfCanvas.saveState().setFillColor(new DeviceRgb(210, 210, 210)).rectangle(importoRectangle2).fill();
			Canvas euroCanvas2 = new Canvas(pdfCanvas, euroRectangle2);
			euroCanvas2.add(euroP);
			euroCanvas2.close();
	
			// Importo
			//int ximp = 495; 
			int ximp = 465;
			ycc0 -= 1;
			//int himp = 80;
			int himp = 110;
			Rectangle importoRectangle3 = new Rectangle(ximp, ycc0, himp,  fteuro + 5);
			if(bDebug)
				pdfCanvas.saveState().setFillColor(new DeviceRgb(210, 210, 210)).rectangle(importoRectangle3).fill();
			Canvas importoCanvas3 = new Canvas(pdfCanvas, importoRectangle3);
			importoCanvas3.add(importoP2);
			importoCanvas3.close();

			//====================================================================================================================================================================//
			//                                                       Data matrix
			//====================================================================================================================================================================//
			//Data matrix Container
			int xdm0 = 480;
			int wdm0 = 93;
			int ydm0 = 83 + offset;
			Rectangle dataMatrixContainerRectangle = new Rectangle(xdm0, ydm0, wdm0, wdm0);
			Canvas dataMatrixContainerCanvas = new Canvas(pdfCanvas, dataMatrixContainerRectangle);
			dataMatrixContainerCanvas.add(asset.getData_matrix_container().scaleToFit(93, 93));
			dataMatrixContainerCanvas.close();
	
			//Data matrix
			int xdm1 = xdm0 + 10;
			int ydm1 = wdm0 + offset;
			int wdm1 = 74;
			Rectangle dataMatrixRectangle = new Rectangle(xdm1, ydm1, wdm1, wdm1);
			Canvas dataMatrixCanvas = new Canvas(pdfCanvas, dataMatrixRectangle);
			dataMatrixCanvas.add(generaDataMatrix(bollettino999.QRcodePagoPa, pdf).scaleToFit(74, 74));
			dataMatrixCanvas.close();
			
			//====================================================================================================================================================================//
			//                                                       2ï¿½ Riga Sezione Bollettino Postale PagoPA
			//====================================================================================================================================================================//
			//Intestato a DE
			int xbp0 = 178 - offsetX;
			int ybp0 = 171 + offset;
			int wbp0 = 41;
			int hbp0 = 12;
			Rectangle intestatoARectangle = new Rectangle(xbp0, ybp0, wbp0, hbp0);
			Canvas intestatoACanvas = new Canvas(pdfCanvas, intestatoARectangle);
			Text intestatoAText = new Text("Lautend auf").setFont(asset.getTitillium_regular());
			Paragraph intestatoAP = new Paragraph().add(intestatoAText).setFontColor(ColorConstants.BLACK).setFontSize(8)
					.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
			intestatoACanvas.add(intestatoAP);
			intestatoACanvas.close();
			
			//Intestato a
			intestatoARectangle = new Rectangle(xbp0, ybp0 - 8, wbp0, hbp0);
			intestatoACanvas = new Canvas(pdfCanvas, intestatoARectangle);
			intestatoAText = new Text(LeggoAsset.INTESTATO_A).setFont(asset.getTitillium_regular());
			intestatoAP = new Paragraph().add(intestatoAText).setFontColor(ColorConstants.BLACK).setFontSize(8)
					.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
			intestatoACanvas.add(intestatoAP);
			intestatoACanvas.close();
	
			// Intestatario CC Postale
			int xbp1 = 226 - offsetX;
			int ybp1 = 154 + offset;
			int wbp1 =  252 + offsetX;
			int hbp1 = (8 + 5) * 2;
			Rectangle intestatarioCCPostaleRectangle = new Rectangle(xbp1, ybp1, wbp1, hbp1);
			if(bDebug)
				pdfCanvas.saveState().setFillColor(new DeviceRgb(232, 232, 232)).rectangle(intestatarioCCPostaleRectangle).fill();
			Canvas intestatarioCCPostaleCanvas = new Canvas(pdfCanvas, intestatarioCCPostaleRectangle);
			Text intestatarioCCPostaleText = null;
			boolean bIntestatarioCC = true;
			if(bIntestatarioCC) {
				intestatarioCCPostaleText = new Text(bollettino999.Descon60Boll).setFont(asset.getTitillium_bold());
			} else {
				intestatarioCCPostaleText = new Text(documento.DatiCreditore.get(0).Denominazione1 + "\r\n" + documento.DatiCreditore.get(0).Denominazione2).setFont(asset.getTitillium_bold());
			}
			Paragraph intestatarioCCPostaleP = new Paragraph().add(intestatarioCCPostaleText)
					.setFontColor(ColorConstants.BLACK).setFontSize(8).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
			intestatarioCCPostaleP.setFixedLeading(8);
			intestatarioCCPostaleCanvas.add(intestatarioCCPostaleP);
			intestatarioCCPostaleCanvas.close();
	
			//====================================================================================================================================================================//
			//                                                       3ï¿½ Riga Sezione Bollettino Postale PagoPA
			//====================================================================================================================================================================//
			// Destinatario DE
			int xbp2 = 178 - offsetX;
			int ybp2 = 146 + offset;
			int wbp2 =  47;
			Rectangle destinatarioRectangle3 = new Rectangle(xbp2, ybp2, wbp2, hbp0);
			Canvas destinatarioCanvas3 = new Canvas(pdfCanvas, destinatarioRectangle3);
			destinatarioCanvas3.add(destinatarioPDE);
			destinatarioCanvas3.close();

			// Destinatario
			destinatarioRectangle3 = new Rectangle(xbp2, ybp2 - 8, wbp2, hbp0);
			destinatarioCanvas3 = new Canvas(pdfCanvas, destinatarioRectangle3);
			destinatarioCanvas3.add(destinatarioP);
			destinatarioCanvas3.close();
	
			// Nome Cognome Destinatario
			int xbp3 = 226 - offsetX;
			int ybp3 = 122 + offset;
			int wbp3 =  252 + offsetX;
			int hbp3 =  32;
			Rectangle nomeCognomeDestinatarioRectangle3 = new Rectangle(xbp3, ybp3, wbp3, hbp3);
			if(bDebug)
				pdfCanvas.saveState().setFillColor(new DeviceRgb(212, 210, 212)).rectangle(nomeCognomeDestinatarioRectangle3).fill();
			Canvas nomeCognomeDestinatarioCanvas3 = new Canvas(pdfCanvas, nomeCognomeDestinatarioRectangle3);
			Text nomeCognomeDestinatarioText2 = new Text(documento.DatiAnagrafici.get(0).Denominazione1).setFont(asset.getTitillium_bold());
			Paragraph nomeCognomeDestinatarioP3 = new Paragraph().add(nomeCognomeDestinatarioText2)
					.setFontColor(ColorConstants.BLACK).setFontSize(8).setMargin(0)
					.setVerticalAlignment(VerticalAlignment.MIDDLE);
			nomeCognomeDestinatarioP3.setFixedLeading(8);
			nomeCognomeDestinatarioCanvas3.add(nomeCognomeDestinatarioP3);
			nomeCognomeDestinatarioCanvas3.close();
	
			//====================================================================================================================================================================//
			//                                                       4ï¿½ Riga Sezione Bollettino Postale PagoPA
			//====================================================================================================================================================================//
			offset -= 4;
			// Oggetto Pagamento DE
			int ybp4 = 126 + offset;
			int wbp4 =  68;
			Rectangle oggettoPagamentoRectangle3 = new Rectangle(xbp2, ybp4, wbp4, hbp0);
			Canvas oggettoPagamentoCanvas3 = new Canvas(pdfCanvas, oggettoPagamentoRectangle3);
			Text oggettoPagamentoText3 = new Text("Zahlungsgrund").setFont(asset.getTitillium_regular());
			Paragraph oggettoPagamentoP3 = new Paragraph().add(oggettoPagamentoText3).setFontColor(ColorConstants.BLACK)
					.setFontSize(8).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
			oggettoPagamentoCanvas3.add(oggettoPagamentoP3);
			oggettoPagamentoCanvas3.close();
			
			// Oggetto Pagamento
			oggettoPagamentoRectangle3 = new Rectangle(xbp2, ybp4 - 8, wbp4, hbp0);
			oggettoPagamentoCanvas3 = new Canvas(pdfCanvas, oggettoPagamentoRectangle3);
			oggettoPagamentoText3 = new Text("Oggetto pagamento").setFont(asset.getTitillium_regular());
			oggettoPagamentoP3 = new Paragraph().add(oggettoPagamentoText3).setFontColor(ColorConstants.BLACK)
					.setFontSize(8).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
			oggettoPagamentoCanvas3.add(oggettoPagamentoP3);
			oggettoPagamentoCanvas3.close();
	
			//Oggetto Pagamento String
			int xbp5 = 254 - offsetX;
			int ybp5 = 109 + offset;
			int wbp5 =  224 + offsetX;
			Rectangle oggettoPagamentoStringRectangle3 = new Rectangle(xbp5, ybp5, wbp5, hbp1);
			if(bDebug)
				pdfCanvas.saveState().setFillColor(new DeviceRgb(225, 0, 22)).rectangle(oggettoPagamentoStringRectangle3).fill();
			Canvas oggettoPagamentoStringCanvas3 = new Canvas(pdfCanvas, oggettoPagamentoStringRectangle3);
			//Text oggettoPagamentoStringText3 = new Text(documento.CausaleDocumento).setFont(asset.getTitillium_bold());
			//Paragraph oggettoPagamentoStringP3 = new Paragraph().add(oggettoPagamentoStringText3)
			//		.setFontColor(ColorConstants.BLACK).setFontSize(8).setMargin(0)
			//		.setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(8);
			Paragraph oggettoPagamentoStringP3 = new Paragraph().add(oggettoPagamentoText)
					.setFontColor(ColorConstants.BLACK).setFontSize(8).setMargin(0)
					.setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(8);
			oggettoPagamentoStringCanvas3.add(oggettoPagamentoStringP3);
			oggettoPagamentoStringCanvas3.close();
	
			//====================================================================================================================================================================//
			//                                                       5ï¿½ Riga Sezione Bollettino Postale PagoPA
			//====================================================================================================================================================================//
			//Coordinate pagamento
			offset -= 5;
			//Codice Avviso DE
			int ybp6 = 103 + offset;
			int wbp6 =  151;
			Rectangle codiceAvvisoRectangle2 = new Rectangle(xbp2, ybp6, wbp6, hbp0);
			Canvas codiceAvvisoCanvas2 = new Canvas(pdfCanvas, codiceAvvisoRectangle2);
			codiceAvvisoCanvas2.add(codiceAvvisoPDE);
			codiceAvvisoCanvas2.close();
	
			//Tipo DE
			int xbp7 = 329 - offsetX;
			int wbp7 =  15;
			Rectangle tipoRectangle = new Rectangle(xbp7, ybp6, wbp7, hbp0);
			Canvas tipoCanvas = new Canvas(pdfCanvas, tipoRectangle);
			Text tipoText = new Text("Typ").setFont(asset.getTitillium_regular());
			Paragraph tipoP = new Paragraph().add(tipoText).setFontColor(ColorConstants.BLACK).setFontSize(8).setMargin(0)
					.setVerticalAlignment(VerticalAlignment.MIDDLE);
			tipoCanvas.add(tipoP);
			tipoCanvas.close();

			//Cod. Fiscale Ente Creditore DE
			int xbp8 = 375 - offsetX;
			int wbp8 =  90;
			Rectangle cfEnteCreditoreRectangle2 = new Rectangle(xbp8, ybp6, wbp8, hbp0);
			Canvas cfEnteCreditoreCanvas2 = new Canvas(pdfCanvas, cfEnteCreditoreRectangle2);
			cfEnteCreditoreCanvas2.add(cfEnteCreditorePDE);
			cfEnteCreditoreCanvas2.close();

			int offesetYc = 8;
			//Codice Avviso
			int ybp6a = ybp6 - offesetYc;
			codiceAvvisoRectangle2 = new Rectangle(xbp2, ybp6a, wbp6, hbp0);
			codiceAvvisoCanvas2 = new Canvas(pdfCanvas, codiceAvvisoRectangle2);
			codiceAvvisoCanvas2.add(codiceAvvisoP);
			codiceAvvisoCanvas2.close();
	
			//Tipo
			tipoRectangle = new Rectangle(xbp7, ybp6a, wbp7, hbp0);
			tipoCanvas = new Canvas(pdfCanvas, tipoRectangle);
			tipoText = new Text(LeggoAsset.TIPO).setFont(asset.getTitillium_regular());
			tipoP = new Paragraph().add(tipoText).setFontColor(ColorConstants.BLACK).setFontSize(8).setMargin(0)
					.setVerticalAlignment(VerticalAlignment.MIDDLE);
			tipoCanvas.add(tipoP);
			tipoCanvas.close();

			//Cod. Fiscale Ente Creditore
			cfEnteCreditoreRectangle2 = new Rectangle(xbp8, ybp6a, wbp8, hbp0);
			cfEnteCreditoreCanvas2 = new Canvas(pdfCanvas, cfEnteCreditoreRectangle2);
			cfEnteCreditoreCanvas2.add(cfEnteCreditoreP);
			cfEnteCreditoreCanvas2.close();

			//Codice Avviso String
			int ybp9 = 93 + offset - offesetYc;
			Rectangle codiceAvvisoStringRectangle2 = new Rectangle(xbp2, ybp9, wbp6, hbp0);
			Canvas codiceAvvisoStringCanvas2 = new Canvas(pdfCanvas, codiceAvvisoStringRectangle2);
			codiceAvvisoStringCanvas2.add(codiceAvvisoStringP);
			codiceAvvisoStringCanvas2.close();
	
			//P1
			int xbp10 = 331 - offsetX;
			int wbp10 = 13;
			Rectangle p1Rectangle = new Rectangle(xbp10, ybp9, wbp10, wbp10);
			Canvas p1Canvas = new Canvas(pdfCanvas, p1Rectangle);
			Text p1Text = new Text(LeggoAsset.P1).setFont(asset.getTitillium_bold());
			Paragraph p1P = new Paragraph().add(p1Text).setFontColor(ColorConstants.BLACK).setFontSize(10).setMargin(0)
					.setVerticalAlignment(VerticalAlignment.MIDDLE);
			p1Canvas.add(p1P);
			p1Canvas.close();
	
			// Cod. Fiscale Ente Creditore String
			int wbp11 = 115;
			Rectangle cfEnteCreditoreStringRectangle2 = new Rectangle(xbp8, ybp9, wbp11, hbp0);
			Canvas cfEnteCreditoreStringCanvas2 = new Canvas(pdfCanvas, cfEnteCreditoreStringRectangle2);
			cfEnteCreditoreStringCanvas2.add(cfEnteCreditoreStringP);
			cfEnteCreditoreStringCanvas2.close();
		}
	
		boolean bfooter = false;
		if(bfooter) {
			//Footer Border
			Rectangle footerBorderRectangle = new Rectangle(0, 25, 595, 1);
			pdfCanvas.saveState().setFillColor(LeggoAsset.bolzanoGrigioForbici).rectangle(footerBorderRectangle).fill();
			Canvas footerBorderCanvas = new Canvas(pdfCanvas, footerBorderRectangle);
			footerBorderCanvas.close();
		}
		
	}

	private static void paginaDueBollettini(PdfPage pageTarget, LeggoAsset asset, Documento documento, PdfDocument pdf, int bollettinoDiPartenza) {
		//TODO
	}

	private static void paginaTreBollettini(PdfPage pageTarget, LeggoAsset asset, Documento documento, PdfDocument pdf, int bollettinoDiPartenza) {
		//TODO
	}

	static Image generaQRCode(String code, PdfDocument pdf) {
		BarcodeQRCode qrCode = new BarcodeQRCode(code);
		return new Image(qrCode.createFormXObject(ColorConstants.BLACK, pdf));
	}

	static Image generaDataMatrix(String code, PdfDocument pdf)
	{
		BarcodeDataMatrix dataMatrix = new BarcodeDataMatrix(code);
		return new Image(dataMatrix.createFormXObject(ColorConstants.BLACK, pdf));
	}

	private static String mettiVirgolaEPuntiAllImportoInCent(String importoSenzaVirgola)
	{
		
		BigDecimal bd = new BigDecimal(importoSenzaVirgola).divide(new BigDecimal("100"));
		String importoConVirgola = formatDecimalNumber(bd);
		return importoConVirgola;
	}
	
	public static String formatDecimalNumber(BigDecimal bdValue)
	{
		DecimalFormat dcFormat = getDecimalFormat();
		bdValue = bdValue.setScale(2, BigDecimal.ROUND_HALF_UP);
		
		return dcFormat.format(bdValue);
	}
	
	public static DecimalFormat getDecimalFormat()
	{
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(); 
		symbols.setDecimalSeparator(',');
		symbols.setGroupingSeparator('.');

		DecimalFormat dcFormat = new DecimalFormat("###,##0.00", symbols);
		return dcFormat;
	}
	
	public static String formatNumAvviso(String numAvviso)
	{
		if(numAvviso.length() == 18) {
			int intervallo = 4;
			char separatore = ' ';
			StringBuilder formattatore = new StringBuilder(numAvviso);
			for(int i = 0; i < numAvviso.length()/intervallo; i++) {
				formattatore.insert(((i + 1) * intervallo) + i, separatore);
			}
			String codAutFormattato = formattatore.toString();
			if(codAutFormattato.charAt(codAutFormattato.length()-1) == separatore) codAutFormattato = codAutFormattato.substring(0, codAutFormattato.length()-1);
	
			return codAutFormattato;
		}
		return numAvviso;
	}
	
	/*
	public static String truncDesc(String desc, int maxDesc) 
	{
		String ret = desc;
		if(desc.length() > maxDesc) {
			if(desc.charAt(maxDesc - 1) == ' ') {
				desc = desc.substring(0, maxDesc - 1);
			} else if(desc.charAt(maxDesc - 1) == '.') {
				desc = desc.substring(0, maxDesc);
			} else {
				desc = desc.substring(0, maxDesc - 1) + ".";
			}
		}
		return desc;
	}
	*/
}

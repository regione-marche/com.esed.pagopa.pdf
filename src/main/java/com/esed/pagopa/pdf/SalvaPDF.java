package com.esed.pagopa.pdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.jsp.tagext.TryCatchFinally;

import org.apache.log4j.Logger;

import com.esed.pagopa.pdf.LeggoAsset.FormatoStampa;
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
import com.seda.payer.commons.geos.Bollettino;
import com.seda.payer.commons.geos.Documento;
import com.seda.payer.commons.geos.Flusso;
import com.seda.payer.commons.inviaAvvisiForGeos.File512;

public class SalvaPDF {
	
	private static final Logger logger = Logger.getLogger(SalvaPDF.class);
	
	private final PropertiesTree propertiesTree;
	
	public SalvaPDF(PropertiesTree propertiesTree) {
		
		this.propertiesTree = propertiesTree;
	}
	
	public byte[] SalvaFile(Flusso flusso) throws IOException, ValidazioneException {
		
//		int stato = 1;
		ByteArrayOutputStream baos = null;

//		il flusso può contenere più di un documento
//		lavoriamone uno per volta
		logger.info("flusso.Documentdata.size() = " + flusso.Documentdata.size());
		for (int i = 0; i < flusso.Documentdata.size(); i++) {

//			validaFlusso controlla i dati del flusso, 
//			se sono corretti restituisce un array contenente la sequenza dei numeri progressivi dei bollettini, 
//			se il numero di bollettini è zero la stampa non parte
			logger.info("inizio Validazione");
			logger.info("flusso.TipoStampa = " + flusso.TipoStampa);
			int[] elencoBollettini = ValidaFlusso.validaFlusso(flusso.Documentdata.get(i), flusso.TipoStampa);
			System.out.println("Validazione eseguita ");
			logger.info("FINE Validazione");
//			chiude il metodo con stato a 1 se il numero dei bollettini è 0
			if (elencoBollettini.length < 1) {
				logger.info("elencoBollettini.length è 0");
				return null;
			}
//			accerta che il bollettino nÂ° 999  vada alla fine
			logger.info("Arrays.sort");
			Arrays.sort(elencoBollettini);
			
			logger.debug(Arrays.toString(elencoBollettini) + "---------------ELENCO BOLLETTINI-----------------");
			
			if (elencoBollettini.length > 0) {
//				File file = creaFile(flusso.Documentdata.get(i).DatiAnagrafici.get(0).Denominazione1);
//				file.getParentFile().mkdirs();
				// Crea il documento su file
				// nel caso di ByteArrayOutputStream 
//				commentare il prossimo rigo e decommentare i successivi 2
//				PdfDocument pdf = new PdfDocument(new PdfWriter(file));
				baos = new ByteArrayOutputStream();
				PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
				logger.info("costruttore LeggoAsset");
				// creato il documento crea il lettore di asset
				LeggoAsset asset =null;
				try {
					
					if(flusso.Documentdata.get(i)==null) logger.info("flusso.Documentdata.get(i) null");
					if(flusso.Documentdata.get(i).DatiCreditore.get(0)==null) logger.info("flusso.Documentdata.get(i).DatiCreditore.get(0) null");
					logger.info("flusso.CuteCute = "+ flusso.CuteCute);
					asset = new LeggoAsset(pdf, flusso.TipoStampa, flusso.Documentdata.get(i).DatiCreditore.get(0).LogoEnte, this.propertiesTree, flusso.CuteCute);
						
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					logger.info("errore1 = " + e.getMessage() + " ");
					logger.info("errore1 = " + e.getCause());
					
				}catch (Throwable e) {
					// TODO: handle exception
					e.printStackTrace();
					logger.info("errore2 = " + e.getMessage());
					logger.info("errore2 = " + e.getCause());
					
				}	
				logger.info(" fine costruttore LeggoAsset");
				// Dimensione e margini del documento
				Document document = new Document(pdf, PageSize.A4);
				document.setMargins(0, 0, 0, 0);
				
//			PREDISPONE LA STAMPA DEL BOLLETTINO N 999 OVVERO LA RATA UNICA
//				paginaUnBollettino(pdf.addNewPage(), asset, flusso.Documentdata.get(i), pdf, flusso.Documentdata.get(i).DatiBollettino.get(elencoBollettini[elencoBollettini.length - 1]) /* PASSA SOLO LA RATA UNICA BOLLETTINO */);
					
				
				Bollettino bollettino999 = flusso.Documentdata.get(i).DatiBollettino
						.stream()
						.filter(x -> x.ProgressivoBoll == 999)
						.findFirst()
						.orElse(null);
				
				if (bollettino999 != null) paginaUnBollettino(pdf.addNewPage(), asset, flusso.Documentdata.get(i), pdf,
								bollettino999 /* PASSARE SOLO UN BOLLETTINO */, flusso.TipoStampa);
				else {
					document.close();
					throw new ValidazioneException("Manca il bollettino rata unica (nÂ° 999)");
				}
				
//				se i bollettini sono 2 allora non c'è rateizzazione perchè Ã¨ il numero 1 e il 999 entrambi con dati coincidenti
//				se invece i bollettini sono almeno 3 il 999 contiene la rata unica e gli altri la rateizzazione
				logger.info("Numero di bollettini nel documento: " + elencoBollettini.length);
				if (elencoBollettini.length > 2) {
					for (int j = 0; j < elencoBollettini.length - 1; ) {
						if (elencoBollettini.length - 1 - j >= 3 && (elencoBollettini.length - 1 - j) != 4) {
							logger.info("chiamato metodo 3 bollettini per pagina");
							paginaTreBollettini(pdf.addNewPage(), asset, flusso.Documentdata.get(i), pdf, j);
							j += 3;
							continue;
						}
						if (elencoBollettini.length - 1 - j >= 2 && (elencoBollettini.length - 1 - j) % 3 != 0) {
							logger.info("chiamato metodo 2 bollettini per pagina");
							paginaDueBollettini(pdf.addNewPage(), asset, flusso.Documentdata.get(i), pdf, j);
							j += 2;
							continue;
						}
						if (j < 0)
							break;
					}
				}
				// Close document
				pdf.close();
				document.close();
//				stato = 0;
			} else
				throw new ValidazioneException("Mancano i bollettini");
		}
//		return stato;
		logger.info("sto per restituire il bite array del pdf");
		return baos.toByteArray();
	}
	
	public int SalvaFileMassivo(UUID uuid, List<File512> listaFile512, String path) {
		int stato = 1;
		
		LeggoAsset.DIRECTORY_SALVATAGGIO_FILE = Paths.get(path).toString();

		listaFile512.stream().filter(x -> x != null).forEach(file512 -> {
			try {
				stampaFile512(uuid, file512);
			} catch (ValidazioneException | IOException | InterruptedException e) {
				logger.error(String.format("%s", uuid), e);
				throw new RuntimeException(e);
			}
		});
		
		stato = 0;
		return stato;
	}

	//GERARCHIA FLUSSO DI DATI
		//<FILE512> --> <DOCUMENTO> -->  <AVVISI (rate) >
		
	//metodo per stampare il singolo File512 e creare il relativo file guida
	private void  stampaFile512(UUID uuid, File512 file512) throws ValidazioneException, IOException, InterruptedException{
		if(file512 != null) {
			
//			Crea il file della guida e del pdf massivo, i file sono omonimi, cambia solo l'estensione
			String nomeFile = generaNomeFile(file512);
			
			File fileGuida = new File(LeggoAsset.DIRECTORY_SALVATAGGIO_FILE, nomeFile + ".txt");
			GuidaDocumento guidaDocumento = new GuidaDocumento(/*nomeFile, */fileGuida);
			
			com.seda.payer.commons.geos.Flusso flusso;
			System.out.println("LeggoAsset.DIRECTORY_SALVATAGGIO_FILE = " + LeggoAsset.DIRECTORY_SALVATAGGIO_FILE);
			flusso = ConvertiFile512FlussoGeos.convertiFlusso(file512, this.propertiesTree);  // CONTROLLA LA CLASSE DEL DOCUMENTO E RESTITUISCE SEMPRE UN OGGETTO FLUSSO
			
			File file = new File(LeggoAsset.DIRECTORY_SALVATAGGIO_FILE, nomeFile + ".pdf");
			file.getParentFile().mkdirs();
			String nomeFileOrigine = file512.getFileName();
			System.out.println("nomeFileOrigine = " + nomeFileOrigine);
			System.out.println("file512.tipoTemplate = " + file512.tipoTemplate);
			System.out.println("flusso.Documentdata.get(0).DatiCreditore.get(0).LogoEnte = " + flusso.Documentdata.get(0).DatiCreditore.get(0).LogoEnte);
			
			// Crea il documento su file
			// nel caso di ByteArrayOutputStream 
			//commentare la prossima istruzione e decommentare i successivi 2
			PdfDocument pdf = new PdfDocument(new PdfWriter(file));
			// ByteArrayOutputStream baos = new ByteArrayOutputStream();
			// PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
			
			// creato il documento crea il lettore di asset
			LeggoAsset asset = new LeggoAsset(pdf, flusso.TipoStampa, flusso.Documentdata.get(0).DatiCreditore.get(0).LogoEnte, this.propertiesTree, flusso.CuteCute);
			
			// Dimensione e margini del documento
			Document document = new Document(pdf, PageSize.A4);
			document.setMargins(0, 0, 0, 0);
			
//			guidaDocumento.numeroPaginaIniziale = 1;
			stampaDocumento(flusso, asset, pdf, guidaDocumento/*, document*/, file.getName(), nomeFileOrigine);
			
//			file512.listaDocumenti.stream().forEach(documento -> {
//				if(documento.listaAvvisi.size() > 0) {
//					try {
//						stampaDocumento(flusso, asset, pdf, guidaDocumento/*, document*/, file.getName(), nomeFileOrigine);
//					} catch (NumberFormatException | IOException | ValidazioneException e) {
//						throw new RuntimeException(e);
//					}
//				}
//			});
			
			pdf.close();
			document.close();
			
			//comprime il file pdf e cancella il file originale
			FileOutputStream fos = new FileOutputStream(file.getAbsolutePath() + ".zip");
			ZipOutputStream zipOut = new ZipOutputStream(fos);
			ZipEntry zipFile = new ZipEntry(file.getName());
			zipOut.putNextEntry(zipFile);
			FileInputStream fisDaZippare = new FileInputStream(file);
			byte[] bytes = new byte[1024];
			int length;
			while((length = fisDaZippare.read(bytes)) >= 0) {
				zipOut.write(bytes, 0, length);
			}
			zipOut.close();
			fisDaZippare.close();
			fos.close();
			file.delete();
			
			Thread.sleep(1000);
		}
		
	}
	
	//metodo per stampare il documento contenuto nel File512 e implementare il relativo file guida
	private int stampaDocumento(
			Flusso flusso, 
			LeggoAsset asset, 
			PdfDocument pdf, 
			GuidaDocumento guidaDocumento, 
			/*Document document,*/
			String nomeFilePDF,
			String nomeFileOrigine) throws NumberFormatException, IOException, ValidazioneException {

		int stato = 1;
//		guidaDocumento.numeroPaginaIniziale = 1;
		for (int i = 0; i < flusso.Documentdata.size(); i++) {
			
			int pagineAggiunteDocumento = 0;
//			validaFlusso controlla i dati del flusso, 
//			se sono corretti restituisce un array contenente la sequenza dei numeri progressivi dei bollettini, 
//			se il numero di bollettini è zero la stampa non parte
			int[] elencoBollettini = ValidaFlusso.validaFlusso(flusso.Documentdata.get(i), flusso.TipoStampa);
//			chiude il metodo con stato a 1 se il numero dei bollettini è 0
			if (elencoBollettini.length < 1) {
				return stato;
			}
//			accerta che il bollettino nÂ° 999  vada alla fine
			Arrays.sort(elencoBollettini);
			
			logger.info(Arrays.toString(elencoBollettini) + "---------------ELENCO BOLLETTINI-----------------");
			
			if (elencoBollettini.length > 0) {
				
//			PREDISPONE LA STAMPA DEL BOLLETTINO NÂ° 999 OVVERO LA RATA UNICA
//				paginaUnBollettino(pdf.addNewPage(), asset, flusso.Documentdata.get(i), pdf, flusso.Documentdata.get(i).DatiBollettino.get(elencoBollettini[elencoBollettini.length - 1]) /* PASSA SOLO LA RATA UNICA BOLLETTINO */);
				Bollettino bollettino999 = flusso.Documentdata.get(i).DatiBollettino
						.stream()
						.filter(x -> x.ProgressivoBoll == 999)
						.findFirst()
						.orElse(null);
				
//				if (bollettino999 != null) {
				paginaUnBollettino(pdf.addNewPage(), asset, flusso.Documentdata.get(i), pdf, bollettino999 /* PASSARE SOLO UN BOLLETTINO */, flusso.TipoStampa);
//				}
//				else {
//					document.close();
//					throw new ValidazioneException("Manca il bollettino rata unica (n. 999)");
//				}
				pagineAggiunteDocumento++;
				
//				se i bollettini sono 2 allora non c'è rateizzazione perchè Ã¨ il numero 1 e il 999 entrambi con dati coincidenti
//				se invece i bollettini sono almeno 3 il 999 contiene la rata unica e gli altri 2 la rateizzazione
				logger.debug(elencoBollettini.length);
				if (elencoBollettini.length > 2) {
					for (int j = 0; j < elencoBollettini.length - 1; ) {
						if (elencoBollettini.length - 1 - j >= 3 && (elencoBollettini.length - 1 - j) != 4) {
							logger.info("chiamato metodo 3 bollettini per pagina");
							paginaTreBollettini(pdf.addNewPage(), asset, flusso.Documentdata.get(i), pdf, j, flusso.TipoStampa);
							j += 3;
							pagineAggiunteDocumento++;
							continue;
						}
						if (elencoBollettini.length - 1 - j >= 2 && (elencoBollettini.length - 1 - j) % 3 != 0) {
							logger.info("chiamato metodo 2 bollettini per pagina");
							paginaDueBollettini(pdf.addNewPage(), asset, flusso.Documentdata.get(i), pdf, j, flusso.TipoStampa);
							j += 2;
							pagineAggiunteDocumento++;
							continue;
						}
						if (j < 0)
							break;
					}
				}
				stato = 0;
			} else {
				throw new ValidazioneException("Mancano i bollettini");
			}
			
			guidaDocumento.aggiungiRigo(
					flusso.Documentdata.get(i).DatiAnagrafici.get(0).Cf,
					Integer.parseInt(flusso.DataFornitura.substring(4)),
					flusso.Documentdata.get(i).NumeroDocumento,
					nomeFilePDF,
					nomeFileOrigine,
//					guidaDocumento.numeroPaginaIniziale, 
//					guidaDocumento.numeroPaginaIniziale + pagineAggiunteDocumento - 1
					pagineAggiunteDocumento,
					flusso.Documentdata.get(i).ImpostaServizio	//PAGONET-303
					);
//			guidaDocumento.numeroPaginaIniziale += pagineAggiunteDocumento;
		}
		
	
		return 0;
	}

	private void paginaUnBollettino(PdfPage pageTarget, LeggoAsset asset, Documento documento, PdfDocument pdf, Bollettino bollettino999, String tipoStampa) {
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		boolean bDebug = false; //se == true mostra alcune aree con fill colorato per verificare posizione e dimensione 
		//fine LP PG200420 - Errori nel pdf dell'avviso
//		creazione del pdf canvas
		PdfCanvas pdfCanvas = new PdfCanvas(pageTarget);
		
	

		// Logo PagoPa
		Rectangle logoPagopaRectangle = new Rectangle(32, 789, 35, 23);
		Canvas logoPagopaCanvas = new Canvas(pdfCanvas, logoPagopaRectangle);
		logoPagopaCanvas.add(asset.getLogo_pagopa().scaleToFit(35, 23));
		logoPagopaCanvas.close();

		// Avviso di Pagamento
		Rectangle avvisoPagamentoRectangle = new Rectangle(70, 800, 174, 15);
		Canvas avvisoPagamentoCanvas = new Canvas(pdfCanvas, avvisoPagamentoRectangle);
		Text avvisoPagamentoText = new Text("AVVISO DI PAGAMENTO").setFont(asset.getTitoloFont());
		Paragraph avvisoPagamentoP = new Paragraph().add(avvisoPagamentoText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getTitoloSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		avvisoPagamentoCanvas.add(avvisoPagamentoP);
		avvisoPagamentoCanvas.close();

		// Logo Ente
		Rectangle logoEnteRectangle = new Rectangle(480, 732, asset.getPathLogoEnteSizeX(), asset.getPathLogoEnteSizeY()); //valori di default 84x84px
		Canvas logoEnteCanvas = new Canvas(pdfCanvas, logoEnteRectangle);
		logoEnteCanvas.add(asset.getLogo_ente().scaleToFit(asset.getPathLogoEnteSizeX(), asset.getPathLogoEnteSizeY())); 
		logoEnteCanvas.close();

		// Oggetto del Pagamento
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		//Rectangle oggettoPagamentoRectangle = new Rectangle(32, 720, 445, 58);
		Rectangle oggettoPagamentoRectangle = new Rectangle(32, 720, 445 - 30 - 15 - 7, 58);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(222, 0, 12)).rectangle(oggettoPagamentoRectangle).fill();
		//fine LP PG200420 - Errori nel pdf dell'avviso
		Canvas oggettoPagamentoCanvas = new Canvas(pdfCanvas, oggettoPagamentoRectangle);
		Text oggettoPagamentoText = new Text(documento.CausaleDocumento).setFont(asset.getTestataFont());
		Paragraph oggettoPagamentoP = new Paragraph().add(oggettoPagamentoText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getTestataSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(18);
		oggettoPagamentoCanvas.add(oggettoPagamentoP);
		oggettoPagamentoCanvas.close();

		// Ente Creditore Background
		Rectangle enteCreditoreGrayRectangle = new Rectangle(0, 702, 297, 17);
		pdfCanvas.saveState().setFillColor(LeggoAsset.grigioBollettino).rectangle(enteCreditoreGrayRectangle).fill();
		Canvas enteCreditoreGrayCanvas = new Canvas(pdfCanvas, enteCreditoreGrayRectangle);
		enteCreditoreGrayCanvas.close();

		// Ente Creditore
		Rectangle enteCreditoreRectangle = new Rectangle(32, 706 - asset.getYoffSet(), 89, 15);
		Canvas enteCreditoreCanvas = new Canvas(pdfCanvas, enteCreditoreRectangle);
		Text enteCreditoreText = new Text("ENTE CREDITORE")
				.setFont(asset.getInEvidenza1Font());
		Paragraph enteCreditoreP = new Paragraph().add(enteCreditoreText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getInEvidenza1Size()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		enteCreditoreCanvas.add(enteCreditoreP);
		enteCreditoreCanvas.close();

		// Testo "Codice Fiscale"
		Rectangle codiceFiscaleRectangle = new Rectangle(119 + asset.getXoffSet(), 707 - asset.getYoffSet(), 50, 12);
		Canvas codiceFiscaleCanvas = new Canvas(pdfCanvas, codiceFiscaleRectangle);
		Text codiceFiscaleText = new Text("Cod. Fiscale").setFont(asset.getInEvidenza2Font());
		Paragraph codiceFiscaleP = new Paragraph().add(codiceFiscaleText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getInEvidenza2Size()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		codiceFiscaleCanvas.add(codiceFiscaleP);
		codiceFiscaleCanvas.close();

		// CF Ente
		Rectangle cfEnteRectangle = new Rectangle(167 + asset.getXoffSet()*2, 708 - asset.getYoffSet() / 2, 124, 10);
		Canvas cfEnteCanvas = new Canvas(pdfCanvas, cfEnteRectangle);
		Text cfEnteText = new Text(documento.DatiCreditore.get(0).Cf).setFont(asset.getInEvidenza3Font());
		Paragraph cfEnteP = new Paragraph().add(cfEnteText).setFontColor(ColorConstants.BLACK).setFontSize(asset.getInEvidenza3Size())
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		cfEnteCanvas.add(cfEnteP);
		cfEnteCanvas.close();

		// Destinatario Avviso Background
		Rectangle destinatariAvvisoGrayRectangle = new Rectangle(299, 702, 297, 17);
		pdfCanvas.saveState().setFillColor(LeggoAsset.grigioBollettino).rectangle(destinatariAvvisoGrayRectangle)
				.fill();
		Canvas destinatariAvvisoGrayCanvas = new Canvas(pdfCanvas, destinatariAvvisoGrayRectangle);
		destinatariAvvisoGrayCanvas.close();

		// Destinatario Avviso
		Rectangle destinatarioAvvisoRectangle = new Rectangle(318, 706 - asset.getYoffSet(), 125, 15);
		Canvas destinatarioAvvisoCanvas = new Canvas(pdfCanvas, destinatarioAvvisoRectangle);
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		//Text destinatarioAvvisoText = new Text(documento.DatiAnagrafici.get(0).Denominazione1)
		//		.setFont(asset.getTitillium_bold());
		Text destinatarioAvvisoText = new Text("DESTINATARIO AVVISO").setFont(asset.getInEvidenza1Font());
		//fine LP PG200420 - Errori nel pdf dell'avviso
		Paragraph destinatarioAvvisoP = new Paragraph().add(destinatarioAvvisoText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getInEvidenza1Size()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		destinatarioAvvisoCanvas.add(destinatarioAvvisoP);
		destinatarioAvvisoCanvas.close();

		// Codice Fiscale
		Rectangle codiceFiscaleRectangle2 = new Rectangle(435 + asset.getXoffSet(), 707 - asset.getYoffSet(), 50, 12);
		Canvas codiceFiscaleCanvas2 = new Canvas(pdfCanvas, codiceFiscaleRectangle2);
		codiceFiscaleCanvas2.add(codiceFiscaleP);
		codiceFiscaleCanvas2.close();

		// CF Destinatario
		Rectangle cfDestinatarioRectangle = new Rectangle(482 + asset.getXoffSet() * 2, 708 - asset.getYoffSet() / 2, 112, 10);
		Canvas cfDestinatarioCanvas = new Canvas(pdfCanvas, cfDestinatarioRectangle);
		Text cfDestinatarioText = new Text(documento.DatiAnagrafici.get(0).Cf).setFont(asset.getInEvidenza3Font());
		Paragraph cfDestinatarioP = new Paragraph().add(cfDestinatarioText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getInEvidenza3Size()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		cfDestinatarioCanvas.add(cfDestinatarioP);
		cfDestinatarioCanvas.close();

		// Ente Creditore String
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		//Rectangle enteCreditoreStringRectangle = new Rectangle(30, 640, 261, 47);
		//Canvas enteCreditoreStringCanvas = new Canvas(pdfCanvas, enteCreditoreStringRectangle);
		//Text enteCreditoreStringText = new Text(documento.DatiCreditore.get(0).Denominazione1)
		//		.setFont(asset.getTitillium_bold());
		//Paragraph enteCreditoreStringP = new Paragraph().add(enteCreditoreStringText).setFontColor(ColorConstants.BLACK)
		//		.setFontSize(12).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(14);
		//enteCreditoreStringCanvas.add(enteCreditoreStringP);
		//enteCreditoreStringCanvas.close();
		Rectangle enteCreditoreStringRectangle = new Rectangle(30, 670 - 17 + 4 - asset.getYoffSet() * 4, 261 - 24 - 12, 48 - 8);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(224, 224, 244)).rectangle(enteCreditoreStringRectangle).fill();
		Canvas enteCreditoreStringCanvas = new Canvas(pdfCanvas, enteCreditoreStringRectangle);
		Text enteCreditoreStringText = new Text(documento.DatiCreditore.get(0).Denominazione1).setFont(asset.getDenominazioneNome1Font());
		Paragraph enteCreditoreStringP = new Paragraph().add(enteCreditoreStringText).setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome1Size())
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		enteCreditoreStringP.setFixedLeading(12);
		enteCreditoreStringCanvas.add(enteCreditoreStringP);
		enteCreditoreStringCanvas.close();
		//fine LP PG200420 - Errori nel pdf dell'avviso

		// Nome Cognome Destinatario
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		//Rectangle nomeCognomeDestinatarioRectangle = new Rectangle(318, 672, 273, 17);
		//Canvas nomeCognomeDestinatarioCanvas = new Canvas(pdfCanvas, nomeCognomeDestinatarioRectangle);
		//Text nomeCognomeDestinatarioText = new Text(documento.DatiAnagrafici.get(0).Denominazione1)
		//		.setFont(asset.getTitillium_bold());
		//Paragraph nomeCognomeDestinatarioP = new Paragraph().add(nomeCognomeDestinatarioText)
		//		.setFontColor(ColorConstants.BLACK).setFontSize(12).setMargin(0)
		//		.setVerticalAlignment(VerticalAlignment.MIDDLE);
		//nomeCognomeDestinatarioCanvas.add(nomeCognomeDestinatarioP);
		//nomeCognomeDestinatarioCanvas.close();
		Rectangle nomeCognomeDestinatarioRectangle = new Rectangle(318, 670 - 17 + 4 - asset.getYoffSet() * 4, 174 + 15, 48 - 8);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(228, 228, 228)).rectangle(nomeCognomeDestinatarioRectangle).fill();
		Canvas nomeCognomeDestinatarioCanvas = new Canvas(pdfCanvas, nomeCognomeDestinatarioRectangle);
		Text nomeCognomeDestinatarioText = new Text(documento.DatiAnagrafici.get(0).Denominazione1).setFont(asset.getDenominazioneNome1Font());
		Paragraph nomeCognomeDestinatarioP = new Paragraph().add(nomeCognomeDestinatarioText).setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome1Size())
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		nomeCognomeDestinatarioP.setFixedLeading(12);
		nomeCognomeDestinatarioCanvas.add(nomeCognomeDestinatarioP);
		nomeCognomeDestinatarioCanvas.close();
		//fine LP PG200420 - Errori nel pdf dell'avviso

		// Settore Ente
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		//Rectangle settoreEnteRectangle = new Rectangle(30, 612, 261, 40);
		//Canvas settoreEnteCanvas = new Canvas(pdfCanvas, settoreEnteRectangle);
		//Text settoreEnteText = new Text(documento.DatiCreditore.get(0).Denominazione2)
		//		.setFont(asset.getTitillium_regular());
		//Paragraph settoreEnteP = new Paragraph().add(settoreEnteText).setFontColor(ColorConstants.BLACK).setFontSize(12)
		//		.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(10);
		//settoreEnteCanvas.add(settoreEnteP);
		//settoreEnteCanvas.close();
		String appo = documento.DatiCreditore.get(0).Denominazione2;
		Rectangle settoreEnteRectangle2 = new Rectangle(30, 618  + 2,  261 - 8, 48 - 8); //261 => 54 ch
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(220, 220, 220)).rectangle(settoreEnteRectangle2).fill();
		Canvas settoreEnteCanvas2 = new Canvas(pdfCanvas, settoreEnteRectangle2);
		Text settoreEnteText2 = new Text(appo).setFont(asset.getDenominazioneDettaglio1Font());
		Paragraph settoreEnteP2 = new Paragraph().add(settoreEnteText2).setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneDettaglio1Size())
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		settoreEnteP2.setFixedLeading(12);
		settoreEnteCanvas2.add(settoreEnteP2);
		settoreEnteCanvas2.close();
		//fine LP PG200420 - Errori nel pdf dell'avviso

		// Indirizzo Destinatario
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		//Rectangle indirizzoDestinatarioRectangle = new Rectangle(318, 618, 279, 39);
		//Nota. per retro comnpatibilitÃ  di usi diversi di pagonet web.
		//inizio LP PG21X007
		//if(documento.DatiAnagrafici .get(0).Cap == null && documento.DatiAnagrafici .get(0).Cap.trim().length() == 0) {
		if(documento.DatiAnagrafici.get(0).Cap == null || documento.DatiAnagrafici.get(0).Cap.trim().length() == 0) {
		//fine LP PG21X007
			Rectangle indirizzoDestinatarioRectangle = new Rectangle(318, 639 + 2, 279, 39);
		//fine LP PG200420 - Errori nel pdf dell'avviso
			Canvas indirizzoDestinatarioCanvas = new Canvas(pdfCanvas, indirizzoDestinatarioRectangle);
			Text indirizzoDestinatarioText = new Text(componiIndirizzo(documento.DatiAnagrafici.get(0).Indirizzo,
				documento.DatiAnagrafici.get(0).Citta, documento.DatiAnagrafici.get(0).Provincia))
							.setFont(asset.getDenominazioneNome1Font());
			Paragraph indirizzoDestinatarioP = new Paragraph().add(indirizzoDestinatarioText)
					.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome1Size()).setMargin(0)
					.setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(12.0f);
			indirizzoDestinatarioCanvas.add(indirizzoDestinatarioP);
			indirizzoDestinatarioCanvas.close();
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		} else {
			Rectangle indirizzoDestinatarioRectangle1 = new Rectangle(318, 639 + 2, 279, 17);
			Canvas indirizzoDestinatarioCanvas1 = new Canvas(pdfCanvas, indirizzoDestinatarioRectangle1);
			String indirizzo = documento.DatiAnagrafici.get(0).Indirizzo;
			indirizzo = indirizzo.substring(0, Math.min(indirizzo.length(), 40));
			Text indirizzoDestinatarioText = new Text(indirizzo).setFont(asset.getDenominazioneNome1Font());
			Paragraph indirizzoDestinatarioP = new Paragraph().add(indirizzoDestinatarioText)
					.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome1Size()).setMargin(0)
					.setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(12.0f);
			indirizzoDestinatarioCanvas1.add(indirizzoDestinatarioP);
			indirizzoDestinatarioCanvas1.close();
	
			Rectangle indirizzoDestinatarioRectangle2 = new Rectangle(318, 618 + 2 + 2 + 4 + 2 + 1, 279, 17);
			Canvas indirizzoDestinatarioCanvas2 = new Canvas(pdfCanvas, indirizzoDestinatarioRectangle2);
			String clocalita = documento.DatiAnagrafici .get(0).Cap + " "; //5+1=6
			String plocalita = " (" + documento.DatiAnagrafici .get(0).Provincia + ")"; //2+2+1=5
			String localita = documento.DatiAnagrafici.get(0).Citta; //max 29
			localita = localita.substring(0, Math.min(localita.length(), 29));
			localita = clocalita + localita + plocalita;
			Text localitaDestinatarioText = new Text(localita).setFont(asset.getDenominazioneNome1Font());
			Paragraph localitaDestinatarioP = new Paragraph().add(localitaDestinatarioText)
					.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome1Size()).setMargin(0)
					.setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(12.0f);
			indirizzoDestinatarioCanvas2.add(localitaDestinatarioP);
			indirizzoDestinatarioCanvas2.close();
		}
		//fine LP PG200420 - Errori nel pdf dell'avviso

		// Info Ente
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		//Rectangle infoEnteRectangle = new Rectangle(32, 565, 259, 52);
		//Canvas infoEnteCanvas = new Canvas(pdfCanvas, infoEnteRectangle);
		//Text infoEnteText = new Text(documento.DatiCreditore.get(0).Denominazione3).setFont(asset.getTitillium_regular());
		//Paragraph infoEnteP = new Paragraph().add(infoEnteText).setFontColor(ColorConstants.BLACK).setFontSize(8)
		//		.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(10);
		//infoEnteCanvas.add(infoEnteP);
		//infoEnteCanvas.close();
		String appo3 = documento.DatiCreditore.get(0).Denominazione3;
		
		//elimina sezione denominazione3 per GEOS
		if(asset.getFormatoStampa() == FormatoStampa.GEOS) {
			appo3 = "";
		}
		if (appo3 == null)
			appo3 = "";
		
		System.out.println("appo3 = " + appo3);
		int yIni = 618 - 5 - 5 - 5;
		int ySpace1 = 0;
		ySpace1 = 8 + 2;
		ySpace1 *= 2;
		Rectangle infoEnteRectangle = new Rectangle(30, yIni - ySpace1 - 4 - 4, 400 - 20 - 20, 48 - 8);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(212, 210, 212)).rectangle(infoEnteRectangle).fill();
		Canvas infoEnteCanvas = new Canvas(pdfCanvas, infoEnteRectangle);
		
		Text infoEnteText = new Text(appo3).setFont(asset.getTitillium_regular());
		Paragraph infoEnteP = new Paragraph().add(infoEnteText).setFontColor(ColorConstants.BLACK).setFontSize(8)
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		infoEnteP.setFixedLeading(8);
		infoEnteCanvas.add(infoEnteP);
		infoEnteCanvas.close();
		//fine LP PG200420 - Errori nel pdf dell'avviso

		// Quanto e Qundo Pagare Background
		Rectangle quantoQuandoPagareGrayRectangle = new Rectangle(0, 565, 297, 17);
		pdfCanvas.saveState().setFillColor(LeggoAsset.grigioBollettino).rectangle(quantoQuandoPagareGrayRectangle)
				.fill();
		Canvas quantoQuandoPagareGrayCanvas = new Canvas(pdfCanvas, quantoQuandoPagareGrayRectangle);
		quantoQuandoPagareGrayCanvas.close();

		// Quanto e Quando Pagare
		Rectangle quantoQuandoPagareRectangle = new Rectangle(32, 568 - asset.getYoffSet(), 153, 15);
		Canvas quantoQuandoPagareCanvas = new Canvas(pdfCanvas, quantoQuandoPagareRectangle);
		Text quantoQuandoPagareText = new Text("QUANTO E QUANDO PAGARE?").setFont(asset.getInEvidenza1Font());
		Paragraph quantoQuandoPagareP = new Paragraph().add(quantoQuandoPagareText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getInEvidenza1Size()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		quantoQuandoPagareCanvas.add(quantoQuandoPagareP);
		quantoQuandoPagareCanvas.close();

		// Pagamento rateale testo opzionale
		Rectangle pagamentoRatealeRectangle = new Rectangle(175 + (int)(asset.getXoffSet() / 1.5) , 569 - asset.getYoffSet(), 186, 12);
		Canvas pagamentoRatealeCanvas = new Canvas(pdfCanvas, pagamentoRatealeRectangle);
		Text pagamentoRatealeText = new Text(documento.DatiBollettino.size() > 2 ? "Puoi pagare anche a rate" : "")
				.setFont(asset.getInEvidenza2Font());
		Paragraph pagamentoRatealeP = new Paragraph().add(pagamentoRatealeText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getInEvidenza2Size()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		pagamentoRatealeCanvas.add(pagamentoRatealeP);
		pagamentoRatealeCanvas.close();

		// Dove Pagare Background
		Rectangle dovePagareGrayRectangle = new Rectangle(299, 565, 297, 17);
		pdfCanvas.saveState().setFillColor(LeggoAsset.grigioBollettino).rectangle(dovePagareGrayRectangle).fill();
		Canvas dovePagareGrayCanvas = new Canvas(pdfCanvas, dovePagareGrayRectangle);
		dovePagareGrayCanvas.close();

		// Dove Pagare
		Rectangle dovePagareRectangle = new Rectangle(317, 568 - asset.getYoffSet(), 80, 15);
		Canvas dovePagareCanvas = new Canvas(pdfCanvas, dovePagareRectangle);
		Text dovePagareText = new Text("DOVE PAGARE?").setFont(asset.getInEvidenza1Font());
		Paragraph dovePagareP = new Paragraph().add(dovePagareText).setFontColor(ColorConstants.BLACK).setFontSize(asset.getInEvidenza1Size())
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		dovePagareCanvas.add(dovePagareP);
		dovePagareCanvas.close();

		// Lista Canali Pagamento
		Rectangle listaCanaliPagamentoRectangle = new Rectangle(388 + asset.getXoffSet() , 569 - asset.getYoffSet(), 210, 12);
		Canvas listaCanaliPagamentoCanvas = new Canvas(pdfCanvas, listaCanaliPagamentoRectangle);
		Text listaCanaliPagamentoText1 = new Text("Lista dei canali di pagamento su ")
				.setFont(asset.getInEvidenza2Font()).setFontSize(asset.getInEvidenza2Size());
		Text listaCanaliPagamentoText2 = new Text(" " + "www.pagopa.gov.it").setFont(asset.getInEvidenza3Font()).setFontSize(asset.getInEvidenza3Size() -  asset.getYoffSet());
		Paragraph listaCanaliPagamentoP = new Paragraph().add(listaCanaliPagamentoText1).add(listaCanaliPagamentoText2)
				.setFontColor(ColorConstants.BLACK).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		listaCanaliPagamentoCanvas.add(listaCanaliPagamentoP);
		listaCanaliPagamentoCanvas.close();

		// Importo
		Rectangle importoRectangle = new Rectangle(31, 523 + asset.getYoffSet()/2, 82, 23);
		Canvas importoCanvas = new Canvas(pdfCanvas, importoRectangle);
		Text importoText = new Text(mettiVirgolaEPuntiAllImportoInCent(bollettino999.Codeline2Boll))
				.setFont(asset.getImporto1Font());
		Paragraph importoP = new Paragraph().add(importoText).setFontColor(ColorConstants.BLACK).setFontSize(asset.getImporto1Size())
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(asset.getAllineamentoImporto());
		importoCanvas.add(importoP);
		importoCanvas.close();

		// Euro Data
		Rectangle euroDataRectangle = new Rectangle(110 + asset.getXoffSet(), 523, 198, 23);
		Canvas euroDataCanvas = new Canvas(pdfCanvas, euroDataRectangle);
		Text euroDataText1 = new Text("Euro   ").setFont(asset.getValuta1Font()).setFontSize(asset.getValuta1Size());
		Text euroDataText2 = new Text(" entro il  ").setFont(asset.getTitillium_bold())
				.setFontSize(8);
		Text euroDataText3 = new Text(" " + bollettino999.ScadenzaRata).setFont(asset.getTitillium_bold()).setFontSize(15 - asset.getYoffSet());
		Paragraph euroDataP = new Paragraph().add(euroDataText1).add(euroDataText2).add(euroDataText3)
				.setFontColor(ColorConstants.BLACK).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		euroDataCanvas.add(euroDataP);
		euroDataCanvas.close();

		// Puoi Pagare
		Rectangle puoiPagareRectangle = new Rectangle(30, 488, 217, 32);
		Canvas puoiPagareCanvas = new Canvas(pdfCanvas, puoiPagareRectangle);
		Text puoiPagareText1 = new Text("Puoi pagare").setFont(asset.getIstruzioniRate1Font());
		Text puoiPagareText2 = new Text(" con una unica rata ").setFont(asset.getIstruzioniRate2Font()); //formatoStampa
		String oppureIn = documento.DatiBollettino.size() > 2 ? "oppure in " + Integer.toString(documento.DatiBollettino.size()-1) +" rate (vedi pagina seguente). La rateizzazione non prevede costi aggiuntivi" : "";
		oppureIn = asset.getFormatoStampa() == FormatoStampa.GEOS ? oppureIn+"." : oppureIn;
		Text puoiPagareText3 = new Text(oppureIn).setFont(asset.getTitillium_regular());
		Paragraph puoiPagareP = new Paragraph().add(puoiPagareText1).add(puoiPagareText2).add(puoiPagareText3)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getIstruzioniRate1Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.TOP).setFixedLeading(10);
		puoiPagareCanvas.add(puoiPagareP);
		puoiPagareCanvas.close();

		// Importo Description
		Rectangle importoDescriptionRectangle = new Rectangle(30, 430, 243, 58);
		Canvas importoDescriptionCanvas = new Canvas(pdfCanvas, importoDescriptionRectangle);
		Text importoDescriptionText = new Text("L'importo è aggiornato automaticamente dal sistema e potrebbe subire variazioni per eventuali sgravi"
				+ ", note di credito, indennità di mora, sanzioni o interessi, ecc. "
				+ "Un operatore, il sito o l'app che userai ti potrebbero quindi chiedere una cifra diversa da quella"
				+ (asset.getFormatoStampa() == FormatoStampa.GEOS ? " qui" : "")
				+ " indicata.")
				.setFont(asset.getInfoImportoFont());
		Paragraph importoDescriptionP = new Paragraph().add(importoDescriptionText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getInfoImportoSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(10);
		importoDescriptionCanvas.add(importoDescriptionP);
		importoDescriptionCanvas.close();

		// Paga Sito
		Rectangle pagaSitoRectangle = new Rectangle(319, 531, 168, 15);
		Canvas pagaSitoCanvas = new Canvas(pdfCanvas, pagaSitoRectangle);
		Text pagaSitoText = new Text("PAGA SUL SITO O CON LE APP").setFont(asset.getIstruzioniTitoloFont());
		Paragraph pagaSitoP = new Paragraph().add(pagaSitoText).setFontColor(ColorConstants.BLACK).setFontSize(asset.getIstruzioniTitoloSize())
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		pagaSitoCanvas.add(pagaSitoP);
		pagaSitoCanvas.close();

		// Paga Sito Description
		Rectangle pagaSitoDescriptionRectangle = new Rectangle(319, 493, 164, 35);
		Canvas pagaSitoDescriptionCanvas = new Canvas(pdfCanvas, pagaSitoDescriptionRectangle);
		String fraseDelTuoEnte = asset.delTuoEnte 
				+ asset.diPoste 
				+ " dalla tua Banca o dagli altri canali di pagamento. Potrai pagare con carte, conto corrente, CBILL.";
		if(asset.getFormatoStampa() == FormatoStampa.GEOS) {
			fraseDelTuoEnte = asset.delTuoEnte 
			+ asset.diPoste 
			+ " della tua Banca o degli altri canali di pagamento. Potrai pagare con carte, conto corrente, CBILL.";
			fraseDelTuoEnte = fraseDelTuoEnte.substring(0,1).toUpperCase() + fraseDelTuoEnte.substring(1);
		}
		Text pagaSitoDescriptionText = new Text(fraseDelTuoEnte)
						.setFont(asset.getIstruzioniTesto1Font());
		Paragraph pagaSitoDescriptionP = new Paragraph().add(pagaSitoDescriptionText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getIstruzioniTesto1Size()).setFixedLeading(8.7f).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		pagaSitoDescriptionCanvas.add(pagaSitoDescriptionP);
		pagaSitoDescriptionCanvas.close();

		// Paga Territorio
		Rectangle pagaTerritorioRectangle = new Rectangle(319, 479, 121, 15);
		Canvas pagaTerritorioCanvas = new Canvas(pdfCanvas, pagaTerritorioRectangle);
		Text pagaTerritorioText = new Text("PAGA SUL  TERRITORIO").setFont(asset.getIstruzioniTitoloFont());
		Paragraph pagaTerritorioP = new Paragraph().add(pagaTerritorioText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getIstruzioniTitoloSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		pagaTerritorioCanvas.add(pagaTerritorioP);
		pagaTerritorioCanvas.close();

		// Paga Territorio Description
		Rectangle pagaTerritorioDescriptionRectangle = new Rectangle(319, 440, 164, 35);
		Canvas pagaTerritorioDescriptionCanvas = new Canvas(pdfCanvas, pagaTerritorioDescriptionRectangle);
		System.out.println("formato impaginazione: " + asset.getFormatoStampa());
		System.out.println("tipo stampa: " + asset.getTipoStampaBancaPoste() );
		System.out.println("è GEOS? " + (asset.getFormatoStampa() == FormatoStampa.GEOS ? "true" : "false"));
		System.out.println("è postale? " + (asset.getTipoStampaBancaPoste().equals("P") ? "true" : "false"));
		String fraseSulTerritorio = asset.getFormatoStampa() == FormatoStampa.GEOS 
				? (asset.getTipoStampaBancaPoste().equals("P") ? "In tutti gli Uffici Postali, " : "")
						+ "in Banca, in Ricevitoria, dal Tabaccaio, al Bancomat, al Supermercato. "
						+ "Potrai pagare in contanti, con carte o conto corrente."
				: "In tutti gli Uffici Postali, in Banca, in Ricevitoria, dal Tabaccaio, al Bancomat, al Supermercato. "
						+ "Potrai pagare in contanti, con carte o conto corrente.";
		System.out.println(fraseSulTerritorio);
				
		Text pagaTerritorioDescriptionText = new Text(fraseSulTerritorio)
				.setFont(asset.getIstruzioniTesto1Font());
		Paragraph pagaTerritorioDescriptionP = new Paragraph().add(pagaTerritorioDescriptionText)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getIstruzioniTesto1Size()).setFixedLeading(8.7f).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		pagaTerritorioDescriptionCanvas.add(pagaTerritorioDescriptionP);
		pagaTerritorioDescriptionCanvas.close();

		// Canali Digitali
		Rectangle canaliDigitaliRectangle = new Rectangle(523, 500, 33, 33);
		Canvas canaliDigitaliCanvas = new Canvas(pdfCanvas, canaliDigitaliRectangle);
		canaliDigitaliCanvas.add(asset.getCanali_digitali().scaleToFit(33, 33));
		canaliDigitaliCanvas.close();

		// Canali Fisici
		Rectangle canaliFisiciRectangle = new Rectangle(523, 450, 33, 33);
		Canvas canaliFisiciCanvas = new Canvas(pdfCanvas, canaliFisiciRectangle);
		canaliFisiciCanvas.add(asset.getCanali_fisici().scaleToFit(33, 33));
		canaliFisiciCanvas.close();

		// Utilizza Porzione
		Rectangle utilizzaPorzioneRectangle = new Rectangle(29, 411, 506, 16);
		Canvas utilizzaPorzioneCanvas = new Canvas(pdfCanvas, utilizzaPorzioneRectangle);
		Text utilizzaPorzioneText1 = new Text("Utilizza la porzione di avviso relativa ").setFont(asset.getIstruzioniTesto2Font());
		Text utilizzaPorzioneText2 = new Text(documento.DatiBollettino.size() > 2 ? "alla rata " : "").setFont(asset.getIstruzioniTesto2Font());
		Text utilizzaPorzioneText3 = new Text("al canale di pagamento che preferisci.").setFont(asset.getIstruzioniTesto2Font());
		Paragraph utilizzaPorzioneP = new Paragraph().add(utilizzaPorzioneText1).add(utilizzaPorzioneText2)
				.add(utilizzaPorzioneText3).setFontColor(ColorConstants.BLACK).setFontSize(asset.getIstruzioniTesto2Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		utilizzaPorzioneCanvas.add(utilizzaPorzioneP);
		utilizzaPorzioneCanvas.close();

		// Banche e Altri Canali Background
		Rectangle bancheAltriCanaliGrayRectangle = new Rectangle(0, 386, 595, 17);
		pdfCanvas.saveState().setFillColor(LeggoAsset.grigioBollettino).rectangle(bancheAltriCanaliGrayRectangle)
				.fill();
		Canvas bancheAltriCanaliGrayCanvas = new Canvas(pdfCanvas, bancheAltriCanaliGrayRectangle);
		bancheAltriCanaliGrayCanvas.close();

		// Banche e Altri Canali
		Rectangle bancheAltriCanaliRectangle = new Rectangle(29, 390 - asset.getYoffSet(), 146, 15);
		Canvas bancheAltriCanaliCanvas = new Canvas(pdfCanvas, bancheAltriCanaliRectangle);
		Text bancheAltriCanaliText = new Text("BANCHE E ALTRI CANALI").setFont(asset.getInEvidenza1Font());
		Paragraph bancheAltriCanaliP = new Paragraph().add(bancheAltriCanaliText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getInEvidenza1Size()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		bancheAltriCanaliCanvas.add(bancheAltriCanaliP);
		bancheAltriCanaliCanvas.close();

		// Rata Unica
		Rectangle rataUnicaRectangle = new Rectangle(416 - asset.getXoffSet() * 3 , 390 - asset.getYoffSet() * 2, 110, 15 + asset.getYoffSet());
		Canvas rataUnicaCanvas = new Canvas(pdfCanvas, rataUnicaRectangle);
		Text rataUnicaText1 = new Text("RATA UNICA  ").setFont(asset.getInEvidenza4Font());
		Text rataUnicaText2 = new Text(" entro il").setFont(asset.getInEvidenza1Font());
		Paragraph rataUnicaP = new Paragraph().add(rataUnicaText1).add(rataUnicaText2)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getInEvidenza1Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(asset.getAllineamentoImporto());
		rataUnicaCanvas.add(rataUnicaP);
		rataUnicaCanvas.close();

		// Rata Unica Data
		Rectangle rataUnicadataRectangle = new Rectangle(517, 390 - asset.getYoffSet(), 83, 15);
		Canvas rataUnicadataCanvas = new Canvas(pdfCanvas, rataUnicadataRectangle);
		Text rataUnicadataText = new Text(bollettino999.ScadenzaRata).setFont(asset.getInEvidenza5Font());
		Paragraph rataUnicadataP = new Paragraph().add(rataUnicadataText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getInEvidenza5Size()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		rataUnicadataCanvas.add(rataUnicadataP);
		rataUnicadataCanvas.close();

		// Banche Altri Canali Description
		Rectangle bancheAltriCanaliDescriptionRectangle = new Rectangle(30, 297, 129, 65);
		Canvas bancheAltriCanaliDescriptionCanvas = new Canvas(pdfCanvas, bancheAltriCanaliDescriptionRectangle);
		Text bancheAltriCanaliDescriptionText1 = new Text("Qui accanto trovi il codice")
				.setFont(asset.getInfoCodiciFont());
		Text bancheAltriCanaliDescriptionText2 = new Text(" QR ").setFont(asset.getInfoCodiciBoldFont());
		Text bancheAltriCanaliDescriptionText3 = new Text("e il codice interbancario")
				.setFont(asset.getInfoCodiciFont());
		Text bancheAltriCanaliDescriptionText4 = new Text(" CBILL ")
				.setFont(asset.getInfoCodiciBoldFont());
		Text bancheAltriCanaliDescriptionText5 = new Text("per pagare attraverso il circuito bancario e gli altri canali di pagamento abilitati.")
				.setFont(asset.getInfoCodiciFont());
		Paragraph bancheAltriCanaliDescriptionP = new Paragraph().add(bancheAltriCanaliDescriptionText1)
				.add(bancheAltriCanaliDescriptionText2).add(bancheAltriCanaliDescriptionText3)
				.add(bancheAltriCanaliDescriptionText4).add(bancheAltriCanaliDescriptionText5)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getInfoCodiciSize()).setMargin(0).setFixedLeading(11);
		bancheAltriCanaliDescriptionCanvas.add(bancheAltriCanaliDescriptionP);
		bancheAltriCanaliDescriptionCanvas.close();


// 		QR CODE 
		Rectangle qRCodeRectangle = new Rectangle(177, 289, 70, 70);
		Canvas qRCodeCanvas = new Canvas(pdfCanvas, qRCodeRectangle);
		qRCodeCanvas.add(generaQRCode(bollettino999.BarcodePagoPa, pdf).scaleToFit(70, 70));
		qRCodeCanvas.close();

		// Destinatario
		Rectangle destinatarioRectangle = new Rectangle(263, 353 - asset.getYoffSet(), 62, 12);
		Canvas destinatarioCanvas = new Canvas(pdfCanvas, destinatarioRectangle);
		Text destinatarioText = new Text("Destinatario").setFont(asset.getEtichettaDenominazioneFont());
		Paragraph destinatarioP = new Paragraph().add(destinatarioText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getEtichettaDenominazioneSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		destinatarioCanvas.add(destinatarioP);
		destinatarioCanvas.close();

		// Nome Cognome Destinatario
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		//Rectangle nomeCognomeDestinatarioRectangle2 = new Rectangle(346, 330, 130, 32);
		Rectangle nomeCognomeDestinatarioRectangle2 = new Rectangle(313 + asset.getXoffSet() * 3, 330, 170 - 19 - 25, 32);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(225, 0, 10)).rectangle(nomeCognomeDestinatarioRectangle2).fill();
		//fine LP PG200420 - Errori nel pdf dell'avviso
		
		Canvas nomeCognomeDestinatarioCanvas2 = new Canvas(pdfCanvas, nomeCognomeDestinatarioRectangle2);
		Text nomeCognomeDestinatarioTextBoll = nomeCognomeDestinatarioText;
		nomeCognomeDestinatarioTextBoll.setFont(asset.getDenominazioneNome2Font());
		Paragraph nomeCognomeDestinatarioP2 = new Paragraph().add(nomeCognomeDestinatarioTextBoll)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(8);
		nomeCognomeDestinatarioCanvas2.add(nomeCognomeDestinatarioP2);
		nomeCognomeDestinatarioCanvas2.close();

		// Ente Creditore
		Rectangle destinatarioRectangle2 = new Rectangle(263, 338 - asset.getYoffSet(), 69, 12);
		Canvas destinatarioCanvas2 = new Canvas(pdfCanvas, destinatarioRectangle2);
		Text destinatarioText2 = new Text("Ente Creditore").setFont(asset.getEtichettaDenominazioneFont());
		Paragraph destinatarioP2 = new Paragraph().add(destinatarioText2).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getEtichettaDenominazioneSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		destinatarioCanvas2.add(destinatarioP2);
		destinatarioCanvas2.close();

		// Ente Creditore String
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		//Rectangle enteCreditoreStringRectangle2 = new Rectangle(346, 314, 241, 32);
		Rectangle enteCreditoreStringRectangle2 = new Rectangle(313 + asset.getXoffSet() * 3, 314, 163 - 10, 32);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(212, 210, 212)).rectangle(enteCreditoreStringRectangle2).fill();
		//fine LP PG200420 - Errori nel pdf dell'avviso
		Canvas enteCreditoreStringCanvas2 = new Canvas(pdfCanvas, enteCreditoreStringRectangle2);
		Text enteCreditoreStringTextBoll = enteCreditoreStringText;
		enteCreditoreStringTextBoll.setFont(asset.getDenominazioneNome2Font());
		Paragraph enteCreditoreStringP2 = new Paragraph().add(enteCreditoreStringTextBoll)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(7);
		enteCreditoreStringCanvas2.add(enteCreditoreStringP2);
		enteCreditoreStringCanvas2.close();

		// Oggetto Pagamento
		Rectangle oggettoPagamentoRectangle2 = new Rectangle(263, 323 - asset.getYoffSet() * 2, 130, 12);
	//	oggettoPagamentoRectangle2 = new Rectangle(263, 323 - 2, 96, 12);
		Canvas oggettoPagamentoCanvas2 = new Canvas(pdfCanvas, oggettoPagamentoRectangle2);
		Text oggettoPagamentoText2 = new Text("Oggetto del pagamento").setFont(asset.getEtichettaDenominazioneFont());
		Paragraph oggettoPagamentoP2 = new Paragraph().add(oggettoPagamentoText2).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getEtichettaDenominazioneSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		oggettoPagamentoCanvas2.add(oggettoPagamentoP2);
		oggettoPagamentoCanvas2.close();

		// Oggetto Pagamento String
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		//Rectangle oggettoPagamentoStringRectangle2 = new Rectangle(346, 301, 248, 32);
		Rectangle oggettoPagamentoStringRectangle2 = new Rectangle(346 + asset.getXoffSet() * 4, 301 - 2 -2, 239 - 20 - 15 -6, 32); 
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(0, 210, 10)).rectangle(oggettoPagamentoStringRectangle2).fill();
		//fine LP PG200420 - Errori nel pdf dell'avviso
		Canvas oggettoPagamentoStringCanvas2 = new Canvas(pdfCanvas, oggettoPagamentoStringRectangle2);
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		//Paragraph oggettoPagamentoStringP2 = new Paragraph().add(oggettoPagamentoText)
		//		.setFontColor(ColorConstants.BLACK).setFontSize(8).setMargin(0).setFixedLeading(11)
		//		.setVerticalAlignment(VerticalAlignment.MIDDLE);
		Text oggettoPagamentoTextBoll = oggettoPagamentoText;
		oggettoPagamentoTextBoll.setFont(asset.getDenominazioneNome2Font());
		Paragraph oggettoPagamentoStringP2 = new Paragraph().add(oggettoPagamentoTextBoll)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0).setFixedLeading(7)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		//fine LP PG200420 - Errori nel pdf dell'avviso
		oggettoPagamentoStringCanvas2.add(oggettoPagamentoStringP2);
		oggettoPagamentoStringCanvas2.close();
		
		// Euro
		Rectangle euroRectangle = new Rectangle(474, 351, 25, 15);
		Canvas euroCanvas = new Canvas(pdfCanvas, euroRectangle);
		Text euroText = new Text("Euro").setFont(asset.getValuta2Font());
		Paragraph euroP = new Paragraph().add(euroText).setFontSize(asset.getValuta2Size()).setFontColor(ColorConstants.BLACK).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		euroCanvas.add(euroP);
		euroCanvas.close();

		// Importo
		Rectangle importoRectangle2 = new Rectangle(510, 352, 70, 15);
		Canvas importoCanvas2 = new Canvas(pdfCanvas, importoRectangle2);
		Text importoTextBoll = importoText;
		importoTextBoll.setFont(asset.getImporto2Font());
		Paragraph importoP2 = new Paragraph().add(importoTextBoll).setFontColor(ColorConstants.BLACK).setFontSize(11)
				.setFont(asset.getRoboto_bold()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		importoCanvas2.add(importoP2);
		importoCanvas2.close();

		// Codice Cbill
		Rectangle codiceCbillRectangle = new Rectangle(263, 301, 73, 12);
		Canvas codiceCbillCanvas = new Canvas(pdfCanvas, codiceCbillRectangle);
		Text codiceCbillText = new Text("Codice CBILL").setFont(asset.getEtichettaDenominazioneFont());
		Paragraph codiceCbillP = new Paragraph().add(codiceCbillText).setFontColor(ColorConstants.BLACK).setFontSize(asset.getEtichettaDenominazioneSize())
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		codiceCbillCanvas.add(codiceCbillP);
		codiceCbillCanvas.close();

		// Codice Cbill String
		Rectangle codiceCbillStringRectangle = new Rectangle(263, 290, 43, 12);
		Canvas codiceCbillStringCanvas = new Canvas(pdfCanvas, codiceCbillStringRectangle);
		Text codiceCbillStringText = new Text(documento.DatiCreditore.get(0).CodiceInterbancario).setFont(asset.getCodiceBoldFont());
		Paragraph codiceCbillStringP = new Paragraph().add(codiceCbillStringText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getCodiceBoldSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		codiceCbillStringCanvas.add(codiceCbillStringP);
		codiceCbillStringCanvas.close();

		// Codice Avviso
		Rectangle codiceAvvisoRectangle = new Rectangle(325, 301, 79, 12);
		Canvas codiceAvvisoCanvas = new Canvas(pdfCanvas, codiceAvvisoRectangle);
		Text codiceAvvisoText = new Text("Codice Avviso").setFont(asset.getEtichettaDenominazioneFont());
		Paragraph codiceAvvisoP = new Paragraph().add(codiceAvvisoText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getEtichettaDenominazioneSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		codiceAvvisoCanvas.add(codiceAvvisoP);
		codiceAvvisoCanvas.close();

		// Codice Avviso String
		Rectangle codiceAvvisoStringRectangle = new Rectangle(325, 290, 151, 12);
		Canvas codiceAvvisoStringCanvas = new Canvas(pdfCanvas, codiceAvvisoStringRectangle);
		Text codiceAvvisoStringText = new Text(formattaCodiceAvviso(bollettino999.AvvisoPagoPa)).setFont(asset.getCodiceBoldFont());
		Paragraph codiceAvvisoStringP = new Paragraph().add(codiceAvvisoStringText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getCodiceBoldSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		codiceAvvisoStringCanvas.add(codiceAvvisoStringP);
		codiceAvvisoStringCanvas.close();

		// Cod. Fiscale Ente Creditore
		Rectangle cfEnteCreditoreRectangle = new Rectangle(475, 301, 135, 12);
		Canvas cfEnteCreditoreCanvas = new Canvas(pdfCanvas, cfEnteCreditoreRectangle);
		Text cfEnteCreditoreText = new Text("Cod. Fiscale Ente Creditore").setFont(asset.getEtichettaDenominazioneFont());
		Paragraph cfEnteCreditoreP = new Paragraph().add(cfEnteCreditoreText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getEtichettaDenominazioneSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		cfEnteCreditoreCanvas.add(cfEnteCreditoreP);
		cfEnteCreditoreCanvas.close();

		// Cod. Fiscale Ente Creditore String
		Rectangle cfEnteCreditoreStringRectangle = new Rectangle(475, 290, 115, 12);
		Canvas cfEnteCreditoreStringCanvas = new Canvas(pdfCanvas, cfEnteCreditoreStringRectangle);
		Text cfEnteCreditoreStringText = new Text(documento.DatiCreditore.get(0).Cf).setFont(asset.getCodiceBoldFont());
		Paragraph cfEnteCreditoreStringP = new Paragraph().add(cfEnteCreditoreStringText)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getCodiceBoldSize()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		cfEnteCreditoreStringCanvas.add(cfEnteCreditoreStringP);
		cfEnteCreditoreStringCanvas.close();

		if(tipoStampa.equals("P")) {
			// Forbici
			Rectangle forbiciRectangle = new Rectangle(548, 260, 14, 10);
			Canvas forbiciCanvas = new Canvas(pdfCanvas, forbiciRectangle);
			forbiciCanvas.add(asset.getLogo_forbici().scaleToFit(14, 10));
			forbiciCanvas.close();
	
			// Bollettino Postale PA Background Border
			Rectangle bollettinoPostalePaBorderRectangle = new Rectangle(0, 260, 595, 1);
			pdfCanvas.saveState().setFillColor(LeggoAsset.grigioForbici).rectangle(bollettinoPostalePaBorderRectangle)
					.fill();
			Canvas bollettinoPostalePaBorderCanvas = new Canvas(pdfCanvas, bollettinoPostalePaBorderRectangle);
			bollettinoPostalePaBorderCanvas.close();
	
			// Bollettino Postale PA Background
			Rectangle bollettinoPostalePaGrayRectangle = new Rectangle(0, 243, 595, 17);
			pdfCanvas.saveState().setFillColor(LeggoAsset.grigioBollettino).rectangle(bollettinoPostalePaGrayRectangle)
					.fill();
			Canvas bollettinoPostalePaGrayCanvas = new Canvas(pdfCanvas, bollettinoPostalePaGrayRectangle);
			bollettinoPostalePaGrayCanvas.close();
	
			// Bollettino Postale PA
			Rectangle bollettinoPostalePaRectangle = new Rectangle(29, 247, 135, 15);
			Canvas bollettinoPostalePaCanvas = new Canvas(pdfCanvas, bollettinoPostalePaRectangle);
			Text bollettinoPostalePaText = new Text(LeggoAsset.BOLLETTINO_POSTALE_PA).setFont(asset.getInEvidenza1Font());
			Paragraph bollettinoPostalePaP = new Paragraph().add(bollettinoPostalePaText).setFontColor(ColorConstants.BLACK)
					.setFontSize(asset.getInEvidenza1Size()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
			bollettinoPostalePaCanvas.add(bollettinoPostalePaP);
			bollettinoPostalePaCanvas.close();
	
			// Logo Banco Posta
			Rectangle logoBancopostaRectangle = new Rectangle(183, 248, 51, 7);
			Canvas logoBancopostaCanvas = new Canvas(pdfCanvas, logoBancopostaRectangle);
			logoBancopostaCanvas.add(asset.getLogo_bancoposta().scaleToFit(51, 7));
			logoBancopostaCanvas.close();
	
			// Rata Unica
			Rectangle rataUnicaRectangle2 = new Rectangle(416 + asset.getXoffSet() , 247, 89, 15);
			Canvas rataUnicaCanvas2 = new Canvas(pdfCanvas, rataUnicaRectangle2);
			rataUnicaCanvas2.add(rataUnicaP);
			rataUnicaCanvas2.close();
	
			// Rata Unica Data
			Rectangle rataUnicadataRectangle2 = new Rectangle(517, 248, 83, 15);
			Canvas rataUnicadataCanvas2 = new Canvas(pdfCanvas, rataUnicadataRectangle2);
			rataUnicadataCanvas2.add(rataUnicadataP);
			rataUnicadataCanvas2.close();
	
			// Logo Poste Italiane
			Rectangle logoPosteitalianeRectangle = new Rectangle(29, 190, 117, 15);
			Canvas logoPosteitalianeCanvas = new Canvas(pdfCanvas, logoPosteitalianeRectangle);
			logoPosteitalianeCanvas.add(asset.getLogo_poste_italiane().scaleToFit(117, 15));
			logoPosteitalianeCanvas.close();
	
			// Logo Euro
			Rectangle logoEuroRectangle = new Rectangle(180, 187, 19, 19);
			Canvas logoEuroCanvas = new Canvas(pdfCanvas, logoEuroRectangle);
			logoEuroCanvas.add(asset.getLogo_euro_bollettino().scaleToFit(19, 19));
			logoEuroCanvas.close();
	
			// Sul CC
			Rectangle sulCcRectangle = new Rectangle(205, 193, 43, 15);
			Canvas sulCcCanvas = new Canvas(pdfCanvas, sulCcRectangle);
			Text sulCcText = new Text(LeggoAsset.SUL_CC).setFont(asset.getSulCcFont());
			Paragraph sulCcP = new Paragraph().add(sulCcText).setFontColor(ColorConstants.BLACK).setFontSize(asset.getSulCcSize())
					.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
			sulCcCanvas.add(sulCcP);
			sulCcCanvas.close();
	
			// Numero CC Postale
			Rectangle numeroCcPostaleRectangle = new Rectangle(248 + asset.getXoffSet(), 193 + asset.getYoffSet(), 156, 15);
			Canvas numeroCcPostaleCanvas = new Canvas(pdfCanvas, numeroCcPostaleRectangle);
			Text numeroCcPostaleText = new Text(bollettino999.Codeline12Boll).setFont(asset.getCodiceBoldFont());
			Paragraph numeroCcPostaleP = new Paragraph().add(numeroCcPostaleText).setFontColor(ColorConstants.BLACK)
					.setFontSize(asset.getCodiceBoldSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
			numeroCcPostaleCanvas.add(numeroCcPostaleP);
			numeroCcPostaleCanvas.close();
	
			// Euro
			Rectangle euroRectangle2 = new Rectangle(474, 194, 25, 15);
			Canvas euroCanvas2 = new Canvas(pdfCanvas, euroRectangle2);
			euroCanvas2.add(euroP);
			euroCanvas2.close();
	
			// Importo
			Rectangle importoRectangle3 = new Rectangle(510, 195, 70, 15);
			Canvas importoCanvas3 = new Canvas(pdfCanvas, importoRectangle3);
			importoCanvas3.add(importoP2);
			importoCanvas3.close();
	
			// Logo Bollettino Postale
			Rectangle logoBollettinoPostaleRectangle = new Rectangle(30, 149, 78, 32);
			Canvas logoBollettinoPostaleCanvas = new Canvas(pdfCanvas, logoBollettinoPostaleRectangle);
			logoBollettinoPostaleCanvas.add(asset.getLogo_bollettino_postale().scaleToFit(78, 32));
			logoBollettinoPostaleCanvas.close();
	
			// Bollettino Postale Descrizione
			Rectangle bollettinoPostaleDescrizioneRectangle = new Rectangle(30, 97, 120, 48);
			Canvas bollettinoPostaleDescrizioneCanvas = new Canvas(pdfCanvas, bollettinoPostaleDescrizioneRectangle);
			Text bollettinoPostaleDescrizioneText = new Text(LeggoAsset.BOLLETTINO_POSTALE_DESCRIZIONE)
					.setFont(asset.getInfoBollettinoFont());
			Paragraph bollettinoPostaleDescrizioneP = new Paragraph().add(bollettinoPostaleDescrizioneText)
					.setFontColor(ColorConstants.BLACK).setFontSize(asset.getInfoBollettinoSize()).setMargin(0)
					.setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(10);
			bollettinoPostaleDescrizioneCanvas.add(bollettinoPostaleDescrizioneP);
			bollettinoPostaleDescrizioneCanvas.close();
	
			// Intestato a
			Rectangle intestatoARectangle = new Rectangle(178, 163, 59, 12);
			Canvas intestatoACanvas = new Canvas(pdfCanvas, intestatoARectangle);
			Text intestatoAText = new Text(LeggoAsset.INTESTATO_A).setFont(asset.getEtichettaDenominazioneFont());
			Paragraph intestatoAP = new Paragraph().add(intestatoAText).setFontColor(ColorConstants.BLACK).setFontSize(asset.getEtichettaDenominazioneSize())
					.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
			intestatoACanvas.add(intestatoAP);
			intestatoACanvas.close();
	
			// Intestatario CC Postale
			//inizio LP PG200420 - Errori nel pdf dell'avviso
			//Rectangle intestatarioCCPostaleRectangle = new Rectangle(220, 163, 258, 10);
			Rectangle intestatarioCCPostaleRectangle = new Rectangle(220, 164 + asset.getYoffSet() / 2, 258, 10);
			//fine LP PG200420 - Errori nel pdf dell'avviso
			Canvas intestatarioCCPostaleCanvas = new Canvas(pdfCanvas, intestatarioCCPostaleRectangle);
			Text intestatarioCCPostaleText = new Text(bollettino999.Descon60Boll).setFont(asset.getDenominazioneNome2Font());
			Paragraph intestatarioCCPostaleP = new Paragraph().add(intestatarioCCPostaleText)
					.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
					.setVerticalAlignment(VerticalAlignment.MIDDLE);
			intestatarioCCPostaleCanvas.add(intestatarioCCPostaleP);
			intestatarioCCPostaleCanvas.close();
	
			// Destinatario
			Rectangle destinatarioRectangle3 = new Rectangle(178, 145, 57, 12);
			Canvas destinatarioCanvas3 = new Canvas(pdfCanvas, destinatarioRectangle3);
			destinatarioCanvas3.add(destinatarioP);
			destinatarioCanvas3.close();
	
			// Nome Cognome Destinatario
			//inizio LP PG200420 - Errori nel pdf dell'avviso
			//Rectangle nomeCognomeDestinatarioRectangle3 = new Rectangle(229, 147, 189, 10);
			Rectangle nomeCognomeDestinatarioRectangle3 = new Rectangle(226, 122 + asset.getYoffSet(), 170 - 10 - 9, 32);
			if(bDebug) //luis
				pdfCanvas.saveState().setFillColor(new DeviceRgb(212, 210, 212)).rectangle(nomeCognomeDestinatarioRectangle3).fill();
			//fine LP PG200420 - Errori nel pdf dell'avviso
			Canvas nomeCognomeDestinatarioCanvas3 = new Canvas(pdfCanvas, nomeCognomeDestinatarioRectangle3);
			Text nomeCognomeDestinatarioText2 = new Text(documento.DatiAnagrafici.get(0).Denominazione1)
					.setFont(asset.getDenominazioneNome2Font());
			Paragraph nomeCognomeDestinatarioP3 = new Paragraph().add(nomeCognomeDestinatarioText2)
					.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
					.setVerticalAlignment(VerticalAlignment.MIDDLE);
			//inizio LP PG200420 - Errori nel pdf dell'avviso
			nomeCognomeDestinatarioP3.setFixedLeading(8);
			//fine LP PG200420 - Errori nel pdf dell'avviso
			nomeCognomeDestinatarioCanvas3.add(nomeCognomeDestinatarioP3);
			nomeCognomeDestinatarioCanvas3.close();
	
			// Oggetto Pagamento
//			Rectangle oggettoPagamentoRectangle3 = new Rectangle(178, 128, 88, 12);
			Rectangle oggettoPagamentoRectangle3 = new Rectangle(178, 128 - 2, 88, 12);
			Canvas oggettoPagamentoCanvas3 = new Canvas(pdfCanvas, oggettoPagamentoRectangle3);
			Text oggettoPagamentoText3 = new Text(LeggoAsset.OGGETTO_PAGAMENTO).setFont(asset.getEtichettaDenominazioneFont());
			Paragraph oggettoPagamentoP3 = new Paragraph().add(oggettoPagamentoText3).setFontColor(ColorConstants.BLACK)
					.setFontSize(asset.getEtichettaDenominazioneSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
			oggettoPagamentoCanvas3.add(oggettoPagamentoP3);
			oggettoPagamentoCanvas3.close();
	
			// Oggetto Pagamento String
			Rectangle oggettoPagamentoStringRectangle3 = new Rectangle(250 + asset.getXoffSet(), 109 - 2 -2 + asset.getYoffSet() / 2, 239 - 8, 30);
			//inizio LP PG200420 - Errori nel pdf dell'avviso
			if(bDebug)
				pdfCanvas.saveState().setFillColor(new DeviceRgb(225, 0, 22)).rectangle(oggettoPagamentoStringRectangle3).fill();
			//fine LP PG200420 - Errori nel pdf dell'avviso
			Canvas oggettoPagamentoStringCanvas3 = new Canvas(pdfCanvas, oggettoPagamentoStringRectangle3);
			Text oggettoPagamentoStringText3 = new Text(documento.CausaleDocumento).setFont(asset.getDenominazioneNome2Font());
			//inizio LP PG200420 - Errori nel pdf dell'avviso
			//Paragraph oggettoPagamentoStringP3 = new Paragraph().add(oggettoPagamentoStringText3).setFixedLeading(11)
			//		.setFontColor(ColorConstants.BLACK).setFontSize(8).setMargin(0)
			//		.setVerticalAlignment(VerticalAlignment.MIDDLE);
			Paragraph oggettoPagamentoStringP3 = new Paragraph().add(oggettoPagamentoStringText3).setFixedLeading(7)
					.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
					.setVerticalAlignment(VerticalAlignment.MIDDLE);
			//fine LP PG200420 - Errori nel pdf dell'avviso
			oggettoPagamentoStringCanvas3.add(oggettoPagamentoStringP3);
			oggettoPagamentoStringCanvas3.close();
	
			// Data matrix Container
			Rectangle dataMatrixContainerRectangle = new Rectangle(480, 83, 93, 93);
			Canvas dataMatrixContainerCanvas = new Canvas(pdfCanvas, dataMatrixContainerRectangle);
			dataMatrixContainerCanvas.add(asset.getData_matrix_container().scaleToFit(93, 93));
			dataMatrixContainerCanvas.close();
	
			// Data matrix
			// Rectangle dataMatrixRectangle = new Rectangle(490, 93, 74, 74);
			// Canvas dataMatrixCanvas = new Canvas(pdfCanvas, dataMatrixRectangle);
			// dataMatrixCanvas.add(asset.getData_matrix().scaleToFit(74, 74));
			// dataMatrixCanvas.close();
	
	// 		Data matrix
			Rectangle dataMatrixRectangle = new Rectangle(490, 93, 74, 74);
			Canvas dataMatrixCanvas = new Canvas(pdfCanvas, dataMatrixRectangle);
			dataMatrixCanvas.add(generaDataMatrix(bollettino999.QRcodePagoPa, pdf).scaleToFit(74, 74));
			dataMatrixCanvas.close();
	
			// Autorizzazione
			Rectangle autorizzazioneRectangle = new Rectangle(30, 85, 160, 9);
			Canvas autorizzazioneCanvas = new Canvas(pdfCanvas, autorizzazioneRectangle);
			Text autorizzazioneText = new Text(bollettino999.AutorizCcp).setFont(asset.getAutorizzazioneFont());
			Paragraph autorizzazioneP = new Paragraph().add(autorizzazioneText).setFontColor(LeggoAsset.grigioForbici)
					.setFontSize(asset.getAutorizzazioneSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
			autorizzazioneCanvas.add(autorizzazioneP);
			autorizzazioneCanvas.close();
	
			// Codice Avviso
			Rectangle codiceAvvisoRectangle2 = new Rectangle(179, 103, 69, 12);
			Canvas codiceAvvisoCanvas2 = new Canvas(pdfCanvas, codiceAvvisoRectangle2);
			codiceAvvisoCanvas2.add(codiceAvvisoP);
			codiceAvvisoCanvas2.close();
	
			// Codice Avviso String
			Rectangle codiceAvvisoStringRectangle2 = new Rectangle(179, 93, 151, 12);
			Canvas codiceAvvisoStringCanvas2 = new Canvas(pdfCanvas, codiceAvvisoStringRectangle2);
			codiceAvvisoStringCanvas2.add(codiceAvvisoStringP);
			codiceAvvisoStringCanvas2.close();
	
			// Tipo
			Rectangle tipoRectangle = new Rectangle(329, 103, 25, 12);
			Canvas tipoCanvas = new Canvas(pdfCanvas, tipoRectangle);
			Text tipoText = new Text(LeggoAsset.TIPO).setFont(asset.getEtichettaDenominazioneFont());
			Paragraph tipoP = new Paragraph().add(tipoText).setFontColor(ColorConstants.BLACK).setFontSize(asset.getEtichettaDenominazioneSize()).setMargin(0)
					.setVerticalAlignment(VerticalAlignment.MIDDLE);
			tipoCanvas.add(tipoP);
			tipoCanvas.close();
	
			// P1
			Rectangle p1Rectangle = new Rectangle(331, 93, 13, 13);
			Canvas p1Canvas = new Canvas(pdfCanvas, p1Rectangle);
			Text p1Text = new Text(LeggoAsset.P1).setFont(asset.getCodiceBoldFont());
			Paragraph p1P = new Paragraph().add(p1Text).setFontColor(ColorConstants.BLACK).setFontSize(asset.getCodiceBoldSize()).setMargin(0)
					.setVerticalAlignment(VerticalAlignment.MIDDLE);
			p1Canvas.add(p1P);
			p1Canvas.close();
	
			// Cod. Fiscale Ente Creditore
			Rectangle cfEnteCreditoreRectangle2 = new Rectangle(375, 103, 135, 12);
			Canvas cfEnteCreditoreCanvas2 = new Canvas(pdfCanvas, cfEnteCreditoreRectangle2);
			cfEnteCreditoreCanvas2.add(cfEnteCreditoreP);
			cfEnteCreditoreCanvas2.close();
	
			// Cod. Fiscale Ente Creditore String
			Rectangle cfEnteCreditoreStringRectangle2 = new Rectangle(375, 93, 115, 12);
			Canvas cfEnteCreditoreStringCanvas2 = new Canvas(pdfCanvas, cfEnteCreditoreStringRectangle2);
			cfEnteCreditoreStringCanvas2.add(cfEnteCreditoreStringP);
			cfEnteCreditoreStringCanvas2.close();
		}
		// Footer Border
		Rectangle footerBorderRectangle = new Rectangle(0, 25, 595, 1);
		pdfCanvas.saveState().setFillColor(LeggoAsset.grigioFooter).rectangle(footerBorderRectangle).fill();
		Canvas footerBorderCanvas = new Canvas(pdfCanvas, footerBorderRectangle);
		footerBorderCanvas.close();
	}

	private void paginaDueBollettini(PdfPage pageTarget, LeggoAsset asset, Documento documento, PdfDocument pdf, int bollettinoDiPartenza, String tipoStampa) {
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		boolean bDebug = false; //se == true mostra alcune aree con fill colorato per verificare posizione e dimensione 
		//fine LP PG200420 - Errori nel pdf dell'avviso
//		creazione del pdf canvas
		PdfCanvas pdfCanvas = new PdfCanvas(pageTarget);
//	INTESTAZIONE
// 		Logo PagoPa
		Rectangle logoPagopaRectangle = new Rectangle(32, 786, 35, 23);
		Canvas logoPagopaCanvas = new Canvas(pdfCanvas, logoPagopaRectangle);
		logoPagopaCanvas.add(asset.getLogo_pagopa().scaleToFit(35, 23));
		logoPagopaCanvas.close();

//	 	Avviso di Pagamento
		Rectangle avvisoPagamentoRectangle = new Rectangle(70, 798, 124, 15);
		Canvas avvisoPagamentoCanvas = new Canvas(pdfCanvas, avvisoPagamentoRectangle);
		Text avvisoPagamentoText = new Text("AVVISO DI PAGAMENTO").setFont(asset.getTitoloFont());
		Paragraph avvisoPagamentoP = new Paragraph().add(avvisoPagamentoText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getTitoloSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		avvisoPagamentoCanvas.add(avvisoPagamentoP);
		avvisoPagamentoCanvas.close();

// 		Logo Ente
		Rectangle logoEnteRectangle = new Rectangle(480, 732, asset.getPathLogoEnteSizeX(), asset.getPathLogoEnteSizeY()); //valori di default 84x84px
		Canvas logoEnteCanvas = new Canvas(pdfCanvas, logoEnteRectangle);
		logoEnteCanvas.add(asset.getLogo_ente().scaleToFit(asset.getPathLogoEnteSizeX(), asset.getPathLogoEnteSizeY()));
		logoEnteCanvas.close();

// 		Rate e Oggetto del Pagamento
		Rectangle oggettoPagamentoRectangle = new Rectangle(30, 715, 445, 60);
		Canvas oggettoPagamentoCanvas = new Canvas(pdfCanvas, oggettoPagamentoRectangle);
		Text oggettoPagamentoText = new Text(
				"Rate " + Integer.toString(documento.DatiBollettino.get(bollettinoDiPartenza).ProgressivoBoll) + "Â°e " + Integer.toString(documento.DatiBollettino.get(bollettinoDiPartenza + 1).ProgressivoBoll) + "Â°- " + documento.CausaleDocumento)
						.setFont(asset.getTestataFont());
		Paragraph oggettoPagamentoP = new Paragraph().add(oggettoPagamentoText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getTestataSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(18);
		oggettoPagamentoCanvas.add(oggettoPagamentoP);
		oggettoPagamentoCanvas.close();

// 		TESTO UTILIZZA LA PORZIONE...
		Rectangle b1_2utilizzaLaPorzioneRettangolo = new Rectangle(32, 721, 288, 15);
		Canvas b1_2utilizzaLaPorzioneCanvas = new Canvas(pdfCanvas, b1_2utilizzaLaPorzioneRettangolo);
		Text b1_2utilizzaLaPorzioneText = new Text("Utilizza la porzione di avviso relativa alla rata ed al canale di pagamento che preferisci.").setFont(asset.getInfoCodiciFont());
		Paragraph b1_2utilizzaLaPorzioneParagrafo = new Paragraph().add(b1_2utilizzaLaPorzioneText)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getInfoCodiciSize()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		b1_2utilizzaLaPorzioneCanvas.add(b1_2utilizzaLaPorzioneParagrafo);
		b1_2utilizzaLaPorzioneCanvas.close();

// BOLLETTINO 1SX
// 		banda grigia
		Rectangle b1sxBandaGrigiaRectangle = new Rectangle(0, 701, 297, 17);
		pdfCanvas.saveState().setFillColor(LeggoAsset.grigioBollettino).rectangle(b1sxBandaGrigiaRectangle).fill();
		Canvas b1sxenteCreditoreGrayCanvas = new Canvas(pdfCanvas, b1sxBandaGrigiaRectangle);
		b1sxenteCreditoreGrayCanvas.close();
// 		BANCHE ED ALTRI
		Rectangle b1sxBancaEAltriRectangle = new Rectangle(30, 704, 89, 15);
		Canvas b1sxBancaEAltriCanvas = new Canvas(pdfCanvas, b1sxBancaEAltriRectangle);
		Text b1sxBancaEAltriText = new Text("BANCHE E ALTRI").setFont(asset.getInEvidenza1Font());
		Paragraph b1sxBancaEAltriParagrafo = new Paragraph().add(b1sxBancaEAltriText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getInEvidenza1Size()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b1sxBancaEAltriCanvas.add(b1sxBancaEAltriParagrafo);
		b1sxBancaEAltriCanvas.close();
// 		RATA E SCADENZA
		Rectangle b1sxNumRataEScadenzaRectangle = new Rectangle(143, 704, 189, 15);
		Canvas b1sxNumRataEScadenzaCanvas = new Canvas(pdfCanvas, b1sxNumRataEScadenzaRectangle);
		Text b1sxNumRataText = new Text(documento.DatiBollettino.get(bollettinoDiPartenza).ProgressivoBoll + "Â° RATA ")
				.setFont(asset.getNumRataFont())
				.setFontSize(asset.getNumRataSize());
		Text b1sxScadenzaText = new Text("entro il ")
				.setFont(asset.getEntroRateFont())
				.setFontSize(asset.getEntroRateSize());
		Text b1sxDataText = new Text("entro il " + documento.DatiBollettino.get(bollettinoDiPartenza).ScadenzaRata)
				.setFont(asset.getInEvidenza5Font())
				.setFontSize(asset.getInEvidenza1Size());
		Paragraph b1sxNumRataEScadenzaParagrafo = new Paragraph().add(b1sxNumRataText).add(b1sxScadenzaText).add(b1sxDataText)
				.setFontColor(ColorConstants.BLACK).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b1sxNumRataEScadenzaCanvas.add(b1sxNumRataEScadenzaParagrafo);
		b1sxNumRataEScadenzaCanvas.close();
// 		QR CODE
		Rectangle b1sxQrCodeRectangle = new Rectangle(32, 611, 71, 71);
		Canvas b1sxQrCodeCanvas = new Canvas(pdfCanvas, b1sxQrCodeRectangle);
		b1sxQrCodeCanvas.add(generaQRCode(documento.DatiBollettino.get(bollettinoDiPartenza).BarcodePagoPa, pdf).scaleToFit(71, 71));
		b1sxQrCodeCanvas.close();
//		TESTO EURO e IMPORTO
		Rectangle b1sxrettangoloEurEImporto = new Rectangle(117, 667, 175, 20);
		Canvas b1sxEurEImportoCanvas = new Canvas(pdfCanvas, b1sxrettangoloEurEImporto);
		Text b1sxEuroTesto = new Text("Euro  ").setFont(asset.getValuta2Font()).setFontSize(asset.getValuta2Size());
		Text b1sxImportTesto = new Text(
				mettiVirgolaEPuntiAllImportoInCent(documento.DatiBollettino.get(bollettinoDiPartenza).Codeline2Boll))
						.setFont(asset.getImporto2Font()).setFontSize(asset.getImporto2Size());
		Paragraph b1sxparagrafoEurEImporto = new Paragraph().add(b1sxEuroTesto).setFontColor(ColorConstants.BLACK)
				.add(b1sxImportTesto).setFontColor(ColorConstants.BLACK).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		b1sxEurEImportoCanvas.add(b1sxparagrafoEurEImporto);
		b1sxEurEImportoCanvas.close();
//		TESTO DESTINATARIO AVVISO
		Rectangle b1sxDestinatarioAvvisoRettangolo = new Rectangle(117, 634, 185, 30);
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(205, 205, 205)).rectangle(b1sxDestinatarioAvvisoRettangolo).fill();
		//fine LP PG200420 - Errori nel pdf dell'avviso
		Canvas b1sxDestinatarioAvvisoCanvas = new Canvas(pdfCanvas, b1sxDestinatarioAvvisoRettangolo);
		Text b1sxDestinatarioAvvisoTesto = new Text("Destinatario dell'avviso\n").setFont(asset.getEtichettaDenominazioneFont())
				.setFontSize(asset.getEtichettaDenominazioneSize());
		Text b1sxValoreDestinatarioAvvisoTesto = new Text(documento.DatiAnagrafici.get(0).Denominazione1)
				.setFont(asset.getDenominazioneNome2Font()).setFontSize(asset.getDenominazioneNome2Size());
		Paragraph b1sxDestinatarioAvvisoParagrafo = new Paragraph().add(b1sxDestinatarioAvvisoTesto)
				.add(b1sxValoreDestinatarioAvvisoTesto).setFontColor(ColorConstants.BLACK).setFontSize(8).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.BOTTOM).setFixedLeading(12);
		b1sxDestinatarioAvvisoCanvas.add(b1sxDestinatarioAvvisoParagrafo);
		b1sxDestinatarioAvvisoCanvas.close();
//		TESTO ENTE CREDITORE
		Rectangle b1sxEnteCreditoreRettangolo = new Rectangle(115, 575, 166, 60);
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(225, 225, 225)).rectangle(b1sxEnteCreditoreRettangolo).fill();
		//fine LP PG200420 - Errori nel pdf dell'avviso
		Canvas b1sxEnteCreditoreCanvas = new Canvas(pdfCanvas, b1sxEnteCreditoreRettangolo);
		Text b1sxEnteCreditoreTesto = new Text("Ente Creditore\n").setFont(asset.getEtichettaDenominazioneFont()).setFontSize(asset.getEtichettaDenominazioneSize());
		Text b1sxValoreEnteCreditoreTesto = new Text(documento.DatiCreditore.get(0).Denominazione1)
				.setFont(asset.getDenominazioneNome2Font()).setFontSize(asset.getDenominazioneNome2Size());
		Paragraph b1sxEnteCreditoreParagrafo = new Paragraph().add(b1sxEnteCreditoreTesto)
				.add(b1sxValoreEnteCreditoreTesto).setFontColor(ColorConstants.BLACK).setFontSize(8).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.BOTTOM).setFixedLeading(11);
		b1sxEnteCreditoreCanvas.add(b1sxEnteCreditoreParagrafo);
		b1sxEnteCreditoreCanvas.close();
//		TESTO OGGETTO DEL PAGAMENTO
		Rectangle b1sxOggettoDelPagamentoRettangolo = new Rectangle(30, 575, 335, 30);
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(210, 210, 210)).rectangle(b1sxOggettoDelPagamentoRettangolo).fill();
		//fine LP PG200420 - Errori nel pdf dell'avviso
		Canvas b1sxOggettoDelPagamentoCanvas = new Canvas(pdfCanvas, b1sxOggettoDelPagamentoRettangolo);
		Text b1sxOggettoDelPagamentoTesto = new Text("Oggetto del pagamento\n").setFont(asset.getEtichettaDenominazioneFont())
				.setFontSize(asset.getEtichettaDenominazioneSize());
		Text b1sxValoreOggettoDelPagamentoTesto = new Text(documento.CausaleDocumento)
				.setFont(asset.getDenominazioneNome2Font()).setFontSize(asset.getDenominazioneNome2Size());
		Paragraph b1sxOggettoDelPagamentoParagrafo = new Paragraph().add(b1sxOggettoDelPagamentoTesto)
				.add(b1sxValoreOggettoDelPagamentoTesto).setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.BOTTOM).setFixedLeading(12);
		b1sxOggettoDelPagamentoCanvas.add(b1sxOggettoDelPagamentoParagrafo);
		b1sxOggettoDelPagamentoCanvas.close();
//		TESTO CBILL
		Rectangle b1sxCbillRettangolo = new Rectangle(30, 537, 135, 35);
		Canvas b1sxCbillCanvas = new Canvas(pdfCanvas, b1sxCbillRettangolo);
		Text b1sxCbillTesto = new Text("Codice CBILL\n").setFont(asset.getEtichettaDenominazioneFont()).setFontSize(asset.getEtichettaDenominazioneSize());
		Text b1sxValoreCbillTesto = new Text(documento.DatiCreditore.get(0).CodiceInterbancario).setFont(asset.getCodiceBoldFont())
				.setFontSize(asset.getCodiceBoldSize());
		Paragraph b1sxCbillParagrafo = new Paragraph().add(b1sxCbillTesto).add(b1sxValoreCbillTesto)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.BOTTOM).setFixedLeading(11.4f);
		b1sxCbillCanvas.add(b1sxCbillParagrafo);
		b1sxCbillCanvas.close();
//		TESTO CODICE FISCALE ENTE CREDITORE
		Rectangle b1sxCodFiscEnteCredRettangolo = new Rectangle(102, 537, 135, 35);
		Canvas b1sxCodFiscEnteCredCanvas = new Canvas(pdfCanvas, b1sxCodFiscEnteCredRettangolo);
		Text b1sxCodFiscEnteCredTesto = new Text("Cod. Fiscale Ente Creditore\n").setFont(asset.getEtichettaDenominazioneFont())
				.setFontSize(asset.getEtichettaDenominazioneSize());
		Text b1sxValoreCodFiscEnteCredTesto = new Text(documento.DatiCreditore.get(0).Cf)
				.setFont(asset.getCodiceBoldFont()).setFontSize(asset.getCodiceBoldSize());
		Paragraph b1sxCodFiscEnteCredParagrafo = new Paragraph().add(b1sxCodFiscEnteCredTesto)
				.add(b1sxValoreCodFiscEnteCredTesto).setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.BOTTOM).setFixedLeading(11.4f);
		b1sxCodFiscEnteCredCanvas.add(b1sxCodFiscEnteCredParagrafo);
		b1sxCodFiscEnteCredCanvas.close();
//		TESTO CODICE AVVISO 
		Rectangle b1sxCBillRettangolo = new Rectangle(30, 507, 135, 35);
		Canvas b1sxCBillCanvas = new Canvas(pdfCanvas, b1sxCBillRettangolo);
		Text b1sxCBillTesto = new Text("Codice Avviso\n").setFont(asset.getEtichettaDenominazioneFont()).setFontSize(asset.getEtichettaDenominazioneSize());
		Text b1sxValoreCBillTesto = new Text(formattaCodiceAvviso(documento.DatiBollettino.get(bollettinoDiPartenza).AvvisoPagoPa)).setFont(asset.getCodiceBoldFont())
				.setFontSize(asset.getCodiceBoldSize());
		Paragraph b1sxCBillParagrafo = new Paragraph().add(b1sxCBillTesto).add(b1sxValoreCBillTesto)
				.setFontColor(ColorConstants.BLACK).setMargin(0).setVerticalAlignment(VerticalAlignment.BOTTOM)
				.setFixedLeading(10.4f);
		b1sxCBillCanvas.add(b1sxCBillParagrafo);
		b1sxCBillCanvas.close();
//		TESTO ISTRUZIONI BANCHE E ALTRI 
		Rectangle b1sxIstruzioniBancheAltroRettangolo = new Rectangle(30, 485, 254, 27);
		Canvas b1sxIstruzioniBancheAltroCanvas = new Canvas(pdfCanvas, b1sxIstruzioniBancheAltroRettangolo);
		Text b1sxIstruzioniBancheAltro1Testo = new Text("Qui sopra trovi il codice ")
				.setFont(asset.getInfoCodiciFont());
		Text b1sxQRIstruzioniBancheAltroTesto = new Text("QR").setFont(asset.getInfoCodiciBoldFont());
		Text b1sxIstruzioniBancheAltro2Testo = new Text(" e il codice interbancario ")
				.setFont(asset.getInfoCodiciFont());
		Text b1sxCbillIstruzioniBancheAltroTesto = new Text("CBILL").setFont(asset.getInfoCodiciBoldFont());
		Text b1sxIstruzioniBancheAltro3Testo = new Text(
				" per pagare\nattraverso il circuito bancario e gli altri canali di pagamento abilitati.")
						.setFont(asset.getInfoCodiciFont());
		Paragraph b1sxIstruzioniBancheAltroParagrafo = new Paragraph().add(b1sxIstruzioniBancheAltro1Testo)
				.add(b1sxQRIstruzioniBancheAltroTesto).add(b1sxIstruzioniBancheAltro2Testo)
				.add(b1sxCbillIstruzioniBancheAltroTesto).add(b1sxIstruzioniBancheAltro3Testo)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getInfoCodiciSize()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.TOP).setFixedLeading(9.6f);
		b1sxIstruzioniBancheAltroCanvas.add(b1sxIstruzioniBancheAltroParagrafo);
		b1sxIstruzioniBancheAltroCanvas.close();

// 		separatore grigio
		Rectangle separatoreGrigioRectangle = new Rectangle(297, 466, 1, 235);
		pdfCanvas.saveState().setFillColor(LeggoAsset.grigioBollettino).rectangle(separatoreGrigioRectangle).fill();
		Canvas separatoreGrigioCanvas = new Canvas(pdfCanvas, separatoreGrigioRectangle);
		separatoreGrigioCanvas.close();

// BOLLETTINO 2DX
// 		banda grigia
		Rectangle b2dxBandaGrigiaRectangle = new Rectangle(298, 701, 297, 17);
		pdfCanvas.saveState().setFillColor(LeggoAsset.grigioBollettino).rectangle(b2dxBandaGrigiaRectangle).fill();
		Canvas b2dxenteCreditoreGrayCanvas = new Canvas(pdfCanvas, b2dxBandaGrigiaRectangle);
		b2dxenteCreditoreGrayCanvas.close();
// 		BANCHE ED ALTRI
		Rectangle b2dxBancaEAltriRectangle = new Rectangle(328, 704, 89, 15);
		Canvas b2dxBancaEAltriCanvas = new Canvas(pdfCanvas, b2dxBancaEAltriRectangle);
		Text b2dxBancaEAltriText = new Text("BANCHE E ALTRI").setFont(asset.getInEvidenza1Font());
		Paragraph b2dxBancaEAltriParagrafo = new Paragraph().add(b2dxBancaEAltriText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getInEvidenza1Size()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b2dxBancaEAltriCanvas.add(b2dxBancaEAltriParagrafo);
		b2dxBancaEAltriCanvas.close();
// 		RATA E SCADENZA
		Rectangle b2dxNumRataEScadenzaRectangle = new Rectangle(433, 704, 189, 15);
		Canvas b2dxNumRataEScadenzaCanvas = new Canvas(pdfCanvas, b2dxNumRataEScadenzaRectangle);
		Text b2dxNumRataText = new Text(Integer.toString(documento.DatiBollettino.get(bollettinoDiPartenza + 1).ProgressivoBoll) + "Â° RATA  ")
				.setFont(asset.getNumRataFont()).setFontSize(asset.getNumRataSize());
		Text b2dxScadenzaText = new Text(" entro il ")
				.setFont(asset.getEntroRateFont()).setFontSize(asset.getEntroRateSize());
		Text b2dxDataText = new Text(documento.DatiBollettino.get(bollettinoDiPartenza + 1).ScadenzaRata)
				.setFont(asset.getInEvidenza5Font()).setFontSize(asset.getInEvidenza1Size());
		Paragraph b2dxNumRataEScadenzaParagrafo = new Paragraph().add(b2dxNumRataText).add(b2dxScadenzaText).add(b2dxDataText)
				.setFontColor(ColorConstants.BLACK).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b2dxNumRataEScadenzaCanvas.add(b2dxNumRataEScadenzaParagrafo);
		b2dxNumRataEScadenzaCanvas.close();
// 		QR CODE 
		Rectangle b2dxQrCodeRectangle = new Rectangle(330, 611, 71, 71);
		Canvas b2dxQrCodeCanvas = new Canvas(pdfCanvas, b2dxQrCodeRectangle);
		b2dxQrCodeCanvas.add(generaQRCode(documento.DatiBollettino.get(bollettinoDiPartenza + 1).BarcodePagoPa, pdf).scaleToFit(71, 71));
		b2dxQrCodeCanvas.close();
//		TESTO EURO e IMPORTO
		Rectangle b2dxrettangoloEurEImporto = new Rectangle(413, 667, 175, 20);
		Canvas b2dxEurEImportoCanvas = new Canvas(pdfCanvas, b2dxrettangoloEurEImporto);
		Text b2dxEuroTesto = new Text("Euro  ").setFont(asset.getValuta2Font()).setFontSize(asset.getValuta2Size());
		Text b2dxImportTesto = new Text(mettiVirgolaEPuntiAllImportoInCent(documento.DatiBollettino.get(bollettinoDiPartenza + 1).Codeline2Boll))
				.setFont(asset.getImporto2Font()).setFontSize(asset.getImporto2Size());
		Paragraph b2dxparagrafoEurEImporto = new Paragraph().add(b2dxEuroTesto).setFontColor(ColorConstants.BLACK)
				.add(b2dxImportTesto).setFontColor(ColorConstants.BLACK).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		b2dxEurEImportoCanvas.add(b2dxparagrafoEurEImporto);
		b2dxEurEImportoCanvas.close();
//		TESTO DESTINATARIO AVVISO
		Rectangle b2dxDestinatarioAvvisoRettangolo = new Rectangle(413, 634, 185, 30);
		Canvas b2dxDestinatarioAvvisoCanvas = new Canvas(pdfCanvas, b2dxDestinatarioAvvisoRettangolo);
		Text b2dxDestinatarioAvvisoTesto = new Text("Destinatario dell'avviso\n").setFont(asset.getEtichettaDenominazioneFont())
				.setFontSize(asset.getEtichettaDenominazioneSize());
		Text b2dxValoreDestinatarioAvvisoTesto = new Text(documento.DatiAnagrafici.get(0).Denominazione1)
				.setFont(asset.getDenominazioneNome2Font()).setFontSize(asset.getDenominazioneNome2Size());
		Paragraph b2dxDestinatarioAvvisoParagrafo = new Paragraph().add(b2dxDestinatarioAvvisoTesto)
				.add(b2dxValoreDestinatarioAvvisoTesto).setFontColor(ColorConstants.BLACK).setFontSize(8).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.BOTTOM).setFixedLeading(12);
		b2dxDestinatarioAvvisoCanvas.add(b2dxDestinatarioAvvisoParagrafo);
		b2dxDestinatarioAvvisoCanvas.close();
//		TESTO ENTE CREDITORE
		Rectangle b2dxEnteCreditoreRettangolo = new Rectangle(413, 575, 166, 60);
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(225, 225, 225)).rectangle(b2dxDestinatarioAvvisoRettangolo).fill();
		//fine LP PG200420 - Errori nel pdf dell'avviso
		Canvas b2dxEnteCreditoreCanvas = new Canvas(pdfCanvas, b2dxEnteCreditoreRettangolo);
		Text b2dxEnteCreditoreTesto = new Text("Ente Creditore\n").setFont(asset.getEtichettaDenominazioneFont()).setFontSize(asset.getEtichettaDenominazioneSize());
		Text b2dxValoreEnteCreditoreTesto = new Text(documento.DatiCreditore.get(0).Denominazione1)
				.setFont(asset.getDenominazioneNome2Font()).setFontSize(asset.getDenominazioneNome2Size());
		Paragraph b2dxEnteCreditoreParagrafo = new Paragraph().add(b2dxEnteCreditoreTesto)
				.add(b2dxValoreEnteCreditoreTesto).setFontColor(ColorConstants.BLACK).setFontSize(8).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.BOTTOM).setFixedLeading(11);
		b2dxEnteCreditoreCanvas.add(b2dxEnteCreditoreParagrafo);
		b2dxEnteCreditoreCanvas.close();
//		TESTO OGGETTO DEL PAGAMENTO
		Rectangle b2dxOggettoDelPagamentoRettangolo = new Rectangle(328, 575, 335, 30);
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(205, 205, 205)).rectangle(b2dxOggettoDelPagamentoRettangolo).fill();
		//fine LP PG200420 - Errori nel pdf dell'avviso
		Canvas b2dxOggettoDelPagamentoCanvas = new Canvas(pdfCanvas, b2dxOggettoDelPagamentoRettangolo);
		Text b2dxOggettoDelPagamentoTesto = new Text("Oggetto del pagamento\n").setFont(asset.getEtichettaDenominazioneFont())
				.setFontSize(asset.getEtichettaDenominazioneSize());
		Text b2dxValoreOggettoDelPagamentoTesto = new Text(documento.CausaleDocumento)
				.setFont(asset.getDenominazioneNome2Font()).setFontSize(asset.getDenominazioneNome2Size());
		Paragraph b2dxOggettoDelPagamentoParagrafo = new Paragraph().add(b2dxOggettoDelPagamentoTesto)
				.add(b2dxValoreOggettoDelPagamentoTesto).setFontColor(ColorConstants.BLACK).setFontSize(8).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.BOTTOM).setFixedLeading(12);
		b2dxOggettoDelPagamentoCanvas.add(b2dxOggettoDelPagamentoParagrafo);
		b2dxOggettoDelPagamentoCanvas.close();
//		TESTO CBILL
		Rectangle b2dxCbillRettangolo = new Rectangle(328, 537, 135, 35);
		Canvas b2dxCbillCanvas = new Canvas(pdfCanvas, b2dxCbillRettangolo);
		Text b2dxCbillTesto = new Text("Codice CBILL\n").setFont(asset.getEtichettaDenominazioneFont()).setFontSize(asset.getEtichettaDenominazioneSize());
		Text b2dxValoreCbillTesto = new Text(documento.DatiCreditore.get(0).CodiceInterbancario).setFont(asset.getCodiceBoldFont())
				.setFontSize(asset.getDenominazioneNome2Size());
		Paragraph b2dxCbillParagrafo = new Paragraph().add(b2dxCbillTesto).add(b2dxValoreCbillTesto)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.BOTTOM).setFixedLeading(11.4f);
		b2dxCbillCanvas.add(b2dxCbillParagrafo);
		b2dxCbillCanvas.close();
//		TESTO CODICE FISCALE ENTE CREDITORE
		Rectangle b2dxCodFiscEnteCredRettangolo = new Rectangle(398, 537, 135, 35);
		Canvas b2dxCodFiscEnteCredCanvas = new Canvas(pdfCanvas, b2dxCodFiscEnteCredRettangolo);
		Text b2dxCodFiscEnteCredTesto = new Text("Cod. Fiscale Ente Creditore\n").setFont(asset.getEtichettaDenominazioneFont())
				.setFontSize(asset.getEtichettaDenominazioneSize());
		Text b2dxValoreCodFiscEnteCredTesto = new Text(documento.DatiCreditore.get(0).Cf)
				.setFont(asset.getCodiceBoldFont()).setFontSize(asset.getCodiceBoldSize());
		Paragraph b2dxCodFiscEnteCredParagrafo = new Paragraph().add(b2dxCodFiscEnteCredTesto)
				.add(b2dxValoreCodFiscEnteCredTesto).setFontColor(ColorConstants.BLACK).setFontSize(8).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.BOTTOM).setFixedLeading(11.4f);
		b2dxCodFiscEnteCredCanvas.add(b2dxCodFiscEnteCredParagrafo);
		b2dxCodFiscEnteCredCanvas.close();
//		TESTO CODICE AVVISO 
		Rectangle b2dxCBillRettangolo = new Rectangle(328, 507, 155, 35);
		Canvas b2dxCBillCanvas = new Canvas(pdfCanvas, b2dxCBillRettangolo);
		Text b2dxCBillTesto = new Text("Codice Avviso\n").setFont(asset.getEtichettaDenominazioneFont()).setFontSize(asset.getEtichettaDenominazioneSize());
		Text b2dxValoreCBillTesto = new Text(formattaCodiceAvviso(documento.DatiBollettino.get(bollettinoDiPartenza + 1).AvvisoPagoPa)).setFont(asset.getCodiceBoldFont())
				.setFontSize(asset.getCodiceBoldSize());
		Paragraph b2dxCBillParagrafo = new Paragraph().add(b2dxCBillTesto).add(b2dxValoreCBillTesto)
				.setFontColor(ColorConstants.BLACK).setMargin(0).setVerticalAlignment(VerticalAlignment.BOTTOM)
				.setFixedLeading(10.4f);
		b2dxCBillCanvas.add(b2dxCBillParagrafo);
		b2dxCBillCanvas.close();
//		TESTO ISTRUZIONI BANCHE E ALTRI 
		Rectangle b2dxIstruzioniBancheAltroRettangolo = new Rectangle(328, 485, 254, 27);
		Canvas b2dxIstruzioniBancheAltroCanvas = new Canvas(pdfCanvas, b2dxIstruzioniBancheAltroRettangolo);
		Text b2dxIstruzioniBancheAltro1Testo = new Text("Qui sopra trovi il codice ")
				.setFont(asset.getInfoCodiciFont());
		Text b2dxQRIstruzioniBancheAltroTesto = new Text("QR").setFont(asset.getInfoCodiciBoldFont());
		Text b2dxIstruzioniBancheAltro2Testo = new Text(" e il codice interbancario ")
				.setFont(asset.getInfoCodiciFont());
		Text b2dxCbillIstruzioniBancheAltroTesto = new Text("CBILL").setFont(asset.getInfoCodiciBoldFont());
		Text b2dxIstruzioniBancheAltro3Testo = new Text(
				" per pagare\nattraverso il circuito bancario e gli altri canali di pagamento abilitati.")
						.setFont(asset.getInfoCodiciFont());
		Paragraph b2dxIstruzioniBancheAltroParagrafo = new Paragraph().add(b2dxIstruzioniBancheAltro1Testo)
				.add(b2dxQRIstruzioniBancheAltroTesto).add(b2dxIstruzioniBancheAltro2Testo)
				.add(b2dxCbillIstruzioniBancheAltroTesto).add(b2dxIstruzioniBancheAltro3Testo)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getInfoCodiciSize()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.TOP).setFixedLeading(9.6f);
		b2dxIstruzioniBancheAltroCanvas.add(b2dxIstruzioniBancheAltroParagrafo);
		b2dxIstruzioniBancheAltroCanvas.close();

//	BOLLETTINO 1
//		banda grigia
		Rectangle b1_2bandaGrigia = new Rectangle(0, 448, 595, 19);
		pdfCanvas.saveState().setFillColor(LeggoAsset.grigioBollettino).rectangle(b1_2bandaGrigia).fill();
//		linea color grigio forbici
		Rectangle b1_2lineaNera = new Rectangle(0, 466, 595, 1);
		pdfCanvas.saveState().setFillColor(LeggoAsset.grigioForbici).rectangle(b1_2lineaNera).fill();
//		forbici
		Rectangle b1_2forbici = new Rectangle(546, 462, 15, 10);
		Canvas b1_2forbiciCanvas = new Canvas(pdfCanvas, b1_2forbici);
		b1_2forbiciCanvas.add(asset.getLogo_forbici().scaleToFit(51, 15));
		b1_2forbiciCanvas.close();
//		TESTO BOLLETTINO POSTALE PA
		Rectangle b1_2rettangoloTestoBollettinoPostePA = new Rectangle(31, 450, 145, 17);
		Canvas b1_2TestoBollettinoPostePACanvas = new Canvas(pdfCanvas, b1_2rettangoloTestoBollettinoPostePA);
		Text b1_2intest = new Text("BOLLETTINO POSTALE PA").setFont(asset.getTitoloFont());
		Paragraph b1_2paragrafoBollettinoPostePA = new Paragraph().add(b1_2intest).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getTitoloSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b1_2TestoBollettinoPostePACanvas.add(b1_2paragrafoBollettinoPostePA);
		b1_2TestoBollettinoPostePACanvas.close();
//		LOGO BANCOPOSTA
		Rectangle b1_2logoBancoPosta = new Rectangle(179, 445, 50, 15);
		Canvas b1_2logoBancoPostaCanvas = new Canvas(pdfCanvas, b1_2logoBancoPosta);
		b1_2logoBancoPostaCanvas.add(asset.getLogo_bancoposta().scaleToFit(50, 15));
		b1_2logoBancoPostaCanvas.close();
//		TESTO NÂ° RATA E SCADENZA
		Rectangle b1_2rettangoloTestoNumEScadRata = new Rectangle(433 - asset.getXoffSet() * 2, 450, 157, 17);
		Canvas b1_2TestoNumEScadRataCanvas = new Canvas(pdfCanvas, b1_2rettangoloTestoNumEScadRata);
		Text b1_2NumRataTesto = new Text(Integer.toString(documento.DatiBollettino.get(bollettinoDiPartenza).ProgressivoBoll) + "Â° RATA ").setFont(asset.getNumRataFont());
		Text b1_2entroIlTesto = new Text("  entro il  ").setFont(asset.getEntroRateFont());
		Text b1_2scadenzaTesto = new Text(documento.DatiBollettino.get(bollettinoDiPartenza).ScadenzaRata).setFont(asset.getInEvidenza5Font());
		Paragraph b1_2paragrafoNumEScadRata = new Paragraph()
				.add(b1_2NumRataTesto).setFontColor(ColorConstants.BLACK).setFontSize(asset.getNumRataSize())
				.add(b1_2entroIlTesto).setFontColor(ColorConstants.BLACK).setFontSize(asset.getEntroRateSize())
				.add(b1_2scadenzaTesto).setFontColor(ColorConstants.BLACK).setFontSize(asset.getInEvidenza1Size())
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b1_2TestoNumEScadRataCanvas.add(b1_2paragrafoNumEScadRata);
		b1_2TestoNumEScadRataCanvas.close();
//		LOGO POSTEITALIANE
		Rectangle b1_2logoPoste = new Rectangle(31, 390, 112, 15);
		Canvas b1_2logoPosteCanvas = new Canvas(pdfCanvas, b1_2logoPoste);
		b1_2logoPosteCanvas.add(asset.getLogo_poste_italiane().scaleToFit(112, 15));
		b1_2logoPosteCanvas.close();
//		LOGO €
		Rectangle b1_2logoEuro = new Rectangle(178, 388, 20, 20);
		Canvas b1_2logoEuroCanvas = new Canvas(pdfCanvas, b1_2logoEuro);
		b1_2logoEuroCanvas.add(asset.getLogo_euro_bollettino().scaleToFit(20, 20));
		b1_2logoEuroCanvas.close();
//		TESTO SUL CC e VALORE NUM CC
		Rectangle b1_2rettangoloCCeNumCC = new Rectangle(205, 390, 205, 20);
		Canvas b1_2CCeNumCCCanvas = new Canvas(pdfCanvas, b1_2rettangoloCCeNumCC);
		Text b1_2CCTesto = new Text("sul C/C n. ").setFont(asset.getSulCcFont()).setFontSize(asset.getSulCcSize());
		Text b1_2numCcPostTesto = new Text(documento.DatiBollettino.get(bollettinoDiPartenza).Codeline12Boll).setFont(asset.getCodiceBoldFont()).setFontSize(asset.getCodiceBoldSize());//numero cc postale
		Paragraph b1_2paragrafoCCeNumCC = new Paragraph().add(b1_2CCTesto).add(b1_2numCcPostTesto)
				.setFontColor(ColorConstants.BLACK).setMargin(0).setVerticalAlignment(VerticalAlignment.BOTTOM);
		b1_2CCeNumCCCanvas.add(b1_2paragrafoCCeNumCC);
		b1_2CCeNumCCCanvas.close();
//		TESTO EURO e IMPORTO
		Rectangle b1_2rettangoloEurEImporto = new Rectangle(464, 390, 175, 20);
		Canvas b1_2EurEImportoCanvas = new Canvas(pdfCanvas, b1_2rettangoloEurEImporto);
		Text b1_2EuroTesto = new Text("Euro  ").setFont(asset.getValuta2Font()).setFontSize(asset.getValuta2Size());
		Text b1_2ImportTesto = b1sxImportTesto.setFont(asset.getImporto2Font()).setFontSize(asset.getImporto2Size());
		Paragraph b1_2paragrafoEurEImporto = new Paragraph().add(b1_2EuroTesto).setFontColor(ColorConstants.BLACK)
				.add(b1_2ImportTesto).setFontColor(ColorConstants.BLACK).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.BOTTOM);
		b1_2EurEImportoCanvas.add(b1_2paragrafoEurEImporto);
		b1_2EurEImportoCanvas.close();
//		LOGO BOLLETTINO
		Rectangle b1_2logoBollettino = new Rectangle(30, 348, 78, 32);
		Canvas b1_2logoBollettinoCanvas = new Canvas(pdfCanvas, b1_2logoBollettino);
		b1_2logoBollettinoCanvas.add(asset.getLogo_bollettino_postale().scaleToFit(78, 32));
		b1_2logoBollettinoCanvas.close();
//		TESTO DOVE PAGARE
		Rectangle b1_2rettangoloDovePagare = new Rectangle(30, 294, 120, 48);
		Canvas b1_2dovePagareCanvas = new Canvas(pdfCanvas, b1_2rettangoloDovePagare);
		Text b1_2dovePagareTesto = new Text("Bollettino Postale pagabile in tutti gli Uffici Postali e sui canali fisici o digitali abilitati di Poste italiane e dell'Ente Creditore").setFont(asset.getInfoBollettinoFont());
		Paragraph b1_2paragrafoDovePagare = new Paragraph().add(b1_2dovePagareTesto).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getInfoBollettinoSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.TOP).setFixedLeading(9.6f);
		b1_2dovePagareCanvas.add(b1_2paragrafoDovePagare);
		b1_2dovePagareCanvas.close();
//		TESTO AUTORIZZAZIONE
		Rectangle b1_2rettangoloAutorizzazione = new Rectangle(30, 282, 160, 10);
		Canvas b1_2AutorizzazioneCanvas = new Canvas(pdfCanvas, b1_2rettangoloAutorizzazione);
		Text b1_2AutorizzazioneTesto = new Text(documento.DatiBollettino.get(bollettinoDiPartenza).AutorizCcp).setFont(asset.getAutorizzazioneFont());
		Paragraph b1_2paragrafoAutorizzazione = new Paragraph().add(b1_2AutorizzazioneTesto)
				.setFontColor(asset.grigioForbici).setFontSize(asset.getAutorizzazioneSize()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.TOP);
		b1_2AutorizzazioneCanvas.add(b1_2paragrafoAutorizzazione);
		b1_2AutorizzazioneCanvas.close();

		// Intestato a
		Rectangle b1_intestatoARectangle = new Rectangle(178, 362, 59, 12);
		Canvas b1_intestatoACanvas = new Canvas(pdfCanvas, b1_intestatoARectangle);
		Text b1_intestatoAText = new Text(LeggoAsset.INTESTATO_A).setFont(asset.getEtichettaDenominazioneFont());
		Paragraph b1_intestatoAP = new Paragraph().add(b1_intestatoAText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getEtichettaDenominazioneSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b1_intestatoACanvas.add(b1_intestatoAP);
		b1_intestatoACanvas.close();

		// Intestatario CC Postale
		Rectangle b1_intestatarioCCPostaleRectangle = new Rectangle(220 + asset.getXoffSet(), 363 + asset.getYoffSet(), 258, 10);
		Canvas b1_intestatarioCCPostaleCanvas = new Canvas(pdfCanvas, b1_intestatarioCCPostaleRectangle);
		Text b1_intestatarioCCPostaleText = new Text(documento.DatiBollettino.get(bollettinoDiPartenza).Descon60Boll)
				.setFont(asset.getDenominazioneNome2Font());
		Paragraph b1_intestatarioCCPostaleP = new Paragraph().add(b1_intestatarioCCPostaleText)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		b1_intestatarioCCPostaleCanvas.add(b1_intestatarioCCPostaleP);
		b1_intestatarioCCPostaleCanvas.close();

		// Destinatario		
		Rectangle b1_destinatarioRectangle3 = new Rectangle(178, 345, 57, 12);
		Canvas b1_destinatarioCanvas3 = new Canvas(pdfCanvas, b1_destinatarioRectangle3);
		Text b1_destinatarioText = new Text("Destinatario").setFont(asset.getEtichettaDenominazioneFont());
		Paragraph b1_destinatarioP = new Paragraph().add(b1_destinatarioText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getEtichettaDenominazioneSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b1_destinatarioCanvas3.add(b1_destinatarioP);
		b1_destinatarioCanvas3.close();

		// Nome Cognome Destnatario
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		//Rectangle b1_nomeDestinatarioRectangle3 = new Rectangle(229, 347, 169, 10);
		Rectangle b1_nomeDestinatarioRectangle3 = new Rectangle(229 + asset.getXoffSet(), 322 + asset.getYoffSet(), 250, 32);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(25, 0, 225)).rectangle(b1_nomeDestinatarioRectangle3).fill();
		//fine LP PG200420 - Errori nel pdf dell'avviso
		Canvas b1_nomeDestinatarioCanvas3 = new Canvas(pdfCanvas, b1_nomeDestinatarioRectangle3);
		Text b1_nomeDestinatarioText = new Text(documento.DatiAnagrafici.get(0).Denominazione1)
				.setFont(asset.getDenominazioneNome2Font());
		Paragraph b1_nomeDestinatarioP3 = new Paragraph().add(b1_nomeDestinatarioText)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		b1_nomeDestinatarioP3.setFixedLeading(8);
		//fine LP PG200420 - Errori nel pdf dell'avviso
		b1_nomeDestinatarioCanvas3.add(b1_nomeDestinatarioP3);
		b1_nomeDestinatarioCanvas3.close();

		// Oggetto Pagamento
		Rectangle b1_oggettoPagamentoRectangle3 = new Rectangle(178, 330, 88, 12);
		Canvas b1_oggettoPagamentoCanvas3 = new Canvas(pdfCanvas, b1_oggettoPagamentoRectangle3);
		Text b1_oggettoPagamentoText3 = new Text(LeggoAsset.OGGETTO_PAGAMENTO).setFont(asset.getEtichettaDenominazioneFont());
		Paragraph b1_oggettoPagamentoP3 = new Paragraph().add(b1_oggettoPagamentoText3)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getEtichettaDenominazioneSize()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		b1_oggettoPagamentoCanvas3.add(b1_oggettoPagamentoP3);
		b1_oggettoPagamentoCanvas3.close();

		// Oggetto Pagamento String
		Rectangle b1_oggettoPagamentoStringRectangle3 = new Rectangle(250 + asset.getXoffSet() * 3, 310 + asset.getYoffSet(), 213, 30);
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(225, 0, 25)).rectangle(b1_oggettoPagamentoStringRectangle3).fill();
		//fine LP PG200420 - Errori nel pdf dell'avviso
		Canvas b1_oggettoPagamentoStringCanvas3 = new Canvas(pdfCanvas, b1_oggettoPagamentoStringRectangle3);
		Text b1_oggettoPagamentoStringText3 = new Text(documento.CausaleDocumento).setFont(asset.getDenominazioneNome2Font());
		Paragraph b1_oggettoPagamentoStringP3 = new Paragraph().add(b1_oggettoPagamentoStringText3)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(9);
		b1_oggettoPagamentoStringCanvas3.add(b1_oggettoPagamentoStringP3);
		b1_oggettoPagamentoStringCanvas3.close();

//		TESTO CODICE AVVISO
		Rectangle b1_2rettangoloCodiceAvviso = new Rectangle(178, 284, 135, 30);
		Canvas b1_2codiceAvvisoCanvas = new Canvas(pdfCanvas, b1_2rettangoloCodiceAvviso);
		Text b1_2codiceAvvisoTesto = new Text("Codice Avviso\n").setFont(asset.getEtichettaDenominazioneFont()).setFontSize(asset.getEtichettaDenominazioneSize());
		Text b1_2valoreCodiceAvvisoTesto = new Text(formattaCodiceAvviso(documento.DatiBollettino.get(bollettinoDiPartenza).AvvisoPagoPa)).setFont(asset.getCodiceBoldFont())
				.setFontSize(asset.getCodiceBoldSize());
		Paragraph b1_2paragrafoCodiceAvviso = new Paragraph().add(b1_2codiceAvvisoTesto)
				.add(b1_2valoreCodiceAvvisoTesto).setFontColor(ColorConstants.BLACK).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.BOTTOM).setFixedLeading(10.2f);
		b1_2codiceAvvisoCanvas.add(b1_2paragrafoCodiceAvviso);
		b1_2codiceAvvisoCanvas.close();
//		TESTO TIPO
		Rectangle b1_2rettangoloTipo = new Rectangle(333, 284, 25, 30);
		Canvas b1_2tipoCanvas = new Canvas(pdfCanvas, b1_2rettangoloTipo);
		Text b1_2tipoTesto = new Text("Tipo\n").setFont(asset.getEtichettaDenominazioneFont()).setFontSize(asset.getEtichettaDenominazioneSize());
		Text b1_2tipoValoreTesto = new Text(LeggoAsset.P1).setFont(asset.getCodiceBoldFont()).setFontSize(asset.getCodiceBoldSize());
		Paragraph b1_2paragrafoTipo = new Paragraph().add(b1_2tipoTesto).add(b1_2tipoValoreTesto)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.BOTTOM).setFixedLeading(asset.getEtichettaDenominazioneSize());
		b1_2tipoCanvas.add(b1_2paragrafoTipo);
		b1_2tipoCanvas.close();
//		TESTO COD FISC ENTE CREDITORE
		Rectangle b1_2rettangoloCodFiscEnteCred = new Rectangle(372, 284, 120, 30);
		Canvas b1_2CodFiscEnteCredCanvas = new Canvas(pdfCanvas, b1_2rettangoloCodFiscEnteCred);
		Text b1_2CodFiscEnteCredTesto = new Text("Cod. Fiscale Ente Creditore\n").setFont(asset.getEtichettaDenominazioneFont())
				.setFontSize(asset.getEtichettaDenominazioneSize());
		Text b1_2CodFiscEnteCredValoreTesto = new Text(documento.DatiCreditore.get(0).Cf)
				.setFont(asset.getCodiceBoldFont()).setFontSize(asset.getCodiceBoldSize());
		Paragraph b1_2paragrafoCodFiscEnteCred = new Paragraph().add(b1_2CodFiscEnteCredTesto)
				.add(b1_2CodFiscEnteCredValoreTesto).setFontColor(ColorConstants.BLACK).setFontSize(asset.getEtichettaDenominazioneSize()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.BOTTOM).setFixedLeading(10.2f);
		b1_2CodFiscEnteCredCanvas.add(b1_2paragrafoCodFiscEnteCred);
		b1_2CodFiscEnteCredCanvas.close();
		// Data matrix Container
		Rectangle b1_2dataMatrixContainerRectangle = new Rectangle(480, 281, 91, 91);
		Canvas b1_2dataMatrixContainerCanvas = new Canvas(pdfCanvas, b1_2dataMatrixContainerRectangle);
		b1_2dataMatrixContainerCanvas.add(asset.getData_matrix_container().scaleToFit(91, 91));
		b1_2dataMatrixContainerCanvas.close();

		// Data matrix
		// Rectangle b1_2dataMatrixRectangle = new Rectangle(490, 291, 72, 72);
		// Canvas b1_2dataMatrixCanvas = new Canvas(pdfCanvas, b1_2dataMatrixRectangle);
		// b1_2dataMatrixCanvas.add(asset.getData_matrix().scaleToFit(72, 72));
		// b1_2dataMatrixCanvas.close();

// 		Data matrix
		Rectangle b1_dataMatrixRectangle = new Rectangle(490, 291, 72, 72);
		Canvas b1_dataMatrixCanvas = new Canvas(pdfCanvas, b1_dataMatrixRectangle);
		b1_dataMatrixCanvas.add(generaDataMatrix(documento.DatiBollettino.get(bollettinoDiPartenza).QRcodePagoPa, pdf).scaleToFit(72, 72));
		b1_dataMatrixCanvas.close();

//	BOLLETTINO 2
//		banda grigia
		Rectangle b2_2bandaGrigia = new Rectangle(0, 224, 595, 19);
		pdfCanvas.saveState().setFillColor(LeggoAsset.grigioBollettino).rectangle(b2_2bandaGrigia).fill();
//		linea color grigio forbici
		Rectangle b2_2lineaNera = new Rectangle(0, 242, 595, 1);
		pdfCanvas.saveState().setFillColor(LeggoAsset.grigioForbici).rectangle(b2_2lineaNera).fill();
//		forbici
		Rectangle b2_2forbici = new Rectangle(546, 238, 15, 10);
		Canvas b2_2forbiciCanvas = new Canvas(pdfCanvas, b2_2forbici);
		b2_2forbiciCanvas.add(asset.getLogo_forbici().scaleToFit(51, 15));
		b2_2forbiciCanvas.close();
//		TESTO BOLLETTINO POSTALE PA
		Rectangle b2_2rettangoloTestoBollettinoPostePA = new Rectangle(31, 226, 135, 17);
		Canvas b2_2TestoBollettinoPostePACanvas = new Canvas(pdfCanvas, b2_2rettangoloTestoBollettinoPostePA);
		Text b2_2intest = new Text("BOLLETTINO POSTALE PA").setFont(asset.getInEvidenza1Font());
		Paragraph b2_2paragrafoBollettinoPostePA = new Paragraph().add(b2_2intest).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getInEvidenza1Size()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b2_2TestoBollettinoPostePACanvas.add(b2_2paragrafoBollettinoPostePA);
		b2_2TestoBollettinoPostePACanvas.close();
//		LOGO BANCOPOSTA
		Rectangle b2_2logoBancoPosta = new Rectangle(179, 221, 50, 15);
		Canvas b2_2logoBancoPostaCanvas = new Canvas(pdfCanvas, b2_2logoBancoPosta);
		b2_2logoBancoPostaCanvas.add(asset.getLogo_bancoposta().scaleToFit(50, 15));
		b2_2logoBancoPostaCanvas.close();
//		TESTO NÂ° RATA E SCADENZA
		Rectangle b2_2rettangoloTestoNumEScadRata = new Rectangle(433 - asset.getXoffSet() * 2, 226, 157, 17);
		Canvas b2_2TestoNumEScadRataCanvas = new Canvas(pdfCanvas, b2_2rettangoloTestoNumEScadRata);
		Text b2_2NumRataTesto = new Text(Integer.toString(documento.DatiBollettino.get(bollettinoDiPartenza + 1).ProgressivoBoll) + "Â° RATA ").setFont(asset.getNumRataFont());
		Text b2_2entroIlTesto = new Text("  entro il  ").setFont(asset.getEntroRateFont());
		Text b2_2scadenzaTesto = new Text(documento.DatiBollettino.get(bollettinoDiPartenza + 1).ScadenzaRata).setFont(asset.getInEvidenza3Font());
		Paragraph b2_2paragrafoNumEScadRata = new Paragraph()
				.add(b2_2NumRataTesto).setFontColor(ColorConstants.BLACK).setFontSize(asset.getNumRataSize())
				.add(b2_2entroIlTesto).setFontColor(ColorConstants.BLACK).setFontSize(asset.getEntroRateSize())
				.add(b2_2scadenzaTesto).setFontColor(ColorConstants.BLACK).setFontSize(asset.getInEvidenza1Size())
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b2_2TestoNumEScadRataCanvas.add(b2_2paragrafoNumEScadRata);
		b2_2TestoNumEScadRataCanvas.close();
//		LOGO POSTEITALIANE
		Rectangle b2_2logoPoste = new Rectangle(31, 166, 112, 15);
		Canvas b2_2logoPosteCanvas = new Canvas(pdfCanvas, b2_2logoPoste);
		b2_2logoPosteCanvas.add(asset.getLogo_poste_italiane().scaleToFit(112, 15));
		b2_2logoPosteCanvas.close();
//		LOGO €
		Rectangle b2_2logoEuro = new Rectangle(178, 164, 20, 20);
		Canvas b2_2logoEuroCanvas = new Canvas(pdfCanvas, b2_2logoEuro);
		b2_2logoEuroCanvas.add(asset.getLogo_euro_bollettino().scaleToFit(20, 20));
		b2_2logoEuroCanvas.close();
//		TESTO SUL CC e VALORE NUM CC
		Rectangle b2_2rettangoloCCeNumCC = new Rectangle(205, 166, 205, 20);
		Canvas b2_2CCeNumCCCanvas = new Canvas(pdfCanvas, b2_2rettangoloCCeNumCC);
		Text b2_2CCTesto = new Text("sul C/C n. ").setFont(asset.getSulCcFont()).setFontSize(asset.getSulCcSize());
		Text b2_2numCcPostTesto = new Text(documento.DatiBollettino.get(bollettinoDiPartenza + 1).Codeline12Boll).setFont(asset.getCodiceBoldFont())
				.setFontSize(asset.getCodiceBoldSize());
		Paragraph b2_2paragrafoCCeNumCC = new Paragraph().add(b2_2CCTesto).add(b2_2numCcPostTesto)
				.setFontColor(ColorConstants.BLACK).setMargin(0).setVerticalAlignment(VerticalAlignment.BOTTOM);
		b2_2CCeNumCCCanvas.add(b2_2paragrafoCCeNumCC);
		b2_2CCeNumCCCanvas.close();
//		TESTO EURO e IMPORTO
		Rectangle b2_2rettangoloEurEImporto = new Rectangle(464, 166, 175, 20);
		Canvas b2_2EurEImportoCanvas = new Canvas(pdfCanvas, b2_2rettangoloEurEImporto);
		Text b2_2EuroTesto = new Text("Euro  ").setFont(asset.getValuta2Font()).setFontSize(asset.getValuta2Size());
		Text b2_2ImportTesto = new Text(mettiVirgolaEPuntiAllImportoInCent(documento.DatiBollettino.get(bollettinoDiPartenza + 1).Codeline2Boll))
				.setFont(asset.getImporto2Font()).setFontSize(asset.getImporto2Size());
		Paragraph b2_2paragrafoEurEImporto = new Paragraph().add(b2_2EuroTesto).setFontColor(ColorConstants.BLACK)
				.add(b2_2ImportTesto).setFontColor(ColorConstants.BLACK).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.BOTTOM);
		b2_2EurEImportoCanvas.add(b2_2paragrafoEurEImporto);
		b2_2EurEImportoCanvas.close();
//		LOGO BOLLETTINO
		Rectangle b2_2logoBollettino = new Rectangle(30, 124, 78, 32);
		Canvas b2_2logoBollettinoCanvas = new Canvas(pdfCanvas, b2_2logoBollettino);
		b2_2logoBollettinoCanvas.add(asset.getLogo_bollettino_postale().scaleToFit(78, 32));
		b2_2logoBollettinoCanvas.close();
//		TESTO DOVE PAGARE
		Rectangle b2_2rettangoloDovePagare = new Rectangle(30, 70, 120, 48);
		Canvas b2_2dovePagareCanvas = new Canvas(pdfCanvas, b2_2rettangoloDovePagare);
		Text b2_2dovePagareTesto = new Text("Bollettino Postale pagabile in tutti gli Uffici Postali e sui canali fisici o digitali abilitati di Poste italiane e dell'Ente Creditore").setFont(asset.getInfoBollettinoFont());
		Paragraph b2_2paragrafoDovePagare = new Paragraph().add(b2_2dovePagareTesto).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getInfoBollettinoSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.TOP).setFixedLeading(9.6f);
		b2_2dovePagareCanvas.add(b2_2paragrafoDovePagare);
		b2_2dovePagareCanvas.close();
//		TESTO AUTORIZZAZIONE
		Rectangle b2_2rettangoloAutorizzazione = new Rectangle(30, 58, 160, 10);
		Canvas b2_2AutorizzazioneCanvas = new Canvas(pdfCanvas, b2_2rettangoloAutorizzazione);
		Text b2_2AutorizzazioneTesto = new Text(documento.DatiBollettino.get(bollettinoDiPartenza + 1).AutorizCcp).setFont(asset.getAutorizzazioneFont());
		Paragraph b2_2paragrafoAutorizzazione = new Paragraph().add(b2_2AutorizzazioneTesto)
				.setFontColor(asset.grigioForbici).setFontSize(asset.getAutorizzazioneSize()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.TOP);
		b2_2AutorizzazioneCanvas.add(b2_2paragrafoAutorizzazione);
		b2_2AutorizzazioneCanvas.close();

		// Intestato a
		Rectangle b2_intestatoARectangle = new Rectangle(178, 362 - 225, 59, 12);
		Canvas b2_intestatoACanvas = new Canvas(pdfCanvas, b2_intestatoARectangle);
		Text b2_intestatoAText = new Text(LeggoAsset.INTESTATO_A).setFont(asset.getEtichettaDenominazioneFont());
		Paragraph b2_intestatoAP = new Paragraph().add(b2_intestatoAText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getEtichettaDenominazioneSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b2_intestatoACanvas.add(b2_intestatoAP);
		b2_intestatoACanvas.close();

		// Intestatario CC Postale
		Rectangle b2_intestatarioCCPostaleRectangle = new Rectangle(220 + asset.getXoffSet(), 363 - 225 + asset.getYoffSet(), 258, 10);
		Canvas b2_intestatarioCCPostaleCanvas = new Canvas(pdfCanvas, b2_intestatarioCCPostaleRectangle);
		Text b2_intestatarioCCPostaleText = new Text(documento.DatiBollettino.get(bollettinoDiPartenza + 1).Descon60Boll)
				.setFont(asset.getDenominazioneNome2Font());
		Paragraph b2_intestatarioCCPostaleP = new Paragraph().add(b2_intestatarioCCPostaleText)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		b2_intestatarioCCPostaleCanvas.add(b2_intestatarioCCPostaleP);
		b2_intestatarioCCPostaleCanvas.close();

		// Destinatario
		Rectangle b2_destinatarioRectangle3 = new Rectangle(178, 345 - 225, 57, 12);
		Canvas b2_destinatarioCanvas3 = new Canvas(pdfCanvas, b2_destinatarioRectangle3);
		Text b2_destinatarioText = new Text("Destinatario").setFont(asset.getEtichettaDenominazioneFont());
		Paragraph b2_destinatarioP = new Paragraph().add(b2_destinatarioText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getEtichettaDenominazioneSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b2_destinatarioCanvas3.add(b2_destinatarioP);
		b2_destinatarioCanvas3.close();

		// Nome Cognome Destnatario
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		//Rectangle b2_nomeDestinatarioRectangle3 = new Rectangle(229, 347 - 225, 169, 10);
		Rectangle b2_nomeDestinatarioRectangle3 = new Rectangle(229 + asset.getXoffSet(), 322 - 225 + asset.getYoffSet(), 250, 32);
		//fine LP PG200420 - Errori nel pdf dell'avviso
		Canvas b2_nomeDestinatarioCanvas3 = new Canvas(pdfCanvas, b2_nomeDestinatarioRectangle3);
		Text b2_nomeDestinatarioText = new Text(documento.DatiAnagrafici.get(0).Denominazione1)
				.setFont(asset.getDenominazioneNome2Font());
		Paragraph b2_nomeDestinatarioP3 = new Paragraph().add(b2_nomeDestinatarioText)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		b2_nomeDestinatarioP3.setFixedLeading(8);
		//fine LP PG200420 - Errori nel pdf dell'avviso
		b2_nomeDestinatarioCanvas3.add(b2_nomeDestinatarioP3);
		b2_nomeDestinatarioCanvas3.close();

		// Oggetto Pagamento
		Rectangle b2_oggettoPagamentoRectangle3 = new Rectangle(178, 330 - 225, 88, 12);
		Canvas b2_oggettoPagamentoCanvas3 = new Canvas(pdfCanvas, b2_oggettoPagamentoRectangle3);
		Text b2_oggettoPagamentoText3 = new Text(LeggoAsset.OGGETTO_PAGAMENTO).setFont(asset.getEtichettaDenominazioneFont());
		Paragraph b2_oggettoPagamentoP3 = new Paragraph().add(b2_oggettoPagamentoText3)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getEtichettaDenominazioneSize()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		b2_oggettoPagamentoCanvas3.add(b2_oggettoPagamentoP3);
		b2_oggettoPagamentoCanvas3.close();

		// Oggetto Pagamento String
		Rectangle b2_oggettoPagamentoStringRectangle3 = new Rectangle(250 + asset.getXoffSet() * 3, 310 - 225 + asset.getYoffSet(), 213, 30);
		Canvas b2_oggettoPagamentoStringCanvas3 = new Canvas(pdfCanvas, b2_oggettoPagamentoStringRectangle3);
		Text b2_oggettoPagamentoStringText3 = new Text(documento.CausaleDocumento).setFont(asset.getDenominazioneNome2Font());
		Paragraph b2_oggettoPagamentoStringP3 = new Paragraph().add(b2_oggettoPagamentoStringText3)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(9);
		b2_oggettoPagamentoStringCanvas3.add(b2_oggettoPagamentoStringP3);
		b2_oggettoPagamentoStringCanvas3.close();

//		TESTO CODICE AVVISO
		Rectangle b2_2rettangoloCodiceAvviso = new Rectangle(178, 60, 155, 30);
		Canvas b2_2codiceAvvisoCanvas = new Canvas(pdfCanvas, b2_2rettangoloCodiceAvviso);
		Text b2_2codiceAvvisoTesto = new Text("Codice Avviso\n").setFont(asset.getEtichettaDenominazioneFont()).setFontSize(asset.getEtichettaDenominazioneSize());
		Text b2_2valoreCodiceAvvisoTesto = new Text(formattaCodiceAvviso(documento.DatiBollettino.get(bollettinoDiPartenza + 1).AvvisoPagoPa)).setFont(asset.getCodiceBoldFont())
				.setFontSize(asset.getCodiceBoldSize());
		Paragraph b2_2paragrafoCodiceAvviso = new Paragraph().add(b2_2codiceAvvisoTesto)
				.add(b2_2valoreCodiceAvvisoTesto).setFontColor(ColorConstants.BLACK).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.BOTTOM).setFixedLeading(10.2f);
		b2_2codiceAvvisoCanvas.add(b2_2paragrafoCodiceAvviso);
		b2_2codiceAvvisoCanvas.close();
//		TESTO TIPO
		Rectangle b2_2rettangoloTipo = new Rectangle(333, 60, 25, 30);
		Canvas b2_2tipoCanvas = new Canvas(pdfCanvas, b2_2rettangoloTipo);
		Text b2_2tipoTesto = new Text("Tipo\n").setFont(asset.getEtichettaDenominazioneFont()).setFontSize(8);
		Text b2_2tipoValoreTesto = new Text(LeggoAsset.P1).setFont(asset.getCodiceBoldFont()).setFontSize(10.2f);
		Paragraph b2_2paragrafoTipo = new Paragraph().add(b2_2tipoTesto).add(b2_2tipoValoreTesto)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.BOTTOM).setFixedLeading(10.2f);
		b2_2tipoCanvas.add(b2_2paragrafoTipo);
		b2_2tipoCanvas.close();
//		TESTO COD FISC ENTE CREDITORE
		Rectangle b2_2rettangoloCodFiscEnteCred = new Rectangle(372, 60, 120, 30);
		Canvas b2_2CodFiscEnteCredCanvas = new Canvas(pdfCanvas, b2_2rettangoloCodFiscEnteCred);
		Text b2_2CodFiscEnteCredTesto = new Text("Cod. Fiscale Ente Creditore\n").setFont(asset.getEtichettaDenominazioneFont())
				.setFontSize(asset.getEtichettaDenominazioneSize());
		Text b2_2CodFiscEnteCredValoreTesto = new Text(documento.DatiCreditore.get(0).Cf)
				.setFont(asset.getCodiceBoldFont()).setFontSize(asset.getCodiceBoldSize());
		Paragraph b2_2paragrafoCodFiscEnteCred = new Paragraph().add(b2_2CodFiscEnteCredTesto)
				.add(b2_2CodFiscEnteCredValoreTesto).setFontColor(ColorConstants.BLACK).setFontSize(asset.getEtichettaDenominazioneSize()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.BOTTOM).setFixedLeading(10.2f);
		b2_2CodFiscEnteCredCanvas.add(b2_2paragrafoCodFiscEnteCred);
		b2_2CodFiscEnteCredCanvas.close();
		// Data matrix Container
		Rectangle b2_2dataMatrixContainerRectangle = new Rectangle(480, 57, 91, 91);
		Canvas b2_2dataMatrixContainerCanvas = new Canvas(pdfCanvas, b2_2dataMatrixContainerRectangle);
		b2_2dataMatrixContainerCanvas.add(asset.getData_matrix_container().scaleToFit(91, 91));
		b2_2dataMatrixContainerCanvas.close();

		// Data matrix
		// Rectangle b2_2dataMatrixRectangle = new Rectangle(490, 67, 72, 72);
		// Canvas b2_2dataMatrixCanvas = new Canvas(pdfCanvas, b2_2dataMatrixRectangle);
		// b2_2dataMatrixCanvas.add(asset.getData_matrix().scaleToFit(72, 72));
		// b2_2dataMatrixCanvas.close();

// 		Data matrix
		Rectangle b2_dataMatrixRectangle = new Rectangle(490, 67, 72, 72);
		Canvas b2_dataMatrixCanvas = new Canvas(pdfCanvas, b2_dataMatrixRectangle);
		b2_dataMatrixCanvas.add(generaDataMatrix(documento.DatiBollettino.get(bollettinoDiPartenza + 1).QRcodePagoPa, pdf).scaleToFit(72, 72));
		b2_dataMatrixCanvas.close();

	}

	private void paginaTreBollettini(PdfPage pageTarget, LeggoAsset asset, Documento documento, PdfDocument pdf, int bollettinoDiPartenza, String tipoStampa) {
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		boolean bDebug = false; //se == true mostra alcune aree con fill colorato per verificare posizione e dimensione 
		//fine LP PG200420 - Errori nel pdf dell'avviso
//		creazione del pdf canvas
		PdfCanvas pdfCanvas = new PdfCanvas(pageTarget);

		// Logo PagoPa
		Rectangle logoPagopaRectangle = new Rectangle(32, 789, 35, 23);
		Canvas logoPagopaCanvas = new Canvas(pdfCanvas, logoPagopaRectangle);
		logoPagopaCanvas.add(asset.getLogo_pagopa().scaleToFit(35, 23));
		logoPagopaCanvas.close();

		// Avviso di Pagamento
		Rectangle avvisoPagamentoRectangle = new Rectangle(70, 800, 124, 15);
		Canvas avvisoPagamentoCanvas = new Canvas(pdfCanvas, avvisoPagamentoRectangle);
		Text avvisoPagamentoText = new Text("AVVISO DI PAGAMENTO").setFont(asset.getTitoloFont());
		Paragraph avvisoPagamentoP = new Paragraph().add(avvisoPagamentoText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getInEvidenza1Size()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);//FIXME uso inEvidenza1Size perchÃ© corrisponde alla combinazione di dimensioni del titolo delle pagine successive
		avvisoPagamentoCanvas.add(avvisoPagamentoP);
		avvisoPagamentoCanvas.close();

		// Logo Ente
		Rectangle logoEnteRectangle = new Rectangle(480, 732, asset.getPathLogoEnteSizeX(), asset.getPathLogoEnteSizeY()); //valori di default 84x84px
		Canvas logoEnteCanvas = new Canvas(pdfCanvas, logoEnteRectangle);
		logoEnteCanvas.add(asset.getLogo_ente().scaleToFit(asset.getPathLogoEnteSizeX(), asset.getPathLogoEnteSizeY()));
		logoEnteCanvas.close();

		// Rate
		Rectangle oggettoPagamentoRectangle = new Rectangle(32, 715, 445, 60);
		Canvas oggettoPagamentoCanvas = new Canvas(pdfCanvas, oggettoPagamentoRectangle);
		Text oggettoPagamentoText = new Text("Rate " + Integer.toString(documento.DatiBollettino.get(bollettinoDiPartenza).ProgressivoBoll) + "Â°, " + Integer.toString(documento.DatiBollettino.get(bollettinoDiPartenza + 1).ProgressivoBoll) + "Â°e "
				+ Integer.toString(documento.DatiBollettino.get(bollettinoDiPartenza + 2).ProgressivoBoll) + "Â°- " + documento.CausaleDocumento).setFont(asset.getTestataFont());
		Paragraph oggettoPagamentoP = new Paragraph().add(oggettoPagamentoText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getImporto1Size()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(18); //FIXME uso getImporto1Size perchÃ© corrisponde alla combinazione di dimensioni del titolo delle pagine successive
		oggettoPagamentoCanvas.add(oggettoPagamentoP);
		oggettoPagamentoCanvas.close();

		// Utilizza Porzione
		Rectangle utilizzaPorzioneRectangle = new Rectangle(32, 725, 345, 13);
		Canvas utilizzaPorzioneCanvas = new Canvas(pdfCanvas, utilizzaPorzioneRectangle);
		Text utilizzaPorzioneText = new Text("Utilizza la porzione di avviso relativa alla rata ed al canale di pagamento che preferisci.").setFont(asset.getInfoCodiciFont());
		Paragraph utilizzaPorzioneP = new Paragraph().add(utilizzaPorzioneText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getInfoCodiciSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		utilizzaPorzioneCanvas.add(utilizzaPorzioneP);
		utilizzaPorzioneCanvas.close();

		// n Rate Rectangle
		Rectangle nRateGrayRectangle = new Rectangle(0, 701, 595, 19);
		pdfCanvas.saveState().setFillColor(LeggoAsset.grigioBollettino).rectangle(nRateGrayRectangle).fill();
		Canvas nRateGrayCanvas = new Canvas(pdfCanvas, nRateGrayRectangle);
		nRateGrayCanvas.close();

		// n Rate Line
		Rectangle nRateLineGrayRectangle = new Rectangle(197, 477, 1, 244);
		pdfCanvas.saveState().setFillColor(LeggoAsset.grigioRate).rectangle(nRateLineGrayRectangle).fill();
		Canvas nRateLineGrayCanvas = new Canvas(pdfCanvas, nRateLineGrayRectangle);
		nRateLineGrayCanvas.close();

		// n Rate Line 2
		Rectangle nRateLineGrayRectangle2 = new Rectangle(396, 477, 1, 244);
		pdfCanvas.saveState().setFillColor(LeggoAsset.grigioRate).rectangle(nRateLineGrayRectangle2).fill();
		Canvas nRateLineGrayCanvas2 = new Canvas(pdfCanvas, nRateLineGrayRectangle2);
		nRateLineGrayCanvas2.close();

		// n3rata
		Rectangle n3RataRectangle = new Rectangle(32, 704, 186, 15);
		Canvas n3RataCanvas = new Canvas(pdfCanvas, n3RataRectangle);
		Text n3RataText1 = new Text(Integer.toString(documento.DatiBollettino.get(bollettinoDiPartenza).ProgressivoBoll) + "Â° ").setFont(asset.getInEvidenza5Font()).setFontSize(asset.getInEvidenza5Size());
		Text n3RataText2 = new Text("RATA  ").setFont(asset.getInEvidenza5Font()).setFontSize(asset.getInEvidenza5Size());
		Text n3RataText3 = new Text(" entro il  ").setFont(asset.getEntroRateFont()).setFontSize(asset.getEntroRateSize());
		Text n3RataText4 = new Text(documento.DatiBollettino.get(bollettinoDiPartenza).ScadenzaRata).setFont(asset.getInEvidenza5Font()).setFontSize(asset.getInEvidenza1Size()); //non è un errore in evidenza 3 e 1, serve una combinaizone diversa
		Paragraph n3RataP = new Paragraph().add(n3RataText1).add(n3RataText2).add(n3RataText3).add(n3RataText4)
				.setFontColor(ColorConstants.BLACK).setMargin(0);
		n3RataCanvas.add(n3RataP);
		n3RataCanvas.close();

		// n4rata
		Rectangle n4RataRectangle = new Rectangle(222, 704, 186, 15);
		Canvas n4RataCanvas = new Canvas(pdfCanvas, n4RataRectangle);
		Text n4RataText1 = new Text(Integer.toString(documento.DatiBollettino.get(bollettinoDiPartenza + 1).ProgressivoBoll) + "Â° ").setFont(asset.getNumRataFont()).setFontSize(asset.getNumRataSize());
		Text n4RataText2 = new Text("RATA  ").setFont(asset.getNumRataFont()).setFontSize(asset.getNumRataSize());
		Text n4RataText3 = new Text(" entro il  ").setFont(asset.getEntroRateFont()).setFontSize(asset.getEntroRateSize());
		Text n4RataText4 = new Text(documento.DatiBollettino.get(bollettinoDiPartenza + 1).ScadenzaRata).setFont(asset.getInEvidenza5Font()).setFontSize(asset.getInEvidenza1Size()); //non è un errore in evidenza 3 e 1, serve una combinaizone diversa
		Paragraph n4RataP = new Paragraph().add(n4RataText1).add(n4RataText2).add(n4RataText3).add(n4RataText4)
				.setFontColor(ColorConstants.BLACK).setMargin(0);
		n4RataCanvas.add(n4RataP);
		n4RataCanvas.close();

		// n5rata
		Rectangle n5RataRectangle = new Rectangle(420, 704, 186, 15);
		Canvas n5RataCanvas = new Canvas(pdfCanvas, n5RataRectangle);
		Text n5RataText1 = new Text(Integer.toString(documento.DatiBollettino.get(bollettinoDiPartenza + 2).ProgressivoBoll) + "Â° ").setFont(asset.getInEvidenza5Font()).setFontSize(asset.getNumRataSize());
		Text n5RataText2 = new Text("RATA  ").setFont(asset.getNumRataFont()).setFontSize(asset.getNumRataSize());
		Text n5RataText3 = new Text(" entro il  ").setFont(asset.getEntroRateFont()).setFontSize(asset.getEntroRateSize());
		Text n5RataText4 = new Text(documento.DatiBollettino.get(bollettinoDiPartenza + 2).ScadenzaRata).setFont(asset.getInEvidenza5Font()).setFontSize(asset.getInEvidenza1Size()); //non Ã¨ un errore in evidenza 3 e 1, serve una combinaizone diversa
		Paragraph n5RataP = new Paragraph().add(n5RataText1).add(n5RataText2).add(n5RataText3).add(n5RataText4)
				.setFontColor(ColorConstants.BLACK).setMargin(0);
		n5RataCanvas.add(n5RataP);
		n5RataCanvas.close();

		// QR 3
		// Rectangle qR3Rectangle = new Rectangle(30, 612, 70, 70);
		// Canvas qR3Canvas = new Canvas(pdfCanvas, qR3Rectangle);
		// qR3Canvas.add(asset.getQr_code().scaleToFit(70, 70));S
		// qR3Canvas.close();

// 		QR CODE 3
		Rectangle qR3Rectangle = new Rectangle(30, 612, 70, 70);
		Canvas qR3Canvas = new Canvas(pdfCanvas, qR3Rectangle);
		qR3Canvas.add(generaQRCode(documento.DatiBollettino.get(bollettinoDiPartenza).BarcodePagoPa, pdf).scaleToFit(70, 70));
		qR3Canvas.close();

		// Euro
		Rectangle euroRectangle = new Rectangle(108, 670, 55, 15);
		Canvas euroCanvas = new Canvas(pdfCanvas, euroRectangle);
		Text euroText = new Text("Euro").setFont(asset.getValuta2Font());
		Paragraph euroP = new Paragraph().add(euroText).setFontSize(asset.getInEvidenza1Size()).setFontColor(ColorConstants.BLACK).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		euroCanvas.add(euroP);
		euroCanvas.close();

		// Importo
		Rectangle importoRectangle = new Rectangle(108, 650, 71, 28);
		Canvas importoCanvas = new Canvas(pdfCanvas, importoRectangle);
		Text importoText = new Text(mettiVirgolaEPuntiAllImportoInCent(documento.DatiBollettino.get(bollettinoDiPartenza).Codeline2Boll))
				.setFont(asset.getCodiceBoldFont());
		Paragraph importoP = new Paragraph().add(importoText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getCodiceBoldSize())
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		importoCanvas.add(importoP);
		importoCanvas.close();

		// Ente Creditore
		Rectangle enteCreditoreRectangle = new Rectangle(108, 640, 61, 12);
		Canvas enteCreditoreCanvas = new Canvas(pdfCanvas, enteCreditoreRectangle);
		Text enteCreditoreText = new Text("Ente Creditore").setFont(asset.getInfoBollettinoFont());
		Paragraph enteCreditoreP = new Paragraph().add(enteCreditoreText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getInfoBollettinoSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		enteCreditoreCanvas.add(enteCreditoreP);
		enteCreditoreCanvas.close();

		// Ente Creditore String
		Rectangle enteCreditoreStringRectangle = new Rectangle(108, 605, 86, 32);
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(225, 0, 22)).rectangle(enteCreditoreStringRectangle).fill();
		//fine LP PG200420 - Errori nel pdf dell'avviso
		Canvas enteCreditoreStringCanvas = new Canvas(pdfCanvas, enteCreditoreStringRectangle);
		Text enteCreditoreStringText = new Text(documento.DatiCreditore.get(0).Denominazione1)
				.setFont(asset.getDenominazioneNome2Font());
		Paragraph enteCreditoreStringP = new Paragraph().add(enteCreditoreStringText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getDenominazioneNome2Size()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(7);
		enteCreditoreStringCanvas.add(enteCreditoreStringP);
		enteCreditoreStringCanvas.close();

		// Oggetto Pagamento
		Rectangle oggettoPagamentoRectangle2 = new Rectangle(30, 598, 100, 12);
		Canvas oggettoPagamentoCanvas2 = new Canvas(pdfCanvas, oggettoPagamentoRectangle2);
		//inizio LP PG200420 
		//Text oggettoPagamentoText2 = new Text(documento.CausaleDocumento).setFont(asset.getTitillium_regular());
		Text oggettoPagamentoText2 = new Text("Oggetto del pagamento").setFont(asset.getInfoBollettinoFont());
		//fine LP PG200420 
		Paragraph oggettoPagamentoP2 = new Paragraph().add(oggettoPagamentoText2).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getInfoBollettinoSize() - asset.getYoffSet()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		oggettoPagamentoCanvas2.add(oggettoPagamentoP2);
		oggettoPagamentoCanvas2.close();

		// Oggetto Pagamento String
		Rectangle oggettoPagamentoStringRectangle2 = new Rectangle(30, 562, 157, 32);
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(25, 0, 225)).rectangle(oggettoPagamentoStringRectangle2).fill();
		//fine LP PG200420 - Errori nel pdf dell'avviso
		Canvas oggettoPagamentoStringCanvas2 = new Canvas(pdfCanvas, oggettoPagamentoStringRectangle2);
		Text oggettoPagamentoStringText2 = new Text(documento.CausaleDocumento).setFont(asset.getDenominazioneNome2Font());
		Paragraph oggettoPagamentoStringP2 = new Paragraph().add(oggettoPagamentoStringText2)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(9);
		oggettoPagamentoStringCanvas2.add(oggettoPagamentoStringP2);
		oggettoPagamentoStringCanvas2.close();

		// Codice Cbill
		Rectangle codiceCbillRectangle = new Rectangle(30, 560, 63, 12);
		Canvas codiceCbillCanvas = new Canvas(pdfCanvas, codiceCbillRectangle);
		Text codiceCbillText = new Text("Codice CBILL").setFont(asset.getInfoBollettinoFont());
		Paragraph codiceCbillP = new Paragraph().add(codiceCbillText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getInfoBollettinoSize())
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		codiceCbillCanvas.add(codiceCbillP);
		codiceCbillCanvas.close();

		// Codice Cbill String
		Rectangle codiceCbillStringRectangle = new Rectangle(30, 550, 43, 12);
		Canvas codiceCbillStringCanvas = new Canvas(pdfCanvas, codiceCbillStringRectangle);
		Text codiceCbillStringText = new Text(documento.DatiCreditore.get(0).CodiceInterbancario).setFont(asset.getCodiceBoldFont());
		Paragraph codiceCbillStringP = new Paragraph().add(codiceCbillStringText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getCodiceBoldSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		codiceCbillStringCanvas.add(codiceCbillStringP);
		codiceCbillStringCanvas.close();

		// Cod. Fiscale Ente Creditore
		Rectangle cfEnteCreditoreRectangle = new Rectangle(90, 560, 120, 12);
		Canvas cfEnteCreditoreCanvas = new Canvas(pdfCanvas, cfEnteCreditoreRectangle);
		Text cfEnteCreditoreText = new Text("Cod. Fiscale Ente Creditore").setFont(asset.getInfoBollettinoFont());
		Paragraph cfEnteCreditoreP = new Paragraph().add(cfEnteCreditoreText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getInfoBollettinoSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		cfEnteCreditoreCanvas.add(cfEnteCreditoreP);
		cfEnteCreditoreCanvas.close();

		// Cod. Fiscale Ente Creditore String
		Rectangle cfEnteCreditoreStringRectangle = new Rectangle(90, 550, 120, 12);
		Canvas cfEnteCreditoreStringCanvas = new Canvas(pdfCanvas, cfEnteCreditoreStringRectangle);
		Text cfEnteCreditoreStringText = new Text(documento.DatiCreditore.get(0).Cf).setFont(asset.getCodiceBoldFont());
		Paragraph cfEnteCreditoreStringP = new Paragraph().add(cfEnteCreditoreStringText)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getCodiceBoldSize()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		cfEnteCreditoreStringCanvas.add(cfEnteCreditoreStringP);
		cfEnteCreditoreStringCanvas.close();

		// Codice Avviso
		Rectangle codiceAvvisoRectangle = new Rectangle(30, 530, 69, 12);
		Canvas codiceAvvisoCanvas = new Canvas(pdfCanvas, codiceAvvisoRectangle);
		Text codiceAvvisoText = new Text("Codice Avviso").setFont(asset.getInfoBollettinoFont());
		Paragraph codiceAvvisoP = new Paragraph().add(codiceAvvisoText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getInfoBollettinoSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		codiceAvvisoCanvas.add(codiceAvvisoP);
		codiceAvvisoCanvas.close();

		// Codice Avviso String
		Rectangle codiceAvvisoStringRectangle = new Rectangle(30, 521, 160, 12);
		Canvas codiceAvvisoStringCanvas = new Canvas(pdfCanvas, codiceAvvisoStringRectangle);
		Text codiceAvvisoStringText = new Text(formattaCodiceAvviso(documento.DatiBollettino.get(bollettinoDiPartenza).AvvisoPagoPa)).setFont(asset.getCodiceBoldFont());
		Paragraph codiceAvvisoStringP = new Paragraph().add(codiceAvvisoStringText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getCodiceBoldSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		codiceAvvisoStringCanvas.add(codiceAvvisoStringP);
		codiceAvvisoStringCanvas.close();

		// Banche Altri Canali Description
		Rectangle bancheAltriCanaliDescriptionRectangle = new Rectangle(30, 480, 157, 33);
		Canvas bancheAltriCanaliDescriptionCanvas = new Canvas(pdfCanvas, bancheAltriCanaliDescriptionRectangle);
		Text bancheAltriCanaliDescriptionText1 = new Text("Qui sopra trovi il codice")
				.setFont(asset.getInfoCodiciFont());
		Text bancheAltriCanaliDescriptionText2 = new Text(" QR ").setFont(asset.getInfoCodiciBoldFont());
		Text bancheAltriCanaliDescriptionText3 = new Text("e il codice interbancario")
				.setFont(asset.getInfoCodiciFont());
		Text bancheAltriCanaliDescriptionText4 = new Text(" CBILL ")
				.setFont(asset.getInfoCodiciBoldFont());
		Text bancheAltriCanaliDescriptionText5 = new Text("per pagare attraverso il circuito bancario e gli altri canali di pagamento abilitati.")
				.setFont(asset.getInfoCodiciFont());
		Paragraph bancheAltriCanaliDescriptionP = new Paragraph().add(bancheAltriCanaliDescriptionText1)
				.add(bancheAltriCanaliDescriptionText2).add(bancheAltriCanaliDescriptionText3)
				.add(bancheAltriCanaliDescriptionText4).add(bancheAltriCanaliDescriptionText5)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getInfoBollettinoSize() - 1).setMargin(0).setFixedLeading(9);
		bancheAltriCanaliDescriptionCanvas.add(bancheAltriCanaliDescriptionP);
		bancheAltriCanaliDescriptionCanvas.close();

		// QR 4
		// Rectangle qR4Rectangle = new Rectangle(220, 612, 70, 70);
		// Canvas qR4Canvas = new Canvas(pdfCanvas, qR4Rectangle);
		// qR4Canvas.add(asset.getQr_code().scaleToFit(70, 70));
		// qR4Canvas.close();

// 		QR 4
		Rectangle qR4Rectangle = new Rectangle(220, 612, 70, 70);
		Canvas qR4Canvas = new Canvas(pdfCanvas, qR4Rectangle);
		qR4Canvas.add(generaQRCode(documento.DatiBollettino.get(bollettinoDiPartenza + 1).BarcodePagoPa, pdf).scaleToFit(70, 70));
		qR4Canvas.close();

		// Euro
		Rectangle euroRectangle2 = new Rectangle(299, 670, 55, 15);
		Canvas euroCanvas2 = new Canvas(pdfCanvas, euroRectangle2);
		euroCanvas2.add(euroP);
		euroCanvas2.close();

		// Importo
		Rectangle importoRectangle2 = new Rectangle(299, 650, 71, 28);
		Canvas importoCanvas2 = new Canvas(pdfCanvas, importoRectangle2);
		Text importoText2 = new Text(mettiVirgolaEPuntiAllImportoInCent(documento.DatiBollettino.get(bollettinoDiPartenza + 1).Codeline2Boll))
				.setFont(asset.getCodiceBoldFont());
		Paragraph importoP2 = new Paragraph().add(importoText2).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getCodiceBoldSize())
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		importoCanvas2.add(importoP2);
		importoCanvas2.close();

		// Ente Creditore
		Rectangle enteCreditoreRectangle2 = new Rectangle(299, 640, 61, 12);
		Canvas enteCreditoreCanvas2 = new Canvas(pdfCanvas, enteCreditoreRectangle2);
		enteCreditoreCanvas2.add(enteCreditoreP);
		enteCreditoreCanvas2.close();

		// Ente Creditore String
		Rectangle enteCreditoreStringRectangle2 = new Rectangle(299, 605, 86, 32);
		Canvas enteCreditoreStringCanvas2 = new Canvas(pdfCanvas, enteCreditoreStringRectangle2);
		enteCreditoreStringCanvas2.add(enteCreditoreStringP);
		enteCreditoreStringCanvas2.close();

		// Oggetto Pagamento
		Rectangle oggettoPagamentoRectangle3 = new Rectangle(221, 598, 100, 12);
		Canvas oggettoPagamentoCanvas3 = new Canvas(pdfCanvas, oggettoPagamentoRectangle3);
		oggettoPagamentoCanvas3.add(oggettoPagamentoP2);
		oggettoPagamentoCanvas3.close();

		// Oggetto Pagamento String
		Rectangle oggettoPagamentoStringRectangle3 = new Rectangle(221, 562, 157, 32);
		Canvas oggettoPagamentoStringCanvas3 = new Canvas(pdfCanvas, oggettoPagamentoStringRectangle3);
		oggettoPagamentoStringCanvas3.add(oggettoPagamentoStringP2);
		oggettoPagamentoStringCanvas3.close();

		// Codice Cbill
		Rectangle codiceCbillRectangle2 = new Rectangle(221, 560, 63, 12);
		Canvas codiceCbillCanvas2 = new Canvas(pdfCanvas, codiceCbillRectangle2);
		codiceCbillCanvas2.add(codiceCbillP);
		codiceCbillCanvas2.close();

		// Codice Cbill String
		Rectangle codiceCbillStringRectangle2 = new Rectangle(221, 550, 43, 12);
		Canvas codiceCbillStringCanvas2 = new Canvas(pdfCanvas, codiceCbillStringRectangle2);
		codiceCbillStringCanvas2.add(codiceCbillStringP);
		codiceCbillStringCanvas2.close();

		// Cod. Fiscale Ente Creditore
		Rectangle cfEnteCreditoreRectangle2 = new Rectangle(280, 560, 135, 12);
		Canvas cfEnteCreditoreCanvas2 = new Canvas(pdfCanvas, cfEnteCreditoreRectangle2);
		cfEnteCreditoreCanvas2.add(cfEnteCreditoreP);
		cfEnteCreditoreCanvas2.close();

		// Cod. Fiscale Ente Creditore String
		Rectangle cfEnteCreditoreStringRectangle2 = new Rectangle(280, 550, 120, 12);
		Canvas cfEnteCreditoreStringCanvas2 = new Canvas(pdfCanvas, cfEnteCreditoreStringRectangle2);
		cfEnteCreditoreStringCanvas2.add(cfEnteCreditoreStringP);
		cfEnteCreditoreStringCanvas2.close();

		// Codice Avviso
		Rectangle codiceAvvisoRectangle2 = new Rectangle(221, 530, 69, 12);
		Canvas codiceAvvisoCanvas2 = new Canvas(pdfCanvas, codiceAvvisoRectangle2);
		codiceAvvisoCanvas2.add(codiceAvvisoP);
		codiceAvvisoCanvas2.close();

		// Codice Avviso String
		Rectangle codiceAvvisoStringRectangle2 = new Rectangle(221, 521, 160, 12);
		Canvas codiceAvvisoStringCanvas2 = new Canvas(pdfCanvas, codiceAvvisoStringRectangle2);
		Text codiceAvvisoStringText2 = new Text(formattaCodiceAvviso(documento.DatiBollettino.get(bollettinoDiPartenza + 1).AvvisoPagoPa)).setFont(asset.getCodiceBoldFont());
		Paragraph codiceAvvisoStringP2 = new Paragraph().add(codiceAvvisoStringText2).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getCodiceBoldSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		codiceAvvisoStringCanvas2.add(codiceAvvisoStringP2);
		codiceAvvisoStringCanvas2.close();

		// Banche Altri Canali Description
		Rectangle bancheAltriCanaliDescriptionRectangle2 = new Rectangle(221, 480, 157, 33);
		Canvas bancheAltriCanaliDescriptionCanvas2 = new Canvas(pdfCanvas, bancheAltriCanaliDescriptionRectangle2);
		bancheAltriCanaliDescriptionCanvas2.add(bancheAltriCanaliDescriptionP);
		bancheAltriCanaliDescriptionCanvas2.close();

		// QR 5
		// Rectangle qR5Rectangle = new Rectangle(420, 612, 70, 70);
		// Canvas qR5Canvas = new Canvas(pdfCanvas, qR5Rectangle);
		// qR5Canvas.add(asset.getQr_code().scaleToFit(70, 70));
		// qR5Canvas.close();

// 		QR 5
		Rectangle qR5Rectangle = new Rectangle(420, 612, 70, 70);
		Canvas qR5Canvas = new Canvas(pdfCanvas, qR5Rectangle);
		qR5Canvas.add(generaQRCode(documento.DatiBollettino.get(bollettinoDiPartenza + 2).BarcodePagoPa, pdf).scaleToFit(70, 70));
		qR5Canvas.close();

		// Euro
		Rectangle euroRectangle3 = new Rectangle(498, 670, 55, 15);
		Canvas euroCanvas3 = new Canvas(pdfCanvas, euroRectangle3);
		euroCanvas3.add(euroP);
		euroCanvas3.close();

		// Importo
		Rectangle importoRectangle3 = new Rectangle(498, 650, 71, 28);
		Canvas importoCanvas3 = new Canvas(pdfCanvas, importoRectangle3);
		Text importoText3 = new Text(mettiVirgolaEPuntiAllImportoInCent(documento.DatiBollettino.get(bollettinoDiPartenza + 2).Codeline2Boll))
				.setFont(asset.getCodiceBoldFont());
		Paragraph importoP3 = new Paragraph().add(importoText3).setFontColor(ColorConstants.BLACK).setFontSize(asset.getCodiceBoldSize())
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		importoCanvas3.add(importoP3);
		importoCanvas3.close();

		// Ente Creditore
		Rectangle enteCreditoreRectangle3 = new Rectangle(498, 640, 61, 12);
		Canvas enteCreditoreCanvas3 = new Canvas(pdfCanvas, enteCreditoreRectangle3);
		enteCreditoreCanvas3.add(enteCreditoreP);
		enteCreditoreCanvas3.close();

		// Ente Creditore String
		Rectangle enteCreditoreStringRectangle3 = new Rectangle(498, 605, 86, 32);
		Canvas enteCreditoreStringCanvas3 = new Canvas(pdfCanvas, enteCreditoreStringRectangle3);
		enteCreditoreStringCanvas3.add(enteCreditoreStringP);
		enteCreditoreStringCanvas3.close();

		// Oggetto Pagamento
		Rectangle oggettoPagamentoRectangle4 = new Rectangle(420, 598, 100, 12);
		Canvas oggettoPagamentoCanvas4 = new Canvas(pdfCanvas, oggettoPagamentoRectangle4);
		oggettoPagamentoCanvas4.add(oggettoPagamentoP2);
		oggettoPagamentoCanvas4.close();

		// Oggetto Pagamento String
		Rectangle oggettoPagamentoStringRectangle4 = new Rectangle(420, 562, 157, 32);
		Canvas oggettoPagamentoStringCanvas4 = new Canvas(pdfCanvas, oggettoPagamentoStringRectangle4);
		oggettoPagamentoStringCanvas4.add(oggettoPagamentoStringP2);
		oggettoPagamentoStringCanvas4.close();

		// Codice Cbill
		Rectangle codiceCbillRectangle3 = new Rectangle(420, 560, 63, 12);
		Canvas codiceCbillCanvas3 = new Canvas(pdfCanvas, codiceCbillRectangle3);
		codiceCbillCanvas3.add(codiceCbillP);
		codiceCbillCanvas3.close();

		// Codice Cbill String
		Rectangle codiceCbillStringRectangle3 = new Rectangle(420, 550, 43, 12);
		Canvas codiceCbillStringCanvas3 = new Canvas(pdfCanvas, codiceCbillStringRectangle3);
		codiceCbillStringCanvas3.add(codiceCbillStringP);
		codiceCbillStringCanvas3.close();

		// Cod. Fiscale Ente Creditore
		Rectangle cfEnteCreditoreRectangle3 = new Rectangle(478, 560, 120, 12);
		Canvas cfEnteCreditoreCanvas3 = new Canvas(pdfCanvas, cfEnteCreditoreRectangle3);
		cfEnteCreditoreCanvas3.add(cfEnteCreditoreP);
		cfEnteCreditoreCanvas3.close();

		// Cod. Fiscale Ente Creditore String
		Rectangle cfEnteCreditoreStringRectangle3 = new Rectangle(478, 550, 120, 12);
		Canvas cfEnteCreditoreStringCanvas3 = new Canvas(pdfCanvas, cfEnteCreditoreStringRectangle3);
		cfEnteCreditoreStringCanvas3.add(cfEnteCreditoreStringP);
		cfEnteCreditoreStringCanvas3.close();

		// Codice Avviso
		Rectangle codiceAvvisoRectangle3 = new Rectangle(420, 530, 69, 12);
		Canvas codiceAvvisoCanvas3 = new Canvas(pdfCanvas, codiceAvvisoRectangle3);
		codiceAvvisoCanvas3.add(codiceAvvisoP);
		codiceAvvisoCanvas3.close();

		// Codice Avviso String
		Rectangle codiceAvvisoStringRectangle3 = new Rectangle(420, 521, 170, 12);
		Canvas codiceAvvisoStringCanvas3 = new Canvas(pdfCanvas, codiceAvvisoStringRectangle3);
		Text codiceAvvisoStringText3 = new Text(formattaCodiceAvviso(documento.DatiBollettino.get(bollettinoDiPartenza + 2).AvvisoPagoPa)).setFont(asset.getCodiceBoldFont());
		Paragraph codiceAvvisoStringP3 = new Paragraph().add(codiceAvvisoStringText3).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getCodiceBoldSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		codiceAvvisoStringCanvas3.add(codiceAvvisoStringP3);
		codiceAvvisoStringCanvas3.close();

		// Banche Altri Canali Description
		Rectangle bancheAltriCanaliDescriptionRectangle3 = new Rectangle(420, 480, 157, 33);
		Canvas bancheAltriCanaliDescriptionCanvas3 = new Canvas(pdfCanvas, bancheAltriCanaliDescriptionRectangle3);
		bancheAltriCanaliDescriptionCanvas3.add(bancheAltriCanaliDescriptionP);
		bancheAltriCanaliDescriptionCanvas3.close();
		
		//PAGONET-527 - Introdotta condizione su tipoStampa
		if(tipoStampa.equals("P")) {
		
		// Bollettino 1
		// Forbici
		Rectangle b1_forbiciRectangle = new Rectangle(548, 474, 14, 10);
		Canvas b1_forbiciCanvas = new Canvas(pdfCanvas, b1_forbiciRectangle);
		b1_forbiciCanvas.add(asset.getLogo_forbici().scaleToFit(14, 10));
		b1_forbiciCanvas.close();

		// Bollettino Postale PA Background Border
		Rectangle b1_bollettinoPostalePaBorderRectangle = new Rectangle(0, 474, 595, 1);
		pdfCanvas.saveState().setFillColor(LeggoAsset.grigioForbici).rectangle(b1_bollettinoPostalePaBorderRectangle)
				.fill();
		Canvas b1_bollettinoPostalePaBorderCanvas = new Canvas(pdfCanvas, b1_bollettinoPostalePaBorderRectangle);
		b1_bollettinoPostalePaBorderCanvas.close();

		// Bollettino Postale PA Background
		Rectangle b1_bollettinoPostalePaGrayRectangle = new Rectangle(0, 457, 595, 17);
		pdfCanvas.saveState().setFillColor(LeggoAsset.grigioBollettino).rectangle(b1_bollettinoPostalePaGrayRectangle)
				.fill();
		Canvas b1_bollettinoPostalePaGrayCanvas = new Canvas(pdfCanvas, b1_bollettinoPostalePaGrayRectangle);
		b1_bollettinoPostalePaGrayCanvas.close();

		// Bollettino Postale PA
		Rectangle b1_bollettinoPostalePaRectangle = new Rectangle(29, 462 - asset.getYoffSet(), 135, 15);
		Canvas b1_bollettinoPostalePaCanvas = new Canvas(pdfCanvas, b1_bollettinoPostalePaRectangle);
		Text b1_bollettinoPostalePaText = new Text(LeggoAsset.BOLLETTINO_POSTALE_PA).setFont(asset.getTitoloFont());
		Paragraph b1_bollettinoPostalePaP = new Paragraph().add(b1_bollettinoPostalePaText)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getTitoloSize()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		b1_bollettinoPostalePaCanvas.add(b1_bollettinoPostalePaP);
		b1_bollettinoPostalePaCanvas.close();

		// Logo Banco Posta
		Rectangle b1_logoBancopostaRectangle = new Rectangle(183, 463, 51, 7);
		Canvas b1_logoBancopostaCanvas = new Canvas(pdfCanvas, b1_logoBancopostaRectangle);
		b1_logoBancopostaCanvas.add(asset.getLogo_bancoposta().scaleToFit(51, 7));
		b1_logoBancopostaCanvas.close();

		// Numero Rata
		Rectangle b1_numeroRataRectangle2 = new Rectangle(437 - asset.getXoffSet() * 2, 463 - asset.getYoffSet() * 2, 230, 15 + asset.getXoffSet());
		Canvas b1_numeroRataCanvas2 = new Canvas(pdfCanvas, b1_numeroRataRectangle2);
		Text b1_numeroRataText1 = new Text(Integer.toString(documento.DatiBollettino.get(bollettinoDiPartenza).ProgressivoBoll) + "Â° ").setFont(asset.getNumRataFont());
		Text b1_numeroRataText2 = new Text(" RATA ").setFont(asset.getNumRataFont());
		Text b1_numeroRataText3 = new Text("  entro il  ").setFont(asset.getEntroRateFont());
		Text b1_numeroRataText4 = new Text(documento.DatiBollettino.get(bollettinoDiPartenza).ScadenzaRata).setFont(asset.getInEvidenza5Font());
		Paragraph b1_numeroRataP = new Paragraph()
				.add(b1_numeroRataText1).setFontSize(asset.getNumRataSize())
				.add(b1_numeroRataText2).setFontSize(asset.getNumRataSize())
				.add(b1_numeroRataText3).setFontSize(asset.getEntroRateSize())
				.add(b1_numeroRataText4).setFontSize(asset.getInEvidenza1Size())
				.setFontColor(ColorConstants.BLACK)
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b1_numeroRataCanvas2.add(b1_numeroRataP);
		b1_numeroRataCanvas2.close();

		// Logo Poste Italiane
		Rectangle b1_logoPosteitalianeRectangle = new Rectangle(29, 425, 117, 15);
		Canvas b1_logoPosteitalianeCanvas = new Canvas(pdfCanvas, b1_logoPosteitalianeRectangle);
		b1_logoPosteitalianeCanvas.add(asset.getLogo_poste_italiane().scaleToFit(117, 15));
		b1_logoPosteitalianeCanvas.close();

		// Logo Euro
		Rectangle b1_logoEuroRectangle = new Rectangle(180, 423, 19, 19);
		Canvas b1_logoEuroCanvas = new Canvas(pdfCanvas, b1_logoEuroRectangle);
		b1_logoEuroCanvas.add(asset.getLogo_euro_bollettino().scaleToFit(19, 19));
		b1_logoEuroCanvas.close();

		// Sul CC
		Rectangle b1_suCcRectangle = new Rectangle(205, 429, 43, 15);
		Canvas b1_suCcCanvas = new Canvas(pdfCanvas, b1_suCcRectangle);
		Text b1_suCcText = new Text(LeggoAsset.SUL_CC).setFont(asset.getSulCcFont());
		Paragraph b1_suCcP = new Paragraph().add(b1_suCcText).setFontColor(ColorConstants.BLACK).setFontSize(asset.getSulCcSize())
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b1_suCcCanvas.add(b1_suCcP);
		b1_suCcCanvas.close();

		// Numero CC Postale
		Rectangle b1_numeroCcPostaleRectangle = new Rectangle(248, 430, 126, 15);
		Canvas b1_numeroCcPostaleCanvas = new Canvas(pdfCanvas, b1_numeroCcPostaleRectangle);
		Text b1_numeroCcPostaleText = new Text(documento.DatiBollettino.get(bollettinoDiPartenza).Codeline12Boll).setFont(asset.getCodiceBoldFont());
		Paragraph b1_numeroCcPostaleP = new Paragraph().add(b1_numeroCcPostaleText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getCodiceBoldSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b1_numeroCcPostaleCanvas.add(b1_numeroCcPostaleP);
		b1_numeroCcPostaleCanvas.close();

		// Euro
		Rectangle b1_euroRectangle2 = new Rectangle(474, 430, 25, 15);
		Canvas b1_euroCanvas2 = new Canvas(pdfCanvas, b1_euroRectangle2);
		b1_euroCanvas2.add(euroP);
		b1_euroCanvas2.close();

		// Importo
		Rectangle b1_importoRectangle3 = new Rectangle(510, 431, 60, 15);
		Canvas b1_importoCanvas3 = new Canvas(pdfCanvas, b1_importoRectangle3);
		b1_importoCanvas3.add(importoP);
		b1_importoCanvas3.close();

		// Logo Bollettino Postale
		Rectangle b1_logoBollettinoPostaleRectangle = new Rectangle(30, 389, 78, 32);
		Canvas b1_logoBollettinoPostaleCanvas = new Canvas(pdfCanvas, b1_logoBollettinoPostaleRectangle);
		b1_logoBollettinoPostaleCanvas.add(asset.getLogo_bollettino_postale().scaleToFit(78, 32));
		b1_logoBollettinoPostaleCanvas.close();

		// Bollettino Postale Descrizione
		Rectangle b1_bollettinoPostaleDescrizioneRectangle = new Rectangle(30, 340, 120, 48);
		Canvas b1_bollettinoPostaleDescrizioneCanvas = new Canvas(pdfCanvas, b1_bollettinoPostaleDescrizioneRectangle);
		Text b1_bollettinoPostaleDescrizioneText = new Text(LeggoAsset.BOLLETTINO_POSTALE_DESCRIZIONE)
				.setFont(asset.getInfoBollettinoFont());
		Paragraph b1_bollettinoPostaleDescrizioneP = new Paragraph().add(b1_bollettinoPostaleDescrizioneText)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getInfoBollettinoSize()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(10);
		b1_bollettinoPostaleDescrizioneCanvas.add(b1_bollettinoPostaleDescrizioneP);
		b1_bollettinoPostaleDescrizioneCanvas.close();

		// Autorizzazione
		Rectangle b1_autorizzazioneRectangle = new Rectangle(30, 327, 180, 9);
		Canvas b1_autorizzazioneCanvas = new Canvas(pdfCanvas, b1_autorizzazioneRectangle);
		Text b1_autorizzazioneText = new Text(documento.DatiBollettino.get(bollettinoDiPartenza).AutorizCcp).setFont(asset.getAutorizzazioneFont());
		Paragraph b1_autorizzazioneP = new Paragraph().add(b1_autorizzazioneText).setFontColor(LeggoAsset.grigioForbici)
				.setFontSize(asset.getAutorizzazioneSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b1_autorizzazioneCanvas.add(b1_autorizzazioneP);
		b1_autorizzazioneCanvas.close();

		// Intestato a
		Rectangle b1_intestatoARectangle = new Rectangle(178, 406, 59, 12);
		Canvas b1_intestatoACanvas = new Canvas(pdfCanvas, b1_intestatoARectangle);
		Text b1_intestatoAText = new Text(LeggoAsset.INTESTATO_A).setFont(asset.getEtichettaDenominazioneFont());
		Paragraph b1_intestatoAP = new Paragraph().add(b1_intestatoAText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getEtichettaDenominazioneSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b1_intestatoACanvas.add(b1_intestatoAP);
		b1_intestatoACanvas.close();

		// Intestatario CC Postale
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		//Rectangle b1_intestatarioCCPostaleRectangle = new Rectangle(220, 406, 258, 10);
		Rectangle b1_intestatarioCCPostaleRectangle = new Rectangle(220 + asset.getXoffSet(), 407 + asset.getYoffSet(), 258, 10);
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		Canvas b1_intestatarioCCPostaleCanvas = new Canvas(pdfCanvas, b1_intestatarioCCPostaleRectangle);
		Text b1_intestatarioCCPostaleText = new Text(documento.DatiBollettino.get(bollettinoDiPartenza).Descon60Boll)
				.setFont(asset.getDenominazioneNome2Font());
		Paragraph b1_intestatarioCCPostaleP = new Paragraph().add(b1_intestatarioCCPostaleText)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		b1_intestatarioCCPostaleCanvas.add(b1_intestatarioCCPostaleP);
		b1_intestatarioCCPostaleCanvas.close();

		// Destinatario
		Rectangle b1_destinatarioRectangle3 = new Rectangle(178, 389, 57, 12);
		Canvas b1_destinatarioCanvas3 = new Canvas(pdfCanvas, b1_destinatarioRectangle3);
		Text b1_destinatarioText = new Text("Destinatario").setFont(asset.getEtichettaDenominazioneFont());
		Paragraph b1_destinatarioP = new Paragraph().add(b1_destinatarioText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getEtichettaDenominazioneSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b1_destinatarioCanvas3.add(b1_destinatarioP);
		b1_destinatarioCanvas3.close();

		// Nome Cognome Destinatario
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		//Rectangle b1_nomeDestinatarioRectangle3 = new Rectangle(229, 391, 169, 10);
		Rectangle b1_nomeDestinatarioRectangle3 = new Rectangle(226 + asset.getXoffSet(), 366 + asset.getYoffSet(), 170 - 10 - 9 - 25 , 32);
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(25, 0, 225)).rectangle(b1_nomeDestinatarioRectangle3).fill();
		//fine LP PG200420 - Errori nel pdf dell'avviso
		Canvas b1_nomeDestinatarioCanvas3 = new Canvas(pdfCanvas, b1_nomeDestinatarioRectangle3);
		Text b1_nomeDestinatarioText = new Text(documento.DatiAnagrafici.get(0).Denominazione1)
				.setFont(asset.getDenominazioneNome2Font());
		Paragraph b1_nomeDestinatarioP3 = new Paragraph().add(b1_nomeDestinatarioText)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		b1_nomeDestinatarioP3.setFixedLeading(8);
		//fine LP PG200420 - Errori nel pdf dell'avviso
		b1_nomeDestinatarioCanvas3.add(b1_nomeDestinatarioP3);
		b1_nomeDestinatarioCanvas3.close();

		// Oggetto Pagamento
		Rectangle b1_oggettoPagamentoRectangle3 = new Rectangle(178, 371, 88, 12);
		Canvas b1_oggettoPagamentoCanvas3 = new Canvas(pdfCanvas, b1_oggettoPagamentoRectangle3);
		Text b1_oggettoPagamentoText3 = new Text(LeggoAsset.OGGETTO_PAGAMENTO).setFont(asset.getEtichettaDenominazioneFont());
		Paragraph b1_oggettoPagamentoP3 = new Paragraph().add(b1_oggettoPagamentoText3)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getEtichettaDenominazioneSize()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		b1_oggettoPagamentoCanvas3.add(b1_oggettoPagamentoP3);
		b1_oggettoPagamentoCanvas3.close();

		// Oggetto Pagamento String
		Rectangle b1_oggettoPagamentoStringRectangle3 = new Rectangle(250 + asset.getXoffSet() * 3, 350 + asset.getYoffSet(), 213, 30);
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		if(bDebug)
			pdfCanvas.saveState().setFillColor(new DeviceRgb(225, 0, 25)).rectangle(b1_oggettoPagamentoStringRectangle3).fill();
		//fine LP PG200420 - Errori nel pdf dell'avviso
		Canvas b1_oggettoPagamentoStringCanvas3 = new Canvas(pdfCanvas, b1_oggettoPagamentoStringRectangle3);
		Text b1_oggettoPagamentoStringText3 = new Text(documento.CausaleDocumento).setFont(asset.getDenominazioneNome2Font());
		Paragraph b1_oggettoPagamentoStringP3 = new Paragraph().add(b1_oggettoPagamentoStringText3)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(9);
		b1_oggettoPagamentoStringCanvas3.add(b1_oggettoPagamentoStringP3);
		b1_oggettoPagamentoStringCanvas3.close();

		// Data matrix Container
		Rectangle b1_dataMatrixContainerRectangle = new Rectangle(480, 328, 93, 93);
		Canvas b1_dataMatrixContainerCanvas = new Canvas(pdfCanvas, b1_dataMatrixContainerRectangle);
		b1_dataMatrixContainerCanvas.add(asset.getData_matrix_container().scaleToFit(93, 93));
		b1_dataMatrixContainerCanvas.close();

		// Data matrix
		// Rectangle b1_dataMatrixRectangle = new Rectangle(490, 339, 74, 74);
		// Canvas b1_dataMatrixCanvas = new Canvas(pdfCanvas, b1_dataMatrixRectangle);
		// b1_dataMatrixCanvas.add(asset.getData_matrix().scaleToFit(74, 74));
		// b1_dataMatrixCanvas.close();

// 		Data matrix
		Rectangle b1_dataMatrixRectangle = new Rectangle(490, 339, 74, 74);
		Canvas b1_dataMatrixCanvas = new Canvas(pdfCanvas, b1_dataMatrixRectangle);
		b1_dataMatrixCanvas.add(generaDataMatrix(documento.DatiBollettino.get(bollettinoDiPartenza).QRcodePagoPa, pdf).scaleToFit(74, 74));
		b1_dataMatrixCanvas.close();

		// Codice Avviso
		Rectangle b1_codiceAvvisoRectangle2 = new Rectangle(179, 348, 79, 12);
		Canvas b1_codiceAvvisoCanvas2 = new Canvas(pdfCanvas, b1_codiceAvvisoRectangle2);
		b1_codiceAvvisoCanvas2.add(codiceAvvisoP);
		b1_codiceAvvisoCanvas2.close();

		// Codice Avviso String
		Rectangle b1_codiceAvvisoStringRectangle2 = new Rectangle(179, 337, 160, 12);
		Canvas b1_codiceAvvisoStringCanvas2 = new Canvas(pdfCanvas, b1_codiceAvvisoStringRectangle2);
		b1_codiceAvvisoStringCanvas2.add(codiceAvvisoStringP);
		b1_codiceAvvisoStringCanvas2.close();

		// Tipo
		Rectangle b1_tipoRectangle = new Rectangle(329, 348, 25, 12);
		Canvas b1_tipoCanvas = new Canvas(pdfCanvas, b1_tipoRectangle);
		Text b1_tipoText = new Text(LeggoAsset.TIPO).setFont(asset.getEtichettaDenominazioneFont());
		Paragraph b1_tipoP = new Paragraph().add(b1_tipoText).setFontColor(ColorConstants.BLACK).setFontSize(asset.getEtichettaDenominazioneSize())
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b1_tipoCanvas.add(b1_tipoP);
		b1_tipoCanvas.close();

		// P1
		Rectangle b1_p1Rectangle = new Rectangle(331, 337, 13, 13);
		Canvas b1_p1Canvas = new Canvas(pdfCanvas, b1_p1Rectangle);
		Text b1_p1Text = new Text(LeggoAsset.P1).setFont(asset.getCodiceBoldFont());
		Paragraph b1_p1P = new Paragraph().add(b1_p1Text).setFontColor(ColorConstants.BLACK).setFontSize(asset.getCodiceBoldSize())
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b1_p1Canvas.add(b1_p1P);
		b1_p1Canvas.close();

		// Cod. Fiscale Ente Creditore
		Rectangle b1_cfEnteCreditoreRectangle2 = new Rectangle(375, 348, 120, 12);
		Canvas b1_cfEnteCreditoreCanvas2 = new Canvas(pdfCanvas, b1_cfEnteCreditoreRectangle2);
		b1_cfEnteCreditoreCanvas2.add(cfEnteCreditoreP);
		b1_cfEnteCreditoreCanvas2.close();

		// Cod. Fiscale Ente Creditore String
		Rectangle b1_cfEnteCreditoreStringRectangle2 = new Rectangle(375, 337, 125, 12);
		Canvas b1_cfEnteCreditoreStringCanvas2 = new Canvas(pdfCanvas, b1_cfEnteCreditoreStringRectangle2);
		b1_cfEnteCreditoreStringCanvas2.add(cfEnteCreditoreStringP);
		b1_cfEnteCreditoreStringCanvas2.close();

		// Bollettino 2
		// Forbici
		Rectangle b2_forbiciRectangle = new Rectangle(548, 474 - 160, 14, 10);
		Canvas b2_forbiciCanvas = new Canvas(pdfCanvas, b2_forbiciRectangle);
		b2_forbiciCanvas.add(asset.getLogo_forbici().scaleToFit(14, 10));
		b2_forbiciCanvas.close();

		// Bollettino Postale PA Background Border
		Rectangle b2_bollettinoPostalePaBorderRectangle = new Rectangle(0, 474 - 160, 595, 1);
		pdfCanvas.saveState().setFillColor(LeggoAsset.grigioForbici).rectangle(b2_bollettinoPostalePaBorderRectangle)
				.fill();
		Canvas b2_bollettinoPostalePaBorderCanvas = new Canvas(pdfCanvas, b2_bollettinoPostalePaBorderRectangle);
		b2_bollettinoPostalePaBorderCanvas.close();

		// Bollettino Postale PA Background
		Rectangle b2_bollettinoPostalePaGrayRectangle = new Rectangle(0, 457 - 160, 595, 17);
		pdfCanvas.saveState().setFillColor(LeggoAsset.grigioBollettino).rectangle(b2_bollettinoPostalePaGrayRectangle)
				.fill();
		Canvas b2_bollettinoPostalePaGrayCanvas = new Canvas(pdfCanvas, b2_bollettinoPostalePaGrayRectangle);
		b2_bollettinoPostalePaGrayCanvas.close();

		// Bollettino Postale PA
		Rectangle b2_bollettinoPostalePaRectangle = new Rectangle(29, 462 - 160  - asset.getYoffSet(), 135, 15);
		Canvas b2_bollettinoPostalePaCanvas = new Canvas(pdfCanvas, b2_bollettinoPostalePaRectangle);
		Text b2_bollettinoPostalePaText = new Text(LeggoAsset.BOLLETTINO_POSTALE_PA).setFont(asset.getTitoloFont());
		Paragraph b2_bollettinoPostalePaP = new Paragraph().add(b2_bollettinoPostalePaText)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getTitoloSize()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		b2_bollettinoPostalePaCanvas.add(b2_bollettinoPostalePaP);
		b2_bollettinoPostalePaCanvas.close();

		// Logo Banco Posta
		Rectangle b2_logoBancopostaRectangle = new Rectangle(183, 463 - 160, 51, 7);
		Canvas b2_logoBancopostaCanvas = new Canvas(pdfCanvas, b2_logoBancopostaRectangle);
		b2_logoBancopostaCanvas.add(asset.getLogo_bancoposta().scaleToFit(51, 7));
		b2_logoBancopostaCanvas.close();

		// Numero Rata 
		Rectangle b2_numeroRataRectangle2 = new Rectangle(437 - asset.getXoffSet() * 2, 463 - 160  - asset.getYoffSet() * 2, 195, 15 + asset.getXoffSet());
		Canvas b2_numeroRataCanvas2 = new Canvas(pdfCanvas, b2_numeroRataRectangle2);
		Text b2_numeroRataText1 = new Text(Integer.toString(documento.DatiBollettino.get(bollettinoDiPartenza + 1).ProgressivoBoll) + "Â° ").setFont(asset.getNumRataFont());
		Text b2_numeroRataText2 = new Text(" RATA ").setFont(asset.getNumRataFont());
		Text b2_numeroRataText3 = new Text("  entro il  ").setFont(asset.getEntroRateFont());
		Text b2_numeroRataText4 = new Text(documento.DatiBollettino.get(bollettinoDiPartenza + 1).ScadenzaRata).setFont(asset.getInEvidenza5Font());
		Paragraph b2_numeroRataP = new Paragraph()
				.add(b2_numeroRataText1).setFontSize(asset.getNumRataSize())
				.add(b2_numeroRataText2).setFontSize(asset.getNumRataSize())
				.add(b2_numeroRataText3).setFontSize(asset.getEntroRateSize())
				.add(b2_numeroRataText4).setFontSize(asset.getInEvidenza1Size())
				.setFontColor(ColorConstants.BLACK)
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b2_numeroRataCanvas2.add(b2_numeroRataP);
		b2_numeroRataCanvas2.close();

		// Logo Poste Italiane
		Rectangle b2_logoPosteitalianeRectangle = new Rectangle(29, 425 - 160, 117, 15);
		Canvas b2_logoPosteitalianeCanvas = new Canvas(pdfCanvas, b2_logoPosteitalianeRectangle);
		b2_logoPosteitalianeCanvas.add(asset.getLogo_poste_italiane().scaleToFit(117, 15));
		b2_logoPosteitalianeCanvas.close();

		// Logo Euro
		Rectangle b2_logoEuroRectangle = new Rectangle(180, 423 - 160, 19, 19);
		Canvas b2_logoEuroCanvas = new Canvas(pdfCanvas, b2_logoEuroRectangle);
		b2_logoEuroCanvas.add(asset.getLogo_euro_bollettino().scaleToFit(19, 19));
		b2_logoEuroCanvas.close();

		// Sul CC
		Rectangle b2_suCcRectangle = new Rectangle(205, 429 - 160, 43, 15);
		Canvas b2_suCcCanvas = new Canvas(pdfCanvas, b2_suCcRectangle);
		Text b2_suCcText = new Text(LeggoAsset.SUL_CC).setFont(asset.getSulCcFont());
		Paragraph b2_suCcP = new Paragraph().add(b2_suCcText).setFontColor(ColorConstants.BLACK).setFontSize(asset.getSulCcSize())
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b2_suCcCanvas.add(b2_suCcP);
		b2_suCcCanvas.close();

		// Numero CC Postale
		Rectangle b2_numeroCcPostaleRectangle = new Rectangle(248, 430 - 160, 126, 15);
		Canvas b2_numeroCcPostaleCanvas = new Canvas(pdfCanvas, b2_numeroCcPostaleRectangle);
		Text b2_numeroCcPostaleText = new Text(documento.DatiBollettino.get(bollettinoDiPartenza + 1).Codeline12Boll).setFont(asset.getCodiceBoldFont());
		Paragraph b2_numeroCcPostaleP = new Paragraph().add(b2_numeroCcPostaleText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getCodiceBoldSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b2_numeroCcPostaleCanvas.add(b2_numeroCcPostaleP);
		b2_numeroCcPostaleCanvas.close();

		// Euro
		Rectangle b2_euroRectangle2 = new Rectangle(474, 430 - 160, 25, 15);
		Canvas b2_euroCanvas2 = new Canvas(pdfCanvas, b2_euroRectangle2);
		b2_euroCanvas2.add(euroP);
		b2_euroCanvas2.close();

		// Importo
		Rectangle b2_importoRectangle3 = new Rectangle(510, 431 - 160, 60, 15);
		Canvas b2_importoCanvas3 = new Canvas(pdfCanvas, b2_importoRectangle3);
		b2_importoCanvas3.add(importoP2);
		b2_importoCanvas3.close();

		// Logo Bollettino Postale
		Rectangle b2_logoBollettinoPostaleRectangle = new Rectangle(30, 389 - 160, 78, 32);
		Canvas b2_logoBollettinoPostaleCanvas = new Canvas(pdfCanvas, b2_logoBollettinoPostaleRectangle);
		b2_logoBollettinoPostaleCanvas.add(asset.getLogo_bollettino_postale().scaleToFit(78, 32));
		b2_logoBollettinoPostaleCanvas.close();

		// Bollettino Postale Descrizione
		Rectangle b2_bollettinoPostaleDescrizioneRectangle = new Rectangle(30, 340 - 160, 120, 48);
		Canvas b2_bollettinoPostaleDescrizioneCanvas = new Canvas(pdfCanvas, b2_bollettinoPostaleDescrizioneRectangle);
		Text b2_bollettinoPostaleDescrizioneText = new Text(LeggoAsset.BOLLETTINO_POSTALE_DESCRIZIONE)
				.setFont(asset.getInfoBollettinoFont());
		Paragraph b2_bollettinoPostaleDescrizioneP = new Paragraph().add(b2_bollettinoPostaleDescrizioneText)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getInfoBollettinoSize()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(10);
		b2_bollettinoPostaleDescrizioneCanvas.add(b2_bollettinoPostaleDescrizioneP);
		b2_bollettinoPostaleDescrizioneCanvas.close();

		// Autorizzazione
		Rectangle b2_autorizzazioneRectangle = new Rectangle(30, 327 - 160, 160, 9);
		Canvas b2_autorizzazioneCanvas = new Canvas(pdfCanvas, b2_autorizzazioneRectangle);
		Text b2_autorizzazioneText = new Text(documento.DatiBollettino.get(bollettinoDiPartenza + 1).AutorizCcp).setFont(asset.getAutorizzazioneFont());
		Paragraph b2_autorizzazioneP = new Paragraph().add(b2_autorizzazioneText).setFontColor(LeggoAsset.grigioForbici)
				.setFontSize(asset.getAutorizzazioneSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b2_autorizzazioneCanvas.add(b2_autorizzazioneP);
		b2_autorizzazioneCanvas.close();

		// Intestato a
		Rectangle b2_intestatoARectangle = new Rectangle(178, 406 - 160, 59, 12);
		Canvas b2_intestatoACanvas = new Canvas(pdfCanvas, b2_intestatoARectangle);
		Text b2_intestatoAText = new Text(LeggoAsset.INTESTATO_A).setFont(asset.getEtichettaDenominazioneFont());
		Paragraph b2_intestatoAP = new Paragraph().add(b2_intestatoAText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getEtichettaDenominazioneSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b2_intestatoACanvas.add(b2_intestatoAP);
		b2_intestatoACanvas.close();

		// Intestatario CC Postale
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		//Rectangle b2_intestatarioCCPostaleRectangle = new Rectangle(220, 406 - 160, 258, 10);
		Rectangle b2_intestatarioCCPostaleRectangle = new Rectangle(220 + asset.getXoffSet(), 407 - 160 + asset.getYoffSet(), 258, 10);
		//fine LP PG200420 - Errori nel pdf dell'avviso
		Canvas b2_intestatarioCCPostaleCanvas = new Canvas(pdfCanvas, b2_intestatarioCCPostaleRectangle);
		Text b2_intestatarioCCPostaleText = new Text(documento.DatiBollettino.get(bollettinoDiPartenza + 1).Descon60Boll)
				.setFont(asset.getDenominazioneNome2Font());
		Paragraph b2_intestatarioCCPostaleP = new Paragraph().add(b2_intestatarioCCPostaleText)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		b2_intestatarioCCPostaleCanvas.add(b2_intestatarioCCPostaleP);
		b2_intestatarioCCPostaleCanvas.close();

		// Destinatario
		Rectangle b2_destinatarioRectangle3 = new Rectangle(178, 389 - 160, 57, 12);
		Canvas b2_destinatarioCanvas3 = new Canvas(pdfCanvas, b2_destinatarioRectangle3);
		Text b2_destinatarioText = new Text("Destinatario").setFont(asset.getEtichettaDenominazioneFont());
		Paragraph b2_destinatarioP = new Paragraph().add(b2_destinatarioText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getEtichettaDenominazioneSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b2_destinatarioCanvas3.add(b2_destinatarioP);
		b2_destinatarioCanvas3.close();

		// Nome Cognome Destinatario
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		//Rectangle b2_nomeDestinatarioRectangle3 = new Rectangle(229, 391 - 160, 169, 10);
		Rectangle b2_nomeDestinatarioRectangle3 = new Rectangle(229 + asset.getXoffSet(), 366 - 160 + asset.getYoffSet(), 170 - 10 - 9 - 25, 32);
		//fine LP PG200420 - Errori nel pdf dell'avviso
		Canvas b2_nomeDestinatarioCanvas3 = new Canvas(pdfCanvas, b2_nomeDestinatarioRectangle3);
		Text b2_nomeDestinatarioText = new Text(documento.DatiAnagrafici.get(0).Denominazione1)
				.setFont(asset.getDenominazioneNome2Font());
		Paragraph b2_nomeDestinatarioP3 = new Paragraph().add(b2_nomeDestinatarioText)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		b2_nomeDestinatarioP3.setFixedLeading(8);
		//fine LP PG200420 - Errori nel pdf dell'avviso
		b2_nomeDestinatarioCanvas3.add(b2_nomeDestinatarioP3);
		b2_nomeDestinatarioCanvas3.close();

		// Oggetto Pagamento
		Rectangle b2_oggettoPagamentoRectangle3 = new Rectangle(178, 371 - 160, 88, 12);
		Canvas b2_oggettoPagamentoCanvas3 = new Canvas(pdfCanvas, b2_oggettoPagamentoRectangle3);
		Text b2_oggettoPagamentoText3 = new Text(LeggoAsset.OGGETTO_PAGAMENTO).setFont(asset.getEtichettaDenominazioneFont());
		Paragraph b2_oggettoPagamentoP3 = new Paragraph().add(b2_oggettoPagamentoText3)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getEtichettaDenominazioneSize()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		b2_oggettoPagamentoCanvas3.add(b2_oggettoPagamentoP3);
		b2_oggettoPagamentoCanvas3.close();

		// Oggetto Pagamento String
		Rectangle b2_oggettoPagamentoStringRectangle3 = new Rectangle(250 + asset.getXoffSet() * 3, 350 - 160 + asset.getYoffSet(), 213, 30);
		Canvas b2_oggettoPagamentoStringCanvas3 = new Canvas(pdfCanvas, b2_oggettoPagamentoStringRectangle3);
		b2_oggettoPagamentoStringCanvas3.add(b1_oggettoPagamentoStringP3);
		b2_oggettoPagamentoStringCanvas3.close();

		// Data matrix Container
		Rectangle b2_dataMatrixContainerRectangle = new Rectangle(480, 328 - 160, 93, 93);
		Canvas b2_dataMatrixContainerCanvas = new Canvas(pdfCanvas, b2_dataMatrixContainerRectangle);
		b2_dataMatrixContainerCanvas.add(asset.getData_matrix_container().scaleToFit(93, 93));
		b2_dataMatrixContainerCanvas.close();

		// Data matrix
		// Rectangle b2_dataMatrixRectangle = new Rectangle(490, 339 - 160, 74, 74);
		// Canvas b2_dataMatrixCanvas = new Canvas(pdfCanvas, b2_dataMatrixRectangle);
		// b2_dataMatrixCanvas.add(asset.getData_matrix().scaleToFit(74, 74));
		// b2_dataMatrixCanvas.close();

// 		Data matrix
		Rectangle b2_dataMatrixRectangle = new Rectangle(490, 339 - 160, 74, 74);
		Canvas b2_dataMatrixCanvas = new Canvas(pdfCanvas, b2_dataMatrixRectangle);
		b2_dataMatrixCanvas.add(generaDataMatrix(documento.DatiBollettino.get(bollettinoDiPartenza + 1).QRcodePagoPa, pdf).scaleToFit(74, 74));
		b2_dataMatrixCanvas.close();

		// Codice Avviso
		Rectangle b2_codiceAvvisoRectangle2 = new Rectangle(179, 348 - 160, 79, 12);
		Canvas b2_codiceAvvisoCanvas2 = new Canvas(pdfCanvas, b2_codiceAvvisoRectangle2);
		b2_codiceAvvisoCanvas2.add(codiceAvvisoP);
		b2_codiceAvvisoCanvas2.close();

		// Codice Avviso String
		Rectangle b2_codiceAvvisoStringRectangle2 = new Rectangle(179, 337 - 160, 160, 12);
		Canvas b2_codiceAvvisoStringCanvas2 = new Canvas(pdfCanvas, b2_codiceAvvisoStringRectangle2);
		b2_codiceAvvisoStringCanvas2.add(codiceAvvisoStringP2);
		b2_codiceAvvisoStringCanvas2.close();

		// Tipo
		Rectangle b2_tipoRectangle = new Rectangle(329, 348 - 160, 25, 12);
		Canvas b2_tipoCanvas = new Canvas(pdfCanvas, b2_tipoRectangle);
		Text b2_tipoText = new Text(LeggoAsset.TIPO).setFont(asset.getEtichettaDenominazioneFont());
		Paragraph b2_tipoP = new Paragraph().add(b2_tipoText).setFontColor(ColorConstants.BLACK).setFontSize(asset.getEtichettaDenominazioneSize())
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b2_tipoCanvas.add(b2_tipoP);
		b2_tipoCanvas.close();

		// P1
		Rectangle b2_p1Rectangle = new Rectangle(331, 337 - 160, 13, 13);
		Canvas b2_p1Canvas = new Canvas(pdfCanvas, b2_p1Rectangle);
		Text b2_p1Text = new Text(LeggoAsset.P1).setFont(asset.getCodiceBoldFont());
		Paragraph b2_p1P = new Paragraph().add(b2_p1Text).setFontColor(ColorConstants.BLACK).setFontSize(asset.getCodiceBoldSize())
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b2_p1Canvas.add(b2_p1P);
		b2_p1Canvas.close();

		// Cod. Fiscale Ente Creditore
		Rectangle b2_cfEnteCreditoreRectangle2 = new Rectangle(375, 348 - 160, 120, 12);
		Canvas b2_cfEnteCreditoreCanvas2 = new Canvas(pdfCanvas, b2_cfEnteCreditoreRectangle2);
		b2_cfEnteCreditoreCanvas2.add(cfEnteCreditoreP);
		b2_cfEnteCreditoreCanvas2.close();

		// Cod. Fiscale Ente Creditore String
		Rectangle b2_cfEnteCreditoreStringRectangle2 = new Rectangle(375, 337 - 160, 125, 12);
		Canvas b2_cfEnteCreditoreStringCanvas2 = new Canvas(pdfCanvas, b2_cfEnteCreditoreStringRectangle2);
		b2_cfEnteCreditoreStringCanvas2.add(cfEnteCreditoreStringP);
		b2_cfEnteCreditoreStringCanvas2.close();

		// Bollettino 3
		// Forbici
		Rectangle b3_forbiciRectangle = new Rectangle(548, 474 - 320, 14, 10);
		Canvas b3_forbiciCanvas = new Canvas(pdfCanvas, b3_forbiciRectangle);
		b3_forbiciCanvas.add(asset.getLogo_forbici().scaleToFit(14, 10));
		b3_forbiciCanvas.close();

		// Bollettino Postale PA Background Border
		Rectangle b3_bollettinoPostalePaBorderRectangle = new Rectangle(0, 474 - 320, 595, 1);
		pdfCanvas.saveState().setFillColor(LeggoAsset.grigioForbici).rectangle(b3_bollettinoPostalePaBorderRectangle)
				.fill();
		Canvas b3_bollettinoPostalePaBorderCanvas = new Canvas(pdfCanvas, b3_bollettinoPostalePaBorderRectangle);
		b3_bollettinoPostalePaBorderCanvas.close();

		// Bollettino Postale PA Background
		Rectangle b3_bollettinoPostalePaGrayRectangle = new Rectangle(0, 457 - 320, 595, 17);
		pdfCanvas.saveState().setFillColor(LeggoAsset.grigioBollettino).rectangle(b3_bollettinoPostalePaGrayRectangle)
				.fill();
		Canvas b3_bollettinoPostalePaGrayCanvas = new Canvas(pdfCanvas, b3_bollettinoPostalePaGrayRectangle);
		b3_bollettinoPostalePaGrayCanvas.close();

		// Bollettino Postale PA
		Rectangle b3_bollettinoPostalePaRectangle = new Rectangle(29, 462 - 320  - asset.getYoffSet(), 135, 15);
		Canvas b3_bollettinoPostalePaCanvas = new Canvas(pdfCanvas, b3_bollettinoPostalePaRectangle);
		Text b3_bollettinoPostalePaText = new Text(LeggoAsset.BOLLETTINO_POSTALE_PA).setFont(asset.getTitoloFont());
		Paragraph b3_bollettinoPostalePaP = new Paragraph().add(b3_bollettinoPostalePaText)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getTitoloSize()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		b3_bollettinoPostalePaCanvas.add(b3_bollettinoPostalePaP);
		b3_bollettinoPostalePaCanvas.close();

		// Logo Banco Posta
		Rectangle b3_logoBancopostaRectangle = new Rectangle(183, 463 - 320, 51, 7);
		Canvas b3_logoBancopostaCanvas = new Canvas(pdfCanvas, b3_logoBancopostaRectangle);
		b3_logoBancopostaCanvas.add(asset.getLogo_bancoposta().scaleToFit(51, 7));
		b3_logoBancopostaCanvas.close();

		// Numero Rata
		Rectangle b3_numeroRataRectangle2 = new Rectangle(437 - asset.getXoffSet() * 2, 463 - 320  - asset.getYoffSet() * 2, 195, 15 + asset.getXoffSet());
		Canvas b3_numeroRataCanvas2 = new Canvas(pdfCanvas, b3_numeroRataRectangle2);
		Text b3_numeroRataText1 = new Text(Integer.toString(documento.DatiBollettino.get(bollettinoDiPartenza + 2).ProgressivoBoll) + "Â° ").setFont(asset.getNumRataFont());
		Text b3_numeroRataText2 = new Text(" RATA ").setFont(asset.getNumRataFont());
		Text b3_numeroRataText3 = new Text("  entro il  ").setFont(asset.getEntroRateFont());
		Text b3_numeroRataText4 = new Text(documento.DatiBollettino.get(bollettinoDiPartenza + 2).ScadenzaRata).setFont(asset.getInEvidenza5Font());
		Paragraph b3_numeroRataP = new Paragraph()
				.add(b3_numeroRataText1).setFontSize(asset.getNumRataSize())
				.add(b3_numeroRataText2).setFontSize(asset.getInEvidenza4Size())
				.add(b3_numeroRataText3).setFontSize(asset.getEntroRateSize())
				.add(b3_numeroRataText4).setFontSize(asset.getInEvidenza1Size())
				.setFontColor(ColorConstants.BLACK)
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b3_numeroRataCanvas2.add(b3_numeroRataP);
		b3_numeroRataCanvas2.close();

		// Logo Poste Italiane
		Rectangle b3_logoPosteitalianeRectangle = new Rectangle(29, 425 - 320, 117, 15);
		Canvas b3_logoPosteitalianeCanvas = new Canvas(pdfCanvas, b3_logoPosteitalianeRectangle);
		b3_logoPosteitalianeCanvas.add(asset.getLogo_poste_italiane().scaleToFit(117, 15));
		b3_logoPosteitalianeCanvas.close();

		// Logo Euro
		Rectangle b3_logoEuroRectangle = new Rectangle(180, 423 - 320, 19, 19);
		Canvas b3_logoEuroCanvas = new Canvas(pdfCanvas, b3_logoEuroRectangle);
		b3_logoEuroCanvas.add(asset.getLogo_euro_bollettino().scaleToFit(19, 19));
		b3_logoEuroCanvas.close();

		// Sul CC
		Rectangle b3_suCcRectangle = new Rectangle(205, 429 - 320, 43, 15);
		Canvas b3_suCcCanvas = new Canvas(pdfCanvas, b3_suCcRectangle);
		Text b3_suCcText = new Text(LeggoAsset.SUL_CC).setFont(asset.getSulCcFont());
		Paragraph b3_suCcP = new Paragraph().add(b3_suCcText).setFontColor(ColorConstants.BLACK).setFontSize(asset.getSulCcSize())
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b3_suCcCanvas.add(b3_suCcP);
		b3_suCcCanvas.close();

		// Numero CC Postale
		Rectangle b3_numeroCcPostaleRectangle = new Rectangle(248, 430 - 320, 126, 15);
		Canvas b3_numeroCcPostaleCanvas = new Canvas(pdfCanvas, b3_numeroCcPostaleRectangle);
		Text b3_numeroCcPostaleText = new Text(documento.DatiBollettino.get(bollettinoDiPartenza + 2).Codeline12Boll).setFont(asset.getCodiceBoldFont());
		Paragraph b3_numeroCcPostaleP = new Paragraph().add(b3_numeroCcPostaleText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getCodiceBoldSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b3_numeroCcPostaleCanvas.add(b3_numeroCcPostaleP);
		b3_numeroCcPostaleCanvas.close();

		// Euro
		Rectangle b3_euroRectangle2 = new Rectangle(474, 430 - 320, 25, 15);
		Canvas b3_euroCanvas2 = new Canvas(pdfCanvas, b3_euroRectangle2);
		b3_euroCanvas2.add(euroP);
		b3_euroCanvas2.close();

		// Importo
		Rectangle b3_importoRectangle3 = new Rectangle(510, 431 - 320, 60, 15);
		Canvas b3_importoCanvas3 = new Canvas(pdfCanvas, b3_importoRectangle3);
		b3_importoCanvas3.add(importoP3);
		b3_importoCanvas3.close();

		// Logo Bollettino Postale
		Rectangle b3_logoBollettinoPostaleRectangle = new Rectangle(30, 389 - 320, 78, 32);
		Canvas b3_logoBollettinoPostaleCanvas = new Canvas(pdfCanvas, b3_logoBollettinoPostaleRectangle);
		b3_logoBollettinoPostaleCanvas.add(asset.getLogo_bollettino_postale().scaleToFit(78, 32));
		b3_logoBollettinoPostaleCanvas.close();

		// Bollettino Postale Descrizione
		Rectangle b3_bollettinoPostaleDescrizioneRectangle = new Rectangle(30, 340 - 320, 120, 48);
		Canvas b3_bollettinoPostaleDescrizioneCanvas = new Canvas(pdfCanvas, b3_bollettinoPostaleDescrizioneRectangle);
		Text b3_bollettinoPostaleDescrizioneText = new Text(LeggoAsset.BOLLETTINO_POSTALE_DESCRIZIONE)
				.setFont(asset.getInfoBollettinoFont());
		Paragraph b3_bollettinoPostaleDescrizioneP = new Paragraph().add(b3_bollettinoPostaleDescrizioneText)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getInfoBollettinoSize()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE).setFixedLeading(10);
		b3_bollettinoPostaleDescrizioneCanvas.add(b3_bollettinoPostaleDescrizioneP);
		b3_bollettinoPostaleDescrizioneCanvas.close();

		// Autorizzazione
		Rectangle b3_autorizzazioneRectangle = new Rectangle(30, 327 - 320, 160, 9);
		Canvas b3_autorizzazioneCanvas = new Canvas(pdfCanvas, b3_autorizzazioneRectangle);
		Text b3_autorizzazioneText = new Text(documento.DatiBollettino.get(bollettinoDiPartenza + 2).AutorizCcp).setFont(asset.getAutorizzazioneFont());
		Paragraph b3_autorizzazioneP = new Paragraph().add(b3_autorizzazioneText).setFontColor(LeggoAsset.grigioForbici)
				.setFontSize(asset.getAutorizzazioneSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b3_autorizzazioneCanvas.add(b3_autorizzazioneP);
		b3_autorizzazioneCanvas.close();

		// Intestato a
		Rectangle b3_intestatoARectangle = new Rectangle(178, 406 - 320, 59, 12);
		Canvas b3_intestatoACanvas = new Canvas(pdfCanvas, b3_intestatoARectangle);
		Text b3_intestatoAText = new Text(LeggoAsset.INTESTATO_A).setFont(asset.getEtichettaDenominazioneFont());
		Paragraph b3_intestatoAP = new Paragraph().add(b3_intestatoAText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getEtichettaDenominazioneSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b3_intestatoACanvas.add(b3_intestatoAP);
		b3_intestatoACanvas.close();

		// Intestatario CC Postale
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		//Rectangle b3_intestatarioCCPostaleRectangle = new Rectangle(220, 406 - 320, 258, 10);
		Rectangle b3_intestatarioCCPostaleRectangle = new Rectangle(220 + asset.getXoffSet(), 407 - 320 + asset.getYoffSet(), 258, 10);
		//fine LP PG200420 - Errori nel pdf dell'avviso
		Canvas b3_intestatarioCCPostaleCanvas = new Canvas(pdfCanvas, b3_intestatarioCCPostaleRectangle);
		Text b3_intestatarioCCPostaleText = new Text(documento.DatiBollettino.get(bollettinoDiPartenza + 2).Descon60Boll)
				.setFont(asset.getDenominazioneNome2Font());
		Paragraph b3_intestatarioCCPostaleP = new Paragraph().add(b3_intestatarioCCPostaleText)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		b3_intestatarioCCPostaleCanvas.add(b3_intestatarioCCPostaleP);
		b3_intestatarioCCPostaleCanvas.close();

		// Destinatario
		Rectangle b3_destinatarioRectangle3 = new Rectangle(178, 389 - 320, 57, 12);
		Canvas b3_destinatarioCanvas3 = new Canvas(pdfCanvas, b3_destinatarioRectangle3);
		Text b3_destinatarioText = new Text("Destinatario").setFont(asset.getEtichettaDenominazioneFont());
		Paragraph b3_destinatarioP = new Paragraph().add(b3_destinatarioText).setFontColor(ColorConstants.BLACK)
				.setFontSize(asset.getEtichettaDenominazioneSize()).setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b3_destinatarioCanvas3.add(b3_destinatarioP);
		b3_destinatarioCanvas3.close();

		// Nome Cognome Destnatario
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		//Rectangle b3_nomeDestinatarioRectangle3 = new Rectangle(229, 391 - 320, 169, 10);
		Rectangle b3_nomeDestinatarioRectangle3 = new Rectangle(229 + asset.getXoffSet(), 366 - 320 + asset.getYoffSet(), 170 - 10 - 9 - 25, 32);
		//fine LP PG200420 - Errori nel pdf dell'avviso
		Canvas b3_nomeDestinatarioCanvas3 = new Canvas(pdfCanvas, b3_nomeDestinatarioRectangle3);
		Text b3_nomeDestinatarioText = new Text(documento.DatiAnagrafici.get(0).Denominazione1)
				.setFont(asset.getDenominazioneNome2Font());
		Paragraph b3_nomeDestinatarioP3 = new Paragraph().add(b3_nomeDestinatarioText)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getDenominazioneNome2Size()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		//inizio LP PG200420 - Errori nel pdf dell'avviso
		b3_nomeDestinatarioP3.setFixedLeading(8);
		//fine LP PG200420 - Errori nel pdf dell'avviso
		b3_nomeDestinatarioCanvas3.add(b3_nomeDestinatarioP3);
		b3_nomeDestinatarioCanvas3.close();

		// Oggetto Pagamento
		Rectangle b3_oggettoPagamentoRectangle3 = new Rectangle(178, 371 - 320, 88, 12);
		Canvas b3_oggettoPagamentoCanvas3 = new Canvas(pdfCanvas, b3_oggettoPagamentoRectangle3);
		Text b3_oggettoPagamentoText3 = new Text(LeggoAsset.OGGETTO_PAGAMENTO).setFont(asset.getEtichettaDenominazioneFont());
		Paragraph b3_oggettoPagamentoP3 = new Paragraph().add(b3_oggettoPagamentoText3)
				.setFontColor(ColorConstants.BLACK).setFontSize(asset.getEtichettaDenominazioneSize()).setMargin(0)
				.setVerticalAlignment(VerticalAlignment.MIDDLE);
		b3_oggettoPagamentoCanvas3.add(b3_oggettoPagamentoP3);
		b3_oggettoPagamentoCanvas3.close();

		// Oggetto Pagamento String
		Rectangle b3_oggettoPagamentoStringRectangle3 = new Rectangle(250 + asset.getXoffSet() * 3, 350 - 320 + asset.getYoffSet(), 213, 30);
		Canvas b3_oggettoPagamentoStringCanvas3 = new Canvas(pdfCanvas, b3_oggettoPagamentoStringRectangle3);
		b3_oggettoPagamentoStringCanvas3.add(b1_oggettoPagamentoStringP3);
		b3_oggettoPagamentoStringCanvas3.close();

		// Data matrix Container
		Rectangle b3_dataMatrixContainerRectangle = new Rectangle(480, 328 - 320, 93, 93);
		Canvas b3_dataMatrixContainerCanvas = new Canvas(pdfCanvas, b3_dataMatrixContainerRectangle);
		b3_dataMatrixContainerCanvas.add(asset.getData_matrix_container().scaleToFit(93, 93));
		b3_dataMatrixContainerCanvas.close();

		// Data matrix
		// Rectangle b3_dataMatrixRectangle = new Rectangle(490, 339 - 320, 74, 74);
		// Canvas b3_dataMatrixCanvas = new Canvas(pdfCanvas, b3_dataMatrixRectangle);
		// b3_dataMatrixCanvas.add(asset.getData_matrix().scaleToFit(74, 74));
		// b3_dataMatrixCanvas.close();

// 		Data matrix
		Rectangle b3_dataMatrixRectangle = new Rectangle(490, 339 - 320, 74, 74);
		Canvas b3_dataMatrixCanvas = new Canvas(pdfCanvas, b3_dataMatrixRectangle);
		b3_dataMatrixCanvas.add(generaDataMatrix(documento.DatiBollettino.get(bollettinoDiPartenza + 2).QRcodePagoPa, pdf).scaleToFit(74, 74));
		b3_dataMatrixCanvas.close();

		// Codice Avviso
		Rectangle b3_codiceAvvisoRectangle2 = new Rectangle(179, 348 - 320, 79, 12);
		Canvas b3_codiceAvvisoCanvas2 = new Canvas(pdfCanvas, b3_codiceAvvisoRectangle2);
		b3_codiceAvvisoCanvas2.add(codiceAvvisoP);
		b3_codiceAvvisoCanvas2.close();

		// Codice Avviso String
		Rectangle b3_codiceAvvisoStringRectangle2 = new Rectangle(179, 337 - 320, 170, 12);
		Canvas b3_codiceAvvisoStringCanvas2 = new Canvas(pdfCanvas, b3_codiceAvvisoStringRectangle2);
		b3_codiceAvvisoStringCanvas2.add(codiceAvvisoStringP3);
		b3_codiceAvvisoStringCanvas2.close();

		// Tipo
		Rectangle b3_tipoRectangle = new Rectangle(329, 348 - 320, 25, 12);
		Canvas b3_tipoCanvas = new Canvas(pdfCanvas, b3_tipoRectangle);
		Text b3_tipoText = new Text(LeggoAsset.TIPO).setFont(asset.getEtichettaDenominazioneFont());
		Paragraph b3_tipoP = new Paragraph().add(b3_tipoText).setFontColor(ColorConstants.BLACK).setFontSize(asset.getEtichettaDenominazioneSize())
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b3_tipoCanvas.add(b3_tipoP);
		b3_tipoCanvas.close();

		// P1
		Rectangle b3_p1Rectangle = new Rectangle(331, 337 - 320, 13, 13);
		Canvas b3_p1Canvas = new Canvas(pdfCanvas, b3_p1Rectangle);
		Text b3_p1Text = new Text(LeggoAsset.P1).setFont(asset.getCodiceBoldFont());
		Paragraph b3_p1P = new Paragraph().add(b3_p1Text).setFontColor(ColorConstants.BLACK).setFontSize(asset.getCodiceBoldSize())
				.setMargin(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
		b3_p1Canvas.add(b3_p1P);
		b3_p1Canvas.close();

		// Cod. Fiscale Ente Creditore
		Rectangle b3_cfEnteCreditoreRectangle2 = new Rectangle(375, 348 - 320, 120, 12);
		Canvas b3_cfEnteCreditoreCanvas2 = new Canvas(pdfCanvas, b3_cfEnteCreditoreRectangle2);
		b3_cfEnteCreditoreCanvas2.add(cfEnteCreditoreP);
		b3_cfEnteCreditoreCanvas2.close();

		// Cod. Fiscale Ente Creditore String
		Rectangle b3_cfEnteCreditoreStringRectangle2 = new Rectangle(375, 337 - 320, 125, 12);
		Canvas b3_cfEnteCreditoreStringCanvas2 = new Canvas(pdfCanvas, b3_cfEnteCreditoreStringRectangle2);
		b3_cfEnteCreditoreStringCanvas2.add(cfEnteCreditoreStringP);
		b3_cfEnteCreditoreStringCanvas2.close();
		}
	}

//	private static File creaFile(String soggettoPagatore) {
//		String nomeFile = "BollettiniPagoPA-" + soggettoPagatore + "-" + java.time.LocalDateTime.now();
//		nomeFile = nomeFile.replaceAll("[^a-zA-Z0-9]", "_");
//		File file = new File(LeggoAsset.DIRECTORY_SALVATAGGIO_FILE, nomeFile + ".pdf");
//		return file;
//	}
	
	private String generaNomeFile(File512 file512) {
		
		String nomeFile = file512.societa
				+ "_"
				+ "F51P"
				+ "_"
				+ file512.cutecute
				+ "_"
				+ file512.ente
				+ "_"
				+ java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
				+ "_"
				+ "all_docs";
		nomeFile = nomeFile.replaceAll("[^a-zA-Z0-9]", "_");
		return nomeFile;
	}
	
	private Image generaQRCode(String code, PdfDocument pdf) {
		BarcodeQRCode qrCode = new BarcodeQRCode(code);
		return new Image(qrCode.createFormXObject(ColorConstants.BLACK, pdf));
	}
	
	//PAGONET-327 - inizio
//	private Image generaDataMatrix(String code, PdfDocument pdf) {
//		BarcodeDataMatrix dataMatrix = new BarcodeDataMatrix(code);
//		return new Image(dataMatrix.createFormXObject(ColorConstants.BLACK, pdf));
//	}
	
	private Image generaDataMatrix(String code, PdfDocument pdf) {	
		BarcodeDataMatrix dataMatrix = new BarcodeDataMatrix();
		dataMatrix.setOptions(BarcodeDataMatrix.DM_ASCII);
		dataMatrix.setHeight(64);
		dataMatrix.setWidth(64);
		dataMatrix.setCode(code);
		return new Image(dataMatrix.createFormXObject(ColorConstants.BLACK, pdf));
	}
	//PAGONET-327 - fine

	private String componiIndirizzo(String indirizzo, String citta, String provincia) {
		int maxlengthIndirizzoReturn = 40;
		int maxLengthCitta = 25;
		
		StringBuilder indirizzoMax40char = new StringBuilder(citta.substring(0, Math.min(maxLengthCitta,  citta.length())));
		indirizzoMax40char.append(" " + provincia); 
		indirizzoMax40char.insert(0, " "); //lunghezza massima qui maxLengthCitta + 5
		int len = indirizzoMax40char.length();
		indirizzoMax40char.insert(0, indirizzo.substring(0, Math.min(indirizzo.length(), maxlengthIndirizzoReturn - len))); //aggiunge la via, minimo vengono aggiunti 6 caratteri
		return indirizzoMax40char.toString();
	}

	private String mettiVirgolaEPuntiAllImportoInCent(String importoSenzaVirgola) {
		
		BigDecimal bd = new BigDecimal(importoSenzaVirgola).divide(new BigDecimal("100"));
		String importoConVirgola = formatDecimalNumber(bd);
		
		return importoConVirgola;
	}

	public String formatDecimalNumber(BigDecimal bdValue) {
		DecimalFormat dcFormat = getDecimalFormat();
		bdValue = bdValue.setScale(2, BigDecimal.ROUND_HALF_UP);
		
		return dcFormat.format(bdValue);
	}
	
	public DecimalFormat getDecimalFormat() {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(); 
		symbols.setDecimalSeparator(',');
		symbols.setGroupingSeparator('.');

		DecimalFormat dcFormat = new DecimalFormat("###,##0.00", symbols);
		return dcFormat;
	}
	
	public String formattaCodiceAvviso(String numeroAvviso) {
		//da flusso 512 arriva il numero codice avviso senza spazi ogni 4 cifre. 
				//controlliamo che non sia formattato dando per assunto che se alla quinta cifra c'è lo spazio allora sono corrette
				if(numeroAvviso != null && !numeroAvviso.trim().substring(4, 5).equals(" ")) 
					return numeroAvviso.trim().replaceAll("(.{4})", "$1 ").trim();
		return numeroAvviso;
	}
	

}
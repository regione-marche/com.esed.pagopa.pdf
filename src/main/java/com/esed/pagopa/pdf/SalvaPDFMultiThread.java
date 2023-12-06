///**
// * 
// */
//package com.esed.pagopa.pdf;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Base64;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.Callable;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//import java.util.concurrent.TimeUnit;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipOutputStream;
//
//import org.apache.pdfbox.multipdf.PDFMergerUtility;
//
//import com.esed.pagopa.pdf.config.PropKeys;
//import com.esed.pagopa.pdf.printer.jppa.InformazioniStampaAosta;
//import com.esed.pagopa.pdf.printer.jppa.InformazioniStampaBolzano;
//import com.esed.pagopa.pdf.printer.jppa.InformazioniStampaGenerico;
//import com.esed.pagopa.pdf.printer.jppa.InformazioniStampaInterface;
//import com.esed.pagopa.pdf.printer.jppa.StampaPdfJppaPagonet;
//import com.esed.pagopa.pdf.printer.threadManager.PrinterThreadManager;
//import com.fasterxml.jackson.core.JsonParseException;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.JsonMappingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.itextpdf.kernel.pdf.PdfDocument;
//import com.itextpdf.layout.Document;
//import com.seda.commons.properties.PropertiesLoader;
//import com.seda.commons.properties.tree.PropertiesNodeException;
//import com.seda.commons.properties.tree.PropertiesTree;
//import com.seda.payer.commons.geos.Bollettino;
//import com.seda.payer.commons.geos.Documento;
//import com.seda.payer.commons.geos.Flusso;
//import com.seda.payer.commons.inviaAvvisiForGeos.File512;
//import com.seda.payer.commons.jppa.utils.LogoBollettino;
//
//import it.maggioli.pagopa.jppa.printer.model.StampaBollettinoRichiesta;
//import it.maggioli.pagopa.jppa.printer.model.StampaBollettinoRisposta;
//
///**
// * 
// */
//public class SalvaPDFMultiThread extends PrinterThreadManager {
//	
//	private final File fileTxtGuida;
//	private final GuidaDocumento guidaDocumento;
//	private final PropertiesTree propertiesTree;
//	private final File512 file512;
//	private final Flusso flusso;
//	private String passwordJppa = "";
//	private String userJppa= "";
//	private String urlPrinter= "";
//	private StampaPdfJppaPagonet stampa = null;
//	private final Map<Documento,StampaBollettinoRichiesta> richieste = new HashMap<>();
//	private int pagineaggiunteDocumento = 0;
//	
//	
//	 public SalvaPDFMultiThread(File fileTxtGuida,GuidaDocumento guidaDocumento
//			 ,PropertiesTree propertiesTree,File512 file512,Flusso flusso) {
//		 
//		 this.fileTxtGuida = fileTxtGuida;
//		 this.guidaDocumento = guidaDocumento;
//		 this.propertiesTree = propertiesTree;
//		 this.file512 = file512;
//		 this.flusso = flusso;
//		 
//	 }
//	
//
//
//		@Override
//		protected void initilizeMassivePrint() {
//			 passwordJppa = propertiesTree.getProperty(PropKeys.passwordJppa.format(file512.cutecute));
//			 userJppa = propertiesTree.getProperty(PropKeys.utenteJppa.format(file512.cutecute));
//			 urlPrinter = propertiesTree.getProperty(PropKeys.urlprinter.format(file512.cutecute));
//			 stampa = new StampaPdfJppaPagonet(userJppa,passwordJppa,urlPrinter);
//		}
//		
//		
//		private static String generaNomeFile(File512 file512) {
//			
//			String nomeFile = file512.societa
//					+ "_"
//					+ "F51P"
//					+ "_"
//					+ file512.cutecute
//					+ "_"
//					+ file512.ente
//					+ "_"
//					+ java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
//					+ "_"
//					+ "all_docs";
//			nomeFile = nomeFile.replaceAll("[^a-zA-Z0-9]", "_");
//			return nomeFile;
//		}
//	
//	
//		public int stampaMassivoJppa(String path,File file) throws ValidazioneException, FileNotFoundException {
//			
//			String nomef = "";
//			
//			List<String> nomiFile = new ArrayList<>();
//			List<String> allBoll = new ArrayList<>();
//			
//			/**
//			 * All interno dell oggetto LogoBollettino sono salvati in modo statico 
//			 * le strighe corrispondenti al logo dell ente
//			 */
//			LogoBollettino logobollettino = new LogoBollettino();
//			
//			int stato = 1;
//			
//			for (int i = 0; i < flusso.Documentdata.size(); i++) {
//				
//				try {
//					nomef = generaNomeFile(file512);
//					FileOutputStream out = new FileOutputStream(path+"/"+nomef+".pdf");
//					nomiFile.add(nomef);
//					allBoll.add(nomef+".pdf");
//				}catch (FileNotFoundException e) {
//					e.printStackTrace();
//				}
//
//				int[] elencoBollettini = ValidaFlusso.validaFlusso(flusso.Documentdata.get(i), flusso.TipoStampa);
//
//				if (elencoBollettini.length < 1) {
//					return stato;
//				}
//				
//				Arrays.sort(elencoBollettini);
//				
//				System.out.println(Arrays.toString(elencoBollettini) + "---------------ELENCO BOLLETTINI-----------------");
//				
//				if (elencoBollettini.length > 0) {
//					
//					if(elencoBollettini.length==2) {
//						
//						stampaBollettinoSingolo(flusso.CuteCute,pagineaggiunteDocumento,
//								logobollettino.getLogo(flusso.CuteCute),flusso.Documentdata.get(i));
//
//						pagineaggiunteDocumento+=elencoBollettini.length-1;
//					}
//							
//				 
//					System.out.println(elencoBollettini.length);
//					
//					if (elencoBollettini.length > 2) {
//						
//						for (int j = 0; j < elencoBollettini.length - 1; ) {			
//
//								stampabollettinoMultiplo(flusso.CuteCute,pagineaggiunteDocumento,
//										logobollettino.getLogo(flusso.CuteCute),flusso.Documentdata.get(i));
//
//								j += elencoBollettini.length;
//								
//								pagineaggiunteDocumento+=elencoBollettini.length-1;
//						} 
//					}// fine if 2
//				}
//					stato = 0;
//			}
//			
//			return 0;
//		
//		}
//	
//
//	  private void stampabollettinoMultiplo(String cuteCute, int pagineAggiunteDocumento, String logo,
//				Documento documento) {
//
//		  if(flusso.CuteCute.equals("000P6")) {
//	    		
//				InformazioniStampaInterface stampabolzano = new InformazioniStampaBolzano(); 
//				System.out.println("info AvvisaturaDto - " + stampabolzano.toString());
//				richieste.put(documento,stampabolzano.stampaBollettinMultirata(documento, logo, flusso.CuteCute, 
//						ValidaFlusso.getDaArchivioCarichi()));
//				
//				
//			}else if(flusso.CuteCute.equals("000P4")) {
//				
//				com.esed.pagopa.pdf.printer.jppa.InformazioniStampaInterface stampaAosta = new InformazioniStampaAosta();
//				richieste.put(documento,stampaAosta.stampaBollettinMultirata(documento,logo, flusso.CuteCute, 
//						ValidaFlusso.getDaArchivioCarichi()));
//			}
//			else {
//				
//				com.esed.pagopa.pdf.printer.jppa.InformazioniStampaInterface stampaGenerico = new InformazioniStampaGenerico();
//				richieste.put(documento,stampaGenerico.stampaBollettinMultirata(documento,logo, flusso.CuteCute, 
//							ValidaFlusso.getDaArchivioCarichi()));
//			
//			}
//		  
//		}
//
//
//
//	private void stampaBollettinoSingolo(String cuteCute, int pagineAggiunteDocumento,String logo,
//			  Documento doc) {
//		
//		if(flusso.CuteCute.equals("000P6")) {
//				com.esed.pagopa.pdf.printer.jppa.InformazioniStampaInterface stampaBolzano = new InformazioniStampaBolzano();
//				richieste.put(doc,stampaBolzano.bollRichiesta(flusso,doc,
//						logo,flusso.CuteCute,ValidaFlusso.getDaArchivioCarichi()));
//				
//			}else if(flusso.CuteCute.equals("000P4")) {
//				com.esed.pagopa.pdf.printer.jppa.InformazioniStampaInterface stampaAosta = new InformazioniStampaAosta();
//				richieste.put(doc,stampaAosta.bollRichiesta(flusso,doc,
//						logo,flusso.CuteCute,ValidaFlusso.getDaArchivioCarichi()));
//			
//			  }else {
//					com.esed.pagopa.pdf.printer.jppa.InformazioniStampaInterface stampaGenerico = new InformazioniStampaGenerico();
//					richieste.put(doc,stampaGenerico.bollRichiesta(flusso,doc,
//							logo,flusso.CuteCute,ValidaFlusso.getDaArchivioCarichi()));
//				}
//		}
//
//	  
//
//	  @Override
//	  protected List<StampaBollettinoRisposta> computePrint() throws InterruptedException, ExecutionException {	
//		
//	    final int numberOfCores = Runtime.getRuntime().availableProcessors();
//	    final double blockingCoefficent = 0.9;
//	    final int poolSize = (int)(numberOfCores / (1-blockingCoefficent));
//
//	    System.out.println("Number of cores " + numberOfCores);
//	    System.out.println("Pool size is" + poolSize);
//	    
//	    final List<Callable<StampaBollettinoRisposta>> partitions =
//	            new ArrayList<>();
//	    
//	    
//	    for(final Documento request : richieste.keySet()) {
//	    	partitions.add(new Callable<StampaBollettinoRisposta>() {
//	            @Override
//	            public StampaBollettinoRisposta call() throws Exception {
//	            	StampaBollettinoRisposta ret = 
//	            			stampa.stampaBolpuntuale(richieste.get(request));
//	            	
//					guidaDocumento.aggiungiRigo(
//							request.DatiAnagrafici.get(0).Cf,
//							Integer.parseInt(flusso.DataFornitura.substring(4)),
//							request.NumeroDocumento,
//							fileTxtGuida.getName(),
//							file512.getFileName(),
//							pagineaggiunteDocumento,
//							request.ImpostaServizio
//							);
//					
//					return ret;
//	            }
//	        });
//	    }
//	    
//	    
//	    final ExecutorService executorPool =
//	            Executors.newFixedThreadPool(poolSize);
//
//	    final List<Future<StampaBollettinoRisposta>> richiesteStampa =
//	            executorPool.invokeAll(partitions,10000,TimeUnit.SECONDS);
//
//	    List<StampaBollettinoRisposta> risposte = new ArrayList<>();
//
//	    for(final Future<StampaBollettinoRisposta> stampe : richiesteStampa) {
//	        risposte.add(stampe.get());
//	    }
//
//	    executorPool.shutdown();
//	        
//	    
//	    return risposte;
//	    
//	  }
//
//	/**
//	 * @param args
//	 * @throws IOException 
//	 * @throws JsonMappingException 
//	 * @throws JsonParseException 
//	 * @throws ValidazioneException 
//	 * @throws ExecutionException 
//	 * @throws InterruptedException 
//	 */
//	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException, ValidazioneException, InterruptedException, ExecutionException {
//		
//		String stampaJppa = "";
//		PdfDocument pdf = null;
//		Document document = null;
//		File file = null;
//		File fileGuida = null;
//		File fileTxtGuida = null;
//		
//        String rootPath = "C:\\work\\Pagonet\\ConfigFiles\\pagopaPDFws\\pagoPaPdfWsRoot.properties";
//		
//		PropertiesTree propertiesTree = null;
//
//		
//		try {
//			propertiesTree = new PropertiesTree(PropertiesLoader.load(rootPath));
//		} catch (IOException | PropertiesNodeException e) {
//			throw new RuntimeException("Impossibile caricare il file di configurazione", e);
//		}
//		
//		ObjectMapper objectMapper = new ObjectMapper();
//		List<File512> file512List = objectMapper.readValue(new File("src\\main\\java\\com\\esed\\pagopa\\pdf\\test\\File512rate.json"), new TypeReference<List<File512>>(){} );
//		
//		UUID uuid = UUID.randomUUID();
//		SalvaPDF salvaPDF = new SalvaPDF(propertiesTree);
//		
//		for(File512 file512 : file512List) {
//		
//		
//			fileTxtGuida = new File("C:\\work\\Pagonet\\ConfigFiles\\pagopaPDFws\\pagopaPdf","nome"+".txt");
//			GuidaDocumento guidaDocumento = new GuidaDocumento(fileTxtGuida);
//			file = new File("C:\\work\\Pagonet\\ConfigFiles\\pagopaPDFws\\pagopaPdf", "file" + ".pdf");
//			
//			String passwordJppa = propertiesTree.getProperty(PropKeys.passwordJppa.format(file512.cutecute));
//			String userJppa = propertiesTree.getProperty(PropKeys.utenteJppa.format(file512.cutecute));
//			String urlPrinter = propertiesTree.getProperty(PropKeys.urlprinter.format(file512.cutecute));
//			
//			Flusso flusso = ConvertiFile512FlussoGeos.convertiFlusso(file512, propertiesTree);
//	
//			file.getParentFile().mkdirs();
//			String nomeFileOrigine = file512.getFileName();
//			System.out.println("nomeFileOrigine = " + nomeFileOrigine);
//			System.out.println("file512.tipoTemplate = " + file512.tipoTemplate);
//
//			SalvaPDFMultiThread multiPdf = 
//					new SalvaPDFMultiThread(file,guidaDocumento,propertiesTree,
//							file512,flusso);
//			
//			multiPdf.initilizeMassivePrint();
//			
//			multiPdf.stampaMassivoJppa("C:\\work\\Pagonet\\ConfigFiles\\pagopaPDFws\\pagopaPdf"
//					, file);
//			
//			
//			multiPdf.computePrint();
//		
//		}
//		
//		
//		
//		
//		
//		
//	}
//
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//

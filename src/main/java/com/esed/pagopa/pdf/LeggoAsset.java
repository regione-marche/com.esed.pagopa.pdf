package com.esed.pagopa.pdf;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.esed.pagopa.pdf.config.PropKeys;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.svg.converter.SvgConverter;
import com.seda.commons.properties.tree.PropertiesTree;


public class LeggoAsset {
	
	public PdfDocument pdf;
	
//	CONFIG
	public static String DIRECTORY_SALVATAGGIO_FILE = "pdfExample/";

	private final PropertiesTree propertiesTree;
	private final ATipoStampa tipoImpaginazione;
	
//	LOGO CUSTOM
	//valori per la configurazione e dimensionamento del logo custom
	private final String pathLogoEnteConf;
	private final int pathLogoEnteSizeX;
	private final int pathLogoEnteSizeY;
	
//  LOGHI
	private static final String CANALI_DIGITALI = "/avviso-pagopa-assets/canali-digitali.svg";
	private static final String CANALI_FISICI = "/avviso-pagopa-assets/canali-fisici.svg";
	private static final String LOGO_BANCOPOSTA = "/avviso-pagopa-assets/logo-bancoposta.svg";
	private static final String LOGO_BOLLETTINO_POSTALE = "/avviso-pagopa-assets/logo-bollettino-postale.svg";
	private static final String LOGO_EURO_BOLLETTINO = "/avviso-pagopa-assets/logo-euro-bollettino.svg";
	private static final String LOGO_PAGOPA = "/avviso-pagopa-assets/logo-pagopa.svg";
	private static final String LOGO_POSTE_ITALIANE = "/avviso-pagopa-assets/logo-poste-italiane.svg";
	private static final String SCRITTA_AVVISO_DI_PAGAMENTO = "/avviso-pagopa-assets/scritta-avviso-di-pagamento.svg";
	private static final String LOGO_FORBICI = "/avviso-pagopa-assets/frobbici.svg";
	private static final String DATA_MATRIX_CONTAINER = "/avviso-pagopa-assets/data-matrix-container.svg";
	private static final String PATH_LOGO_ENTE = "/avviso-pagopa-assets/bianco.png";
	//inizio LP PG210070
	private static final String LOGO_PAGOPA_BLU = "/avviso-pagopa-assets/logo-pagopa-blu.svg";
	private static final String LOGO_BOLZANO = "/avviso-pagopa-assets/logo-bolzano.png";
	private static final String LOGO_NUMEROVERDE_BOLZANO = "/avviso-pagopa-assets/logo-numeroverde-bolzano.png";
	private static final String LOGO_FORBICI_GRIGIO = "/avviso-pagopa-assets/forbici-grigio.svg";
	private static final String LOGO_BANCOPOSTA_GRIGIO = "/avviso-pagopa-assets/logo-bancoposta-grigio.svg";
	public boolean bLogoEnteExtern = false;
	//fine LP PG210070

//  IMMAGINI
	// Creating an ImageData object
	// String imageFile = "./avviso-pagopa-assets/logo-pagopa@3x.png";
	// ImageData data = ImageDataFactory.create(imageFile);

	// Creating an Image object
	// Image img = new Image(data);

// STRINGHE 
//	public String infoEnte = "";
	
	public static final String BOLLETTINO_POSTALE_PA = "BOLLETTINO POSTALE PA";
	public static final String SUL_CC = "sul C/C n.";
	public static final String BOLLETTINO_POSTALE_DESCRIZIONE= "Bollettino Postale pagabile in tutti gli Uffici Postali e sui canali fisici o digitali abilitati di Poste italiane e dell'Ente Creditore";
	public static final String INTESTATO_A = "Intestato a";
	public static final String OGGETTO_PAGAMENTO = "Oggetto pagamento";
	public static final String TIPO = "Tipo";
	public static final String P1 = "P1";
	
	public String diPoste;
	public String delTuoEnte;
	
	
//	RETURN
	private Image canali_digitali;
	private Image canali_fisici;
	private Image logo_bancoposta;
	private Image logo_bollettino_postale;
	private Image logo_euro_bollettino;
	private Image logo_pagopa;
	private Image logo_poste_italiane;
	private Image scritta_avviso_di_pagamento;
	private Image logo_forbici;
	private Image logo_ente;
	private Image qr_code;
	private Image data_matrix_container;
	private Image data_matrix;
	//inizio LP PG210070
	private Image logo_pagopa_blu;
	private Image logo_bolzano;
	private Image logo_numeroverde_bolzano;
	private Image logo_forbici_grigio;
	private Image logo_bancoposta_grigio;
	//fine LP PG210070

//	COLORI
	public static final DeviceRgb grigioBollettino = new DeviceRgb(234, 234, 234);
	public static final DeviceRgb grigioForbici = new DeviceRgb(134, 134, 134);
	public static final DeviceRgb grigioRate = new DeviceRgb(248, 248, 248);
	public static final DeviceRgb grigioFooter = new DeviceRgb(177, 177, 177);
	//inizio LP PG210070
	//#0066CC 0,102,204
	//#848484 132,132,132
	public static final DeviceRgb bolzanoBluBollettino = new DeviceRgb(0,102,203);
	public static final DeviceRgb bolzanoGrigioForbici = new DeviceRgb(134, 134, 134);
	public static final DeviceRgb bolzanoRigaPerForbici = new DeviceRgb(0, 0, 0);
	public static final DeviceRgb bolzanoBluRate = new DeviceRgb(0,102,203);
	//fine LP PG210070
	
	public enum FormatoStampa {
		GEOS,
		PAGOPAPDF
	}
	
	private FormatoStampa formatoStampa = FormatoStampa.PAGOPAPDF;

	private String tipoStampaBancaPoste;

	private ATipoStampa getTipoImpaginazione(String cuteCute) {
		
		ATipoStampa tipoImpaginazione = null;
		
		if (this.propertiesTree != null) {
			String sTipoStampa = this.propertiesTree.getProperty(PropKeys.tipoStampa.format(cuteCute)).toUpperCase();
			System.out.println("tipo stampa da properties: " + sTipoStampa);
			switch(sTipoStampa) {
			case "PAGOPAPDF":
				tipoImpaginazione = new TipoStampaPagoPa();
				break;
			case "GEOS":
				tipoImpaginazione = new TipoStampaGeos();
				formatoStampa = FormatoStampa.GEOS;
				break;
			}

		}
		
		if (tipoImpaginazione == null)
			tipoImpaginazione = new TipoStampaPagoPa();
		
		return tipoImpaginazione;
	}

	private String getPathLogoEnte(String cuteCute) {
		
		String pathLogoEnte = null;
		if (this.propertiesTree != null) {
			pathLogoEnte = this.propertiesTree.getProperty(PropKeys.pathLogoEnte.format(cuteCute));
		}
		return pathLogoEnte;
	}
	
	private int getPathLogoEnteSizeX (String cuteCute) {
		
		int pathLogoEnteSizeX = 84;
		if (this.propertiesTree != null) {
			try {
				pathLogoEnteSizeX = Integer.parseInt(this.propertiesTree.getProperty(PropKeys.pathLogoEnteSizeX.format(cuteCute)));
			} catch(NumberFormatException e) { }
		}
		return pathLogoEnteSizeX;
	}
	
	private int getPathLogoEnteSizeY (String cuteCute) {
		
		int pathLogoEnteSizeY = 84;
		if (this.propertiesTree != null) {
			try {
				pathLogoEnteSizeY = Integer.parseInt(this.propertiesTree.getProperty(PropKeys.pathLogoEnteSizeY.format(cuteCute)));
			} catch(NumberFormatException e) { }
		}
		return pathLogoEnteSizeY;
	}
	
	public LeggoAsset(PdfDocument pdf, String tipoStampa, String pathLogoEnte) throws IOException {
		this(pdf, tipoStampa, pathLogoEnte, null, null);
	}
	public LeggoAsset(PdfDocument pdf, String tipoStampa, String pathLogoEnte, PropertiesTree propertiesTree, String cuteCute) throws IOException {
		super();
		this.tipoStampaBancaPoste = tipoStampa; 
		this.propertiesTree = propertiesTree;
		this.tipoImpaginazione = getTipoImpaginazione(cuteCute);
		this.pathLogoEnteConf = getPathLogoEnte(cuteCute); 
		this.pathLogoEnteSizeX = getPathLogoEnteSizeX(cuteCute);
		this.pathLogoEnteSizeY = getPathLogoEnteSizeY(cuteCute);
		
		InputStream canali_digitali_svg = this.getClass().getResourceAsStream(CANALI_DIGITALI);
		canali_digitali = SvgConverter.convertToImage(canali_digitali_svg, pdf);

		InputStream canali_fisici_svg = this.getClass().getResourceAsStream(CANALI_FISICI);
		canali_fisici = SvgConverter.convertToImage(canali_fisici_svg, pdf);

		InputStream logo_bancoposta_svg =this.getClass().getResourceAsStream(LOGO_BANCOPOSTA);
		logo_bancoposta = SvgConverter.convertToImage(logo_bancoposta_svg, pdf);

		InputStream logo_bollettino_postale_svg = this.getClass().getResourceAsStream(LOGO_BOLLETTINO_POSTALE);
		logo_bollettino_postale = SvgConverter.convertToImage(logo_bollettino_postale_svg, pdf);

		InputStream logo_euro_bollettino_svg = this.getClass().getResourceAsStream(LOGO_EURO_BOLLETTINO);
		logo_euro_bollettino = SvgConverter.convertToImage(logo_euro_bollettino_svg, pdf);

		InputStream logo_pagopa_svg = this.getClass().getResourceAsStream(LOGO_PAGOPA);
		logo_pagopa = SvgConverter.convertToImage(logo_pagopa_svg, pdf);

		InputStream logo_poste_italiane_svg =this.getClass().getResourceAsStream(LOGO_POSTE_ITALIANE);
		logo_poste_italiane = SvgConverter.convertToImage(logo_poste_italiane_svg, pdf);

		InputStream scritta_avviso_di_pagamento_svg = this.getClass().getResourceAsStream(SCRITTA_AVVISO_DI_PAGAMENTO);
		scritta_avviso_di_pagamento = SvgConverter.convertToImage(scritta_avviso_di_pagamento_svg, pdf);
		
		InputStream logo_forbici_svg = this.getClass().getResourceAsStream(LOGO_FORBICI);
		logo_forbici = SvgConverter.convertToImage(logo_forbici_svg, pdf);

		//inizio LP PG210070
		InputStream logo_pagopa_blu_svg = this.getClass().getResourceAsStream(LOGO_PAGOPA_BLU);
		logo_pagopa_blu = SvgConverter.convertToImage(logo_pagopa_blu_svg, pdf);

		logo_bolzano = convertLogo(LOGO_BOLZANO);
		
		logo_numeroverde_bolzano = convertLogo(LOGO_NUMEROVERDE_BOLZANO);
		
		InputStream logo_forbici_grigio_svg = this.getClass().getResourceAsStream(LOGO_FORBICI_GRIGIO);
		logo_forbici_grigio = SvgConverter.convertToImage(logo_forbici_grigio_svg, pdf);

		InputStream logo_bancoposta_grigio_svg = this.getClass().getResourceAsStream(LOGO_BANCOPOSTA_GRIGIO);
		logo_bancoposta_grigio = SvgConverter.convertToImage(logo_bancoposta_grigio_svg, pdf);
		//fine LP PG210070
		
		InputStream logo_ente_input_stream = null;
		String formatoLogoEnte = null;
		if (pathLogoEnte != null && pathLogoEnte.length() > 4) {
			logo_ente_input_stream = new FileInputStream(pathLogoEnte);
			formatoLogoEnte = pathLogoEnte.substring(pathLogoEnte.length()-3, pathLogoEnte.length());
			//inizio LP PG210070
			bLogoEnteExtern = true;
			//fine LP PG210070
		} else if (pathLogoEnteConf != null && pathLogoEnteConf.length() > 4){
			logo_ente_input_stream = new FileInputStream(pathLogoEnteConf);
			formatoLogoEnte = pathLogoEnteConf.substring(pathLogoEnteConf.length()-3, pathLogoEnteConf.length());
			//inizio LP PG210070
			bLogoEnteExtern = true;
			//fine LP PG210070
		} else {
			logo_ente_input_stream = this.getClass().getResourceAsStream(PATH_LOGO_ENTE);
			formatoLogoEnte = PATH_LOGO_ENTE.substring(PATH_LOGO_ENTE.length()-3, PATH_LOGO_ENTE.length());
		}
		
		switch (formatoLogoEnte) {
		case "svg":
			logo_ente = SvgConverter.convertToImage(logo_ente_input_stream, pdf);
			break;
		case "jpg":
		case "peg":
		case "gif":
		case "tif":
		case "png":
			byte[] logoEnteInByte = IOUtils.toByteArray(logo_ente_input_stream);
			ImageData logoEnteImgData = ImageDataFactory.create(logoEnteInByte);
			logo_ente = new Image(logoEnteImgData);
			break;
		}
		
		InputStream data_matrix_container_svg = this.getClass().getResourceAsStream(DATA_MATRIX_CONTAINER);
		data_matrix_container = SvgConverter.convertToImage(data_matrix_container_svg, pdf);

		
		diPoste = this.tipoStampaBancaPoste.equalsIgnoreCase("P") ? " di Poste Italiane," : "";
		
//		delTuoEnte = true ? " del tuo Ente Creditore," : "";
		//inizio LP PG210070
		//delTuoEnte = " del tuo Ente Creditore,";
		delTuoEnte = "del tuo Ente Creditore,";
		//fine LP PG210070
	}

	public Image getCanali_digitali() {
		return canali_digitali;
	}

	public Image getCanali_fisici() {
		return canali_fisici;
	}

	public Image getLogo_bancoposta() {
		return logo_bancoposta;
	}

	public Image getLogo_bollettino_postale() {
		return logo_bollettino_postale;
	}

	public Image getLogo_euro_bollettino() {
		return logo_euro_bollettino;
	}

	public Image getLogo_pagopa() {
		return logo_pagopa;
	}

	public Image getLogo_poste_italiane() {
		return logo_poste_italiane;
	}

	public Image getScritta_avviso_di_pagamentoe() {
		return scritta_avviso_di_pagamento;
	}
	
	public Image getLogo_forbici() {
		return logo_forbici;
	}

	public Image getLogo_ente() {
		return logo_ente;
	}

	public Image getQr_code() {
		return qr_code;
	}

	public Image getData_matrix_container() {
		return data_matrix_container;
	}
	
	public Image getData_matrix() {
		return data_matrix;
	}
	
	public PdfFont getRoboto_bold() {
		return this.tipoImpaginazione.getRoboto_bold();
	}

	public PdfFont getRoboto_regular() {
		return this.tipoImpaginazione.getRoboto_regular();
	}

	public PdfFont getTitillium_black() {
		return this.tipoImpaginazione.getTitillium_black();
	}

	public PdfFont getTitillium_bold() {
		return this.tipoImpaginazione.getTitillium_bold();
	}

	public PdfFont getTitillium_regular() {
		return this.tipoImpaginazione.getTitillium_regular();
	}
		
	//inizio LP PG210070
	public Image getLogo_pagopa_blu() {
		return logo_pagopa_blu;
	}

	public Image getLogo_bolzano() {
		return logo_bolzano;
	}

	public Image getLogo_numeroverde_bolzano() {
		return logo_numeroverde_bolzano;
	}

	public Image getLogo_forbici_grigio() {
		return logo_forbici_grigio;
	}

	public Image getLogo_bancoposta_grigio() {
		return logo_bancoposta_grigio;
	}
	
	public int getXoffSet() {
		return this.tipoImpaginazione.getxOffSet();
	}
	
	public int getYoffSet() {
		return this.tipoImpaginazione.getyOffSet();
	}

	
	private Image convertLogo(String pathLogo) throws IOException {
		InputStream logo_input_stream = null;
		Image logo = null;
		String formatoLogo = null;
		if (pathLogo != null && pathLogo.length() > 4) {
			//logo_input_stream = new FileInputStream(pathLogo);
			logo_input_stream = this.getClass().getResourceAsStream(pathLogo);
			formatoLogo = pathLogo.substring(pathLogo.length()-3, pathLogo.length());
		
			switch (formatoLogo) {
				case "svg":
					logo = SvgConverter.convertToImage(logo_input_stream, pdf);
					break;
				case "jpg":
				case "peg":
				case "gif":
				case "tif":
				case "png":
					byte[] logoInByte = IOUtils.toByteArray(logo_input_stream);
					ImageData logoImgData = ImageDataFactory.create(logoInByte);
					logo = new Image(logoImgData);
					break;
			}
		}
		return logo;
	}
	//fine LP PG210070
	
	public int getPathLogoEnteSizeX() {
		return pathLogoEnteSizeX;
	}

	public int getPathLogoEnteSizeY() {
		return pathLogoEnteSizeY;
	}
	
	public PdfFont getTitoloFont() {
		return this.tipoImpaginazione.getTitoloFont();
	}

	public PdfFont getTestataFont() {
		return this.tipoImpaginazione.getTestataFont();
	}

	public PdfFont getInEvidenza1Font() {
		return this.tipoImpaginazione.getInEvidenza1Font();
	}

	public PdfFont getInEvidenza2Font() {
		return this.tipoImpaginazione.getInEvidenza2Font();
	}

	public PdfFont getInEvidenza3Font() {
		return this.tipoImpaginazione.getInEvidenza3Font();
	}

	public PdfFont getDenominazioneNome1Font() {
		return this.tipoImpaginazione.getDenominazioneNome1Font();
	}

	public PdfFont getDenominazioneDettaglio1Font() {
		return this.tipoImpaginazione.getDenominazioneDettaglio1Font();
	}

	public PdfFont getImporto1Font() {
		return this.tipoImpaginazione.getImporto1Font();
	}

	public PdfFont getImporto2Font() {
		return this.tipoImpaginazione.getImporto2Font();
	}

	public PdfFont getValuta1Font() {
		return this.tipoImpaginazione.getValuta1Font();
	}

	public PdfFont getValuta2Font() {
		return this.tipoImpaginazione.getValuta2Font();
	}

	public PdfFont getLimiteFont() {
		return this.tipoImpaginazione.getLimiteFont();
	}

	public PdfFont getScadenza1Font() {
		return this.tipoImpaginazione.getScadenza1Font();
	}

	public PdfFont getIstruzioniRate1Font() {
		return this.tipoImpaginazione.getIstruzioniRate1Font();
	}

	public PdfFont getIstruzioniRate2Font() {
		return this.tipoImpaginazione.getIstruzioniRate2Font();
	}

	public PdfFont getInfoImportoFont() {
		return this.tipoImpaginazione.getInfoImportoFont();
	}

	public PdfFont getIstruzioniTitoloFont() {
		return this.tipoImpaginazione.getIstruzioniTitoloFont();
	}

	public PdfFont getIstruzioniTesto1Font() {
		return this.tipoImpaginazione.getIstruzioniTesto1Font();
	}

	public PdfFont getIstruzioniTesto2Font() {
		return this.tipoImpaginazione.getIstruzioniTesto2Font();
	}

	public PdfFont getInEvidenza4Font() {
		return this.tipoImpaginazione.getInEvidenza4Font();
	}

	public PdfFont getInEvidenza5Font() {
		return this.tipoImpaginazione.getInEvidenza5Font();
	}

	public PdfFont getInfoCodiciFont() {
		return this.tipoImpaginazione.getInfoCodiciFont();
	}

	public PdfFont getInfoCodiciBoldFont() {
		return this.tipoImpaginazione.getInfoCodiciBoldFont();
	}

	public PdfFont getCodiceBoldFont() {
		return this.tipoImpaginazione.getCodiceBoldFont();
	}

	public PdfFont getAutorizzazioneFont() {
		return this.tipoImpaginazione.getAutorizzazioneFont();
	}

	public PdfFont getSulCcFont() {
		return this.tipoImpaginazione.getSulCcFont();
	}
	
	public PdfFont getEtichettaDenominazioneFont() {
		return this.tipoImpaginazione.getEtichettaDenominazioneFont();
	}

	public PdfFont getDenominazioneNome2Font() {
		return this.tipoImpaginazione.getDenominazioneNome2Font();
	}

	public PdfFont getInfoBollettinoFont() {
		return this.tipoImpaginazione.getInfoBollettinoFont();
	}

	public int getTitoloSize() {
		return this.tipoImpaginazione.getTitoloSize();
	}

	public int getTestataSize() {
		return this.tipoImpaginazione.getTestataSize();
	}

	public int getInEvidenza1Size() {
		return this.tipoImpaginazione.getInEvidenza1Size();
	}

	public int getInEvidenza2Size() {
		return this.tipoImpaginazione.getInEvidenza2Size();
	}

	public int getInEvidenza3Size() {
		return this.tipoImpaginazione.getInEvidenza3Size();
	}

	public int getDenominazioneNome1Size() {
		return this.tipoImpaginazione.getDenominazioneNome1Size();
	}

	public int getDenominazioneDettaglio1Size() {
		return this.tipoImpaginazione.getDenominazioneDettaglio1Size();
	}

	public int getImporto1Size() {
		return this.tipoImpaginazione.getImporto1Size();
	}

	public int getImporto2Size() {
		return this.tipoImpaginazione.getImporto2Size();
	}

	public int getValuta1Size() {
		return this.tipoImpaginazione.getValuta1Size();
	}

	public int getValuta2Size() {
		return this.tipoImpaginazione.getValuta2Size();
	}

	public int getLimiteSize() {
		return this.tipoImpaginazione.getLimiteSize();
	}

	public int getScadenza1Size() {
		return this.tipoImpaginazione.getScadenza1Size();
	}

	public int getIstruzioniRate1Size() {
		return this.tipoImpaginazione.getIstruzioniRate1Size();
	}

	public int getIstruzioniRate2Size() {
		return this.tipoImpaginazione.getIstruzioniRate2Size();
	}

	public int getInfoImportoSize() {
		return this.tipoImpaginazione.getInfoImportoSize();
	}

	public int getIstruzioniTitoloSize() {
		return this.tipoImpaginazione.getIstruzioniTitoloSize();
	}

	public int getIstruzioniTesto1Size() {
		return this.tipoImpaginazione.getIstruzioniTesto1Size();
	}

	public int getIstruzioniTesto2Size() {
		return this.tipoImpaginazione.getIstruzioniTesto2Size();
	}

	public int getInEvidenza4Size() {
		return this.tipoImpaginazione.getInEvidenza4Size();
	}

	public int getInEvidenza5Size() {
		return this.tipoImpaginazione.getInEvidenza5Size();
	}

	public int getInfoCodiciSize() {
		return this.tipoImpaginazione.getInfoCodiciSize();
	}

	public int getInfoCodiciBoldSize() {
		return this.tipoImpaginazione.getInfoCodiciBoldSize();
	}

	public int getCodiceBoldSize() {
		return this.tipoImpaginazione.getCodiceBoldSize();
	}

	public int getAutorizzazioneSize() {
		return this.tipoImpaginazione.getAutorizzazioneSize();
	}

	public int getSulCcSize() {
		return this.tipoImpaginazione.getSulCcSize();
	}

	public int getEtichettaDenominazioneSize() {
		return this.tipoImpaginazione.getEtichettaDenominazioneSize();
	}

	public int getDenominazioneNome2Size() {
		return this.tipoImpaginazione.getDenominazioneNome2Size();
	}

	public int getInfoBollettinoSize() {
		return this.tipoImpaginazione.getInfoBollettinoSize();
	}

	public PdfFont getNumRataFont() {
		return this.tipoImpaginazione.getNumRataFont();
	}

	public int getNumRataSize() {
		return this.tipoImpaginazione.getNumRataSize();
	}
	
	public PdfFont getEntroRateFont() {
		return this.tipoImpaginazione.getEntroRateFont();
	}

	public int getEntroRateSize() {
		return this.tipoImpaginazione.getEntroRateSize();
	}
	
	public TextAlignment getAllineamentoImporto() {
		return this.tipoImpaginazione.getAllineamentoImporto();
	}

	public FormatoStampa getFormatoStampa() {
		return formatoStampa;
	}

	public String getTipoStampaBancaPoste() {
		return tipoStampaBancaPoste;
	}
}
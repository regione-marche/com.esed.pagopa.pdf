package com.esed.pagopa.pdf;

import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.property.TextAlignment;

public class TipoStampaGeos extends ATipoStampa {
	
	public TipoStampaGeos() {
		
		boolean embedded = true;
		boolean subset = true;
		
		try {
			this.roboto_bold = PdfFontFactory.createFont(IOUtils.toByteArray(this.getClass().getResourceAsStream("/fonts/Roboto_Mono/static/RobotoMono-Bold.ttf")), embedded);
			this.roboto_bold.setSubset(subset);
			
			this.roboto_regular = PdfFontFactory.createFont(IOUtils.toByteArray(this.getClass().getResourceAsStream("/fonts/Roboto_Mono/static/RobotoMono-Regular.ttf")), embedded);
			this.roboto_regular.setSubset(subset);

			this.titillium_black = PdfFontFactory.createFont(IOUtils.toByteArray(this.getClass().getResourceAsStream("/fonts/Titillium_Web/TitilliumWeb-Black.ttf")), embedded);
			this.titillium_black.setSubset(subset);
			
			this.titilliumo_bold = PdfFontFactory.createFont(IOUtils.toByteArray(this.getClass().getResourceAsStream("/fonts//Titillium_Web/TitilliumWeb-Bold.ttf")), embedded);
			this.titilliumo_bold.setSubset(subset);
			
			this.titillium_regular = PdfFontFactory.createFont(IOUtils.toByteArray(this.getClass().getResourceAsStream("/fonts/Titillium_Web/TitilliumWeb-Regular.ttf")), embedded);
			this.titillium_regular.setSubset(subset);

			this.source_sans_pro_black = PdfFontFactory.createFont(IOUtils.toByteArray(this.getClass().getResourceAsStream("/fonts/Source_Sans_Pro/SourceSansPro-Black.ttf")), embedded);
			this.source_sans_pro_black.setSubset(subset);
	
			this.dejavu_sans = PdfFontFactory.createFont(IOUtils.toByteArray(this.getClass().getResourceAsStream("/fonts/DejaVu_Sans/DejaVuSans.ttf")), embedded);
			this.dejavu_sans.setSubset(subset);
			
			
			this.titoloFont = this.source_sans_pro_black;
			this.testataFont = this.roboto_bold;
			this.inEvidenza1Font = this.source_sans_pro_black;
			this.inEvidenza2Font = this.dejavu_sans;
			this.inEvidenza3Font = this.roboto_bold;
			this.inEvidenza4Font = this.source_sans_pro_black;
			this.inEvidenza5Font = this.roboto_bold;
			this.denominazioneNome1Font = this.roboto_bold;
			this.denominazioneDettaglio1Font = this.roboto_bold;
			this.importo1Font = this.roboto_bold;
			this.importo2Font = this.roboto_bold;
			this.valuta1Font = this.source_sans_pro_black;
			this.valuta2Font = this.dejavu_sans;
			this.limiteFont = this.source_sans_pro_black;
			this.scadenza1Font = this.roboto_bold;
			this.istruzioniRate1Font = this.dejavu_sans;
			this.istruzioniRate2Font = this.source_sans_pro_black;
			this.infoImportoFont = this.dejavu_sans;
			this.istruzioniTitoloFont = this.source_sans_pro_black;
			this.istruzioniTesto1Font = this.dejavu_sans;
			this.istruzioniTesto2Font = this.dejavu_sans;
			this.infoCodiciFont = this.dejavu_sans;
			this.infoCodiciBoldFont = this.source_sans_pro_black;
			this.codiceBoldFont = this.roboto_bold;
			this.infoBollettinoFont = this.dejavu_sans;
			this.etichettaDenominazioneFont = this.dejavu_sans;
			this.denominazioneNome2Font = this.roboto_bold;
			this.autorizzazioneFont = this.roboto_bold;
			this.sulCcFont = this.dejavu_sans;
			this.numRataFont = this.roboto_bold;
			this.entroRateFont = this.source_sans_pro_black;
			
			this.titoloSize = 10;
			this.testataSize = 14;
			this.inEvidenza1Size = 9;
			this.inEvidenza2Size = 7;
			this.inEvidenza3Size = 8;
			this.inEvidenza4Size = 9;
			this.inEvidenza5Size = 10;
			this.denominazioneNome1Size = 10;
			this.denominazioneDettaglio1Size = 10;
			this.importo1Size = 12;
			this.importo2Size = 10;
			this.valuta1Size = 16;
			this.valuta2Size = 10;
			this.limiteSize = 8;
			this.scadenza1Size = 12;
			this.istruzioniRate1Size = 7;
			this.istruzioniRate2Size = 7;
			this.infoImportoSize = 7;
			this.istruzioniTitoloSize = 9;
			this.istruzioniTesto1Size = 7;
			this.istruzioniTesto2Size = 10;
			this.infoCodiciSize = 7;
			this.infoCodiciBoldSize = 9;
			this.codiceBoldSize = 10;
			this.infoBollettinoSize = 7;
			this.etichettaDenominazioneSize = 7;
			this.denominazioneNome2Size = 7;
			this.infoBollettinoSize = 7;
			this.autorizzazioneSize = 5;
			this.sulCcSize = 9;
			this.numRataSize = 9;
			this.entroRateSize = 10;
			
			this.xOffSet = 5;
			this.yOffSet = 2;
			
			this.allineamentoImporto = TextAlignment.RIGHT;
		
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}

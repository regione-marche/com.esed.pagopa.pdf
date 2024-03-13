package com.esed.pagopa.pdf;

import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.property.TextAlignment;
import com.seda.commons.logger.CustomLoggerManager;
import com.seda.commons.logger.LoggerWrapper;

public class TipoStampaPagoPa extends ATipoStampa {
	
	protected static LoggerWrapper logger = CustomLoggerManager.get(TipoStampaPagoPa.class);
	
	public TipoStampaPagoPa() {
		
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
			
			this.titoloFont = this.titilliumo_bold;
			this.testataFont = this.titilliumo_bold;
			this.inEvidenza1Font = this.titilliumo_bold;
			this.inEvidenza2Font = this.titillium_regular;
			this.inEvidenza3Font = this.roboto_regular;
			this.denominazioneNome1Font = this.titilliumo_bold;
			this.denominazioneDettaglio1Font = this.titillium_regular;
			this.importo1Font = this.titilliumo_bold;
			this.importo2Font = this.titilliumo_bold;
			this.valuta1Font = this.titilliumo_bold;
			this.valuta2Font = this.titillium_regular;
			this.limiteFont = this.titilliumo_bold;
			this.scadenza1Font = this.titilliumo_bold;
			this.istruzioniRate1Font = this.titillium_regular;
			this.istruzioniRate2Font = this.titilliumo_bold;
			this.infoImportoFont = this.titillium_regular;
			this.istruzioniTitoloFont = this.titilliumo_bold;
			this.istruzioniTesto1Font = this.titillium_regular;
			this.istruzioniTesto2Font = this.titillium_regular;
			this.inEvidenza4Font = this.titillium_black;
			this.inEvidenza5Font = this.titillium_black;
			this.infoCodiciFont = this.titillium_regular;
			this.infoCodiciBoldFont = this.titilliumo_bold;
			this.codiceBoldFont = this.roboto_bold;
			this.infoBollettinoFont = this.titillium_regular;
			this.etichettaDenominazioneFont = this.titillium_regular;
			this.denominazioneNome2Font = this.titilliumo_bold;
			this.autorizzazioneFont = this.titillium_regular;
			this.sulCcFont = this.titillium_regular;
			this.numRataFont = this.titillium_black;
			this.entroRateFont = this.titillium_regular;
			
			this.titoloSize = 10;
			this.testataSize = 16;
			this.inEvidenza1Size = 10;
			this.inEvidenza2Size = 8;
			this.inEvidenza3Size = 8;
			this.denominazioneNome1Size = 12;
			this.denominazioneDettaglio1Size = 12;
			this.importo1Size = 15;
			this.importo2Size = 12;
			this.valuta1Size = 14;
			this.valuta2Size = 10;
			this.limiteSize = 8;
			this.scadenza1Size = 15;
			this.istruzioniRate1Size = 8;
			this.istruzioniRate2Size = 8;
			this.infoImportoSize = 8;
			this.istruzioniTitoloSize = 10;
			this.istruzioniTesto1Size = 8;
			this.istruzioniTesto2Size = 11;
			this.inEvidenza4Size = 10;
			this.inEvidenza5Size = 11;
			this.infoCodiciSize = 9;
			this.infoCodiciBoldSize = 9;
			this.codiceBoldSize = 10;
			this.infoBollettinoSize = 8;
			this.etichettaDenominazioneSize = 8;
			this.denominazioneNome2Size = 8;
			this.infoBollettinoSize = 8;
			this.autorizzazioneSize = 6;
			this.sulCcSize = 10;
			this.numRataSize = 10;
			
			
			this.xOffSet = 0;
			this.yOffSet = 0;
			
			this.allineamentoImporto = TextAlignment.LEFT;
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}

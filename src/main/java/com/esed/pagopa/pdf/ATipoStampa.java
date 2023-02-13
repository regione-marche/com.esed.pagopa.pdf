package com.esed.pagopa.pdf;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.layout.property.TextAlignment;

public abstract class ATipoStampa {
	
	protected PdfFont roboto_bold;
	protected PdfFont roboto_regular;
	protected PdfFont titillium_black;
	protected PdfFont titilliumo_bold;
	protected PdfFont titillium_regular;
	protected PdfFont source_sans_pro_black;
	protected PdfFont dejavu_sans;
	
	protected PdfFont titoloFont;
	protected PdfFont testataFont;
	protected PdfFont inEvidenza1Font;
	protected PdfFont inEvidenza2Font;
	protected PdfFont inEvidenza3Font;
	protected PdfFont denominazioneNome1Font;
	protected PdfFont denominazioneDettaglio1Font;
	protected PdfFont importo1Font;
	protected PdfFont importo2Font;
	protected PdfFont valuta1Font;
	protected PdfFont valuta2Font;
	protected PdfFont limiteFont;
	protected PdfFont scadenza1Font;
	protected PdfFont istruzioniRate1Font;
	protected PdfFont istruzioniRate2Font;
	protected PdfFont infoImportoFont;
	protected PdfFont istruzioniTitoloFont;
	protected PdfFont istruzioniTesto1Font;
	protected PdfFont istruzioniTesto2Font;
	protected PdfFont inEvidenza4Font;
	protected PdfFont inEvidenza5Font;
	protected PdfFont infoCodiciFont;
	protected PdfFont infoCodiciBoldFont;
	protected PdfFont etichettaDenominazioneFont;
	protected PdfFont denominazioneNome2Font;
	protected PdfFont codiceBoldFont;
	protected PdfFont infoBollettinoFont;
	protected PdfFont autorizzazioneFont;
	protected PdfFont sulCcFont;
	protected PdfFont numRataFont;
	protected PdfFont entroRateFont;
	
	protected int titoloSize;
	protected int testataSize;
	protected int inEvidenza1Size;
	protected int inEvidenza2Size;
	protected int inEvidenza3Size;
	protected int denominazioneNome1Size;
	protected int denominazioneDettaglio1Size;
	protected int importo1Size;
	protected int importo2Size;
	protected int valuta1Size;
	protected int valuta2Size;
	protected int limiteSize;
	protected int scadenza1Size;
	protected int istruzioniRate1Size;
	protected int istruzioniRate2Size;
	protected int infoImportoSize;
	protected int istruzioniTitoloSize;
	protected int istruzioniTesto1Size;
	protected int istruzioniTesto2Size;
	protected int inEvidenza4Size;
	protected int inEvidenza5Size;
	protected int infoCodiciSize;
	protected int infoCodiciBoldSize;
	protected int etichettaDenominazioneSize;
	protected int denominazioneNome2Size;
	protected int codiceBoldSize;
	protected int infoBollettinoSize;
	protected int autorizzazioneSize;
	protected int sulCcSize;
	protected int numRataSize;
	protected int entroRateSize;
	
	
	protected int xOffSet;
	protected int yOffSet;
	
	protected TextAlignment allineamentoImporto;
	
	
	public PdfFont getRoboto_bold() {
		
		return this.roboto_bold;
	}
	
	public PdfFont getRoboto_regular() {
		
		return this.roboto_regular;
	}

	public PdfFont getTitillium_black() {
		
		return this.titillium_black;
	}

	public PdfFont getTitillium_bold() {
		
		return this.titilliumo_bold;
	}

	public PdfFont getTitillium_regular() {
		
		return this.titillium_regular;
	}

	public int getxOffSet() {
		
		return this.xOffSet;
	}

	public int getyOffSet() {
		
		return this.yOffSet;
	}

	public PdfFont getTitilliumo_bold() {
		return titilliumo_bold;
	}

	public PdfFont getTitoloFont() {
		return titoloFont;
	}

	public PdfFont getTestataFont() {
		return testataFont;
	}

	public PdfFont getInEvidenza1Font() {
		return inEvidenza1Font;
	}

	public PdfFont getInEvidenza2Font() {
		return inEvidenza2Font;
	}

	public PdfFont getInEvidenza3Font() {
		return inEvidenza3Font;
	}

	public PdfFont getDenominazioneNome1Font() {
		return denominazioneNome1Font;
	}

	public PdfFont getDenominazioneDettaglio1Font() {
		return denominazioneDettaglio1Font;
	}

	public PdfFont getImporto1Font() {
		return importo1Font;
	}

	public PdfFont getImporto2Font() {
		return importo2Font;
	}

	public PdfFont getValuta1Font() {
		return valuta1Font;
	}

	public PdfFont getValuta2Font() {
		return valuta2Font;
	}

	public PdfFont getLimiteFont() {
		return limiteFont;
	}

	public PdfFont getScadenza1Font() {
		return scadenza1Font;
	}

	public PdfFont getIstruzioniRate1Font() {
		return istruzioniRate1Font;
	}

	public PdfFont getIstruzioniRate2Font() {
		return istruzioniRate2Font;
	}

	public PdfFont getInfoImportoFont() {
		return infoImportoFont;
	}

	public PdfFont getIstruzioniTitoloFont() {
		return istruzioniTitoloFont;
	}

	public PdfFont getIstruzioniTesto1Font() {
		return istruzioniTesto1Font;
	}

	public PdfFont getIstruzioniTesto2Font() {
		return istruzioniTesto2Font;
	}

	public PdfFont getInEvidenza4Font() {
		return inEvidenza4Font;
	}

	public PdfFont getInEvidenza5Font() {
		return inEvidenza5Font;
	}

	public PdfFont getInfoCodiciFont() {
		return infoCodiciFont;
	}

	public PdfFont getInfoCodiciBoldFont() {
		return infoCodiciBoldFont;
	}

	public PdfFont getCodiceBoldFont() {
		return codiceBoldFont;
	}

	public PdfFont getAutorizzazioneFont() {
		return autorizzazioneFont;
	}

	public PdfFont getSulCcFont() {
		return sulCcFont;
	}
	
	public PdfFont getEtichettaDenominazioneFont() {
		return etichettaDenominazioneFont;
	}

	public PdfFont getDenominazioneNome2Font() {
		return denominazioneNome2Font;
	}

	public PdfFont getInfoBollettinoFont() {
		return infoBollettinoFont;
	}

	public int getTitoloSize() {
		return titoloSize;
	}

	public int getTestataSize() {
		return testataSize;
	}

	public int getInEvidenza1Size() {
		return inEvidenza1Size;
	}

	public int getInEvidenza2Size() {
		return inEvidenza2Size;
	}

	public int getInEvidenza3Size() {
		return inEvidenza3Size;
	}

	public int getDenominazioneNome1Size() {
		return denominazioneNome1Size;
	}

	public int getDenominazioneDettaglio1Size() {
		return denominazioneDettaglio1Size;
	}

	public int getImporto1Size() {
		return importo1Size;
	}

	public int getImporto2Size() {
		return importo2Size;
	}

	public int getValuta1Size() {
		return valuta1Size;
	}

	public int getValuta2Size() {
		return valuta2Size;
	}

	public int getLimiteSize() {
		return limiteSize;
	}

	public int getScadenza1Size() {
		return scadenza1Size;
	}

	public int getIstruzioniRate1Size() {
		return istruzioniRate1Size;
	}

	public int getIstruzioniRate2Size() {
		return istruzioniRate2Size;
	}

	public int getInfoImportoSize() {
		return infoImportoSize;
	}

	public int getIstruzioniTitoloSize() {
		return istruzioniTitoloSize;
	}

	public int getIstruzioniTesto1Size() {
		return istruzioniTesto1Size;
	}

	public int getIstruzioniTesto2Size() {
		return istruzioniTesto2Size;
	}

	public int getInEvidenza4Size() {
		return inEvidenza4Size;
	}

	public int getInEvidenza5Size() {
		return inEvidenza5Size;
	}

	public int getInfoCodiciSize() {
		return infoCodiciSize;
	}

	public int getInfoCodiciBoldSize() {
		return infoCodiciBoldSize;
	}

	public int getCodiceBoldSize() {
		return codiceBoldSize;
	}

	public int getAutorizzazioneSize() {
		return autorizzazioneSize;
	}

	public int getSulCcSize() {
		return sulCcSize;
	}

	public int getEtichettaDenominazioneSize() {
		return etichettaDenominazioneSize;
	}

	public int getDenominazioneNome2Size() {
		return denominazioneNome2Size;
	}

	public int getInfoBollettinoSize() {
		return infoBollettinoSize;
	}

	public PdfFont getNumRataFont() {
		return numRataFont;
	}

	public int getNumRataSize() {
		return numRataSize;
	}

	public PdfFont getSource_sans_pro_black() {
		return source_sans_pro_black;
	}

	public PdfFont getEntroRateFont() {
		return entroRateFont;
	}

	public int getEntroRateSize() {
		return entroRateSize;
	}

	public TextAlignment getAllineamentoImporto() {
		return allineamentoImporto;
	}
	
	
	
}

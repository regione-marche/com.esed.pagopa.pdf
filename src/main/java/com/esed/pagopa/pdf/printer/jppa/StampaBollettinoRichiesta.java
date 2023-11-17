package com.esed.pagopa.pdf.printer.jppa;

import java.io.Serializable;
import java.util.List;

import it.maggioli.pagopa.jppa.printer.model.DatiEnteAvvisaturaDto;
import it.maggioli.pagopa.jppa.printer.model.PosizioneDebitoriaAvvisaturaDto;

public class StampaBollettinoRichiesta implements Serializable {

    private static final long serialVersionUID = 7242741915094874502L;
    private final com.seda.payer.commons.jppa.DatiEnteAvvisaturaDto datiEnte;

    private final List<com.seda.payer.commons.jppa.PosizioneDebitoriaAvvisaturaDto> posizioneDebitoria;

    private String numeroAvviso;

    private String testoInformativaCanaliDigitali; //Non piu in uso

    private String locale;


    private final String base64FileLogoEnte;

    public StampaBollettinoRichiesta(com.seda.payer.commons.jppa.DatiEnteAvvisaturaDto datiEnte, List<com.seda.payer.commons.jppa.PosizioneDebitoriaAvvisaturaDto> posizioneDebitoria, String locale, String base64FileLogoEnte) {
        this.datiEnte = datiEnte;
        this.posizioneDebitoria = posizioneDebitoria;
        this.locale = locale;
        this.base64FileLogoEnte = base64FileLogoEnte;
    }

    public com.seda.payer.commons.jppa.DatiEnteAvvisaturaDto getDatiEnte() {
        return datiEnte;
    }

    public List<com.seda.payer.commons.jppa.PosizioneDebitoriaAvvisaturaDto> getPosizioneDebitoria() {
        return posizioneDebitoria;
    }

    public String getNumeroAvviso() {
        return numeroAvviso;
    }

    public String getBase64FileLogoEnte() {
        return base64FileLogoEnte;
    }

    public String getTestoInformativaCanaliDigitali() {
        return testoInformativaCanaliDigitali;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

}
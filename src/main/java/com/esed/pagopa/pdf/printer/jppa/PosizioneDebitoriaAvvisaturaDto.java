package com.esed.pagopa.pdf.printer.jppa;

import java.io.Serializable;
import java.util.Date;

public class PosizioneDebitoriaAvvisaturaDto implements Serializable {

    private static final long serialVersionUID = -1344667146079269313L;


    private String numeroAvviso;


    private Float importo;


    private Date dataScadenza;

    private String causaleDebitoriaDe;

    private String causaleDebitoria;

    private TIPO_SOGGETTO titDebitoTipoSoggetto;

    private String titDebitoNazioneSedeLegale;

    private String titDebitoProvSedeLegale;

    private String titDebitoComuneSedeLegale;

    private String titDebitoCapSedeLegale;

    private String titDebitoIndirizzoSedeLegale;

    private String titDebitoCivicoSedeLegale;

    private String titDebitoRagioneSociale;


    private String titDebitonazioneRes;


    private String titDebitoprovRes;


    private String titDebitoComuneRes;


    private String titDebitoCapRes;


    private String titDebitoIndirizzoRes;

    
    private String titDebitoCivicoRes;

 
    private String titDebitoNome;

   
    private String titDebitoCognome;

   
    private String titDebitoNominativo;

   
    private String titDebitoCf;

    private String codiceTipoDebito;

    public PosizioneDebitoriaAvvisaturaDto() {
        super();
    }

    public String getNumeroAvviso() {
        return numeroAvviso;
    }

    public void setNumeroAvviso(String numeroAvviso) {
        this.numeroAvviso = numeroAvviso;
    }

    public String getCausaleDebitoriaDe() {
        return causaleDebitoriaDe;
    }

    public void setCausaleDebitoriaDe(String causaleDebitoriaDe) {
        this.causaleDebitoriaDe = causaleDebitoriaDe;
    }

    public String getCausaleDebitoria() {
        return causaleDebitoria;
    }

    public void setCausaleDebitoria(String causaleDebitoria) {
        this.causaleDebitoria = causaleDebitoria;
    }

    public TIPO_SOGGETTO getTitDebitoTipoSoggetto() {
        return titDebitoTipoSoggetto;
    }

    public void setTitDebitoTipoSoggetto(TIPO_SOGGETTO titDebitoTipoSoggetto) {
        this.titDebitoTipoSoggetto = titDebitoTipoSoggetto;
    }

    public String getTitDebitoNazioneSedeLegale() {
        return titDebitoNazioneSedeLegale;
    }

    public void setTitDebitoNazioneSedeLegale(String titDebitoNazioneSedeLegale) {
        this.titDebitoNazioneSedeLegale = titDebitoNazioneSedeLegale;
    }

    public String getTitDebitoProvSedeLegale() {
        return titDebitoProvSedeLegale;
    }

    public void setTitDebitoProvSedeLegale(String titDebitoProvSedeLegale) {
        this.titDebitoProvSedeLegale = titDebitoProvSedeLegale;
    }

    public String getTitDebitoComuneSedeLegale() {
        return titDebitoComuneSedeLegale;
    }

    public void setTitDebitoComuneSedeLegale(String titDebitoComuneSedeLegale) {
        this.titDebitoComuneSedeLegale = titDebitoComuneSedeLegale;
    }

    public String getTitDebitoCapSedeLegale() {
        return titDebitoCapSedeLegale;
    }

    public void setTitDebitoCapSedeLegale(String titDebitoCapSedeLegale) {
        this.titDebitoCapSedeLegale = titDebitoCapSedeLegale;
    }

    public String getTitDebitoIndirizzoSedeLegale() {
        return titDebitoIndirizzoSedeLegale;
    }

    public void setTitDebitoIndirizzoSedeLegale(String titDebitoIndirizzoSedeLegale) {
        this.titDebitoIndirizzoSedeLegale = titDebitoIndirizzoSedeLegale;
    }

    public String getTitDebitoCivicoSedeLegale() {
        return titDebitoCivicoSedeLegale;
    }

    public void setTitDebitoCivicoSedeLegale(String titDebitoCivicoSedeLegale) {
        this.titDebitoCivicoSedeLegale = titDebitoCivicoSedeLegale;
    }

    public String getTitDebitoRagioneSociale() {
        return titDebitoRagioneSociale;
    }

    public void setTitDebitoRagioneSociale(String titDebitoRagioneSociale) {
        this.titDebitoRagioneSociale = titDebitoRagioneSociale;
    }

    public String getTitDebitonazioneRes() {
        return titDebitonazioneRes;
    }

    public void setTitDebitonazioneRes(String titDebitonazioneRes) {
        this.titDebitonazioneRes = titDebitonazioneRes;
    }

    public String getTitDebitoprovRes() {
        return titDebitoprovRes;
    }

    public void setTitDebitoprovRes(String titDebitoprovRes) {
        this.titDebitoprovRes = titDebitoprovRes;
    }

    public String getTitDebitoComuneRes() {
        return titDebitoComuneRes;
    }

    public void setTitDebitoComuneRes(String titDebitoComuneRes) {
        this.titDebitoComuneRes = titDebitoComuneRes;
    }

    public String getTitDebitoCapRes() {
        return titDebitoCapRes;
    }

    public void setTitDebitoCapRes(String titDebitoCapRes) {
        this.titDebitoCapRes = titDebitoCapRes;
    }

    public String getTitDebitoIndirizzoRes() {
        return titDebitoIndirizzoRes;
    }

    public void setTitDebitoIndirizzoRes(String titDebitoIndirizzoRes) {
        this.titDebitoIndirizzoRes = titDebitoIndirizzoRes;
    }

    public String getTitDebitoCivicoRes() {
        return titDebitoCivicoRes;
    }

    public void setTitDebitoCivicoRes(String titDebitoCivicoRes) {
        this.titDebitoCivicoRes = titDebitoCivicoRes;
    }

    public String getTitDebitoNome() {
        return titDebitoNome;
    }

    public void setTitDebitoNome(String titDebitoNome) {
        this.titDebitoNome = titDebitoNome;
    }

    public String getTitDebitoCognome() {
        return titDebitoCognome;
    }

    public void setTitDebitoCognome(String titDebitoCognome) {
        this.titDebitoCognome = titDebitoCognome;
    }

    public String getTitDebitoCf() {
        return titDebitoCf;
    }

    public void setTitDebitoCf(String titDebitoCf) {
        this.titDebitoCf = titDebitoCf;
    }

    public Float getImporto() {
        return importo;
    }

    public void setImporto(Float importo) {
        this.importo = importo;
    }

    public Date getDataScadenza() {
        return dataScadenza;
    }

    public void setDataScadenza(Date dataScadenza) {
        this.dataScadenza = dataScadenza;
    }

    public enum TIPO_SOGGETTO {
        PERSIONE_FISICA("F"),
        PERSIONE_GIURIDICA("G"),
        ;

        private final String value;

        TIPO_SOGGETTO(String v) {
            value = v;
        }

        @Override
        public String toString() {
            return value;
        }

        public String getName() {
            return value;
        }
    }

    public String getTitDebitoNominativo() {
        return titDebitoNominativo;
    }

    public void setTitDebitoNominativo(String titDebitoNominativo) {
        this.titDebitoNominativo = titDebitoNominativo;
    }

    public String getCodiceTipoDebito() {
        return codiceTipoDebito;
    }

    public void setCodiceTipoDebito(String codiceTipoDebito) {
        this.codiceTipoDebito = codiceTipoDebito;
    }
}

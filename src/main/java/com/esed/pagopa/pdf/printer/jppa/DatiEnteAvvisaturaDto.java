package com.esed.pagopa.pdf.printer.jppa;

import java.io.Serializable;

public class DatiEnteAvvisaturaDto implements Serializable {
    private static final long serialVersionUID = -4264511160019323677L;


    private String codiceFiscale;

    private String codiceInterbancario;


    private String nomeDe;


    private String nome;


    private String settore;


    private String informazioni;


    private boolean cpAbilitato;


    private String cpIntestatarioDe;


    private String cpIntestatario;


    private String cpNumero;

    private String cpAutorizzazione;

    public DatiEnteAvvisaturaDto(String codiceFiscale,
            String codiceInterbancario,
            String nome,
            String settore,
            String informazioni,
            boolean cpAbilitato,
            String cpIntestatario,
            String cpNumero,
            String cpAutorizzazione) {
        super();
        this.codiceFiscale = codiceFiscale;
        this.codiceInterbancario = codiceInterbancario;
        this.nome = nome;
        this.settore = settore;
        this.informazioni = informazioni;
        this.cpAbilitato = cpAbilitato;
        this.cpIntestatario = cpIntestatario;
        this.cpNumero = cpNumero;
        this.cpAutorizzazione = cpAutorizzazione;
    }

    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    public void setCodiceFiscale(String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
    }

    public String getCodiceInterbancario() {
        return codiceInterbancario;
    }

    public void setCodiceInterbancario(String codiceInterbancario) {
        this.codiceInterbancario = codiceInterbancario;
    }

    public String getNomeDe() {
        return nomeDe;
    }

    public void setNomeDe(String nomeDe) {
        this.nomeDe = nomeDe;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSettore() {
        return settore;
    }

    public void setSettore(String settore) {
        this.settore = settore;
    }

    public String getInformazioni() {
        return informazioni;
    }

    public void setInformazioni(String informazioni) {
        this.informazioni = informazioni;
    }

    public boolean isCpAbilitato() {
        return cpAbilitato;
    }

    public void setCpAbilitato(boolean cpAbilitato) {
        this.cpAbilitato = cpAbilitato;
    }

    public String getCpIntestatarioDe() {
        return cpIntestatarioDe;
    }

    public void setCpIntestatarioDe(String cpIntestatarioDe) {
        this.cpIntestatarioDe = cpIntestatarioDe;
    }

    public String getCpIntestatario() {
        return cpIntestatario;
    }

    public void setCpIntestatario(String cpIntestatario) {
        this.cpIntestatario = cpIntestatario;
    }

    public String getCpNumero() {
        return cpNumero;
    }

    public void setCpNumero(String cpNumero) {
        this.cpNumero = cpNumero;
    }

    public String getCpAutorizzazione() {
        return cpAutorizzazione;
    }

    public void setCpAutorizzazione(String cpAutorizzazione) {
        this.cpAutorizzazione = cpAutorizzazione;
    }
}
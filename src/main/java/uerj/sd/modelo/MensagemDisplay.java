package uerj.sd.modelo;

import com.google.gson.Gson;

/**
 * Mensagem publicada no cluster JGroups para atualização dos módulos Display.
 * Formato especificado na seção 4.3 do SPEC.md.
 */
public class MensagemDisplay {
    private static final Gson gson = new Gson();

    public static final String EVENTO_JOGADA = "JOGADA";
    public static final String EVENTO_INICIO = "INICIO";
    public static final String EVENTO_FIM = "FIM";

    private String evento;
    private int jogadorId;
    private int numeroJogada;
    private int coluna;
    private int[] tabuleiro;
    private String resultado;

    public MensagemDisplay() {
    }

    public MensagemDisplay(String evento, int jogadorId, int numeroJogada, int coluna, int[] tabuleiro, String resultado) {
        this.evento = evento;
        this.jogadorId = jogadorId;
        this.numeroJogada = numeroJogada;
        this.coluna = coluna;
        this.tabuleiro = tabuleiro;
        this.resultado = resultado;
    }

    public String getEvento() {
        return evento;
    }

    public void setEvento(String evento) {
        this.evento = evento;
    }

    public int getJogadorId() {
        return jogadorId;
    }

    public void setJogadorId(int jogadorId) {
        this.jogadorId = jogadorId;
    }

    public int getNumeroJogada() {
        return numeroJogada;
    }

    public void setNumeroJogada(int numeroJogada) {
        this.numeroJogada = numeroJogada;
    }

    public int getColuna() {
        return coluna;
    }

    public void setColuna(int coluna) {
        this.coluna = coluna;
    }

    public int[] getTabuleiro() {
        return tabuleiro;
    }

    public void setTabuleiro(int[] tabuleiro) {
        this.tabuleiro = tabuleiro;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String paraJson() {
        return gson.toJson(this);
    }

    public static MensagemDisplay deJson(String json) {
        return gson.fromJson(json, MensagemDisplay.class);
    }
}

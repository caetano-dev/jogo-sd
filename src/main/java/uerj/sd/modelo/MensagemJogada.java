package uerj.sd.modelo;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * Representa a mensagem de jogada transmitida entre jogadores via Socket TCP.
 * Formato especificado na seção 4.2 do SPEC.md.
 */
public class MensagemJogada {
    private static final Gson gson = new Gson();

    public static final String RESULTADO_JOGANDO = "jog";
    public static final String RESULTADO_VITORIA = "vit";
    public static final String RESULTADO_DERROTA = "der";
    public static final String RESULTADO_EMPATE  = "emp";

    @SerializedName("Numero")
    private int numero;

    @SerializedName("Jogada")
    private int jogada;

    @SerializedName("Tabuleiro")
    private int[] tabuleiro;

    @SerializedName("Resultado")
    private String resultado;

    public MensagemJogada() {
    }

    public MensagemJogada(int numero, int jogada, int[] tabuleiro, String resultado) {
        this.numero = numero;
        this.jogada = jogada;
        this.tabuleiro = tabuleiro;
        this.resultado = resultado;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public int getJogada() {
        return jogada;
    }

    public void setJogada(int jogada) {
        this.jogada = jogada;
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

    public static MensagemJogada deJson(String json) {
        return gson.fromJson(json, MensagemJogada.class);
    }
}

package uerj.sd.modelo;

import com.google.gson.Gson;

public record MensagemJogada(int Numero, int Jogada, int[] Tabuleiro, String Resultado) {
    private static final Gson gson = new Gson();

    public static final String RESULTADO_JOGANDO = "jog";
    public static final String RESULTADO_VITORIA = "vit";
    public static final String RESULTADO_DERROTA = "der";
    public static final String RESULTADO_EMPATE  = "emp";

    public int getNumero() { return Numero; }
    public int getJogada() { return Jogada; }
    public int[] getTabuleiro() { return Tabuleiro; }
    public String getResultado() { return Resultado; }

    public String paraJson() { return gson.toJson(this); }
    public static MensagemJogada deJson(String json) { return gson.fromJson(json, MensagemJogada.class); }
}

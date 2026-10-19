package uerj.sd.modelo;

import com.google.gson.Gson;

/**
 * Payload JSON enviado pelo jogador ao invocar getOponent no servidor de match.
 */
public class RequisicaoMatch {
    private static final Gson gson = new Gson();

    private String ip;
    private int porta;
    private String protocolo;

    public RequisicaoMatch() {
    }

    public RequisicaoMatch(String ip, int porta, String protocolo) {
        this.ip = ip;
        this.porta = porta;
        this.protocolo = protocolo;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPorta() {
        return porta;
    }

    public void setPorta(int porta) {
        this.porta = porta;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

    public String paraJson() {
        return gson.toJson(this);
    }

    public static RequisicaoMatch deJson(String json) {
        return gson.fromJson(json, RequisicaoMatch.class);
    }
}

package uerj.sd.modelo;

import com.google.gson.Gson;

/**
 * Payload JSON retornado pelo servidor de match em getOponent.
 */
public class RespostaMatch {
    private static final Gson gson = new Gson();

    private String ip;
    private Integer porta;
    private Boolean inicia;
    private String erro;

    public RespostaMatch() {
    }

    public RespostaMatch(String ip, int porta, boolean inicia) {
        this.ip = ip;
        this.porta = porta;
        this.inicia = inicia;
    }

    public static RespostaMatch erro(String mensagemErro) {
        RespostaMatch r = new RespostaMatch();
        r.erro = mensagemErro;
        return r;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPorta() {
        return porta;
    }

    public void setPorta(Integer porta) {
        this.porta = porta;
    }

    public Boolean getInicia() {
        return inicia;
    }

    public void setInicia(Boolean inicia) {
        this.inicia = inicia;
    }

    public String getErro() {
        return erro;
    }

    public void setErro(String erro) {
        this.erro = erro;
    }

    public boolean temErro() {
        return erro != null && !erro.isEmpty();
    }

    public String paraJson() {
        return gson.toJson(this);
    }

    public static RespostaMatch deJson(String json) {
        return gson.fromJson(json, RespostaMatch.class);
    }
}

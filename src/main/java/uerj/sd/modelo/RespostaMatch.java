package uerj.sd.modelo;

import com.google.gson.Gson;

public record RespostaMatch(String ip, Integer porta, Boolean inicia, String erro) {
    private static final Gson gson = new Gson();

    public RespostaMatch(String ip, int porta, boolean inicia) {
        this(ip, porta, inicia, null);
    }

    public static RespostaMatch erro(String mensagemErro) {
        return new RespostaMatch(null, null, null, mensagemErro);
    }

    public String getIp() { return ip; }
    public Integer getPorta() { return porta; }
    public Boolean getInicia() { return inicia; }
    public String getErro() { return erro; }
    public boolean temErro() { return erro != null && !erro.isEmpty(); }

    public String paraJson() { return gson.toJson(this); }
    public static RespostaMatch deJson(String json) { return gson.fromJson(json, RespostaMatch.class); }
}

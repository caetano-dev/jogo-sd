package uerj.sd.modelo;

import com.google.gson.Gson;

/**
 * Payload JSON enviado pelo jogador ao invocar getOponent no servidor de match.
 */
public record RequisicaoMatch(String ip, int porta, String protocolo) {
    private static final Gson gson = new Gson();

    public String getIp() { return ip; }
    public int getPorta() { return porta; }
    public String getProtocolo() { return protocolo; }

    public String paraJson() { return gson.toJson(this); }
    public static RequisicaoMatch deJson(String json) { return gson.fromJson(json, RequisicaoMatch.class); }
}

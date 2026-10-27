package uerj.sd.modelo;

import com.google.gson.Gson;

public record RequisicaoMatch(String ip, int porta, String protocolo) {
    private static final Gson gson = new Gson();

    public String paraJson() { return gson.toJson(this); }
    public static RequisicaoMatch deJson(String json) { return gson.fromJson(json, RequisicaoMatch.class); }
}

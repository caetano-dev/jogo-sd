package uerj.sd.servidor;

import uerj.sd.modelo.RequisicaoMatch;
import uerj.sd.modelo.RespostaMatch;
import uerj.sd.rmi.MatchServiceInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class ServicoMatch extends UnicastRemoteObject implements MatchServiceInterface {

    private final int timeoutSegundos;
    private final Map<String, RequisicaoMatch> fila = new HashMap<>();
    private final Map<String, RespostaMatch> respostas = new HashMap<>();

    public ServicoMatch() throws RemoteException {
        this(60);
    }

    public ServicoMatch(int timeoutSegundos) throws RemoteException {
        super();
        this.timeoutSegundos = timeoutSegundos;
    }

    @Override
    public synchronized String getOponent(String jsonIn) throws RemoteException {
        if (jsonIn == null || jsonIn.isBlank()) {
            return RespostaMatch.erro("Requisição vazia").paraJson();
        }
        RequisicaoMatch req;
        try {
            req = RequisicaoMatch.deJson(jsonIn);
        } catch (Exception e) {
            return RespostaMatch.erro("JSON inválido: " + e.getMessage()).paraJson();
        }

        if (req == null || req.ip() == null || req.protocolo() == null) {
            return RespostaMatch.erro("Campos obrigatórios ausentes").paraJson();
        }

        String protocolo = req.protocolo().trim().toLowerCase();
        String minhaChave = req.ip() + ":" + req.porta();

        RequisicaoMatch esperando = fila.get(protocolo);
        if (esperando != null && minhaChave.equals(esperando.ip() + ":" + esperando.porta())) {
            return RespostaMatch.erro("Já existe um jogador aguardando com o mesmo IP e porta").paraJson();
        }

        if (esperando != null) {
            RequisicaoMatch oponente = fila.remove(protocolo);
            String chaveOponente = oponente.ip() + ":" + oponente.porta();

            respostas.put(chaveOponente, new RespostaMatch(req.ip(), req.porta(), true));
            notifyAll();

            System.out.printf("[ServidorMatch] Par formado (%s): %s (inicia=true) vs %s (inicia=false)%n",
                    protocolo, chaveOponente, minhaChave);

            return new RespostaMatch(oponente.ip(), oponente.porta(), false).paraJson();
        }

        fila.put(protocolo, req);
        System.out.printf("[ServidorMatch] Jogador aguardando oponente (%s): %s%n", protocolo, minhaChave);

        long limite = System.currentTimeMillis() + (timeoutSegundos * 1000L);
        while (!respostas.containsKey(minhaChave)) {
            long restante = limite - System.currentTimeMillis();
            if (restante <= 0) break;
            try {
                wait(restante);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        if (respostas.containsKey(minhaChave)) {
            return respostas.remove(minhaChave).paraJson();
        }

        fila.remove(protocolo);
        System.out.printf("[ServidorMatch] Timeout esgotado para %s (%s)%n", minhaChave, protocolo);
        return RespostaMatch.erro("Tempo limite de pareamento esgotado (" + timeoutSegundos + "s)").paraJson();
    }
}

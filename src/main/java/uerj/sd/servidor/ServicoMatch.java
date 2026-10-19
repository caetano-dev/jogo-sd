package uerj.sd.servidor;

import uerj.sd.modelo.RequisicaoMatch;
import uerj.sd.modelo.RespostaMatch;
import uerj.sd.rmi.MatchServiceInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Implementação do serviço de pareamento (matchmaking) via Java RMI.
 */
public class ServicoMatch extends UnicastRemoteObject implements MatchServiceInterface {

    private static class JogadorEsperando {
        final RequisicaoMatch requisicao;
        final CompletableFuture<RespostaMatch> respostaFutura;

        JogadorEsperando(RequisicaoMatch requisicao, CompletableFuture<RespostaMatch> respostaFutura) {
            this.requisicao = requisicao;
            this.respostaFutura = respostaFutura;
        }
    }

    private final Map<String, JogadorEsperando> filaPorProtocolo = new HashMap<>();
    private final int timeoutSegundos;

    public ServicoMatch() throws RemoteException {
        this(60);
    }

    public ServicoMatch(int timeoutSegundos) throws RemoteException {
        super();
        this.timeoutSegundos = timeoutSegundos;
    }

    @Override
    public String getOponent(String jsonIn) throws RemoteException {
        if (jsonIn == null || jsonIn.isBlank()) {
            return RespostaMatch.erro("Requisição vazia ou inválida").paraJson();
        }

        RequisicaoMatch req;
        try {
            req = RequisicaoMatch.deJson(jsonIn);
        } catch (Exception e) {
            return RespostaMatch.erro("Erro ao processar JSON: " + e.getMessage()).paraJson();
        }

        if (req == null || req.getIp() == null || req.getProtocolo() == null) {
            return RespostaMatch.erro("Campos obrigatórios ausentes: ip, porta ou protocolo").paraJson();
        }

        String protocolo = req.getProtocolo().trim().toLowerCase();
        JogadorEsperando jogadorEsperando;

        synchronized (filaPorProtocolo) {
            jogadorEsperando = filaPorProtocolo.remove(protocolo);

            if (jogadorEsperando != null) {
                // Formou par!
                // Jogador A (que estava esperando na fila) recebe os dados de B com inicia: true
                RespostaMatch respA = new RespostaMatch(req.getIp(), req.getPorta(), true);
                jogadorEsperando.respostaFutura.complete(respA);

                // Jogador B (o atual) recebe os dados de A com inicia: false
                RespostaMatch respB = new RespostaMatch(
                        jogadorEsperando.requisicao.getIp(),
                        jogadorEsperando.requisicao.getPorta(),
                        false
                );
                System.out.printf("[ServidorMatch] Par formado (%s): %s:%d (inicia=true) vs %s:%d (inicia=false)%n",
                        protocolo,
                        jogadorEsperando.requisicao.getIp(), jogadorEsperando.requisicao.getPorta(),
                        req.getIp(), req.getPorta());

                return respB.paraJson();
            } else {
                // Não há ninguém esperando por esse protocolo: entra na fila
                CompletableFuture<RespostaMatch> futuro = new CompletableFuture<>();
                JogadorEsperando novoEsperando = new JogadorEsperando(req, futuro);
                filaPorProtocolo.put(protocolo, novoEsperando);
                jogadorEsperando = novoEsperando;
                System.out.printf("[ServidorMatch] Jogador aguardando oponente (%s): %s:%d%n",
                        protocolo, req.getIp(), req.getPorta());
            }
        }

        // Aguarda formação de par com timeout
        try {
            RespostaMatch resposta = jogadorEsperando.respostaFutura.get(timeoutSegundos, TimeUnit.SECONDS);
            return resposta.paraJson();
        } catch (TimeoutException e) {
            synchronized (filaPorProtocolo) {
                if (filaPorProtocolo.get(protocolo) == jogadorEsperando) {
                    filaPorProtocolo.remove(protocolo);
                }
            }
            System.out.printf("[ServidorMatch] Timeout esgotado para %s:%d (%s)%n",
                    req.getIp(), req.getPorta(), protocolo);
            return RespostaMatch.erro("Tempo limite de pareamento esgotado (" + timeoutSegundos + "s)").paraJson();
        } catch (Exception e) {
            return RespostaMatch.erro("Erro interno ao aguardar pareamento: " + e.getMessage()).paraJson();
        }
    }
}

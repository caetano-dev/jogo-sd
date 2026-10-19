package uerj.sd.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface RMI para o Servidor de Matchmaking (pareamento).
 * Conforme especificado na seção 4.1 do SPEC.md.
 */
public interface MatchServiceInterface extends Remote {

    /**
     * Registra o interesse de um jogador em participar de uma partida e bloqueia
     * até encontrar um oponente com o mesmo protocolo.
     *
     * @param jsonIn Payload JSON com {"ip": "...", "porta": int, "protocolo": "..."}
     * @return Payload JSON com {"ip": "...", "porta": int, "inicia": boolean} ou erro
     * @throws RemoteException Em caso de falha na chamada RMI remota
     */
    String getOponent(String jsonIn) throws RemoteException;
}

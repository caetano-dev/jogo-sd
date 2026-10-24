package uerj.sd.servidor;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Inicializador do Servidor de Matchmaking (RMI).
 */
public class ServidorMatch {
    public static final String NOME_SERVICO = "MatchService";
    public static final int PORTA_PADRAO = 9823;

    public static void iniciar(int porta) {
        try {
            // Se o hostname do servidor RMI não foi informado nas flags JVM, usa localhost
            // por padrão
            if (System.getProperty("java.rmi.server.hostname") == null) {
                System.setProperty("java.rmi.server.hostname", "[IP_ADDRESS]");
            }

            Registry registro;
            try {
                registro = LocateRegistry.createRegistry(porta);
                System.out.println("[ServidorMatch] RMIRegistry criado na porta " + porta);
            } catch (Exception e) {
                // Se já existir um registro rodando nessa porta, obtém a referência
                registro = LocateRegistry.getRegistry(porta);
                System.out.println("[ServidorMatch] Conectado ao RMIRegistry existente na porta " + porta);
            }

            ServicoMatch servico = new ServicoMatch();
            registro.rebind(NOME_SERVICO, servico);

            System.out.println("=================================================");
            System.out.println(" Servidor de Matchmaking (Jogo da Velha) Iniciado ");
            System.out.println(" Porta RMI: " + porta);
            System.out.println(" Nome do Serviço: " + NOME_SERVICO);
            System.out.println(" IP RMI Hostname: " + System.getProperty("java.rmi.server.hostname"));
            System.out.println(" Aguardando conexões de jogadores...");
            System.out.println("=================================================");

        } catch (Exception e) {
            System.err.println("[ServidorMatch] Erro fatal ao iniciar o servidor: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

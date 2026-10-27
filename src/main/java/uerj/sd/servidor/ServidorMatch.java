package uerj.sd.servidor;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServidorMatch {
    public static final String NOME_SERVICO = "MatchService";
    public static final int PORTA_PADRAO = 9823;

    public static void iniciar(int porta, String host) {
        try {
            if (System.getProperty("java.rmi.server.hostname") == null) {
                System.setProperty("java.rmi.server.hostname", host);
            }

            Registry registro;
            try {
                registro = LocateRegistry.createRegistry(porta);
                System.out.println("[ServidorMatch] RMIRegistry criado na porta " + porta);
            } catch (Exception e) {
                registro = LocateRegistry.getRegistry(porta);
                System.out.println("[ServidorMatch] Conectado ao RMIRegistry existente na porta " + porta);
            }

            ServicoMatch servico = new ServicoMatch();
            registro.rebind(NOME_SERVICO, servico);

            System.out.println("Servidor de matchmaking iniciado ");
            System.out.println("Porta RMI: " + porta);
            System.out.println("Nome do serviço: " + NOME_SERVICO);
            System.out.println("IP RMI hostname: " + System.getProperty("java.rmi.server.hostname"));
            System.out.println("Aguardando conexões...");

        } catch (Exception e) {
            System.err.println("[ServidorMatch] Erro ao iniciar o servidor: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

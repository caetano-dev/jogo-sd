package uerj.sd;

import uerj.sd.display.Display;
import uerj.sd.jogador.Jogador;
import uerj.sd.servidor.ServidorMatch;

/**
 * Ponto de entrada unificado para execução dos módulos do sistema:
 * Servidor de Matchmaking, Módulo Jogador e Módulo Display.
 */
public class Principal {

    public static void main(String[] args) {
        if (args.length == 0) {
            exibirAjuda();
            return;
        }

        String comando = args[0].toLowerCase().trim();

        switch (comando) {
            case "server":
            case "servidor":
                executarServidor(args);
                break;

            case "player":
            case "jogador":
                executarJogador(args);
                break;

            case "display":
                executarDisplay();
                break;

            case "--help":
            case "-h":
            case "help":
            case "ajuda":
            default:
                exibirAjuda();
                break;
        }
    }

    private static void executarServidor(String[] args) {
        int porta = ServidorMatch.PORTA_PADRAO;

        for (int i = 1; i < args.length; i++) {
            if ("--port".equalsIgnoreCase(args[i]) || "-p".equalsIgnoreCase(args[i])) {
                if (i + 1 < args.length) {
                    porta = Integer.parseInt(args[++i]);
                }
            }
        }

        ServidorMatch.iniciar(porta);
    }

    private static void executarJogador(String[] args) {
        String serverIp = "127.0.0.1";
        int serverPort = ServidorMatch.PORTA_PADRAO;
        String meuIp = "127.0.0.1";
        int minhaPorta = 9001;

        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if ("--server-ip".equalsIgnoreCase(arg) && i + 1 < args.length) {
                serverIp = args[++i];
            } else if ("--server-port".equalsIgnoreCase(arg) && i + 1 < args.length) {
                serverPort = Integer.parseInt(args[++i]);
            } else if (("--port".equalsIgnoreCase(arg) || "--listen-port".equalsIgnoreCase(arg) || "-p".equalsIgnoreCase(arg)) && i + 1 < args.length) {
                minhaPorta = Integer.parseInt(args[++i]);
            } else if ("--ip".equalsIgnoreCase(arg) && i + 1 < args.length) {
                meuIp = args[++i];
            }
        }

        Jogador jogador = new Jogador(serverIp, serverPort, meuIp, minhaPorta);
        jogador.iniciar();
    }

    private static void executarDisplay() {
        Display.iniciar();
    }

    private static void exibirAjuda() {
        System.out.println("===============================================================");
        System.out.println(" Connect Four Distribuído (RMI + Sockets P2P + JGroups)        ");
        System.out.println("===============================================================");
        System.out.println("Uso:");
        System.out.println("  java -jar app.jar server  [--port <porta>]");
        System.out.println("      Inicia o Servidor de Matchmaking RMI (porta padrão: 9800)");
        System.out.println();
        System.out.println("  java -jar app.jar player  [--server-ip <ip>] [--server-port <porta>] [--port <porta>] [--ip <ip>]");
        System.out.println("      Inicia o Módulo Jogador (busca oponente via RMI e conecta via P2P)");
        System.out.println("      Exemplo Jogador 1: java -jar app.jar player --port 9001");
        System.out.println("      Exemplo Jogador 2: java -jar app.jar player --port 9002");
        System.out.println();
        System.out.println("  java -jar app.jar display");
        System.out.println("      Inicia o Módulo Display (observador de partidas via JGroups)");
        System.out.println("===============================================================");
    }
}

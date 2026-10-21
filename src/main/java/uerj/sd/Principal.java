package uerj.sd;

import uerj.sd.display.Display;
import uerj.sd.jogador.Jogador;
import uerj.sd.servidor.ServidorMatch;

/**
 * Ponto de entrada unificado para execução dos módulos do sistema:
 * Servidor de Matchmaking (RMI), Módulo Jogador (P2P + Multicast) e Módulo Display (Multicast).
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
                executarDisplay(args);
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
            String arg = args[i];
            if (("--port".equalsIgnoreCase(arg) || "-p".equalsIgnoreCase(arg)) && i + 1 < args.length) {
                porta = Integer.parseInt(args[++i]);
            } else if ("--id".equalsIgnoreCase(arg) && i + 1 < args.length) {
                int id = Integer.parseInt(args[++i]);
                porta = 9800 + id;
            }
        }

        ServidorMatch.iniciar(porta);
    }

    private static void executarJogador(String[] args) {
        String serverIp = "127.0.0.1";
        int serverPort = ServidorMatch.PORTA_PADRAO;
        String meuIp = "127.0.0.1";
        int minhaPorta = 9001;
        String multicastIp = Display.IP_MULTICAST_PADRAO;
        int multicastPorta = Display.PORTA_MULTICAST_PADRAO;

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
            } else if ("--multicast-ip".equalsIgnoreCase(arg) && i + 1 < args.length) {
                multicastIp = args[++i];
            } else if ("--multicast-port".equalsIgnoreCase(arg) && i + 1 < args.length) {
                multicastPorta = Integer.parseInt(args[++i]);
            } else if ("--id".equalsIgnoreCase(arg) && i + 1 < args.length) {
                int id = Integer.parseInt(args[++i]);
                serverPort = 9800 + id;
                multicastIp = "225.1.1." + id;
                multicastPorta = 8800 + id;
            }
        }

        Jogador jogador = new Jogador(serverIp, serverPort, meuIp, minhaPorta, multicastIp, multicastPorta);
        jogador.iniciar();
    }

    private static void executarDisplay(String[] args) {
        String multicastIp = Display.IP_MULTICAST_PADRAO;
        int multicastPorta = Display.PORTA_MULTICAST_PADRAO;

        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if ("--multicast-ip".equalsIgnoreCase(arg) && i + 1 < args.length) {
                multicastIp = args[++i];
            } else if (("--multicast-port".equalsIgnoreCase(arg) || "--port".equalsIgnoreCase(arg) || "-p".equalsIgnoreCase(arg)) && i + 1 < args.length) {
                multicastPorta = Integer.parseInt(args[++i]);
            } else if ("--id".equalsIgnoreCase(arg) && i + 1 < args.length) {
                int id = Integer.parseInt(args[++i]);
                multicastIp = "225.1.1." + id;
                multicastPorta = 8800 + id;
            }
        }

        Display.iniciar(multicastIp, multicastPorta);
    }

    private static void exibirAjuda() {
        System.out.println("=========================================================================");
        System.out.println(" Sistema Distribuído de Turnos (RMI + Sockets P2P + Multicast Nativo)    ");
        System.out.println("=========================================================================");
        System.out.println("Uso:");
        System.out.println("  java -jar app.jar server  [--port <porta>] [--id <NN>]");
        System.out.println("      Inicia o Servidor de Matchmaking RMI (padrão: 9800 ou 98NN)");
        System.out.println();
        System.out.println("  java -jar app.jar player  [--server-ip <ip>] [--server-port <porta>] [--port <porta>] [--id <NN>]");
        System.out.println("      Inicia o Módulo Jogador (busca oponente via RMI e conecta via P2P)");
        System.out.println("      Exemplo Jogador 1: java -jar app.jar player --port 9001 --id 14");
        System.out.println("      Exemplo Jogador 2: java -jar app.jar player --port 9002 --id 14");
        System.out.println();
        System.out.println("  java -jar app.jar display [--multicast-ip <ip>] [--multicast-port <porta>] [--id <NN>]");
        System.out.println("      Inicia o Módulo Display (observador de partidas via Multicast UDP)");
        System.out.println("      Exemplo Display:   java -jar app.jar display --id 14");
        System.out.println("=========================================================================");
    }
}

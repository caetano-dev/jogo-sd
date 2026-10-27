package uerj.sd;

import uerj.sd.display.Display;
import uerj.sd.jogador.Jogador;
import uerj.sd.servidor.ServidorMatch;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            exibirAjuda();
            return;
        }

        String comando = args[0].toLowerCase();
        int id = getInt(args, -1, "--id");
        int defaultPorta = id != -1 ? 9800 + id : ServidorMatch.PORTA_PADRAO;
        String defaultMcastIp = id != -1 ? "225.1.1." + id : Display.IP_MULTICAST_PADRAO;
        int defaultMcastPort = id != -1 ? 8800 + id : Display.PORTA_MULTICAST_PADRAO;

        switch (comando) {
            case "server", "servidor" -> {
                int porta = getInt(args, defaultPorta, "--port", "-p");
                ServidorMatch.iniciar(porta, getStr(args, "127.0.0.1", "--host", "--ip"));
            }
            case "player", "jogador" -> {
                String serverIp = getStr(args, "127.0.0.1", "--server-ip");
                int serverPort = getInt(args, defaultPorta, "--server-port");
                String meuIp = getStr(args, "127.0.0.1", "--ip");
                int minhaPorta = getInt(args, 9001, "--port", "--listen-port", "-p");
                String mcastIp = getStr(args, defaultMcastIp, "--multicast-ip");
                int mcastPort = getInt(args, defaultMcastPort, "--multicast-port");

                new Jogador(serverIp, serverPort, meuIp, minhaPorta, mcastIp, mcastPort).iniciar();
            }
            case "display" -> {
                String mcastIp = getStr(args, defaultMcastIp, "--multicast-ip");
                int mcastPort = getInt(args, defaultMcastPort, "--multicast-port", "--port", "-p");
                Display.iniciar(mcastIp, mcastPort);
            }
            default -> exibirAjuda();
        }
    }

    private static String getStr(String[] args, String padrao, String... flags) {
        for (int i = 1; i < args.length - 1; i++) {
            for (String f : flags) {
                if (f.equalsIgnoreCase(args[i])) return args[i + 1];
            }
        }
        return padrao;
    }

    private static int getInt(String[] args, int padrao, String... flags) {
        String val = getStr(args, null, flags);
        return val != null ? Integer.parseInt(val) : padrao;
    }

    private static void exibirAjuda() {
        System.out.println("""
            Uso:
              java -jar app.jar server  [--host <ip>] [--port <porta>] [--id <NN>]
              java -jar app.jar player  [--server-ip <ip>] [--server-port <porta>] [--port <porta>] [--id <NN>]
              java -jar app.jar display [--multicast-ip <ip>] [--multicast-port <porta>] [--id <NN>]
            """);
    }
}

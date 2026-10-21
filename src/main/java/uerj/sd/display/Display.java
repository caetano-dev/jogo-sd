package uerj.sd.display;

import uerj.sd.jogo.Tabuleiro;
import uerj.sd.modelo.MensagemJogada;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.function.Consumer;

/**
 * Módulo Display: Transmissão e recepção multicast nativa conforme o PDF.
 * Grupo multicast padrão: 225.1.1.NN | Porta multicast: 88NN.
 */
public class Display {

    public static final String IP_MULTICAST_PADRAO = "225.1.1.1";
    public static final int PORTA_MULTICAST_PADRAO = 8801;

    /**
     * Envia uma jogada via Multicast UDP para o grupo de exibição.
     */
    public static void transmitirJogada(MensagemJogada msg, String ipMulticast, int portaMulticast) {
        if (msg == null) {
            return;
        }
        try (MulticastSocket socket = new MulticastSocket()) {
            NetworkInterface ni = obterInterfaceDeRede();
            if (ni != null) {
                socket.setNetworkInterface(ni);
            }
            socket.setTimeToLive(2);
            byte[] bytes = msg.paraJson().getBytes(StandardCharsets.UTF_8);
            DatagramPacket pacote = new DatagramPacket(bytes, bytes.length,
                    InetAddress.getByName(ipMulticast), portaMulticast);
            socket.send(pacote);
        } catch (Exception e) {
            System.err.println("[Display] Aviso: Falha ao transmitir via multicast: " + e.getMessage());
        }
    }

    /**
     * Inicia o receptor multicast para visualização de partidas via CLI.
     */
    public static void iniciar(String ipMulticast, int portaMulticast) {
        System.out.println("==================================================");
        System.out.println("   Módulo Display - Transmissão Multicast Nativa  ");
        System.out.printf("   Grupo: %s | Porta: %d%n", ipMulticast, portaMulticast);
        System.out.println("==================================================");

        ouvirMulticast(ipMulticast, portaMulticast, Display::exibirMensagem, true);
    }

    /**
     * Registra o listener no grupo multicast.
     *
     * @param callback Função acionada a cada jogada recebida
     * @param bloquear Se true, executa loop síncrono bloqueando a thread atual
     */
    public static MulticastSocket ouvirMulticast(String ipMulticast, int portaMulticast,
                                                Consumer<MensagemJogada> callback, boolean bloquear) {
        try {
            MulticastSocket socket = new MulticastSocket(portaMulticast);
            InetAddress grupo = InetAddress.getByName(ipMulticast);
            NetworkInterface ni = obterInterfaceDeRede();

            socket.joinGroup(new InetSocketAddress(grupo, portaMulticast), ni);

            Runnable loop = () -> {
                byte[] buffer = new byte[4096];
                System.out.println("[Display] Conectado ao grupo multicast. Aguardando jogadas...");
                while (!socket.isClosed()) {
                    try {
                        DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
                        socket.receive(pacote);
                        String json = new String(pacote.getData(), 0, pacote.getLength(), StandardCharsets.UTF_8).trim();
                        MensagemJogada msg = MensagemJogada.deJson(json);
                        if (msg != null && callback != null) {
                            callback.accept(msg);
                        }
                    } catch (SocketException se) {
                        break;
                    } catch (Exception e) {
                        System.err.println("[Display] Erro ao receber pacote: " + e.getMessage());
                    }
                }
            };

            if (bloquear) {
                loop.run();
            } else {
                Thread t = new Thread(loop, "Display-Multicast-Worker");
                t.setDaemon(true);
                t.start();
            }
            return socket;
        } catch (Exception e) {
            System.err.println("[Display] Erro ao inicializar socket multicast: " + e.getMessage());
            return null;
        }
    }

    public static void exibirMensagem(MensagemJogada msg) {
        if (msg == null) return;

        System.out.println("\n--------------------------------------------------");
        System.out.printf(">>> Transmissão Multicast | Jogada nº: %d | Coluna jogada: %d%n",
                msg.getNumero(), msg.getJogada());

        if (msg.getTabuleiro() != null && msg.getTabuleiro().length == Tabuleiro.TOTAL_CASAS) {
            Tabuleiro t = new Tabuleiro(msg.getTabuleiro());
            System.out.println(t.renderizarAscii());
        }

        String res = msg.getResultado();
        if (MensagemJogada.RESULTADO_VITORIA.equalsIgnoreCase(res)) {
            System.out.println("🏆 RESULTADO FINAL: VITÓRIA NA PARTIDA!");
        } else if (MensagemJogada.RESULTADO_DERROTA.equalsIgnoreCase(res)) {
            System.out.println("🏁 RESULTADO FINAL: DERROTA DECLARADA!");
        } else if (MensagemJogada.RESULTADO_EMPATE.equalsIgnoreCase(res)) {
            System.out.println("🤝 RESULTADO FINAL: PARTIDA EMPATADA!");
        } else {
            System.out.println("⏱ Estado: Partida em andamento...");
        }
        System.out.println("--------------------------------------------------");
    }

    public static NetworkInterface obterInterfaceDeRede() {
        try {
            NetworkInterface local = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            if (local != null && local.isUp() && local.supportsMulticast()) {
                return local;
            }
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface nif = interfaces.nextElement();
                if (nif.isUp() && nif.supportsMulticast() && !nif.isLoopback()) {
                    return nif;
                }
            }
            return NetworkInterface.getByInetAddress(InetAddress.getByName("127.0.0.1"));
        } catch (Exception e) {
            return null;
        }
    }
}

package uerj.sd.jogador;

import uerj.sd.display.Display;
import uerj.sd.jogo.Tabuleiro;
import uerj.sd.modelo.MensagemJogada;
import uerj.sd.modelo.RequisicaoMatch;
import uerj.sd.modelo.RespostaMatch;
import uerj.sd.rmi.MatchServiceInterface;
import uerj.sd.servidor.ServidorMatch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/**
 * Módulo Jogador: Pareamento via RMI, troca de jogadas via Socket TCP e transmissão multicast para os Displays.
 */
public class Jogador {

    private final String rmiIp;
    private final int rmiPorta;
    private final String meuIp;
    private final int minhaPorta;
    private final String multicastIp;
    private final int multicastPorta;

    public Jogador(String rmiIp, int rmiPorta, String meuIp, int minhaPorta) {
        this(rmiIp, rmiPorta, meuIp, minhaPorta, Display.IP_MULTICAST_PADRAO, Display.PORTA_MULTICAST_PADRAO);
    }

    public Jogador(String rmiIp, int rmiPorta, String meuIp, int minhaPorta, String multicastIp, int multicastPorta) {
        this.rmiIp = rmiIp;
        this.rmiPorta = rmiPorta;
        this.meuIp = meuIp;
        this.minhaPorta = minhaPorta;
        this.multicastIp = multicastIp;
        this.multicastPorta = multicastPorta;
    }

    public void iniciar() {
        iniciarComEntrada(new Scanner(System.in));
    }

    public void iniciarComEntrada(Scanner scanner) {
        Socket socketP2P = null;
        ServerSocket serverSocket = null;

        try {
            System.out.println("==================================================");
            System.out.println("           Conectando ao Connect Four             ");
            System.out.printf(" Servidor RMI: %s:%d%n", rmiIp, rmiPorta);
            System.out.printf(" Minha conexão P2P: %s:%d%n", meuIp, minhaPorta);
            System.out.printf(" Multicast Display: %s:%d%n", multicastIp, multicastPorta);
            System.out.println("==================================================");

            // 1. Pareamento via RMI
            System.out.println("[Jogador] Buscando oponente no Servidor de Match...");
            Registry registro = LocateRegistry.getRegistry(rmiIp, rmiPorta);
            MatchServiceInterface servicoMatch = (MatchServiceInterface) registro.lookup(ServidorMatch.NOME_SERVICO);

            RequisicaoMatch requisicao = new RequisicaoMatch(meuIp, minhaPorta, "tcp");
            String jsonResposta = servicoMatch.getOponent(requisicao.paraJson());

            RespostaMatch resposta = RespostaMatch.deJson(jsonResposta);
            if (resposta.temErro()) {
                System.err.println("[Jogador] Erro retornado pelo servidor: " + resposta.getErro());
                return;
            }

            String oponenteIp = resposta.getIp();
            int oponentePorta = resposta.getPorta();
            boolean souIniciante = Boolean.TRUE.equals(resposta.getInicia());

            int meuId = souIniciante ? Tabuleiro.JOGADOR_1 : Tabuleiro.JOGADOR_2;
            int oponenteId = souIniciante ? Tabuleiro.JOGADOR_2 : Tabuleiro.JOGADOR_1;
            char meuSimbolo = meuId == Tabuleiro.JOGADOR_1 ? 'X' : 'O';
            char oponenteSimbolo = oponenteId == Tabuleiro.JOGADOR_1 ? 'X' : 'O';

            System.out.println("[Jogador] Oponente encontrado!");
            System.out.printf("  -> IP do Oponente: %s:%d%n", oponenteIp, oponentePorta);
            System.out.printf("  -> Você é o Jogador %d (%c)%n", meuId, meuSimbolo);
            System.out.printf("  -> Você %s a partida.%n", souIniciante ? "INICIA" : "AGUARDA");

            // 2. Estabelecimento da Conexão Socket TCP P2P
            if (souIniciante) {
                System.out.println("[Jogador] Conectando ao socket do oponente...");
                for (int t = 0; t < 20 && socketP2P == null; t++) {
                    try {
                        socketP2P = new Socket(oponenteIp, oponentePorta);
                    } catch (Exception e) {
                        Thread.sleep(300);
                    }
                }
                if (socketP2P == null) {
                    System.err.println("[Jogador] Falha ao conectar ao oponente após tentativas.");
                    return;
                }
            } else {
                System.out.printf("[Jogador] Aguardando conexão do oponente na porta %d...%n", minhaPorta);
                serverSocket = new ServerSocket(minhaPorta);
                socketP2P = serverSocket.accept();
            }

            System.out.println("[Jogador] Conexão P2P estabelecida! Iniciando partida...");

            BufferedReader leitorSocket = new BufferedReader(
                    new InputStreamReader(socketP2P.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter escritorSocket = new PrintWriter(
                    new OutputStreamWriter(socketP2P.getOutputStream(), StandardCharsets.UTF_8), true);

            // 3. Loop de Turnos do Jogo
            Tabuleiro tabuleiro = new Tabuleiro();
            int numeroSequencia = 0;
            boolean meuTurno = souIniciante;

            while (true) {
                if (meuTurno) {
                    System.out.println(tabuleiro.renderizarAscii());
                    System.out.printf(">>> SUA VEZ (Jogador %d - [%c]) - Rodada nº %d <<<%n", meuId, meuSimbolo, numeroSequencia);

                    int coluna = lerColunaValida(scanner, tabuleiro);
                    if (coluna == -1) break;

                    tabuleiro.soltarPeca(coluna, meuId);

                    String resultado = tabuleiro.verificarVitoria(meuId) ? MensagemJogada.RESULTADO_VITORIA
                            : tabuleiro.estaCheio() ? MensagemJogada.RESULTADO_EMPATE
                            : MensagemJogada.RESULTADO_JOGANDO;

                    MensagemJogada msg = new MensagemJogada(numeroSequencia, coluna, tabuleiro.getGrade(), resultado);
                    escritorSocket.println(msg.paraJson());

                    // Transmissão Multicast para os Displays
                    Display.transmitirJogada(msg, multicastIp, multicastPorta);

                    if (MensagemJogada.RESULTADO_VITORIA.equals(resultado)) {
                        System.out.println(tabuleiro.renderizarAscii());
                        System.out.println("🎉 PARABÉNS! VOCÊ VENCEU O JOGO!");
                        break;
                    } else if (MensagemJogada.RESULTADO_EMPATE.equals(resultado)) {
                        System.out.println(tabuleiro.renderizarAscii());
                        System.out.println("🤝 O JOGO TERMINOU EM EMPATE!");
                        break;
                    }

                    numeroSequencia++;
                    meuTurno = false;
                    System.out.println("Jogada enviada! Aguardando oponente jogar...");

                } else {
                    String linha = leitorSocket.readLine();
                    if (linha == null) {
                        System.out.println("\n[Jogador] Conexão encerrada pelo oponente.");
                        break;
                    }

                    MensagemJogada msgRecebida;
                    try {
                        msgRecebida = MensagemJogada.deJson(linha);
                    } catch (Exception e) {
                        System.err.println("[Jogador] Mensagem corrompida: " + linha);
                        continue;
                    }

                    if (MensagemJogada.RESULTADO_DERROTA.equalsIgnoreCase(msgRecebida.getResultado())) {
                        System.out.println("\n[Jogador] O oponente aceitou a derrota. Fim de jogo!");
                        break;
                    }

                    // Validação da jogada recebida
                    int col = msgRecebida.getJogada();
                    if (col < 0 || col >= Tabuleiro.COLUNAS || !tabuleiro.colunaValida(col)) {
                        System.out.printf("⚠️ Jogada inválida do oponente (Coluna %d). Ignorando.%n", col);
                        continue;
                    }

                    tabuleiro.soltarPeca(col, oponenteId);
                    System.out.printf("%n>>> Oponente (Jogador %d - [%c]) jogou na coluna %d%n",
                            oponenteId, oponenteSimbolo, col);

                    if (MensagemJogada.RESULTADO_VITORIA.equalsIgnoreCase(msgRecebida.getResultado()) ||
                            tabuleiro.verificarVitoria(oponenteId)) {
                        System.out.println(tabuleiro.renderizarAscii());
                        System.out.println("💀 O OPONENTE VENCEU A PARTIDA!");

                        // Envia mensagem "der" aceitando a derrota para o oponente
                        MensagemJogada derrota = new MensagemJogada(
                                msgRecebida.getNumero() + 1, -1, tabuleiro.getGrade(), MensagemJogada.RESULTADO_DERROTA
                        );
                        escritorSocket.println(derrota.paraJson());

                        Display.transmitirJogada(new MensagemJogada(
                                msgRecebida.getNumero(), col, tabuleiro.getGrade(), MensagemJogada.RESULTADO_VITORIA
                        ), multicastIp, multicastPorta);
                        break;
                    } else if (MensagemJogada.RESULTADO_EMPATE.equalsIgnoreCase(msgRecebida.getResultado()) ||
                            tabuleiro.estaCheio()) {
                        System.out.println(tabuleiro.renderizarAscii());
                        System.out.println("🤝 A PARTIDA TERMINOU EM EMPATE!");
                        break;
                    }

                    numeroSequencia = msgRecebida.getNumero() + 1;
                    meuTurno = true;
                }
            }

        } catch (Exception e) {
            System.err.println("[Jogador] Erro na execução: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (socketP2P != null) {
                try { socketP2P.close(); } catch (Exception ignored) {}
            }
            if (serverSocket != null) {
                try { serverSocket.close(); } catch (Exception ignored) {}
            }
            System.out.println("[Jogador] Sessão encerrada e recursos liberados.");
        }
    }

    private int lerColunaValida(Scanner scanner, Tabuleiro tabuleiro) {
        while (true) {
            System.out.print("Escolha a coluna (0 a 6): ");
            if (!scanner.hasNextLine()) return -1;
            String entrada = scanner.nextLine().trim();
            try {
                int col = Integer.parseInt(entrada);
                if (col >= 0 && col < Tabuleiro.COLUNAS && tabuleiro.colunaValida(col)) {
                    return col;
                }
                System.out.println("❌ Coluna inválida ou cheia (0 a 6). Tente novamente.");
            } catch (NumberFormatException e) {
                System.out.println("❌ Digite um número inteiro entre 0 e 6.");
            }
        }
    }
}

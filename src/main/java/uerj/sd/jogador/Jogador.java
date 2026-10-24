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
            System.out.println("Conectando ao Jogo da Velha");
            System.out.printf(" Servidor RMI: %s:%d%n", rmiIp, rmiPorta);
            System.out.printf(" Minha conexão P2P: %s:%d%n", meuIp, minhaPorta);
            System.out.printf(" Multicast Display: %s:%d%n", multicastIp, multicastPorta);
            System.out.println("-----------------------------");

            System.out.println("[Jogador] Buscando oponente no servidor de match...");
            Registry registro = LocateRegistry.getRegistry(rmiIp, rmiPorta);
            MatchServiceInterface servicoMatch = (MatchServiceInterface) registro.lookup(ServidorMatch.NOME_SERVICO);

            RequisicaoMatch requisicao = new RequisicaoMatch(meuIp, minhaPorta, "tcp");
            String jsonResposta = servicoMatch.getOponent(requisicao.paraJson());

            RespostaMatch resposta = RespostaMatch.deJson(jsonResposta);
            if (resposta.temErro()) {
                System.err.println("[Jogador] Erro do servidor: " + resposta.getErro());
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
            System.out.printf("IP do Oponente: %s:%d%n", oponenteIp, oponentePorta);
            System.out.printf("Você é o Jogador %d (%c)%n", meuId, meuSimbolo);
            System.out.printf("Você %s a partida.%n", souIniciante ? "inicia" : "aguarda");

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

            Tabuleiro tabuleiro = new Tabuleiro();
            int numeroSequencia = 0;
            boolean meuTurno = souIniciante;
            int ultimaCasaJogada = -1;

            while (true) {
                if (meuTurno) {
                    System.out.println(tabuleiro.renderizarAscii());
                    System.out.printf("SUA VEZ (Jogador %d - [%c]) - Rodada nº %d <<<%n", meuId, meuSimbolo, numeroSequencia);

                    int casa = lerCasaValida(scanner, tabuleiro);
                    if (casa == -1) break;

                    tabuleiro.jogar(casa, meuId);
                    ultimaCasaJogada = casa;

                    String resultado = tabuleiro.verificarVitoria(meuId) ? MensagemJogada.RESULTADO_VITORIA
                            : tabuleiro.estaCheio() ? MensagemJogada.RESULTADO_EMPATE
                            : MensagemJogada.RESULTADO_JOGANDO;

                    MensagemJogada msg = new MensagemJogada(numeroSequencia, casa, tabuleiro.getGrade(), resultado);
                    escritorSocket.println(msg.paraJson());

                    Display.transmitirJogada(msg, multicastIp, multicastPorta);

                    if (MensagemJogada.RESULTADO_VITORIA.equals(resultado)) {
                        System.out.println(tabuleiro.renderizarAscii());
                        System.out.println("VOCÊ VENCEU O JOGO!");
                        break;
                    } else if (MensagemJogada.RESULTADO_EMPATE.equals(resultado)) {
                        System.out.println(tabuleiro.renderizarAscii());
                        System.out.println("O JOGO TERMINOU EM EMPATE");
                        break;
                    }

                    numeroSequencia++;
                    meuTurno = false;
                    System.out.println(tabuleiro.renderizarAscii());
                    System.out.printf("Você jogou na linha %d, coluna %d. Aguardando oponente jogar...%n",
                            casa / Tabuleiro.COLUNAS, casa % Tabuleiro.COLUNAS);

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

                    if (msgRecebida.getNumero() == numeroSequencia - 1 && msgRecebida.getJogada() == ultimaCasaJogada) {
                        System.out.println("\n Sua última jogada foi rejeitada pelo oponente!");
                        System.out.println(" Você deve corrigir a sua jogada.");
                        if (ultimaCasaJogada >= 0) {
                            tabuleiro.desfazer(ultimaCasaJogada);
                            ultimaCasaJogada = -1;
                        }
                        numeroSequencia--;
                        meuTurno = true;
                        continue;
                    }

                    int casaRecebida = msgRecebida.getJogada();
                    int[] gradeRecebida = msgRecebida.getTabuleiro();

                    boolean jogadaInvalida = msgRecebida.getNumero() != numeroSequencia
                            || !tabuleiro.casaValida(casaRecebida)
                            || gradeRecebida == null
                            || gradeRecebida.length != Tabuleiro.TOTAL_CASAS;

                    if (!jogadaInvalida) {
                        int[] gradeAtual = tabuleiro.getGrade();
                        for (int i = 0; i < Tabuleiro.TOTAL_CASAS; i++) {
                            if (i == casaRecebida) {
                                if (gradeRecebida[i] != oponenteId) {
                                    jogadaInvalida = true;
                                    break;
                                }
                            } else {
                                if (gradeRecebida[i] != gradeAtual[i]) {
                                    jogadaInvalida = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (jogadaInvalida) {
                        System.out.printf("Jogada inválida.");
                        System.out.println("Devolvendo a mensagem ao oponente");
                        escritorSocket.println(msgRecebida.paraJson());
                        continue;
                    }

                    tabuleiro.jogar(casaRecebida, oponenteId);
                    System.out.printf("%n>>> Oponente (Jogador %d - [%c]) jogou na linha %d, coluna %d%n",
                            oponenteId, oponenteSimbolo, casaRecebida / Tabuleiro.COLUNAS, casaRecebida % Tabuleiro.COLUNAS);

                    if (MensagemJogada.RESULTADO_VITORIA.equalsIgnoreCase(msgRecebida.getResultado()) ||
                            tabuleiro.verificarVitoria(oponenteId)) {
                        System.out.println(tabuleiro.renderizarAscii());
                        System.out.println("Você perdeu!");

                        MensagemJogada derrota = new MensagemJogada(
                                msgRecebida.getNumero() + 1, -1, tabuleiro.getGrade(), MensagemJogada.RESULTADO_DERROTA
                        );
                        escritorSocket.println(derrota.paraJson());

                        Display.transmitirJogada(new MensagemJogada(
                                msgRecebida.getNumero(), casaRecebida, tabuleiro.getGrade(), MensagemJogada.RESULTADO_VITORIA
                        ), multicastIp, multicastPorta);
                        break;
                    } else if (MensagemJogada.RESULTADO_EMPATE.equalsIgnoreCase(msgRecebida.getResultado()) ||
                            tabuleiro.estaCheio()) {
                        System.out.println(tabuleiro.renderizarAscii());
                        System.out.println("EMPATE");
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

    private int lerCasaValida(Scanner scanner, Tabuleiro tabuleiro) {
        while (true) {
            System.out.print("Escolha LINHA e COLUNA (0 a 2), ex: 1 2 : ");
            if (!scanner.hasNextLine()) return -1;
            String[] partes = scanner.nextLine().trim().split("[\\s,;]+");
            if (partes.length != 2) {
                System.out.println("❌ Informe dois números: linha e coluna (ex: 1 2).");
                continue;
            }
            try {
                int linha = Integer.parseInt(partes[0]);
                int coluna = Integer.parseInt(partes[1]);
                if (linha < 0 || linha >= Tabuleiro.LINHAS || coluna < 0 || coluna >= Tabuleiro.COLUNAS) {
                    System.out.println("❌ Linha e coluna devem estar entre 0 e 2.");
                    continue;
                }
                int casa = Tabuleiro.paraCasa(linha, coluna);
                if (!tabuleiro.casaValida(casa)) {
                    System.out.println("❌ Essa casa já está ocupada. Escolha outra.");
                    continue;
                }
                return casa;
            } catch (NumberFormatException e) {
                System.out.println("❌ Digite números inteiros entre 0 e 2.");
            }
        }
    }
}

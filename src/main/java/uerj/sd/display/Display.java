package uerj.sd.display;

import uerj.sd.jogo.Tabuleiro;
import uerj.sd.modelo.MensagemDisplay;
import uerj.sd.modelo.MensagemJogada;

/**
 * Módulo Display (Espectador de partidas em tempo real via JGroups).
 */
public class Display {

    public static void iniciar() {
        System.out.println("==================================================");
        System.out.println("     Módulo Display - Transmissão de Partidas     ");
        System.out.println(" Cluster: " + CanalDisplay.NOME_CLUSTER);
        System.out.println(" Conectando ao cluster JGroups...");
        System.out.println("==================================================");

        CanalDisplay canal = new CanalDisplay();

        try {
            canal.conectar(Display::exibirMensagem);
            System.out.println("[Display] Conectado com sucesso! Aguardando jogadas...");

            // Mantém a thread principal viva enquanto aguarda eventos do cluster
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("\n[Display] Encerrando módulo Display...");
        } catch (Exception e) {
            System.err.println("[Display] Erro fatal no canal JGroups: " + e.getMessage());
            e.printStackTrace();
        } finally {
            canal.fechar();
        }
    }

    private static void exibirMensagem(MensagemDisplay msg) {
        if (msg == null) {
            return;
        }

        System.out.println("\n--------------------------------------------------");
        System.out.printf(">>> Evento recebido: %s | Jogada nº: %d%n",
                msg.getEvento(), msg.getNumeroJogada());

        String nomeJogador = msg.getJogadorId() == Tabuleiro.JOGADOR_1 ? "Jogador 1 (X)" : "Jogador 2 (O)";
        System.out.printf(">>> Ação: %s jogou na coluna %d%n", nomeJogador, msg.getColuna());

        if (msg.getTabuleiro() != null && msg.getTabuleiro().length == Tabuleiro.TOTAL_CASAS) {
            Tabuleiro t = new Tabuleiro(msg.getTabuleiro());
            System.out.println(t.renderizarAscii());
        }

        String res = msg.getResultado();
        if (MensagemJogada.RESULTADO_VITORIA.equalsIgnoreCase(res)) {
            System.out.printf("🏆 RESULTADO FINAL: %s VENCEU A PARTIDA!%n", nomeJogador);
        } else if (MensagemJogada.RESULTADO_DERROTA.equalsIgnoreCase(res)) {
            System.out.printf("🏁 RESULTADO FINAL: %s declarou derrota.%n", nomeJogador);
        } else if (MensagemJogada.RESULTADO_EMPATE.equalsIgnoreCase(res)) {
            System.out.println("🤝 RESULTADO FINAL: A partida terminou em EMPATE!");
        } else {
            System.out.println("⏱ Estado: Partida em andamento. Aguardando próximo movimento...");
        }
        System.out.println("--------------------------------------------------");
    }
}

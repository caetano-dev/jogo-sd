package uerj.sd;

import org.junit.jupiter.api.Test;
import uerj.sd.jogo.Tabuleiro;
import uerj.sd.modelo.MensagemJogada;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class FluxoP2PTest {

    @Test
    public void testTrocaDeJogadasP2P() throws Exception {
        int porta = 9123;
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<Boolean> futJogador2 = executor.submit(() -> {
            try (ServerSocket server = new ServerSocket(porta);
                 Socket socket = server.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

                Tabuleiro tabLocal = new Tabuleiro();

                String linha0 = in.readLine();
                MensagemJogada msg0 = MensagemJogada.deJson(linha0);
                assertEquals(0, msg0.getNumero());
                assertEquals(4, msg0.getJogada());
                tabLocal.jogar(msg0.getJogada(), Tabuleiro.JOGADOR_1);

                tabLocal.jogar(0, Tabuleiro.JOGADOR_2);
                MensagemJogada msg1 = new MensagemJogada(1, 0, tabLocal.getGrade(), MensagemJogada.RESULTADO_JOGANDO);
                out.println(msg1.paraJson());

                String linha2 = in.readLine();
                MensagemJogada msg2 = MensagemJogada.deJson(linha2);
                assertEquals(2, msg2.getNumero());
                assertEquals(1, msg2.getJogada());

                return true;
            }
        });

        Future<Boolean> futJogador1 = executor.submit(() -> {
            Thread.sleep(100);
            try (Socket socket = new Socket("127.0.0.1", porta);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

                Tabuleiro tabLocal = new Tabuleiro();

                tabLocal.jogar(4, Tabuleiro.JOGADOR_1);
                MensagemJogada msg0 = new MensagemJogada(0, 4, tabLocal.getGrade(), MensagemJogada.RESULTADO_JOGANDO);
                out.println(msg0.paraJson());

                String linha1 = in.readLine();
                MensagemJogada msg1 = MensagemJogada.deJson(linha1);
                assertEquals(1, msg1.getNumero());
                assertEquals(0, msg1.getJogada());
                tabLocal.jogar(msg1.getJogada(), Tabuleiro.JOGADOR_2);

                tabLocal.jogar(1, Tabuleiro.JOGADOR_1);
                MensagemJogada msg2 = new MensagemJogada(2, 1, tabLocal.getGrade(), MensagemJogada.RESULTADO_JOGANDO);
                out.println(msg2.paraJson());

                return true;
            }
        });

        assertTrue(futJogador1.get(5, TimeUnit.SECONDS));
        assertTrue(futJogador2.get(5, TimeUnit.SECONDS));
        executor.shutdown();
    }
}

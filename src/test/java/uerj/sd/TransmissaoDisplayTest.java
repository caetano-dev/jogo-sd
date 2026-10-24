package uerj.sd;

import org.junit.jupiter.api.Test;
import uerj.sd.display.Display;
import uerj.sd.jogo.Tabuleiro;
import uerj.sd.modelo.MensagemJogada;

import java.net.MulticastSocket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class TransmissaoDisplayTest {

    @Test
    public void testTransmissaoMulticastDisplay() throws Exception {
        String ipMulticast = "225.1.1.99";
        int portaMulticast = 8899;

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<MensagemJogada> msgRecebida = new AtomicReference<>();

        MulticastSocket socket = Display.ouvirMulticast(ipMulticast, portaMulticast, msg -> {
            msgRecebida.set(msg);
            latch.countDown();
        }, false);

        assertNotNull(socket, "Socket multicast deve ser inicializado com sucesso");

        // Pequeno intervalo para o socket se registrar no grupo
        Thread.sleep(200);

        Tabuleiro tab = new Tabuleiro();
        tab.jogar(4, Tabuleiro.JOGADOR_1);

        MensagemJogada jogada = new MensagemJogada(
                0,
                4,
                tab.getGrade(),
                MensagemJogada.RESULTADO_JOGANDO
        );

        Display.transmitirJogada(jogada, ipMulticast, portaMulticast);

        boolean sucesso = latch.await(3, TimeUnit.SECONDS);
        socket.close();

        assertTrue(sucesso, "O Display deve ter recebido a jogada via Multicast nativo");
        assertNotNull(msgRecebida.get());
        assertEquals(4, msgRecebida.get().getJogada());
        assertEquals(0, msgRecebida.get().getNumero());
        assertEquals(MensagemJogada.RESULTADO_JOGANDO, msgRecebida.get().getResultado());
    }
}

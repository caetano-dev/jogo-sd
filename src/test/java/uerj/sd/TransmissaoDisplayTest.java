package uerj.sd;

import org.junit.jupiter.api.Test;
import uerj.sd.display.CanalDisplay;
import uerj.sd.jogo.Tabuleiro;
import uerj.sd.modelo.MensagemDisplay;
import uerj.sd.modelo.MensagemJogada;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class TransmissaoDisplayTest {

    @Test
    public void testTransmissaoMultiplosDisplays() throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");

        CanalDisplay emissor = new CanalDisplay();
        CanalDisplay display1 = new CanalDisplay();
        CanalDisplay display2 = new CanalDisplay();

        CountDownLatch latch = new CountDownLatch(2);
        AtomicReference<MensagemDisplay> msgRecebida1 = new AtomicReference<>();
        AtomicReference<MensagemDisplay> msgRecebida2 = new AtomicReference<>();

        display1.conectar(msg -> {
            msgRecebida1.set(msg);
            latch.countDown();
        });

        display2.conectar(msg -> {
            msgRecebida2.set(msg);
            latch.countDown();
        });

        emissor.conectar(null);

        // Aguarda formação da visão do cluster
        Thread.sleep(500);

        Tabuleiro tab = new Tabuleiro();
        tab.soltarPeca(3, Tabuleiro.JOGADOR_1);

        MensagemDisplay evento = new MensagemDisplay(
                MensagemDisplay.EVENTO_JOGADA,
                Tabuleiro.JOGADOR_1,
                0,
                3,
                tab.getGrade(),
                MensagemJogada.RESULTADO_JOGANDO
        );

        emissor.publicar(evento);

        boolean sucesso = latch.await(6, TimeUnit.SECONDS);

        emissor.fechar();
        display1.fechar();
        display2.fechar();

        assertTrue(sucesso, "Ambos os displays deveriam ter recebido o evento");
        assertNotNull(msgRecebida1.get());
        assertNotNull(msgRecebida2.get());

        assertEquals(3, msgRecebida1.get().getColuna());
        assertEquals(3, msgRecebida2.get().getColuna());
        assertEquals(Tabuleiro.JOGADOR_1, msgRecebida1.get().getJogadorId());
        assertEquals(Tabuleiro.JOGADOR_1, msgRecebida2.get().getJogadorId());
    }
}

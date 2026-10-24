package uerj.sd;

import org.junit.jupiter.api.Test;
import uerj.sd.display.Display;
import uerj.sd.jogador.Jogador;
import uerj.sd.modelo.MensagemJogada;
import uerj.sd.servidor.ServicoMatch;
import uerj.sd.servidor.ServidorMatch;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class JogoCompletoIntegracaoTest {

    @Test
    public void testPartidaCompletaComVitoriaEDisplay() throws Exception {
        int portaRmi = 9815;
        String mcastIp = "225.1.1.15";
        int mcastPorta = 8815;

        Registry registro = LocateRegistry.createRegistry(portaRmi);
        ServicoMatch servico = new ServicoMatch(10);
        registro.rebind(ServidorMatch.NOME_SERVICO, servico);

        List<MensagemJogada> eventosRecebidos = new CopyOnWriteArrayList<>();
        CountDownLatch vitoriaLatch = new CountDownLatch(1);

        MulticastSocket socketDisplay = Display.ouvirMulticast(mcastIp, mcastPorta, msg -> {
            eventosRecebidos.add(msg);
            if ("vit".equalsIgnoreCase(msg.getResultado()) || "der".equalsIgnoreCase(msg.getResultado())) {
                vitoriaLatch.countDown();
            }
        }, false);

        Thread.sleep(200);

        String entradaP1 = "0 0\n0 1\n0 2\n";
        String entradaP2 = "1 0\n1 1\n";

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Void> taskP1 = () -> {
            InputStream in = new ByteArrayInputStream(entradaP1.getBytes(StandardCharsets.UTF_8));
            Jogador p1 = new Jogador("127.0.0.1", portaRmi, "127.0.0.1", 9031, mcastIp, mcastPorta);
            p1.iniciarComEntrada(new Scanner(in));
            return null;
        };

        Callable<Void> taskP2 = () -> {
            Thread.sleep(100);
            InputStream in = new ByteArrayInputStream(entradaP2.getBytes(StandardCharsets.UTF_8));
            Jogador p2 = new Jogador("127.0.0.1", portaRmi, "127.0.0.1", 9032, mcastIp, mcastPorta);
            p2.iniciarComEntrada(new Scanner(in));
            return null;
        };

        Future<Void> fut1 = executor.submit(taskP1);
        Future<Void> fut2 = executor.submit(taskP2);

        fut1.get(10, TimeUnit.SECONDS);
        fut2.get(10, TimeUnit.SECONDS);

        boolean displayRecebeuVitoria = vitoriaLatch.await(5, TimeUnit.SECONDS);

        if (socketDisplay != null) {
            socketDisplay.close();
        }
        executor.shutdown();

        assertTrue(displayRecebeuVitoria, "O Display deve ter recebido a notificação de vitória pelo Multicast nativo");
        assertTrue(eventosRecebidos.size() >= 3, "Display deve ter recebido os eventos das jogadas");

        MensagemJogada ultimoEvento = eventosRecebidos.get(eventosRecebidos.size() - 1);
        assertEquals("vit", ultimoEvento.getResultado());
        assertEquals(2, ultimoEvento.getJogada());
    }
}

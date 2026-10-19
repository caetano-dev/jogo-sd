package uerj.sd;

import org.junit.jupiter.api.Test;
import uerj.sd.display.CanalDisplay;
import uerj.sd.jogador.Jogador;
import uerj.sd.modelo.MensagemDisplay;
import uerj.sd.servidor.ServicoMatch;
import uerj.sd.servidor.ServidorMatch;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class JogoCompletoIntegracaoTest {

    @Test
    public void testPartidaCompletaComVitoriaEDisplay() throws Exception {
        int portaRmi = 9810;
        Registry registro = LocateRegistry.createRegistry(portaRmi);
        ServicoMatch servico = new ServicoMatch(10);
        registro.rebind(ServidorMatch.NOME_SERVICO, servico);

        // Display escutando no JGroups
        CanalDisplay display = new CanalDisplay();
        List<MensagemDisplay> eventosRecebidos = new CopyOnWriteArrayList<>();
        CountDownLatch vitoriaLatch = new CountDownLatch(1);

        display.conectar(msg -> {
            eventosRecebidos.add(msg);
            if ("vit".equalsIgnoreCase(msg.getResultado()) || "der".equalsIgnoreCase(msg.getResultado())) {
                vitoriaLatch.countDown();
            }
        });

        // Simulação de jogadas:
        // Jogador 1 (inicia) joga colunas: 0, 0, 0, 0 (vertical de 4)
        // Jogador 2 joga colunas: 1, 1, 1
        String entradaP1 = "0\n0\n0\n0\n";
        String entradaP2 = "1\n1\n1\n";

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Void> taskP1 = () -> {
            InputStream in = new ByteArrayInputStream(entradaP1.getBytes(StandardCharsets.UTF_8));
            Jogador p1 = new Jogador("127.0.0.1", portaRmi, "127.0.0.1", 9021);
            p1.iniciarComEntrada(new Scanner(in));
            return null;
        };

        Callable<Void> taskP2 = () -> {
            Thread.sleep(100);
            InputStream in = new ByteArrayInputStream(entradaP2.getBytes(StandardCharsets.UTF_8));
            Jogador p2 = new Jogador("127.0.0.1", portaRmi, "127.0.0.1", 9022);
            p2.iniciarComEntrada(new Scanner(in));
            return null;
        };

        Future<Void> fut1 = executor.submit(taskP1);
        Future<Void> fut2 = executor.submit(taskP2);

        fut1.get(10, TimeUnit.SECONDS);
        fut2.get(10, TimeUnit.SECONDS);

        boolean displayRecebeuVitoria = vitoriaLatch.await(5, TimeUnit.SECONDS);

        display.fechar();
        executor.shutdown();

        assertTrue(displayRecebeuVitoria, "O Display deve ter recebido a notificação de vitória pelo JGroups");
        assertTrue(eventosRecebidos.size() >= 4, "Display deve ter recebido os eventos das jogadas");

        MensagemDisplay ultimoEvento = eventosRecebidos.get(eventosRecebidos.size() - 1);
        assertEquals("vit", ultimoEvento.getResultado());
        assertEquals(0, ultimoEvento.getColuna());
    }
}

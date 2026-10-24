package uerj.sd;

import org.junit.jupiter.api.Test;
import uerj.sd.modelo.RequisicaoMatch;
import uerj.sd.modelo.RespostaMatch;
import uerj.sd.servidor.ServicoMatch;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class PareamentoRmiTest {

    @Test
    public void testPareamentoDoisJogadores() throws Exception {
        ServicoMatch servico = new ServicoMatch(5);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<RespostaMatch> jogador1Task = () -> {
            RequisicaoMatch req = new RequisicaoMatch("127.0.0.1", 9001, "tcp");
            String ret = servico.getOponent(req.paraJson());
            return RespostaMatch.deJson(ret);
        };

        Callable<RespostaMatch> jogador2Task = () -> {
            Thread.sleep(100);
            RequisicaoMatch req = new RequisicaoMatch("127.0.0.1", 9002, "tcp");
            String ret = servico.getOponent(req.paraJson());
            return RespostaMatch.deJson(ret);
        };

        Future<RespostaMatch> fut1 = executor.submit(jogador1Task);
        Future<RespostaMatch> fut2 = executor.submit(jogador2Task);

        RespostaMatch resp1 = fut1.get(4, TimeUnit.SECONDS);
        RespostaMatch resp2 = fut2.get(4, TimeUnit.SECONDS);

        assertNotNull(resp1);
        assertNotNull(resp2);

        assertFalse(resp1.temErro(), "Jogador 1 não deve receber erro");
        assertFalse(resp2.temErro(), "Jogador 2 não deve receber erro");

        assertEquals("127.0.0.1", resp1.getIp());
        assertEquals(9002, resp1.getPorta());
        assertTrue(resp1.getInicia(), "Jogador 1 (primeiro na fila) deve receber inicia = true");

        assertEquals("127.0.0.1", resp2.getIp());
        assertEquals(9001, resp2.getPorta());
        assertFalse(resp2.getInicia(), "Jogador 2 (segundo na fila) deve receber inicia = false");

        executor.shutdown();
    }

    @Test
    public void testTimeoutJogadorSemPar() throws Exception {
        ServicoMatch servico = new ServicoMatch(1);

        RequisicaoMatch req = new RequisicaoMatch("127.0.0.1", 9005, "tcp");
        String ret = servico.getOponent(req.paraJson());
        RespostaMatch resp = RespostaMatch.deJson(ret);

        assertNotNull(resp);
        assertTrue(resp.temErro(), "Deveria ter retornado erro por timeout");
        assertTrue(resp.getErro().toLowerCase().contains("tempo limite"), "Mensagem deve indicar tempo limite");
    }
}

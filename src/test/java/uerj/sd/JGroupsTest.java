package uerj.sd;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ObjectMessage;
import org.jgroups.Receiver;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JGroupsTest {

    @Test
    public void testJGroupsLoopback() throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");
        // Cria canal com pilha udp padrão ou fast-local
        JChannel ch1 = new JChannel();
        JChannel ch2 = new JChannel();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> recebido = new AtomicReference<>();

        ch2.setReceiver(new Receiver() {
            @Override
            public void receive(Message msg) {
                recebido.set(msg.getObject());
                latch.countDown();
            }
        });

        ch1.connect("TesteCluster");
        ch2.connect("TesteCluster");

        ch1.send(new ObjectMessage(null, "Olá JGroups"));

        boolean ok = latch.await(5, TimeUnit.SECONDS);
        ch1.close();
        ch2.close();

        assertTrue(ok, "Deveria ter recebido mensagem via JGroups");
        assertEquals("Olá JGroups", recebido.get());
    }
}

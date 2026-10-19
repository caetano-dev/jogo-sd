package uerj.sd.display;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ObjectMessage;
import org.jgroups.Receiver;
import uerj.sd.modelo.MensagemDisplay;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Gerenciador de comunicação em grupo via JGroups para o canal de Display.
 */
public class CanalDisplay {
    public static final String NOME_CLUSTER = "ConnectFourDisplayCluster";
    public static final String CONFIGURACAO_PADRAO = "jgroups-tcp.xml";

    private JChannel canal;

    /**
     * Inicializa o canal e conecta ao cluster.
     *
     * @param receptor Callback para mensagens recebidas (pode ser null se for apenas emissor)
     */
    public void conectar(Consumer<MensagemDisplay> receptor) throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");

        String configCustomizada = System.getProperty("jgroups.configuration");
        if (configCustomizada != null && !configCustomizada.isBlank()) {
            this.canal = new JChannel(configCustomizada);
        } else {
            InputStream is = getClass().getClassLoader().getResourceAsStream(CONFIGURACAO_PADRAO);
            if (is != null) {
                this.canal = new JChannel(is);
            } else {
                this.canal = new JChannel();
            }
        }

        if (receptor != null) {
            this.canal.setReceiver(new Receiver() {
                @Override
                public void receive(Message msg) {
                    try {
                        String json = extrairTextoMensagem(msg);
                        if (json != null && !json.isBlank()) {
                            MensagemDisplay m = MensagemDisplay.deJson(json);
                            receptor.accept(m);
                        }
                    } catch (Exception e) {
                        System.err.println("[Display] Erro ao processar mensagem do cluster: " + e.getMessage());
                    }
                }
            });
        }

        this.canal.connect(NOME_CLUSTER);
    }

    /**
     * Publica uma mensagem de atualização no cluster JGroups.
     */
    public void publicar(MensagemDisplay mensagem) {
        if (canal == null || canal.isClosed()) {
            return;
        }
        try {
            String json = mensagem.paraJson();
            canal.send(new ObjectMessage(null, json));
        } catch (Exception e) {
            System.err.println("[CanalDisplay] Aviso: Falha ao publicar evento no JGroups: " + e.getMessage());
        }
    }

    /**
     * Fecha o canal JGroups.
     */
    public void fechar() {
        if (canal != null) {
            try {
                canal.close();
            } catch (Exception ignored) {
            }
        }
    }

    private static String extrairTextoMensagem(Message msg) {
        if (msg instanceof ObjectMessage) {
            Object obj = ((ObjectMessage) msg).getObject();
            return obj != null ? obj.toString() : null;
        }
        byte[] payload = msg.getArray();
        if (payload != null && msg.getLength() > 0) {
            return new String(payload, msg.getOffset(), msg.getLength(), StandardCharsets.UTF_8);
        }
        Object obj = msg.getObject();
        return obj != null ? obj.toString() : null;
    }
}

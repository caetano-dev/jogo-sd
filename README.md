# Connect Four Distribuído (Lig 4)

Sistema distribuído em Java para partida de **Connect Four (Lig 4)** em turnos entre dois jogadores, com serviço de pareamento centralizado via **Java RMI**, troca de jogadas ponto a ponto via **Sockets TCP**, e transmissão em tempo real para múltiplos observadores (**Displays**) através de canal em grupo com **JGroups**.

---

## 1. Arquitetura e Módulos

O sistema é dividido em três módulos principais:

1. **Servidor de Matchmaking (`server`) - Java RMI**:
   - Registra o interesse de jogadores por meio da interface remota [`MatchServiceInterface`](file:///Users/pedrocaetano/Documents/UERJ/Sistèmes%20Distribués/trabalho%20jogo/src/main/java/uerj/sd/rmi/MatchServiceInterface.java).
   - Realiza o emparelhamento 1x1 dos jogadores que utilizam o mesmo protocolo (`tcp`).
   - Define deterministicamente quem inicia a partida (`inicia: true` para o primeiro da fila, `inicia: false` para o segundo).

2. **Módulo Jogador (`player`) - RMI + Sockets TCP + JGroups**:
   - Localiza o oponente via RMI.
   - Estabelece conexão P2P via Socket TCP.
   - Gerencia os turnos, validação anti-cheat (coluna dentro dos limites, espaço disponível, integridade de sequência e estado).
   - Renderiza o tabuleiro na CLI e lê a coluna [0..6] escolhida.
   - Publica os eventos de cada jogada no cluster JGroups para os Displays.

3. **Módulo Display (`display`) - JGroups**:
   - Conecta-se ao cluster `ConnectFourDisplayCluster`.
   - Renderiza em arte ASCII o estado atualizado do tabuleiro em tempo real e exibe o andamento e resultado da partida.

---

## 2. Compilação e Geração do JAR Executável

O projeto utiliza Maven. Para compilar, rodar todos os testes automatizados e gerar o executável fat JAR (`app.jar`):

```bash
mvn clean package
cp target/app.jar ./app.jar
```

---

## 3. Instruções de Execução

### 3.1 Execução Local (Mesma Máquina)

Abra 4 terminais distintos:

**Terminal 1: Servidor de Matchmaking (RMI)**
```bash
java -jar app.jar server --port 9800
```

**Terminal 2: Módulo Display (Observador em Tempo Real)**
```bash
java -jar app.jar display
```
*(Você pode abrir quantas instâncias do Display desejar em terminais separados)*

**Terminal 3: Jogador 1**
```bash
java -jar app.jar player --server-ip 127.0.0.1 --server-port 9800 --port 9001
```

**Terminal 4: Jogador 2**
```bash
java -jar app.jar player --server-ip 127.0.0.1 --server-port 9800 --port 9002
```

---

### 3.2 Execução em Rede / VMs do Laboratório (152.92.236.x)

Para executar entre máquinas diferentes (como as VMs `152.92.236.14`, `152.92.236.16` e `152.92.236.17`):

**Na VM do Servidor (ex: 152.92.236.14):**
```bash
java -Djava.rmi.server.hostname=152.92.236.14 -jar app.jar server --port 9800
```

**Nas VMs dos Jogadores (ex: Jogador na 152.92.236.16 e outro na 152.92.236.17):**
```bash
# Na VM 236.16:
java -jar app.jar player --server-ip 152.92.236.14 --server-port 9800 --ip 152.92.236.16 --port 9001

# Na VM 236.17:
java -jar app.jar player --server-ip 152.92.236.14 --server-port 9800 --ip 152.92.236.17 --port 9002
```

**Em qualquer máquina para assistir à partida (Display):**
```bash
java -jar app.jar display
```

---

## 4. Testes Automatizados

O projeto inclui uma suíte completa de testes unitários e de integração cobrindo todos os critérios do enunciado:

- [`TabuleiroTest`](file:///Users/pedrocaetano/Documents/UERJ/Sistèmes%20Distribués/trabalho%20jogo/src/test/java/uerj/sd/TabuleiroTest.java): Física da gravidade, rejeição de coluna cheia, detecção de vitória nas 4 direções (horizontal, vertical, diagonais ascendente e descendente) e empate.
- [`PareamentoRmiTest`](file:///Users/pedrocaetano/Documents/UERJ/Sistèmes%20Distribués/trabalho%20jogo/src/test/java/uerj/sd/PareamentoRmiTest.java): Concorrência do RMI, formação de pares e timeout.
- [`FluxoP2PTest`](file:///Users/pedrocaetano/Documents/UERJ/Sistèmes%20Distribués/trabalho%20jogo/src/test/java/uerj/sd/FluxoP2PTest.java): Conexão TCP socket e sincronização de turnos.
- [`TransmissaoDisplayTest`](file:///Users/pedrocaetano/Documents/UERJ/Sistèmes%20Distribués/trabalho%20jogo/src/test/java/uerj/sd/TransmissaoDisplayTest.java): Transmissão concorrente para múltiplos displays via JGroups.
- [`JogoCompletoIntegracaoTest`](file:///Users/pedrocaetano/Documents/UERJ/Sistèmes%20Distribués/trabalho%20jogo/src/test/java/uerj/sd/JogoCompletoIntegracaoTest.java): Simulação ponta a ponta de uma partida completa com pareamento, jogadas, vitória e recepção no display.

Para executar todos os testes:
```bash
mvn test
```

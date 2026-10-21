# Connect Four Distribuído (Lig 4)

Sistema distribuído em Java para partida de **Connect Four (Lig 4)** em turnos entre dois jogadores, com serviço de pareamento centralizado via **Java RMI**, troca de jogadas ponto a ponto via **Sockets TCP**, e transmissão em tempo real para múltiplos observadores (**Displays**) através de **Multicast UDP Nativo** (`MulticastSocket`), estritamente conforme a especificação do trabalho.

---

## 1. Arquitetura e Módulos

O sistema é dividido em três módulos principais:

1. **Servidor de Matchmaking (`server`) - Java RMI**:
   - Registra o interesse de jogadores por meio da interface remota [`MatchServiceInterface`](file:///Users/pedrocaetano/Documents/UERJ/Sistèmes%20Distribués/trabalho%20jogo/src/main/java/uerj/sd/rmi/MatchServiceInterface.java).
   - Realiza o emparelhamento 1x1 dos jogadores que utilizam o mesmo protocolo (`tcp`).
   - Define deterministicamente quem inicia a partida (`inicia: true` para o primeiro da fila, `inicia: false` para o segundo).
   - Executa na porta designada `98NN` (ou `9800` por padrão).

2. **Módulo Jogador (`player`) - RMI + Sockets TCP + Multicast UDP**:
   - Localiza o oponente via RMI.
   - Estabelece conexão P2P via Socket TCP.
   - Gerencia os turnos, validação anti-cheat (coluna dentro dos limites [0..6], espaço disponível, integridade de sequência e estado).
   - Renderiza o tabuleiro na CLI e lê a coluna [0..6] escolhida.
   - Transmite a cada jogada um pacote UDP Multicast para o grupo `225.1.1.NN` na porta `88NN`.

3. **Módulo Display (`display`) - Multicast UDP**:
   - Conecta-se ao grupo multicast `225.1.1.NN:88NN`.
   - Renderiza em arte ASCII o estado atualizado do tabuleiro em tempo real e exibe o andamento e resultado da partida.

---

## 2. Compilação e Geração do Executável

O projeto utiliza Maven com dependências mínimas (apenas Gson para serialização de mensagens e JUnit para testes).

```bash
mvn clean package
```

O binário final é gerado em `target/app.jar` (e na raiz `./app.jar`), com apenas ~330 KB.

---

## 3. Instruções de Execução

### 3.1 Execução Local

Abra terminais distintos:

**Terminal 1: Servidor de Matchmaking (RMI)**
```bash
java -jar app.jar server --port 9800
```
*Ou usando a flag de conta/aluno `--id NN`:*
```bash
java -jar app.jar server --id 14
# Configura automaticamente porta 9814
```

**Terminal 2: Módulo Display (Observador em Tempo Real)**
```bash
java -jar app.jar display --id 14
# Ouve no grupo 225.1.1.14 e porta 8814
```
*(Você pode abrir quantas instâncias do Display desejar em terminais separados)*

**Terminal 3: Jogador 1**
```bash
java -jar app.jar player --server-port 9814 --port 9001 --id 14
```

**Terminal 4: Jogador 2**
```bash
java -jar app.jar player --server-port 9814 --port 9002 --id 14
```

---

### 3.2 Execução em Rede / VMs do Laboratório (`152.92.236.x`)

Para executar entre máquinas diferentes (como as VMs `152.92.236.14`, `152.92.236.16` e `152.92.236.17`):

**Na VM do Servidor (ex: 152.92.236.14):**
```bash
java -Djava.rmi.server.hostname=152.92.236.14 -jar app.jar server --id 14
```

**Nas VMs dos Jogadores (ex: Jogador na 236.16 e outro na 236.17):**
```bash
# Na VM 236.16:
java -jar app.jar player --server-ip 152.92.236.14 --ip 152.92.236.16 --port 9001 --id 14

# Na VM 236.17:
java -jar app.jar player --server-ip 152.92.236.14 --ip 152.92.236.17 --port 9002 --id 14
```

**Em qualquer máquina para assistir à partida (Display):**
```bash
java -jar app.jar display --id 14
```

---

## 4. Testes Automatizados

Para rodar todos os testes automatizados:

```bash
mvn test
```

- [`TabuleiroTest`](file:///Users/pedrocaetano/Documents/UERJ/Sistèmes%20Distribués/trabalho%20jogo/src/test/java/uerj/sd/TabuleiroTest.java): Lógica da matriz 6x7, gravidade, detecção de vitória nas 4 direções e empate.
- [`PareamentoRmiTest`](file:///Users/pedrocaetano/Documents/UERJ/Sistèmes%20Distribués/trabalho%20jogo/src/test/java/uerj/sd/PareamentoRmiTest.java): Concorrência RMI, formação de pares e timeout.
- [`FluxoP2PTest`](file:///Users/pedrocaetano/Documents/UERJ/Sistèmes%20Distribués/trabalho%20jogo/src/test/java/uerj/sd/FluxoP2PTest.java): Conexão TCP socket e sincronização de turnos.
- [`TransmissaoDisplayTest`](file:///Users/pedrocaetano/Documents/UERJ/Sistèmes%20Distribués/trabalho%20jogo/src/test/java/uerj/sd/TransmissaoDisplayTest.java): Transmissão e recebimento multicast nativo.
- [`JogoCompletoIntegracaoTest`](file:///Users/pedrocaetano/Documents/UERJ/Sistèmes%20Distribués/trabalho%20jogo/src/test/java/uerj/sd/JogoCompletoIntegracaoTest.java): Partida ponta a ponta (RMI + TCP + Multicast).

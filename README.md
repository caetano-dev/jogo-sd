# Jogo da Velha Distribuído (Tic-Tac-Toe)

Sistema distribuído em Java para partida de **Jogo da Velha** em turnos entre dois jogadores, com serviço de pareamento centralizado via **Java RMI**, troca de jogadas ponto a ponto via **Sockets TCP**, e transmissão em tempo real para múltiplos observadores (**Displays**) através de **Multicast UDP Nativo** (`MulticastSocket`), estritamente conforme a especificação do trabalho da disciplina de Sistemas Distribuídos.

---

## 1. Arquitetura e Módulos

O sistema é dividido em três módulos principais:

1. **Servidor de Matchmaking (`server`) - Java RMI**:
   - Registra o interesse de jogadores por meio da interface remota `MatchServiceInterface`.
   - Realiza o emparelhamento 1x1 dos jogadores que utilizam o mesmo protocolo (`tcp`).
   - Define deterministicamente quem inicia a partida (`inicia: true` para o primeiro da fila, `inicia: false` para o segundo).
   - Executa na porta designada `98NN` (ou `9800` por padrão).

2. **Módulo Jogador (`player`) - RMI + Sockets TCP + Multicast UDP**:
   - Localiza o oponente via RMI.
   - Estabelece conexão P2P via Socket TCP.
   - Gerencia os turnos e controle de consistência/anti-cheat (validação de limites [0..8], casa desocupada, sequência de rodadas).
   - Renderiza o tabuleiro na CLI e lê a posição escolhida pelo usuário informando **linha e coluna** (`0 a 2`).
   - Transmite a cada jogada um pacote UDP Multicast para o grupo `225.1.1.NN` na porta `88NN`.

3. **Módulo Display (`display`) - Multicast UDP**:
   - Conecta-se ao grupo multicast `225.1.1.NN:88NN`.
   - Renderiza em arte ASCII o estado atualizado do tabuleiro em tempo real e exibe o andamento e resultado da partida.

---

## 2. Compilação e Geração do Executável

O projeto utiliza Maven com dependências mínimas (Gson para serialização de mensagens e JUnit 5 para testes).

```bash
mvn clean package
```

O binário final é gerado em `target/app.jar` (e na raiz `./app.jar`).

Também é possível compilar diretamente com `javac`:
```powershell
javac --release 17 -encoding UTF-8 -cp "target/lib/*" -d target/classes (Get-ChildItem -Recurse src/main/java -Filter *.java | % FullName)
```

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

Para rodar todos os testes automatizados com Maven:

```bash
mvn test
```

Ou executando o console launcher standalone do JUnit:
```bash
java -jar target/lib/junit-platform-console-standalone-1.10.2.jar --class-path "target/classes;target/test-classes;target/lib/gson-2.11.0.jar" --scan-class-path
```

- `TabuleiroTest`: Lógica da matriz 3x3 (9 casas), validação de casas livres, detecção de vitória nas 8 linhas/colunas/diagonais e empate.
- `PareamentoRmiTest`: Concorrência RMI, formação de pares com exclusão mútua e timeout.
- `FluxoP2PTest`: Conexão TCP socket e sincronização de turnos (casas 0 a 8).
- `TransmissaoDisplayTest`: Transmissão e recebimento multicast nativo.
- `JogoCompletoIntegracaoTest`: Partida ponta a ponta (RMI + TCP + Multicast) com vitória e encerramento.

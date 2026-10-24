# SPEC: Sistema Distribuído de Jogo da Velha (Tic-Tac-Toe) com RMI, Sockets TCP e Multicast Nativo

## 1. Visão Geral do Sistema
O objetivo é implementar um sistema distribuído em Java para uma partida em turnos de Jogo da Velha entre dois jogadores, com infraestrutura de pareamento centralizada e transmissão em tempo real para módulos de exibição independentes (Displays).

O sistema implementa a arquitetura descrita na especificação de Sistemas Distribuídos:
1. **Servidor de Match (RMI)**: Registra requisições de jogadores e realiza o pareamento (1x1).
2. **Módulo Jogador (RMI + Socket P2P TCP + Multicast UDP)**: Busca oponente via RMI, troca jogadas por conexão Socket ponto a ponto e transmite as jogadas via Multicast para os displays.
3. **Módulo Display (Multicast UDP)**: Conecta-se ao canal de comunicação em grupo e renderiza o tabuleiro e as jogadas em tempo real.

O sistema opera localmente em diferentes terminais/sessões (usando `127.0.0.1` e portas distintas) e permite a execução nas VMs do laboratório (`152.92.236.x`) por meio de argumentos CLI.

---

## 2. Componentes e Tecnologias
- **Linguagem / Runtime**: Java 17+.
- **Gerenciador de Build**: Maven.
- **Comunicação de Matchmaking**: Java RMI (`LocateRegistry`).
- **Comunicação entre Jogadores (P2P)**: Java Sockets TCP com controle de sequência e anti-cheat.
- **Canal Multicast de Display**: Java `java.net.MulticastSocket` / `DatagramPacket` nativo (endereço `225.1.1.NN` e porta `88NN`).
- **Serialização de Mensagens**: JSON via Gson.

---

## 3. Regras de Negócio: Jogo da Velha
- **Dimensões do Tabuleiro**: Matriz de 3 linhas (0 a 2) por 3 colunas (0 a 2). Total de 9 posições [0..8] onde `casa = linha * 3 + coluna`.
- **Mecânica de Jogada**: O jogador escolhe a linha e a coluna desejadas (`0 a 2`). O módulo traduz internamente para o índice da casa `0 a 8`.
- **Condições de Encerramento**:
  - `vit`: 3 peças consecutivas do mesmo jogador (3 linhas, 3 colunas ou 2 diagonais).
  - `der`: Declaração de derrota enviada pelo perdedor em resposta a uma vitória confirmada.
  - `emp`: Todas as 9 casas ocupadas sem vencedor.
  - `jog`: Partida continua.
- **Anti-Cheat e Validação**:
  - Validação de limites da casa e se a posição está livre.
  - Validação de número sequencial de rodada.
  - Se inválida ou fora de ordem, o receptor devolve a mesma mensagem sem avançar seu estado local, notificando o usuário e obrigando o oponente a corrigir a jogada.

---

## 4. Protocolo e Formato das Mensagens

### 4.1 Interface RMI (Servidor de Match)
- **Interface**: `MatchServiceInterface extends java.rmi.Remote`
- **Método**: `String getOponent(String jsonIn) throws RemoteException;`
- **Entrada (`jsonIn`)**:
  ```json
  { "ip": "127.0.0.1", "porta": 9001, "protocolo": "tcp" }
  ```
- **Retorno (`jsonRet`)**:
  ```json
  { "ip": "127.0.0.1", "porta": 9002, "inicia": true }
  ```

### 4.2 Protocolo P2P e Multicast de Display
Transmitido via TCP entre os jogadores e via UDP Multicast (`225.1.1.NN:88NN`) para os displays:
```json
{
  "Numero": 0,
  "Jogada": 4,
  "Tabuleiro": [
    0, 0, 0,
    0, 1, 0,
    0, 0, 0
  ],
  "Resultado": "jog"
}
```

---

## 5. Arquitetura dos Módulos de Execução

1. **`server`**: Inicia o RMIRegistry programaticamente na porta designada (`98NN` ou `9800`) e expõe o serviço `"MatchService"`.
2. **`player`**: Busca oponente via RMI, conecta via Socket TCP, joga por turnos e transmite cada movimento ao display via Multicast nativo.
3. **`display`**: Ouve no grupo multicast `225.1.1.NN` e porta `88NN` via `MulticastSocket`, renderizando em tempo real cada jogada recebida.

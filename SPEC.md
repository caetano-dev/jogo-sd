# SPEC: Sistema Distribuído de Connect Four (Lig 4) com RMI, Sockets TCP e Multicast Nativo

## 1. Visão Geral do Sistema
O objetivo é implementar um sistema distribuído em Java para uma partida em turnos de Connect Four (Lig 4) entre dois jogadores, com infraestrutura de pareamento centralizada e transmissão em tempo real para módulos de exibição independentes (Displays).

O sistema implementa a arquitetura descrita na especificação de Sistemas Distribuídos:
1. **Servidor de Match (RMI)**: Registra requisições de jogadores e realiza o pareamento (1x1).
2. **Módulo Jogador (RMI + Socket P2P TCP + Multicast UDP)**: Busca oponente via RMI, troca jogadas por conexão Socket ponto a ponto e transmite as jogadas via Multicast para os displays.
3. **Módulo Display (Multicast UDP)**: Conecta-se ao canal de comunicação em grupo e renderiza o tabuleiro e as jogadas em tempo real.

O sistema opera localmente em diferentes terminais/sessões (usando `127.0.0.1` e portas distintas) e permite a migração para as VMs do laboratório (`152.92.236.x`) por meio de argumentos CLI.

---

## 2. Componentes e Tecnologias
- **Linguagem / Runtime**: Java 17+ (ou 21).
- **Gerenciador de Build**: Maven.
- **Comunicação de Matchmaking**: Java RMI (`LocateRegistry`).
- **Comunicação entre Jogadores (P2P)**: Java Sockets TCP com controle de sequência e anti-cheat.
- **Canal Multicast de Display**: Java `java.net.MulticastSocket` / `DatagramPacket` nativo (endereço `225.1.1.NN` e porta `88NN`).
- **Serialização de Mensagens**: JSON via Gson.

---

## 3. Regras de Negócio: Connect Four (Lig 4)
- **Dimensões do Tabuleiro**: Matriz de 6 linhas (0 a 5) por 7 colunas (0 a 6). Total de 42 posições.
- **Mecânica de Gravidade**: O jogador escolhe exclusivamente o índice da coluna (0 a 6). A peça ocupa a posição livre mais baixa da coluna.
- **Condições de Encerramento**:
  - `vit`: 4 peças consecutivas do mesmo jogador (horizontal, vertical ou diagonal).
  - `emp`: Todas as 42 casas ocupadas sem vencedor.
  - `jog`: Partida continua.
- **Anti-Cheat e Validação**:
  - Validação de limites de coluna e espaço livre.
  - Receptor valida número de sequência e consistência da jogada contra seu estado local. Se inválida, devolve a mesma mensagem sem avançar turno.

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
  "Jogada": 3,
  "Tabuleiro": [
    0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 1, 0, 0, 0
  ],
  "Resultado": "jog"
}
```

---

## 5. Arquitetura dos Módulos de Execução

1. **`server`**: Inicia o RMIRegistry programaticamente na porta designada (`98NN` ou `9800`) e expõe o serviço `"MatchService"`.
2. **`player`**: Busca oponente via RMI, conecta via Socket TCP, joga por turnos e transmite cada movimento ao display via Multicast nativo.
3. **`display`**: Ouve no grupo multicast `225.1.1.NN` e porta `88NN` via `MulticastSocket`, renderizando em tempo real cada jogada recebida.

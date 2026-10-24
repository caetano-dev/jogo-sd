# 🎮 Resumo do Projeto: Jogo da Velha Distribuído (Tic-Tac-Toe)

O projeto **Jogo da Velha Distribuído** é uma aplicação distribuída desenvolvida em **Java 17+** para partidas em turnos do Jogo da Velha (tabuleiro 3x3, 9 casas). Ele combina três paradigmas clássicos de comunicação em Sistemas Distribuídos dentro de um único ponto de entrada unificado (`Main.java`):

1. **Cliente-Servidor via RPC/RMI** para descoberta e pareamento (*matchmaking*).
2. **Peer-to-Peer (P2P) via Sockets TCP** para troca direta de jogadas e controle de turnos entre os dois jogadores com tolerância a trapaças/erros (*anti-cheat*).
3. **Comunicação em Grupo via Multicast UDP Nativo** para transmissão em tempo real das partidas a múltiplos espectadores (*Displays*).

---

## 🧠 1. Técnicas de Sistemas Distribuídos Utilizadas

### 1.1 Invocação Remota de Métodos (Java RMI)
* **Onde é usado:** Módulo Servidor (`ServidorMatch` e `ServicoMatch`) e Módulo Jogador (`Jogador`).
* **Como funciona:**
  * A interface remota `MatchServiceInterface` estende `java.rmi.Remote` e expõe o método `getOponent(String jsonIn)`.
  * O `ServicoMatch` estende `UnicastRemoteObject` e é registrado no `RMIRegistry` sob o nome `"MatchService"`.
  * Permite abstrair a chamada de rede como uma chamada de método bloqueante entre processos em máquinas distintas.

### 1.2 Sincronização Concorrente e Coordenação por Monitor (`synchronized`, `wait` / `notifyAll`)
* **Onde é usado:** Método `ServicoMatch.getOponent`.
* **Como funciona:**
  * Como múltiplas threads de clientes RMI podem invocar `getOponent` simultaneamente, o método utiliza exclusão mútua (`synchronized`).
  * **Barreira de Pareamento (Rendezvous):** O primeiro jogador a chegar não recebe retorno imediato; sua thread entra em estado de espera (`wait(restante)`) com **timeout de 60 segundos** até que um segundo jogador com o mesmo protocolo (`"tcp"`) invoque o serviço e dispare `notifyAll()`.
  * **Eleição Determinística de Líder/Iniciante:** O servidor define de forma atômica quem inicia a partida (`inicia: true` para o primeiro da fila, `inicia: false` para o segundo), evitando condição de corrida na abertura da conexão P2P.

### 1.3 Arquitetura Peer-to-Peer (P2P) via Sockets TCP e Anti-Cheat
* **Onde é usado:** Módulo `Jogador`.
* **Como funciona:**
  * Após o pareamento via RMI, o servidor sai de cena e a comunicação passa a ser **descentralizada (direta entre os dois jogadores)**.
  * **Papéis assimétricos no Handshake TCP:**
    * O jogador com `inicia == false` atua como servidor TCP passivo, abrindo um `ServerSocket(minhaPorta)` e aguardando em `accept()`.
    * O jogador com `inicia == true` atua como cliente TCP ativo, tentando conectar (`new Socket(oponenteIp, oponentePorta)`) com **mecanismo de retry** (até 20 tentativas com intervalo de 300 ms) para tolerar atrasos de escalonamento até que o oponente abra a porta.
  * **Passagem de Token Implícita (Turnos Estritos):** O canal TCP orientado a fluxo (`BufferedReader`/`PrintWriter`) funciona em modo alternado.
  * **Validação Anti-Cheat:**
    * Validação do número de sequência da rodada (`Numero`).
    * Validação da consistência do tabuleiro (`casaValida`).
    * Caso ocorra uma tentativa de trapaça ou mensagem inválida, o receptor devolve a mesma mensagem sem incrementar a rodada e sem atualizar seu tabuleiro local, alertando o usuário e exigindo a correção pelo oponente.

### 1.4 Comunicação em Grupo (One-to-Many) via IP Multicast UDP Nativo
* **Onde é usado:** Módulo `Display`.
* **Como funciona:**
  * Utiliza `java.net.MulticastSocket` e `DatagramPacket` sobre um endereço IP de Classe D (`225.1.1.NN`, porta `88NN`).
  * **Desacoplamento Espacial:** Os jogadores não precisam conhecer quantos espectadores existem nem seus IPs; basta enviar um único datagrama para o grupo multicast a cada jogada (`Display.transmitirJogada`).
  * **Múltiplos Observadores:** Qualquer número de instâncias do `Display` pode ingressar no grupo via `socket.joinGroup(...)` (`Display.ouvirMulticast`) para assistir à partida em tempo real.

### 1.5 Serialização Independente de Linguagem (JSON) e Replicação de Estado
* **Onde é usado:** Classes de modelo `RequisicaoMatch`, `RespostaMatch` e `MensagemJogada`.
* **Como funciona:**
  * Todas as mensagens trocadas via RMI, TCP e UDP são serializadas em **JSON (UTF-8)** usando **Gson**.
  * **Transferência de Estado Completo vs. Eventos:** Cada `MensagemJogada` carrega tanto o evento incremental (`Numero` e `Jogada` indicando a casa `0 a 8`) quanto o **estado completo do tabuleiro** (`int[9]`). Isso garante que um `Display` que entre no meio da partida ou perca um pacote UDP consiga renderizar o estado exato imediatamente na próxima jogada.

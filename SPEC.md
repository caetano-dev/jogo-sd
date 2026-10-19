SPEC: Sistema Distribuído de Connect Four (Lig 4) com RMI, Sockets e JGroups
1. Visão Geral do Sistema
O objetivo é implementar um sistema distribuído em Java para uma partida em turnos de Connect Four (Lig 4) entre dois jogadores, com infraestrutura de pareamento centralizada e transmissão em tempo real para módulos de exibição independentes (Displays).
O sistema adapta a arquitetura descrita na especificação de Sistemas Distribuídos:
1. Servidor de Match (RMI): Registra requisições de jogadores e realiza o pareamento (1x1).
2. Módulo Jogador (RMI + Socket P2P + JGroups): Busca oponente via RMI, troca jogadas por conexão Socket ponto a ponto e publica os eventos do tabuleiro no cluster de exibição.
3. Módulo Display (JGroups): Conecta-se ao canal de comunicação em grupo e renderiza o tabuleiro e as jogadas em tempo real.
O sistema deve operar localmente em diferentes terminais/sessões (usando 127.0.0.1 e portas distintas) e permitir a migração para as VMs do laboratório (152.92.236.x) por meio de argumentos CLI ou variáveis de ambiente.
2. Componentes e Tecnologias
⚬ Linguagem / Runtime: Java 17+ (ou 21).
⚬ Gerenciador de Build: Maven ou Gradle.
⚬ Comunicação de Matchmaking: Java RMI.
⚬ Comunicação entre Jogadores (P2P): Java Sockets (TCP preferencialmente para garantir confiabilidade de ordem e integridade de turnos, ou UDP com controle de entrega).
⚬ Canal Multicast / Grupo de Display: JGroups (JChannel), substituindo o socket multicast cru para garantir entrega confiável, isolamento por canal e compatibilidade sem problemas de roteamento/TTL.
⚬ Serialização de Mensagens: JSON (utilizar Jackson, Gson ou org.json).
3. Regras de Negócio: Connect Four (Lig 4)
⚬ Dimensões do Tabuleiro: Matriz de 6 linhas (0 a 5) por 7 colunas (0 a 6). Total de 42 posições. ⚬ O índice plano do tabuleiro varia de 0 a 41, mapeado como: \text{pos} = \text{linha} \times 7 + \text{coluna} (com linha 0 no topo e linha 5 na base, ou vice-versa).
⚬ Mecânica de Gravidade: ⚬ O jogador escolhe exclusivamente o índice da coluna (0 \le \text{coluna} \le 6). ⚬ A peça ocupa a linha de índice mais baixo livre na coluna (simulando a gravidade caindo no fundo da grade).
⚬ Condições de Encerramento: ⚬ Vitória (vit): 4 peças consecutivas do mesmo jogador em qualquer direção: horizontal, vertical ou diagonal (ascendente/descendente). ⚬ Empate (emp): Todas as 42 casas ocupadas sem vencedor. ⚬ Em andamento (jog): Partida continua.
⚬ Validação e Tolerância a Trapaças (Anti-Cheat): ⚬ O cliente deve validar localmente se a coluna está dentro do intervalo [0..6] e se possui espaço livre antes de enviar a jogada. ⚬ O receptor deve validar a jogada do oponente contra seu próprio estado local: ⚬ Se a jogada for inválida (coluna cheia, índice inválido ou número de sequência quebrado), o receptor devolve a mensagem sem avançar o turno, sinalizando erro. ⚬ O contador de jogadas (Numero) inicia em 0 e incrementa monotonicamente a cada turno.
4. Protocolo e Formato das Mensagens
4.1 Interface RMI (Servidor de Match)
⚬ Interface Remota: MatchServiceInterface extends java.rmi.Remote
⚬ Método: String getOponent(String jsonIn) throws java.rmi.RemoteException;
⚬ Payload de Entrada (jsonIn): {   "ip": "127.0.0.1",   "porta": 9001,   "protocolo": "tcp" }
⚬ Payload de Retorno (jsonRet): {   "ip": "127.0.0.1",   "porta": 9002,   "inicia": true }
⚬ Regra de Concorrência do Servidor: ⚬ O método getOponent é bloqueante (synchronized, CompletableFuture ou fila de espera com pares de threads). ⚬ Quando dois jogadores com o mesmo protocolo chegam à fila, o servidor forma o par: ⚬ Jogador A recebe os dados de B com "inicia": true. ⚬ Jogador B recebe os dados de A com "inicia": false. ⚬ Timeout configurável: se nenhum par for encontrado após N segundos (ex: 60s), retornar JSON de erro/timeout.
4.2 Protocolo P2P (Mensagem de Jogada entre Jogadores)
Enviado por socket TCP (ou UDP) a cada movimento:
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

⚬ Campos: ⚬ Numero (int): número sequencial da jogada (0 \le \text{Numero} < 42). ⚬ Jogada (int): coluna escolhida (0 \le \text{Jogada} \le 6). ⚬ Tabuleiro (int[42]): estado do tabuleiro (0 = \text{vazio}, 1 = \text{Jogador 1}, 2 = \text{Jogador 2}). ⚬ Resultado (string): "jog" (continua), "vit" (o remetente venceu), "der" (o remetente aceitou a derrota), "emp" (empate).
4.3 Protocolo Multicast JGroups (Canal de Display)
⚬ Nome do Cluster JGroups: ConnectFourDisplayCluster
⚬ Mensagem: O módulo Jogador publica a cada jogada (ou atualização de estado) uma mensagem no cluster JGroups contendo o JSON acima acrescido de metadados: {   "evento": "JOGADA",   "jogadorId": 1,   "numeroJogada": 5,   "coluna": 3,   "tabuleiro": [...],   "resultado": "jog" }
5. Arquitetura dos Módulos de Execução
5.1 Módulo MatchServer
⚬ Inicializa o LocateRegistry programaticamente na porta designada (default 9800 ou passada por CLI --port).
⚬ Expõe o serviço RMI com nome "MatchService".
⚬ Mantém controle de concorrência seguro (ConcurrentLinkedQueue ou monitor thread-safe) para emparelhar requisições ativas.
5.2 Módulo Player
Fluxo de execução:
1. Configuração inicial: Recebe via CLI ou input: ⚬ Endereço do Servidor RMI (--server-ip, --server-port). ⚬ Sua porta local para o socket P2P (--listen-port). ⚬ Configuração de rede JGroups (arquivo XML ou flags para bind local).
2. Matchmaking: ⚬ Invoca getOponent via RMI passando IP, porta e protocolo. ⚬ Bloqueia aguardando pareamento.
3. Estabelecimento de Conexão P2P: ⚬ Se "inicia": true: assume papel de conexão inicial (conecta no socket do oponente ou escuta como servidor P2P conforme estratégia de handshake), realiza a jogada 0 e envia ao oponente. ⚬ Se "inicia": false: aguarda o recebimento da jogada inicial.
4. Loop do Jogo: ⚬ Renderiza tabuleiro na CLI. ⚬ Ao jogar: calcula a física da coluna, atualiza tabuleiro local, despacha mensagem P2P para o oponente e envia mensagem JGroups para o canal de display. ⚬ Ao receber jogada: valida consistência. Se detectada vitória do oponente, emite mensagem "der" e finaliza.
5. Finalização: Fecha sockets e desconecta do canal JGroups.
5.3 Módulo Display
⚬ Cria um JChannel do JGroups apontando para o cluster ConnectFourDisplayCluster.
⚬ Implementa ReceiverAdapter (ou Receiver).
⚬ No callback receive(Message msg): ⚬ Desserializa o JSON da jogada. ⚬ Limpa a tela ou imprime linha separadora. ⚬ Renderiza o tabuleiro Connect Four em arte ASCII destacando peças de cada jogador (ex: [X], [O], [ ]). ⚬ Imprime o status da partida (Quem jogou, rodada atual, se houve vitória/empate).
6. Requisitos de Configuração de Rede e Execução Local
Para garantir a execução simultânea de múltiplos processos no mesmo computador sem conflitos de bind:
1. RMI: ⚬ Propriedade java.rmi.server.hostname deve ser explicitamente configurável (ex: -Djava.rmi.server.hostname=127.0.0.1 para testes locais ou o IP da VM em produção).
2. JGroups: ⚬ Usar configuração compatível com loopback (ex: udp.xml customizado ou pilha TCP usando tcp.xml) para que instâncias rodando na mesma máquina descubram o canal sem restrições de multicast do sistema operacional. ⚬ Permitir passagem de flag -Djgroups.bind_addr=127.0.0.1.
3. Parâmetros de Linha de Comando: ⚬ server: java -jar app.jar server --port 9800 ⚬ player 1: java -jar app.jar player --server-ip 127.0.0.1 --server-port 9800 --port 9001 ⚬ player 2: java -jar app.jar player --server-ip 127.0.0.1 --server-port 9800 --port 9002 ⚬ display: java -jar app.jar display (pode abrir quantas instâncias desejar)
7. Critérios de Aceitação e Testes
1. Teste de Pareamento RMI: ⚬ Iniciar o servidor e dois clientes jogadores com portas distintas. ⚬ Confirmar que ambos recebem o retorno do método RMI e que exatamente um recebe inicia = true.
2. Teste de Lógica do Connect Four: ⚬ Teste unitário para: ⚬ Inserção de peças com empilhamento correto (gravidade). ⚬ Rejeição de jogada em coluna com 6 peças já preenchidas. ⚬ Detecção de vitória nas 4 direções (linhas, colunas, diagonais principais e secundárias). ⚬ Detecção de empate ao atingir 42 jogadas.
3. Teste do Fluxo de Jogo P2P: ⚬ Alternância rigorosa de turnos entre os dois terminais de jogadores. ⚬ Encerramento limpo de conexões ao fim do jogo.
4. Teste de Transmissão para Displays: ⚬ Abrir 2 ou mais terminais rodando o módulo Display. ⚬ Confirmar que todos os displays refletem cada jogada simultaneamente através do JGroups.
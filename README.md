# Jogo da Velha Distribuído (Tic-Tac-Toe)

## Compilação

```bash
mvn clean package
```

Também é possível compilar diretamente com `javac`:
```powershell
javac --release 17 -encoding UTF-8 -cp "target/lib/*" -d target/classes (Get-ChildItem -Recurse src/main/java -Filter *.java | % FullName)
```

## Instruções de Execução

### Execução Local (execução na MV mais abaixo)

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

**Terminal 3: Jogador 1**
```bash
java -jar app.jar player --server-port 9814 --port 9001 --id 14
```

**Terminal 4: Jogador 2**
```bash
java -jar app.jar player --server-port 9814 --port 9002 --id 14
```

### Execução em VMs (`152.92.236.x`)

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

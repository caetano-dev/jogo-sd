# Instruções de Desinstalação dos Pacotes Instalados

Este documento descreve exatamente como remover todas as ferramentas e dependências que foram instaladas no sistema para o desenvolvimento e execução do jogo distribuído de Connect Four (Lig 4).

> [!NOTE]
> Nenhum dos pacotes listados abaixo estava instalado no sistema antes desta operação. Pacotes pré-existentes (como Docker, Python, Go, Node, Neovim, etc.) **não** foram afetados e **não** serão removidos.

---

## 1. O Que Foi Instalado

Para atender aos requisitos descritos no [SPEC.md](file:///Users/pedrocaetano/Documents/UERJ/Siste%CC%80mes%20Distribue%CC%81s/trabalho%20jogo/SPEC.md) (Java 17+/21, Maven, RMI, Sockets e JGroups), foram instalados:

1. **Compiladores e Runtimes Java**:
   - `openjdk@21` (OpenJDK 21 LTS — versão recomendada na especificação)
   - `openjdk` (OpenJDK 27 — dependência direta do Maven no Homebrew)
2. **Gerenciador de Build**:
   - `maven` (Apache Maven 3.10.0)
3. **Bibliotecas de Suporte do Homebrew** (instaladas como dependências do OpenJDK e Maven):
   - `cairo`, `fontconfig`, `freetype`, `glib`, `graphite2`, `harfbuzz`, `libx11`, `libxau`, `libxcb`, `libxdmcp`, `libxext`, `libxrender`, `little-cms2`, `lzo`, `pixman`, `xorgproto`
4. **Symlinks de Conveniência em `/opt/homebrew/bin`**:
   - `/opt/homebrew/bin/java` -> `/opt/homebrew/opt/openjdk@21/bin/java`
   - `/opt/homebrew/bin/javac` -> `/opt/homebrew/opt/openjdk@21/bin/javac`
   - `/opt/homebrew/bin/jar` -> `/opt/homebrew/opt/openjdk@21/bin/jar`

---

## 2. Como Desinstalar Tudo (Passo a Passo)

### Passo 1: Remover os symlinks criados em `/opt/homebrew/bin`

Execute o comando abaixo no terminal:

```bash
rm -f /opt/homebrew/bin/java /opt/homebrew/bin/javac /opt/homebrew/bin/jar
```

### Passo 2: Desinstalar os pacotes via Homebrew

Execute o comando de desinstalação contendo exclusivamente os pacotes que foram adicionados:

```bash
brew uninstall --ignore-dependencies \
  maven \
  openjdk \
  openjdk@21 \
  cairo \
  fontconfig \
  freetype \
  glib \
  graphite2 \
  harfbuzz \
  libx11 \
  libxau \
  libxcb \
  libxdmcp \
  libxext \
  libxrender \
  little-cms2 \
  lzo \
  pixman \
  xorgproto
```

### Passo 3 (Opcional): Remover cache local do Maven

Se você compilou o projeto com Maven, ele armazena downloads de bibliotecas (`.jar`) em `~/.m2`. Para apagá-los:

```bash
rm -rf ~/.m2
```

---

## 3. Comando Único para Desinstalação Rápida

Se desejar remover tudo de uma só vez, copie e cole este comando único no terminal:

```bash
rm -f /opt/homebrew/bin/java /opt/homebrew/bin/javac /opt/homebrew/bin/jar && \
brew uninstall --ignore-dependencies \
  maven openjdk openjdk@21 \
  cairo fontconfig freetype glib graphite2 harfbuzz \
  libx11 libxau libxcb libxdmcp libxext libxrender \
  little-cms2 lzo pixman xorgproto
```

---

## 4. Como Verificar que Nada Sobrou

Para confirmar que nenhum desses pacotes continua instalado, execute:

```bash
brew list --formula | grep -E '^(maven|openjdk|openjdk@21|cairo|fontconfig|freetype|glib|graphite2|harfbuzz|libx11|libxau|libxcb|libxdmcp|libxext|libxrender|little-cms2|lzo|pixman|xorgproto)$'
```

Se o comando não retornar nenhuma linha, a remoção foi completa e limpa.

package uerj.sd.jogo;

import java.util.Arrays;

/**
 * Representa o tabuleiro do jogo Connect Four (Lig 4).
 * Dimensões: 6 linhas (0 a 5, sendo 0 o topo e 5 a base) por 7 colunas (0 a 6).
 * Total de 42 posições mapeadas como: pos = linha * 7 + coluna.
 */
public class Tabuleiro {
    public static final int LINHAS = 6;
    public static final int COLUNAS = 7;
    public static final int TOTAL_CASAS = LINHAS * COLUNAS; // 42

    public static final int VAZIO = 0;
    public static final int JOGADOR_1 = 1;
    public static final int JOGADOR_2 = 2;

    private final int[] grade;

    public Tabuleiro() {
        this.grade = new int[TOTAL_CASAS];
    }

    public Tabuleiro(int[] gradeExistente) {
        if (gradeExistente == null || gradeExistente.length != TOTAL_CASAS) {
            throw new IllegalArgumentException("O tabuleiro deve conter exatamente " + TOTAL_CASAS + " posições.");
        }
        this.grade = Arrays.copyOf(gradeExistente, TOTAL_CASAS);
    }

    /**
     * Retorna uma cópia do array plano da grade.
     */
    public int[] getGrade() {
        return Arrays.copyOf(grade, TOTAL_CASAS);
    }

    /**
     * Obtém o valor em uma linha e coluna específicas.
     */
    public int getValor(int linha, int coluna) {
        if (linha < 0 || linha >= LINHAS || coluna < 0 || coluna >= COLUNAS) {
            throw new IndexOutOfBoundsException("Coordenada fora do tabuleiro: [" + linha + ", " + coluna + "]");
        }
        return grade[linha * COLUNAS + coluna];
    }

    /**
     * Verifica se a coluna é válida e se possui espaço para mais uma peça.
     */
    public boolean colunaValida(int coluna) {
        if (coluna < 0 || coluna >= COLUNAS) {
            return false;
        }
        // Linha 0 é o topo da coluna. Se estiver ocupada, a coluna está cheia.
        return grade[coluna] == VAZIO;
    }

    /**
     * Aplica a gravidade: insere a peça do jogador na posição livre mais baixa da coluna.
     * Retorna o índice da linha onde a peça caiu, ou -1 se a coluna estiver cheia.
     */
    public int soltarPeca(int coluna, int idJogador) {
        if (coluna < 0 || coluna >= COLUNAS) {
            return -1;
        }
        for (int linha = LINHAS - 1; linha >= 0; linha--) {
            int indice = linha * COLUNAS + coluna;
            if (grade[indice] == VAZIO) {
                grade[indice] = idJogador;
                return linha;
            }
        }
        return -1; // Coluna cheia
    }

    /**
     * Verifica se o jogador indicado possui 4 peças alinhadas (horizontal, vertical ou diagonal).
     */
    public boolean verificarVitoria(int idJogador) {
        // 1. Horizontal
        for (int r = 0; r < LINHAS; r++) {
            for (int c = 0; c <= COLUNAS - 4; c++) {
                if (grade[r * COLUNAS + c] == idJogador &&
                    grade[r * COLUNAS + c + 1] == idJogador &&
                    grade[r * COLUNAS + c + 2] == idJogador &&
                    grade[r * COLUNAS + c + 3] == idJogador) {
                    return true;
                }
            }
        }

        // 2. Vertical
        for (int r = 0; r <= LINHAS - 4; r++) {
            for (int c = 0; c < COLUNAS; c++) {
                if (grade[r * COLUNAS + c] == idJogador &&
                    grade[(r + 1) * COLUNAS + c] == idJogador &&
                    grade[(r + 2) * COLUNAS + c] == idJogador &&
                    grade[(r + 3) * COLUNAS + c] == idJogador) {
                    return true;
                }
            }
        }

        // 3. Diagonal descendente (\)
        for (int r = 0; r <= LINHAS - 4; r++) {
            for (int c = 0; c <= COLUNAS - 4; c++) {
                if (grade[r * COLUNAS + c] == idJogador &&
                    grade[(r + 1) * COLUNAS + c + 1] == idJogador &&
                    grade[(r + 2) * COLUNAS + c + 2] == idJogador &&
                    grade[(r + 3) * COLUNAS + c + 3] == idJogador) {
                    return true;
                }
            }
        }

        // 4. Diagonal ascendente (/)
        for (int r = 3; r < LINHAS; r++) {
            for (int c = 0; c <= COLUNAS - 4; c++) {
                if (grade[r * COLUNAS + c] == idJogador &&
                    grade[(r - 1) * COLUNAS + c + 1] == idJogador &&
                    grade[(r - 2) * COLUNAS + c + 2] == idJogador &&
                    grade[(r - 3) * COLUNAS + c + 3] == idJogador) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Verifica se o tabuleiro está completamente preenchido (todas as 42 casas ocupadas).
     */
    public boolean estaCheio() {
        for (int c = 0; c < COLUNAS; c++) {
            if (grade[c] == VAZIO) {
                return false;
            }
        }
        return true;
    }

    /**
     * Clona o estado atual do tabuleiro.
     */
    public Tabuleiro clonar() {
        return new Tabuleiro(this.grade);
    }

    /**
     * Renderiza o tabuleiro em arte ASCII.
     */
    public String renderizarAscii() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n  0   1   2   3   4   5   6\n");
        sb.append("+---+---+---+---+---+---+---+\n");
        for (int r = 0; r < LINHAS; r++) {
            sb.append("|");
            for (int c = 0; c < COLUNAS; c++) {
                int valor = grade[r * COLUNAS + c];
                String simbolo;
                if (valor == JOGADOR_1) {
                    simbolo = " X ";
                } else if (valor == JOGADOR_2) {
                    simbolo = " O ";
                } else {
                    simbolo = "   ";
                }
                sb.append(simbolo).append("|");
            }
            sb.append("\n+---+---+---+---+---+---+---+\n");
        }
        sb.append("  0   1   2   3   4   5   6\n");
        return sb.toString();
    }
}

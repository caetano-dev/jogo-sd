package uerj.sd.jogo;

import java.util.Arrays;

/**
 * Representa o tabuleiro do Jogo da Velha (conforme o enunciado do trabalho).
 * Dimensões: 3 linhas (0 a 2) por 3 colunas (0 a 2).
 * Total de 9 casas mapeadas como: casa = linha * 3 + coluna  (casas [0..8]).
 */
public class Tabuleiro {
    public static final int LINHAS = 3;
    public static final int COLUNAS = 3;
    public static final int TOTAL_CASAS = LINHAS * COLUNAS; // 9

    public static final int VAZIO = 0;
    public static final int JOGADOR_1 = 1;
    public static final int JOGADOR_2 = 2;

    /** Todas as combinações vencedoras (3 linhas, 3 colunas, 2 diagonais). */
    private static final int[][] LINHAS_VITORIA = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
            {0, 4, 8}, {2, 4, 6}
    };

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
     * Converte (linha, coluna) no índice da casa [0..8].
     */
    public static int paraCasa(int linha, int coluna) {
        return linha * COLUNAS + coluna;
    }

    /**
     * Obtém o valor em uma linha e coluna específicas.
     */
    public int getValor(int linha, int coluna) {
        if (linha < 0 || linha >= LINHAS || coluna < 0 || coluna >= COLUNAS) {
            throw new IndexOutOfBoundsException("Coordenada fora do tabuleiro: [" + linha + ", " + coluna + "]");
        }
        return grade[paraCasa(linha, coluna)];
    }

    /**
     * Verifica se a casa está dentro do tabuleiro e livre.
     */
    public boolean casaValida(int casa) {
        return casa >= 0 && casa < TOTAL_CASAS && grade[casa] == VAZIO;
    }

    /**
     * Marca a casa com a peça do jogador. Retorna true se a jogada foi aplicada.
     */
    public boolean jogar(int casa, int idJogador) {
        if (!casaValida(casa)) {
            return false;
        }
        grade[casa] = idJogador;
        return true;
    }

    /**
     * Desfaz uma jogada em uma casa específica, voltando ao estado VAZIO.
     */
    public void desfazer(int casa) {
        if (casa >= 0 && casa < TOTAL_CASAS) {
            grade[casa] = VAZIO;
        }
    }

    /**
     * Verifica se o jogador possui 3 peças alinhadas (linha, coluna ou diagonal).
     */
    public boolean verificarVitoria(int idJogador) {
        for (int[] l : LINHAS_VITORIA) {
            if (grade[l[0]] == idJogador && grade[l[1]] == idJogador && grade[l[2]] == idJogador) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica se todas as 9 casas estão ocupadas.
     */
    public boolean estaCheio() {
        for (int v : grade) {
            if (v == VAZIO) {
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
     * Renderiza o tabuleiro em arte ASCII, com índices de linha e coluna.
     */
    public String renderizarAscii() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n     0   1   2\n");
        sb.append("   +---+---+---+\n");
        for (int r = 0; r < LINHAS; r++) {
            sb.append(' ').append(r).append(" |");
            for (int c = 0; c < COLUNAS; c++) {
                int valor = grade[paraCasa(r, c)];
                String simbolo = valor == JOGADOR_1 ? " X " : valor == JOGADOR_2 ? " O " : "   ";
                sb.append(simbolo).append("|");
            }
            sb.append("\n   +---+---+---+\n");
        }
        return sb.toString();
    }
}

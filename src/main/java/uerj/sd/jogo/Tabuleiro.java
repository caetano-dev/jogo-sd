package uerj.sd.jogo;

import java.util.Arrays;

public class Tabuleiro {
    public static final int LINHAS = 3;
    public static final int COLUNAS = 3;
    public static final int TOTAL_CASAS = LINHAS * COLUNAS;

    public static final int VAZIO = 0;
    public static final int JOGADOR_1 = 1;
    public static final int JOGADOR_2 = 2;

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

    public int[] getGrade() {
        return Arrays.copyOf(grade, TOTAL_CASAS);
    }

    public static int paraCasa(int linha, int coluna) {
        return linha * COLUNAS + coluna;
    }

    public boolean casaValida(int casa) {
        return casa >= 0 && casa < TOTAL_CASAS && grade[casa] == VAZIO;
    }

    public boolean jogar(int casa, int idJogador) {
        if (!casaValida(casa)) {
            return false;
        }
        grade[casa] = idJogador;
        return true;
    }

    public void desfazer(int casa) {
        if (casa >= 0 && casa < TOTAL_CASAS) {
            grade[casa] = VAZIO;
        }
    }

    public boolean verificarVitoria(int idJogador) {
        for (int[] l : LINHAS_VITORIA) {
            if (grade[l[0]] == idJogador && grade[l[1]] == idJogador && grade[l[2]] == idJogador) {
                return true;
            }
        }
        return false;
    }

    public boolean estaCheio() {
        for (int v : grade) {
            if (v == VAZIO) {
                return false;
            }
        }
        return true;
    }

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

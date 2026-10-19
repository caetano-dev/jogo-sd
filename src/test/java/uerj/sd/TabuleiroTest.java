package uerj.sd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uerj.sd.jogo.Tabuleiro;

import static org.junit.jupiter.api.Assertions.*;

public class TabuleiroTest {

    private Tabuleiro tabuleiro;

    @BeforeEach
    public void setup() {
        tabuleiro = new Tabuleiro();
    }

    @Test
    public void testEmpilhamentoGravidade() {
        int col = 3;
        // Solta 6 peças na coluna 3
        for (int i = 0; i < Tabuleiro.LINHAS; i++) {
            assertTrue(tabuleiro.colunaValida(col));
            int linha = tabuleiro.soltarPeca(col, Tabuleiro.JOGADOR_1);
            assertEquals(Tabuleiro.LINHAS - 1 - i, linha, "A peça deve ocupar da base para o topo");
        }

        // 7ª tentativa deve falhar
        assertFalse(tabuleiro.colunaValida(col));
        int linhaFalha = tabuleiro.soltarPeca(col, Tabuleiro.JOGADOR_1);
        assertEquals(-1, linhaFalha);
    }

    @Test
    public void testVitoriaHorizontal() {
        // Jogador 1 insere nas colunas 0, 1, 2, 3
        for (int c = 0; c < 3; c++) {
            tabuleiro.soltarPeca(c, Tabuleiro.JOGADOR_1);
            assertFalse(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_1));
        }
        tabuleiro.soltarPeca(3, Tabuleiro.JOGADOR_1);
        assertTrue(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_1));
        assertFalse(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_2));
    }

    @Test
    public void testVitoriaVertical() {
        int col = 2;
        for (int i = 0; i < 3; i++) {
            tabuleiro.soltarPeca(col, Tabuleiro.JOGADOR_2);
            assertFalse(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_2));
        }
        tabuleiro.soltarPeca(col, Tabuleiro.JOGADOR_2);
        assertTrue(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_2));
        assertFalse(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_1));
    }

    @Test
    public void testVitoriaDiagonalDescendente() {
        /*
          Montar uma diagonal descendente (\) para Jogador 1:
          col 0: 3 peças (linha 5=O, 4=O, 3=X)
          col 1: 2 peças (linha 5=O, 4=X)
          col 2: 1 peça  (linha 5=X)
          Wait, let's verify positions:
          linha 2, col 0; linha 3, col 1; linha 4, col 2; linha 5, col 3:
          Col 0 needs 4 pieces (linha 5, 4, 3, 2). Linha 2 is X.
          Col 1 needs 3 pieces (linha 5, 4, 3). Linha 3 is X.
          Col 2 needs 2 pieces (linha 5, 4). Linha 4 is X.
          Col 3 needs 1 piece  (linha 5). Linha 5 is X.
        */
        // Col 0: base 5, 4, 3 com O, linha 2 com X
        tabuleiro.soltarPeca(0, Tabuleiro.JOGADOR_2);
        tabuleiro.soltarPeca(0, Tabuleiro.JOGADOR_2);
        tabuleiro.soltarPeca(0, Tabuleiro.JOGADOR_2);
        tabuleiro.soltarPeca(0, Tabuleiro.JOGADOR_1); // linha 2

        // Col 1: base 5, 4 com O, linha 3 com X
        tabuleiro.soltarPeca(1, Tabuleiro.JOGADOR_2);
        tabuleiro.soltarPeca(1, Tabuleiro.JOGADOR_2);
        tabuleiro.soltarPeca(1, Tabuleiro.JOGADOR_1); // linha 3

        // Col 2: base 5 com O, linha 4 com X
        tabuleiro.soltarPeca(2, Tabuleiro.JOGADOR_2);
        tabuleiro.soltarPeca(2, Tabuleiro.JOGADOR_1); // linha 4

        // Col 3: base 5 com X
        assertFalse(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_1));
        tabuleiro.soltarPeca(3, Tabuleiro.JOGADOR_1); // linha 5

        assertTrue(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_1));
    }

    @Test
    public void testVitoriaDiagonalAscendente() {
        /*
          Montar diagonal ascendente (/) para Jogador 2:
          linha 5, col 0; linha 4, col 1; linha 3, col 2; linha 2, col 3
          Col 0: 1 peça (linha 5=O)
          Col 1: 2 peças (linha 5=X, linha 4=O)
          Col 2: 3 peças (linha 5=X, linha 4=X, linha 3=O)
          Col 3: 4 peças (linha 5=X, linha 4=X, linha 3=X, linha 2=O)
        */
        tabuleiro.soltarPeca(0, Tabuleiro.JOGADOR_2); // linha 5

        tabuleiro.soltarPeca(1, Tabuleiro.JOGADOR_1);
        tabuleiro.soltarPeca(1, Tabuleiro.JOGADOR_2); // linha 4

        tabuleiro.soltarPeca(2, Tabuleiro.JOGADOR_1);
        tabuleiro.soltarPeca(2, Tabuleiro.JOGADOR_1);
        tabuleiro.soltarPeca(2, Tabuleiro.JOGADOR_2); // linha 3

        tabuleiro.soltarPeca(3, Tabuleiro.JOGADOR_1);
        tabuleiro.soltarPeca(3, Tabuleiro.JOGADOR_1);
        tabuleiro.soltarPeca(3, Tabuleiro.JOGADOR_1);
        assertFalse(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_2));

        tabuleiro.soltarPeca(3, Tabuleiro.JOGADOR_2); // linha 2

        assertTrue(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_2));
    }

    @Test
    public void testTabuleiroCheioEmpate() {
        // Preenche o tabuleiro de forma alternada para não gerar 4 em linha
        // Padrão que evita 4 em linha:
        // Colunas 0, 1, 2 preenchidas com 1 1 2 2 1 1 etc.
        // Ou simplesmente preencher manualmente todas as 42 casas
        int[] gradeEmpate = new int[Tabuleiro.TOTAL_CASAS];
        for (int r = 0; r < Tabuleiro.LINHAS; r++) {
            for (int c = 0; c < Tabuleiro.COLUNAS; c++) {
                // Alterna jogadores
                gradeEmpate[r * Tabuleiro.COLUNAS + c] = ((r + c) % 2) + 1;
            }
        }
        Tabuleiro t = new Tabuleiro(gradeEmpate);
        assertTrue(t.estaCheio());
    }
}

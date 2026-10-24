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
    public void testJogadaValidaEOcupada() {
        int casa = Tabuleiro.paraCasa(1, 1); // centro: 4
        assertTrue(tabuleiro.casaValida(casa));
        assertTrue(tabuleiro.jogar(casa, Tabuleiro.JOGADOR_1));

        // Casa já ocupada deve ser inválida
        assertFalse(tabuleiro.casaValida(casa));
        assertFalse(tabuleiro.jogar(casa, Tabuleiro.JOGADOR_2));

        // Desfazer jogada
        tabuleiro.desfazer(casa);
        assertTrue(tabuleiro.casaValida(casa));
    }

    @Test
    public void testVitoriaHorizontal() {
        // Linha 0 preenchida pelo Jogador 1 (casas 0, 1, 2)
        tabuleiro.jogar(Tabuleiro.paraCasa(0, 0), Tabuleiro.JOGADOR_1);
        tabuleiro.jogar(Tabuleiro.paraCasa(0, 1), Tabuleiro.JOGADOR_1);
        assertFalse(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_1));

        tabuleiro.jogar(Tabuleiro.paraCasa(0, 2), Tabuleiro.JOGADOR_1);
        assertTrue(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_1));
        assertFalse(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_2));
    }

    @Test
    public void testVitoriaVertical() {
        // Coluna 2 preenchida pelo Jogador 2 (casas 2, 5, 8)
        tabuleiro.jogar(Tabuleiro.paraCasa(0, 2), Tabuleiro.JOGADOR_2);
        tabuleiro.jogar(Tabuleiro.paraCasa(1, 2), Tabuleiro.JOGADOR_2);
        assertFalse(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_2));

        tabuleiro.jogar(Tabuleiro.paraCasa(2, 2), Tabuleiro.JOGADOR_2);
        assertTrue(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_2));
        assertFalse(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_1));
    }

    @Test
    public void testVitoriaDiagonalPrincipal() {
        // Diagonal principal: (0,0), (1,1), (2,2) -> casas 0, 4, 8
        tabuleiro.jogar(Tabuleiro.paraCasa(0, 0), Tabuleiro.JOGADOR_1);
        tabuleiro.jogar(Tabuleiro.paraCasa(1, 1), Tabuleiro.JOGADOR_1);
        assertFalse(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_1));

        tabuleiro.jogar(Tabuleiro.paraCasa(2, 2), Tabuleiro.JOGADOR_1);
        assertTrue(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_1));
    }

    @Test
    public void testVitoriaDiagonalSecundaria() {
        // Diagonal secundária: (0,2), (1,1), (2,0) -> casas 2, 4, 6
        tabuleiro.jogar(Tabuleiro.paraCasa(0, 2), Tabuleiro.JOGADOR_2);
        tabuleiro.jogar(Tabuleiro.paraCasa(1, 1), Tabuleiro.JOGADOR_2);
        assertFalse(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_2));

        tabuleiro.jogar(Tabuleiro.paraCasa(2, 0), Tabuleiro.JOGADOR_2);
        assertTrue(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_2));
    }

    @Test
    public void testTabuleiroCheioEmpate() {
        /*
           Grade de velha sem vencedor:
           X | O | X
           X | O | O
           O | X | X
        */
        int[] gradeEmpate = {
                Tabuleiro.JOGADOR_1, Tabuleiro.JOGADOR_2, Tabuleiro.JOGADOR_1,
                Tabuleiro.JOGADOR_1, Tabuleiro.JOGADOR_2, Tabuleiro.JOGADOR_2,
                Tabuleiro.JOGADOR_2, Tabuleiro.JOGADOR_1, Tabuleiro.JOGADOR_1
        };
        Tabuleiro t = new Tabuleiro(gradeEmpate);
        assertTrue(t.estaCheio());
        assertFalse(t.verificarVitoria(Tabuleiro.JOGADOR_1));
        assertFalse(t.verificarVitoria(Tabuleiro.JOGADOR_2));
    }
}

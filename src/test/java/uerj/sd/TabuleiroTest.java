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
        int casa = Tabuleiro.paraCasa(1, 1);
        assertTrue(tabuleiro.casaValida(casa));
        assertTrue(tabuleiro.jogar(casa, Tabuleiro.JOGADOR_1));

        assertFalse(tabuleiro.casaValida(casa));
        assertFalse(tabuleiro.jogar(casa, Tabuleiro.JOGADOR_2));

        tabuleiro.desfazer(casa);
        assertTrue(tabuleiro.casaValida(casa));
    }

    @Test
    public void testVitoriaHorizontal() {
        tabuleiro.jogar(Tabuleiro.paraCasa(0, 0), Tabuleiro.JOGADOR_1);
        tabuleiro.jogar(Tabuleiro.paraCasa(0, 1), Tabuleiro.JOGADOR_1);
        assertFalse(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_1));

        tabuleiro.jogar(Tabuleiro.paraCasa(0, 2), Tabuleiro.JOGADOR_1);
        assertTrue(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_1));
        assertFalse(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_2));
    }

    @Test
    public void testVitoriaVertical() {
        tabuleiro.jogar(Tabuleiro.paraCasa(0, 2), Tabuleiro.JOGADOR_2);
        tabuleiro.jogar(Tabuleiro.paraCasa(1, 2), Tabuleiro.JOGADOR_2);
        assertFalse(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_2));

        tabuleiro.jogar(Tabuleiro.paraCasa(2, 2), Tabuleiro.JOGADOR_2);
        assertTrue(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_2));
        assertFalse(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_1));
    }

    @Test
    public void testVitoriaDiagonalPrincipal() {
        tabuleiro.jogar(Tabuleiro.paraCasa(0, 0), Tabuleiro.JOGADOR_1);
        tabuleiro.jogar(Tabuleiro.paraCasa(1, 1), Tabuleiro.JOGADOR_1);
        assertFalse(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_1));

        tabuleiro.jogar(Tabuleiro.paraCasa(2, 2), Tabuleiro.JOGADOR_1);
        assertTrue(tabuleiro.verificarVitoria(Tabuleiro.JOGADOR_1));
    }

    @Test
    public void testVitoriaDiagonalSecundaria() {
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

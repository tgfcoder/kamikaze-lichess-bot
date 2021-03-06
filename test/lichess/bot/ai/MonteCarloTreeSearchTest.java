package lichess.bot.ai;

import chesslib.Board;
import chesslib.Square;
import chesslib.move.Move;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.Arrays;

import static chesslib.Square.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class MonteCarloTreeSearchTest {
    @Test
    public void makesWinningMove() {
        // Given a state whereby exactly 2 of 4 possible moves wins the game
        // See: figures/fig1.png
        Board state = new Board();
        state.loadFromFen("8/8/8/8/8/8/1N6/r7 w - -");

        MonteCarloTreeSearch mcts = new MonteCarloTreeSearch(state);

        // When I search for the best move
        Move bestMove = mcts.findBestMove(200L);

        // Then the move selected is the winning move
        assertThat(bestMove.getFrom(), is(Square.B2));
        assertThat(bestMove.getTo(), anyOf(is(Square.A4), is(Square.D1))); // Both moves win, black is forced to take.
    }

    @Test
    public void findsForcedWin() {
        // Given a state whereby a series of moves can guarantee a win
        // See: figures/fig2.png
        Board state = new Board();
        state.loadFromFen("6b1/8/8/8/8/8/3PP3/7R w - -");

        MonteCarloTreeSearch mcts = new MonteCarloTreeSearch(state);

        // When I search for the best moves
        // Then the move selected is the winning move for each turn
        assertNextMove(mcts, H1, H7);
        mcts.applyMove(new Move(G8, H7));
        assertNextMove(mcts, E2, E4);
        mcts.applyMove(new Move(H7, E4));
        assertNextMove(mcts, D2, D3);
    }
    @Test
    public void doesNotLose() {
        // Given a state whereby there are two winning moves, and all other moves lose immediately
        // See: figures/fig3.png
        Board state = new Board();
        state.loadFromFen("8/8/8/2R5/5r2/8/8/8 w - -");

        MonteCarloTreeSearch mcts = new MonteCarloTreeSearch(state);

        // When I search for the best move
        // Then the move returned is the winning move
        Move bestMove = mcts.findBestMove(200L);
        assertThat(bestMove.getFrom(), is(Square.C5));
        assertThat(bestMove.getTo(), anyOf(is(Square.C4), is(Square.F5)));
    }

    @Test
    public void winsWithLargeSearchSpace() {
        // Given a state where there is one obvious way to win but many possible moves that do not force win
        // See: figures/fig4.png
        Board state = new Board();
        state.loadFromFen("8/7p/8/6Q1/5Q2/4Q3/3Q4/2Q5 w - -");

        MonteCarloTreeSearch mcts = new MonteCarloTreeSearch(state);

        // When I search for the best moves
        // Then the move selected is the winning move for each turn
        assertNextMove(mcts, G5, G6);
        mcts.applyMove(new Move(H7, G6));
        assertNextMove(mcts, F4, F5);
        mcts.applyMove(new Move(G6, F5));
        assertNextMove(mcts, E3, E4);
        mcts.applyMove(new Move(F5, E4));
        assertNextMove(mcts, D2, D3);
        mcts.applyMove(new Move(E4, D3));
        assertNextMove(mcts, C1, C2);
    }

    @Test
    public void drawState() {
        // Given a node that was considered in a real game
        Board state = new Board();
        state.loadFromFen("r5n1/pp1b4/4p3/B7/P7/7p/2P1PK2/RN6 b - a3 0 21");
        // See: figures/fig5.png
        // When I check to compute if this is a draw state
        // Then the answer should be false
        assertFalse(MonteCarloTreeSearch.gameIsDraw(state));
    }

    @Test
    public void computesNumberOfThreats() {
        // Given a state where white has 10 moves that capture a black piece
        Board state = new Board();
        state.loadFromFen("8/8/8/4N3/8/3rQp2/1N1KP1P1/1B6 w - -");
        // See: figures/fig6.png

        // When I compute the number of threats
        // Then I expect it to return 10
        assertThat(MonteCarloTreeSearch.getNumberOfThreats(state), is(10));
    }

    @Test
    public void doesNotMakeStupidMove() {
        Board b = new Board();
        b.loadFromFen("8/8/8/8/4K2p/8/4NP1P/8 w - - 0 23");
        MonteCarloTreeSearch mcts = new MonteCarloTreeSearch(b);
        Move bestMove = mcts.findBestMove(500);
        assertThat(bestMove.toString(), not(is("h2h3")));
    }

    private void assertNextMove(MonteCarloTreeSearch mcts, Square from, Square... to) {
        Move bestMove = mcts.findBestMove(500L);
        if (to.length == 1) {
            assertThat(bestMove, is(new Move(from, to[0])));
        } else {
            assertThat(bestMove.getFrom(), is(from));
            assertThat(bestMove.getTo(), anyOf((Matcher[])Arrays.stream(to).map(CoreMatchers::is).toArray()));
        }
        mcts.applyMove(new Move(from, bestMove.getTo()));
    }
}
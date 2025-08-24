import java.io.*;
import java.util.*;

public class Othello {
    int turn;
    int winner;
    int board[][];
    // directions: 8 neighbors (row, col)
    private static final int[] dr = {-1,-1,-1, 0, 0, 1, 1, 1};
    private static final int[] dc = {-1, 0, 1,-1, 1,-1, 0, 1};

    public Othello(String filename) throws Exception {
        File file = new File(filename);
        Scanner sc = new Scanner(file);
        turn = sc.nextInt();
        board = new int[8][8];
        for(int i = 0; i < 8; ++i) {
            for(int j = 0; j < 8; ++j){
                board[i][j] = sc.nextInt();
            }
        }
        winner = -1;
        sc.close();
    }

    // ---------------- Helper functions ----------------

    // Returns true if (r,c) is inside the board
    private boolean onBoard(int r, int c) {
        return r >= 0 && r < 8 && c >= 0 && c < 8;
    }

    // Returns list of positions (as integers i*8 + j) that would be flipped if player places at (r,c).
    // If no flips, returns empty list.
    private ArrayList<Integer> tilesToFlip(int[][] b, int r, int c, int player) {
        ArrayList<Integer> flips = new ArrayList<>();
        if (b[r][c] != -1) return flips; // must be empty tile

        int opp = 1 - player;
        for (int d = 0; d < 8; ++d) {
            int nr = r + dr[d], nc = c + dc[d];
            ArrayList<Integer> seq = new ArrayList<>();
            // Must first encounter at least one opponent tile
            while (onBoard(nr, nc) && b[nr][nc] == opp) {
                seq.add(nr * 8 + nc);
                nr += dr[d]; nc += dc[d];
            }
            if (onBoard(nr, nc) && b[nr][nc] == player && !seq.isEmpty()) {
                // this direction will flip seq
                flips.addAll(seq);
            }
        }
        return flips;
    }

    // Returns list of valid move positions (i*8 + j) for given player on board b
    private ArrayList<Integer> validMoves(int[][] b, int player) {
        ArrayList<Integer> moves = new ArrayList<>();
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                ArrayList<Integer> flips = tilesToFlip(b, i, j, player);
                if (!flips.isEmpty()) moves.add(i * 8 + j);
            }
        }
        return moves;
    }

    // Applies move at pos (i*8+j) for player on board b (modifies b) and returns true if move applied.
    // The method assumes the move is valid (will flip at least one); it will place the tile and flip.
    private void applyMoveInPlace(int[][] b, int pos, int player) {
        int r = pos / 8, c = pos % 8;
        ArrayList<Integer> flips = tilesToFlip(b, r, c, player);
        if (flips.isEmpty()) return; // nothing to do (shouldn't happen if used correctly)
        b[r][c] = player;
        for (int p : flips) {
            int fr = p / 8, fc = p % 8;
            b[fr][fc] = player;
        }
    }

    // Return a deep copy of board b
    private int[][] copyBoard(int[][] b) {
        int[][] cp = new int[8][8];
        for (int i = 0; i < 8; ++i) System.arraycopy(b[i], 0, cp[i], 0, 8);
        return cp;
    }

    // Check if board is full
    private boolean boardFull(int[][] b) {
        for (int i = 0; i < 8; ++i)
            for (int j = 0; j < 8; ++j)
                if (b[i][j] == -1) return false;
        return true;
    }

    // Check if there is any valid move for either player
    private boolean anyMoves(int[][] b) {
        return !validMoves(b, 0).isEmpty() || !validMoves(b, 1).isEmpty();
    }

    // ---------------- Public required functions ----------------

    public int boardScore() {
        /* Return num_black - num_white if original (instance) turn==0,
         * else num_white - num_black.
         * This always evaluates with respect to the original 'turn' variable (do not change it).
         */
        int black = 0, white = 0;
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                if (board[i][j] == 0) black++;
                else if (board[i][j] == 1) white++;
            }
        }
        if (turn == 0) return black - white;
        else return white - black;
    }

    // Overloaded helper: evaluate arbitrary board state w.r.t. original 'turn'
    private int boardScoreFor(int[][] b) {
        int black = 0, white = 0;
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                if (b[i][j] == 0) black++;
                else if (b[i][j] == 1) white++;
            }
        }
        if (turn == 0) return black - white;
        else return white - black;
    }

    // Minimax recursion: returns score (int) for board 'b' at current depth, with currentPlayer to move.
    // depth is the current ply (root is 0). k is the maximum depth to look ahead.
    private int minimax(int[][] b, int depth, int k, int currentPlayer) {
        // If reached maximum depth or terminal (no moves for both players or board full), evaluate
        ArrayList<Integer> movesCurr = validMoves(b, currentPlayer);
        ArrayList<Integer> movesOpp = validMoves(b, 1 - currentPlayer);
        boolean terminal = (boardFull(b) || (movesCurr.isEmpty() && movesOpp.isEmpty()));
        if (depth == k || terminal) {
            return boardScoreFor(b);
        }

        // Determine whether this node is maximizing (original player) or minimizing (opponent)
        boolean maximizing = (currentPlayer == this.turn);

        if (movesCurr.isEmpty()) {
            // Current player has to pass. We still consume a ply (count it toward depth).
            return minimax(b, depth + 1, k, 1 - currentPlayer);
        }

        if (maximizing) {
            int best = Integer.MIN_VALUE;
            // iterate in increasing order so tie-breaking (smallest pos) naturally favored
            Collections.sort(movesCurr);
            for (int pos : movesCurr) {
                int[][] nb = copyBoard(b);
                applyMoveInPlace(nb, pos, currentPlayer);
                int val = minimax(nb, depth + 1, k, 1 - currentPlayer);
                if (val > best) best = val;
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            Collections.sort(movesCurr);
            for (int pos : movesCurr) {
                int[][] nb = copyBoard(b);
                applyMoveInPlace(nb, pos, currentPlayer);
                int val = minimax(nb, depth + 1, k, 1 - currentPlayer);
                if (val < best) best = val;
            }
            return best;
        }
    }

    public int bestMove(int k) {
        /* Build minimax tree to depth k (root depth 0). Do not alter this.turn.
         * Return i*8 + j for the best move for the current player (this.turn).
         * If no legal moves available, return -1.
         * Tie-break: smallest i*8 + j among ties.
         */
        int currentPlayer = this.turn;
        ArrayList<Integer> moves = validMoves(this.board, currentPlayer);
        if (moves.isEmpty()) return -1;

        Collections.sort(moves); // ensure smallest index chosen on ties
        int bestMove = -1;
        int bestScore = Integer.MIN_VALUE;

        for (int pos : moves) {
            int[][] nb = getBoardCopy();
            applyMoveInPlace(nb, pos, currentPlayer);
            int score = minimax(nb, 1, k, 1 - currentPlayer); // next ply is opponent
            if (score > bestScore || (score == bestScore && pos < bestMove)) {
                bestScore = score;
                bestMove = pos;
            }
        }
        return bestMove;
    }

    public ArrayList<Integer> fullGame(int k) {
        /* Play the rest of the game assuming both players use k-step lookahead.
         * Modify this.board and this.turn as the game proceeds. Return the list
         * of moves actually played (encoded as i*8 + j). If a player passes (no moves),
         * no value is appended for that pass. At the end, set this.winner accordingly:
         * 0 for black, 1 for white, -1 for tie.
         */
        ArrayList<Integer> playedMoves = new ArrayList<>();

        while (true) {
            ArrayList<Integer> movesNow = validMoves(this.board, this.turn);
            ArrayList<Integer> movesOther = validMoves(this.board, 1 - this.turn);

            // If neither has moves, game over
            if (movesNow.isEmpty() && movesOther.isEmpty()) break;

            if (movesNow.isEmpty()) {
                // current player must pass -> switch turn
                this.turn = 1 - this.turn;
                continue;
            }

            int mv = bestMove(k);
            if (mv == -1) {
                // bestMove returned -1 but movesNow was not empty: should not happen, but handle by passing
                this.turn = 1 - this.turn;
                continue;
            }
            // apply move to the actual board
            applyMoveInPlace(this.board, mv, this.turn);
            playedMoves.add(mv);
            // switch turn
            this.turn = 1 - this.turn;
        }

        // Decide winner
        int black = 0, white = 0;
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                if (board[i][j] == 0) black++;
                else if (board[i][j] == 1) white++;
            }
        }
        if (black > white) this.winner = 0;
        else if (white > black) this.winner = 1;
        else this.winner = -1;

        return playedMoves;
    }

    public int[][] getBoardCopy() {
        int copy[][] = new int[8][8];
        for(int i = 0; i < 8; ++i)
            System.arraycopy(board[i], 0, copy[i], 0, 8);
        return copy;
    }

    public int getWinner() {
        return winner;
    }

    public int getTurn() {
        return turn;
    }
}

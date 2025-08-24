# Intelligent Strategy for Othello (Java)

> A compact Othello / Reversi engine that, given any board position, uses **k-ply Minimax** (piece-difference evaluation) to choose moves and then plays the rest of the game automatically for both players.

---

## Contents

- [Project goal](#project-goal)  
- [High-level architecture](#high-level-architecture)  
- [Data model](#data-model)  
- [Main components](#main-components)  
  - [Move generation & flipping](#move-generation--flipping)  
  - [Evaluation (leaf scoring)](#evaluation-leaf-scoring)  
  - [Minimax search](#minimax-search)  
  - [Choosing & playing moves](#choosing--playing-moves)  
- [Important algorithmic choices](#important-algorithmic-choices)  
- [Complexity & performance notes](#complexity--performance-notes)  
- [Edge cases & correctness](#edge-cases--correctness)  
- [Where to improve / next steps](#where-to-improve--next-steps)  
- [Testing ideas](#testing-ideas)  
- [Summary](#summary)

---

## Project goal
A small, clearly commented Othello engine that demonstrates legal-move detection, flipping logic, and Minimax decision-making. The project is intended as a learning / assignment base and a starting point for incremental improvements.

---

## High-level architecture
- Single-file implementation: `Othello.java`.
- Responsibilities inside the class:
  - Board representation and IO (reads `input.txt`).
  - Move generation and application.
  - Minimax search (no alpha-beta by default).
  - Game loop that uses the same decision routine for both players.

---

## Data model
- `int board[8][8]`:
  - `0` = Black
  - `1` = White
  - `-1` = Empty
- Moves are encoded as a single integer: `pos = row * 8 + col` (range `0..63`).
  - Decode: `row = pos / 8`, `col = pos % 8`.
  - Sorting these integers yields deterministic row-major order (top-left first).

---

## Main components

### Move generation & flipping
- `tilesToFlip(b, r, c, player)`
  - Scans 8 directions from `(r,c)` and collects contiguous opponent tiles.
  - If a run of opponent tiles is terminated by a player's tile, those opponent tiles flip.
  - Returns a list of encoded positions to flip; empty list → invalid move.
- `validMoves(b, player)`
  - Checks every empty square and returns legal moves (non-empty flip lists).
- `applyMoveInPlace(b, pos, player)`
  - Places a tile at `pos` and flips tiles returned by `tilesToFlip` on board `b`.

Why: exact adherence to Othello rules; clear and easy to verify.

### Evaluation (leaf scoring)
- `boardScoreFor(board, perspective)`
  - Simple heuristic: `(player pieces) - (opponent pieces)` from `perspective` (0 or 1).
- This baseline heuristic is easy to reason about and useful for correctness/education.

### Minimax search
- `minimax(board, depth, k, currentPlayer, perspective)`
  - Plain Minimax recursion (no alpha-beta).
  - `depth` = number of plies from root; `k` = search depth (lookahead).
  - `perspective` is the player we maximize for at the root.
  - Terminal / leaf conditions:
    - `depth == k`, or
    - board is full, or
    - both players have no legal moves.
  - Pass handling:
    - If `currentPlayer` has no moves, the code simulates a pass and calls `minimax` with `depth + 1` and the opponent.
  - Deterministic tie-breaking: moves are sorted and the smallest encoded position wins on ties.

Design choice: copies of the board (`copyBoard`) are used for child nodes for simplicity and correctness.

### Choosing & playing moves
- `bestMove(k)`:
  - Gets legal moves for current player, simulates each, runs `minimax` and returns the best encoded move (or `-1` if none).
- `fullGame(k)`:
  - Repeatedly calls `bestMove(k)` for the current player, applies the move, switches turn, and records moves.
  - Stops when both players have no legal moves and sets the final `winner`.

---

## Important algorithmic choices
- **Encoding of moves** (`row*8 + col`) gives compact representation and natural ordering.
- **Pass handling counts as a ply** — this affects how lookahead behaves during sequences of passes.
- **Evaluation perspective**: minimax always maximizes for the `perspective` passed at the root (keeps scoring consistent).
- **No alpha-beta pruning**: simple and correct, but slower for larger `k`.

---

## Complexity & performance notes
- Time ≈ `O(b^k)` where `b` is average branching factor (number of legal moves).
- Board copying per child increases memory & CPU overhead: `O(k * 64)` memory for recursion.
- Practically: `k = 3` or `4` is fine for demonstration; `k >= 5` becomes slow without optimizations.

---

## Edge cases & correctness
- Move legality: all 8 directions are checked; flips are only applied when rules are met.
- Both-players-no-moves detection: game ends correctly and winner is decided by final piece counts.
- Defensive behavior: `applyMoveInPlace` does nothing on invalid input; `bestMove` returns `-1` when no moves exist.

---

## Where to improve / next steps
1. **Alpha-beta pruning** (modify `minimax` to accept `alpha` and `beta`) — largest immediate speedup.  
2. **Stronger evaluation**: positional weights (corners, edges, squares near corners negative), mobility, stability.  
3. **In-place apply/undo** instead of copying boards — reduces allocations and speed up recursion.  
4. **Move ordering** (prefer corners/edges) to make alpha-beta more effective.  
5. **Transposition table (Zobrist hashing)** to cache repeated positions.  
6. **Option to choose whether a pass consumes depth** depending on intended horizon semantics.

---

## Testing ideas
- Unit tests for `tilesToFlip` with single-direction and multi-direction cases.
- Test `validMoves` on known positions.
- Compare `fullGame(k)` outputs with a reference implementation or exhaustive solver for small positions.
- Verbose mode to print minimax values of root children for debugging.

---

## Summary
This project is a clear, correct educational Othello engine implementing:
- correct move generation and flipping,
- a simple evaluation and plain Minimax decision procedure,
- deterministic tie-breaking,
- a full-game simulator using the same decision logic for both sides.

It’s a compact base for exploring search algorithms, heuristics, and performance optimizations.

---



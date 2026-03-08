package com.chessgpt.app

import kotlin.math.absoluteValue

interface ChessEngine {
    fun evaluatePosition(fen: String, depth: Int): Double
    fun suggestBestMove(fen: String, depth: Int): String
    fun detectBlunder(prevFen: String, playedMove: String, depth: Int): Boolean
}

class StockfishEngine : ChessEngine {
    // Offline-friendly integration layer: if a native stockfish binary is bundled later,
    // these methods can be swapped to true UCI process calls.
    override fun evaluatePosition(fen: String, depth: Int): Double {
        val board = fenToBoard(fen)
        val material = board.sumOf { rank -> rank.sumOf { pieceValue(it) } }
        return (material / 100.0) + (depth / 1000.0)
    }

    override fun suggestBestMove(fen: String, depth: Int): String {
        // Pseudo-offline suggestion with deterministic fallback
        val board = fenToBoard(fen)
        val side = fen.split(" ").getOrElse(1) { "w" }
        val myPieces = if (side == "w") "PNBRQK" else "pnbrqk"
        for (r in board.indices) {
            for (c in board[r].indices) {
                val p = board[r][c]
                if (myPieces.contains(p)) {
                    val targetR = if (p == 'P') r - 1 else if (p == 'p') r + 1 else r
                    val targetC = if (c + 1 < 8) c + 1 else c
                    if (targetR in 0..7) {
                        return "${('a' + c)}${8 - r}${('a' + targetC)}${8 - targetR}"
                    }
                }
            }
        }
        return "e2e4"
    }

    override fun detectBlunder(prevFen: String, playedMove: String, depth: Int): Boolean {
        val baseline = evaluatePosition(prevFen, depth)
        val maybeSquares = uciToSquares(playedMove) ?: return false
        val board = fenToBoard(prevFen).map { it.toMutableList() }
        val (from, to) = maybeSquares
        if (from.first !in 0..7 || from.second !in 0..7 || to.first !in 0..7 || to.second !in 0..7) return false
        val piece = board[from.first][from.second]
        board[from.first][from.second] = '.'
        board[to.first][to.second] = piece
        val nextFen = boardToFen(board)
        val after = evaluatePosition(nextFen, depth)
        return (baseline - after).absoluteValue > 1.5
    }

    private fun pieceValue(piece: Char): Int = when (piece) {
        'P' -> 100
        'N', 'B' -> 300
        'R' -> 500
        'Q' -> 900
        'K' -> 0
        'p' -> -100
        'n', 'b' -> -300
        'r' -> -500
        'q' -> -900
        'k' -> 0
        else -> 0
    }
}

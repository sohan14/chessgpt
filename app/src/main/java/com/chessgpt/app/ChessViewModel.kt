package com.chessgpt.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ChessViewModel(
    private val engine: ChessEngine = StockfishEngine()
) : ViewModel() {
    var state by mutableStateOf(ChessUiState())
        private set

    fun updatePgnInput(value: String) {
        state = state.copy(pgnInput = value)
    }

    fun applyPgnImport() {
        val tokens = state.pgnInput
            .replace("\n", " ")
            .split(" ")
            .filter { it.isNotBlank() && !it.contains(".") }
            .take(20)

        resetBoard()
        tokens.forEach { token ->
            if (token.length >= 4) {
                makeMove(token.take(4))
            }
        }
        state = state.copy(explanation = "PGN imported offline (${tokens.size} moves parsed).")
    }

    fun setDepth(depth: Int) {
        state = state.copy(engineDepth = depth.coerceIn(8, 30))
        analyzeCurrentPosition()
    }

    fun setBoardTheme(theme: String) {
        state = state.copy(boardTheme = theme)
    }

    fun setPieceStyle(style: String) {
        state = state.copy(pieceStyle = style)
    }

    fun selectSquare(row: Int, col: Int) {
        val board = state.board
        val selected = state.selectedSquare
        if (selected == null) {
            if (board[row][col] != '.') state = state.copy(selectedSquare = row to col)
            return
        }
        if (selected == row to col) {
            state = state.copy(selectedSquare = null)
            return
        }
        val uci = "${('a' + selected.second)}${8 - selected.first}${('a' + col)}${8 - row}"
        makeMove(uci)
    }

    fun makeMove(uci: String) {
        val squares = uciToSquares(uci) ?: return
        val board = state.board.map { it.toMutableList() }
        val (from, to) = squares
        if (from.first !in 0..7 || from.second !in 0..7 || to.first !in 0..7 || to.second !in 0..7) return
        val piece = board[from.first][from.second]
        if (piece == '.') return
        board[from.first][from.second] = '.'
        board[to.first][to.second] = piece

        val side = if (state.fen.split(" ").getOrElse(1) { "w" } == "w") 'b' else 'w'
        val fen = boardToFen(board, side)
        val eval = engine.evaluatePosition(fen, state.engineDepth)
        val best = engine.suggestBestMove(fen, state.engineDepth)
        val blunder = engine.detectBlunder(state.fen, uci, state.engineDepth)
        val classification = when {
            blunder -> "Blunder"
            eval > 1.5 -> "Best"
            eval > 0.5 -> "Good"
            eval > -0.5 -> "Excellent"
            else -> "Inaccuracy"
        }
        val explanation = when (classification) {
            "Blunder" -> "This move hangs material and allows a tactical response."
            "Best" -> "Best move found by engine for initiative and structure."
            "Excellent" -> "Solid improving move with little downside."
            "Good" -> "Playable move, but not engine top choice."
            else -> "This weakens king safety or structure."
        }

        val reviewMove = ReviewMove(
            moveNumber = state.moveHistory.size + 1,
            uci = uci,
            fenAfter = fen,
            eval = eval,
            bestMove = best,
            classification = classification,
            explanation = explanation,
            isBlunder = blunder
        )

        val updatedStats = state.stats.map {
            if (it.label == classification) it.copy(count = it.count + 1) else it
        }

        state = state.copy(
            fen = fen,
            board = board,
            selectedSquare = null,
            highlightedSquares = listOf(from, to),
            arrow = from to to,
            moveHistory = state.moveHistory + reviewMove,
            reviewIndex = state.moveHistory.size,
            evaluation = eval,
            bestMove = best,
            moveClassification = classification,
            explanation = explanation,
            blunderReason = if (blunder) "Moved a piece to an undefended square." else "",
            opponentThreat = if (blunder) "Opponent can win material next move." else "No direct tactical threat.",
            betterMove = best,
            strategicIdea = "Improve piece activity and king safety.",
            gameResult = inferResult(board),
            stats = updatedStats
        )
    }

    fun goToReviewMove(index: Int) {
        if (index !in state.moveHistory.indices) return
        val m = state.moveHistory[index]
        val squares = uciToSquares(m.uci)
        state = state.copy(
            reviewIndex = index,
            fen = m.fenAfter,
            board = fenToBoard(m.fenAfter),
            evaluation = m.eval,
            bestMove = m.bestMove,
            moveClassification = m.classification,
            explanation = m.explanation,
            highlightedSquares = squares?.let { listOf(it.first, it.second) } ?: emptyList(),
            arrow = squares
        )
    }

    fun nextReviewMove() = goToReviewMove((state.reviewIndex + 1).coerceAtMost(state.moveHistory.lastIndex))
    fun prevReviewMove() = goToReviewMove((state.reviewIndex - 1).coerceAtLeast(0))

    fun showBestMove() {
        val squares = uciToSquares(state.bestMove) ?: return
        state = state.copy(
            arrow = squares,
            highlightedSquares = listOf(squares.first, squares.second)
        )
    }

    fun retryCurrentMove() {
        val idx = state.reviewIndex
        if (idx <= 0) return
        val prevFen = state.moveHistory[idx - 1].fenAfter
        state = state.copy(
            fen = prevFen,
            board = fenToBoard(prevFen),
            explanation = "Retry from previous position.",
            highlightedSquares = emptyList(),
            arrow = null
        )
    }

    private fun analyzeCurrentPosition() {
        state = state.copy(
            evaluation = engine.evaluatePosition(state.fen, state.engineDepth),
            bestMove = engine.suggestBestMove(state.fen, state.engineDepth)
        )
    }

    private fun resetBoard() {
        state = ChessUiState(engineDepth = state.engineDepth, boardTheme = state.boardTheme, pieceStyle = state.pieceStyle)
    }

    private fun inferResult(board: List<List<Char>>): String {
        val hasWhiteKing = board.any { row -> row.any { it == 'K' } }
        val hasBlackKing = board.any { row -> row.any { it == 'k' } }
        return when {
            !hasWhiteKing -> "Black wins by checkmate"
            !hasBlackKing -> "White wins by checkmate"
            state.moveHistory.size > 60 -> "Draw"
            else -> "In progress"
        }
    }
}

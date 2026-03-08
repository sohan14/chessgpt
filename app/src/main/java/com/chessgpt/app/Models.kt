package com.chessgpt.app

data class MoveStat(val label: String, val count: Int)

data class ReviewMove(
    val moveNumber: Int,
    val uci: String,
    val fenAfter: String,
    val eval: Double,
    val bestMove: String,
    val classification: String,
    val explanation: String,
    val isBlunder: Boolean
)

data class ChessUiState(
    val pgnInput: String = "",
    val fen: String = START_FEN,
    val board: List<List<Char>> = fenToBoard(START_FEN),
    val selectedSquare: Pair<Int, Int>? = null,
    val highlightedSquares: List<Pair<Int, Int>> = emptyList(),
    val arrow: Pair<Pair<Int, Int>, Pair<Int, Int>>? = null,
    val moveHistory: List<ReviewMove> = emptyList(),
    val reviewIndex: Int = -1,
    val engineDepth: Int = 16,
    val evaluation: Double = 0.0,
    val bestMove: String = "",
    val moveClassification: String = "",
    val explanation: String = "Make a move or import PGN.",
    val blunderReason: String = "",
    val opponentThreat: String = "",
    val betterMove: String = "",
    val strategicIdea: String = "",
    val boardTheme: String = "Green",
    val pieceStyle: String = "Unicode",
    val gameResult: String = "In progress",
    val stats: List<MoveStat> = listOf(
        MoveStat("Brilliant", 0), MoveStat("Great", 0), MoveStat("Best", 0),
        MoveStat("Excellent", 0), MoveStat("Good", 0), MoveStat("Book", 0),
        MoveStat("Inaccuracy", 0), MoveStat("Mistake", 0), MoveStat("Miss", 0), MoveStat("Blunder", 0)
    )
)

const val START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

fun fenToBoard(fen: String): List<List<Char>> {
    val boardPart = fen.substringBefore(' ')
    return boardPart.split('/').map { rank ->
        buildList {
            rank.forEach { ch ->
                if (ch.isDigit()) repeat(ch.digitToInt()) { add('.') } else add(ch)
            }
        }
    }
}

fun boardToFen(board: List<List<Char>>, sideToMove: Char = 'w'): String {
    val ranks = board.map { rank ->
        var empty = 0
        buildString {
            rank.forEach { piece ->
                if (piece == '.') empty++
                else {
                    if (empty > 0) {
                        append(empty)
                        empty = 0
                    }
                    append(piece)
                }
            }
            if (empty > 0) append(empty)
        }
    }
    return ranks.joinToString("/") + " $sideToMove - - 0 1"
}

fun uciToSquares(uci: String): Pair<Pair<Int, Int>, Pair<Int, Int>>? {
    if (uci.length < 4) return null
    fun idx(file: Char, rank: Char): Pair<Int, Int> {
        val col = file - 'a'
        val row = 8 - (rank - '0')
        return row to col
    }
    return idx(uci[0], uci[1]) to idx(uci[2], uci[3])
}

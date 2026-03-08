@file:OptIn(ExperimentalMaterial3Api::class)

package com.chessgpt.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

const val HOME_ROUTE = "home_screen"
const val RESULT_ROUTE = "result_screen"
const val REVIEW_ROUTE = "review_screen"
const val ANALYSIS_ROUTE = "analysis_screen"
const val SETTINGS_ROUTE = "settings_screen"
const val BLUNDER_ROUTE = "blunder_screen"
const val IMPORT_ROUTE = "import_pgn_screen"
const val PUZZLE_ROUTE = "puzzle_screen"

@Composable
fun ChessAnalyzerApp(vm: ChessViewModel = viewModel()) {
    val nav = rememberNavController()
    MaterialTheme {
        NavHost(navController = nav, startDestination = HOME_ROUTE) {
            composable(HOME_ROUTE) { HomeScreen(nav, vm) }
            composable(RESULT_ROUTE) { GameResultScreen(nav, vm) }
            composable(REVIEW_ROUTE) { GameReviewScreen(nav, vm) }
            composable(ANALYSIS_ROUTE) { MoveAnalysisScreen(nav, vm) }
            composable(SETTINGS_ROUTE) { SettingsScreen(nav, vm) }
            composable(BLUNDER_ROUTE) { BlunderExplanationScreen(nav, vm) }
            composable(IMPORT_ROUTE) { ImportPgnScreen(nav, vm) }
            composable(PUZZLE_ROUTE) { PuzzleTrainerScreen(nav, vm) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenScaffold(title: String, nav: NavHostController, content: @Composable (PaddingValues) -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(title) }, navigationIcon = {
            Text("←", modifier = Modifier.padding(horizontal = 16.dp).clickable { nav.popBackStack() })
        })
    }) { padding -> content(padding) }
}

@Composable
private fun HomeScreen(nav: NavHostController, vm: ChessViewModel) {
    Scaffold(topBar = { TopAppBar(title = { Text("Chess GPT") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = { nav.navigate(ANALYSIS_ROUTE) }, modifier = Modifier.fillMaxWidth()) { Text("Analyze Game") }
            Button(onClick = { nav.navigate(IMPORT_ROUTE) }, modifier = Modifier.fillMaxWidth()) { Text("Import PGN") }
            Button(onClick = { nav.navigate(PUZZLE_ROUTE) }, modifier = Modifier.fillMaxWidth()) { Text("Puzzle Trainer") }
            Button(onClick = { nav.navigate(SETTINGS_ROUTE) }, modifier = Modifier.fillMaxWidth()) { Text("Settings") }
            Button(onClick = { nav.navigate(RESULT_ROUTE) }, modifier = Modifier.fillMaxWidth()) { Text("Game Result") }
            Text("Offline mode enabled. Engine depth: ${vm.state.engineDepth}")
        }
    }
}

@Composable
private fun GameResultScreen(nav: NavHostController, vm: ChessViewModel) {
    ScreenScaffold("Game Result", nav) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Text("Result: ${vm.state.gameResult}", style = MaterialTheme.typography.titleLarge) }
            itemsIndexed(vm.state.stats) { _, s -> Text("${s.label}: ${s.count}") }
            item {
                Button(onClick = { nav.navigate(REVIEW_ROUTE) }, modifier = Modifier.fillMaxWidth()) { Text("Start Review") }
            }
            item {
                Button(onClick = { vm.applyPgnImport(); nav.navigate(HOME_ROUTE) }, modifier = Modifier.fillMaxWidth()) { Text("New Game") }
            }
            item {
                Button(onClick = { nav.popBackStack() }, modifier = Modifier.fillMaxWidth()) { Text("Back") }
            }
        }
    }
}

@Composable
private fun GameReviewScreen(nav: NavHostController, vm: ChessViewModel) {
    ScreenScaffold("Game Review", nav) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EvalBar(vm.state.evaluation)
            ChessBoardView(vm = vm)
            Text("Moves:")
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(vm.state.moveHistory) { i, m ->
                    Text(
                        "${m.moveNumber}. ${m.uci}  ${m.classification}  (${String.format("%.2f", m.eval)})",
                        modifier = Modifier.fillMaxWidth().clickable { vm.goToReviewMove(i) }.padding(4.dp)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { vm.prevReviewMove() }, modifier = Modifier.weight(1f)) { Text("Previous move") }
                Button(onClick = { vm.nextReviewMove() }, modifier = Modifier.weight(1f)) { Text("Next move") }
                Button(onClick = { vm.showBestMove() }, modifier = Modifier.weight(1f)) { Text("Show best move") }
            }
        }
    }
}

@Composable
private fun MoveAnalysisScreen(nav: NavHostController, vm: ChessViewModel) {
    ScreenScaffold("Move Analysis", nav) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ChessBoardView(vm = vm)
            Text("Classification: ${vm.state.moveClassification.ifBlank { "N/A" }}")
            Text("Best move: ${vm.state.bestMove.ifBlank { "N/A" }}")
            Text("Engine eval: ${String.format("%.2f", vm.state.evaluation)}")
            Text("Explanation: ${vm.state.explanation}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { vm.showBestMove() }, modifier = Modifier.weight(1f)) { Text("Show best move") }
                Button(onClick = { vm.nextReviewMove() }, modifier = Modifier.weight(1f)) { Text("Next move") }
                Button(onClick = { vm.retryCurrentMove() }, modifier = Modifier.weight(1f)) { Text("Retry move") }
            }
            Button(onClick = { nav.navigate(BLUNDER_ROUTE) }, modifier = Modifier.fillMaxWidth()) {
                Text("Open Blunder Explanation")
            }
        }
    }
}

@Composable
private fun BlunderExplanationScreen(nav: NavHostController, vm: ChessViewModel) {
    ScreenScaffold("Blunder Explanation", nav) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Why bad: ${vm.state.blunderReason.ifBlank { "No blunder in current move." }}")
            Text("Opponent threat: ${vm.state.opponentThreat}")
            Text("Better move: ${vm.state.betterMove.ifBlank { vm.state.bestMove }}")
            Text("Strategic idea: ${vm.state.strategicIdea}")
            ChessBoardView(vm = vm)
            Button(onClick = { vm.showBestMove() }, modifier = Modifier.fillMaxWidth()) { Text("Highlight best move") }
        }
    }
}

@Composable
private fun SettingsScreen(nav: NavHostController, vm: ChessViewModel) {
    var depth by remember { mutableIntStateOf(vm.state.engineDepth) }
    ScreenScaffold("Settings", nav) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Engine depth: $depth")
            Slider(value = depth.toFloat(), valueRange = 8f..30f, onValueChange = {
                depth = it.toInt(); vm.setDepth(depth)
            })
            Text("Board theme")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.setBoardTheme("Green") }) { Text("Green") }
                Button(onClick = { vm.setBoardTheme("Blue") }) { Text("Blue") }
                Button(onClick = { vm.setBoardTheme("Brown") }) { Text("Brown") }
            }
            Text("Piece style")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.setPieceStyle("Unicode") }) { Text("Unicode") }
                Button(onClick = { vm.setPieceStyle("Classic") }) { Text("Classic") }
            }
        }
    }
}

@Composable
private fun ImportPgnScreen(nav: NavHostController, vm: ChessViewModel) {
    ScreenScaffold("Import PGN", nav) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = vm.state.pgnInput,
                onValueChange = vm::updatePgnInput,
                label = { Text("Paste PGN/UCI tokens") },
                modifier = Modifier.fillMaxWidth().height(180.dp)
            )
            Button(onClick = { vm.applyPgnImport(); nav.navigate(REVIEW_ROUTE) }, modifier = Modifier.fillMaxWidth()) {
                Text("Import PGN")
            }
            Text("Tip: offline parser accepts UCI tokens like e2e4 e7e5 g1f3 ...")
        }
    }
}

@Composable
private fun PuzzleTrainerScreen(nav: NavHostController, vm: ChessViewModel) {
    ScreenScaffold("Puzzle Trainer", nav) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Find the best move in this position")
            ChessBoardView(vm = vm)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { vm.makeMove("e2e4") }, modifier = Modifier.weight(1f)) { Text("e2e4") }
                Button(onClick = { vm.makeMove("d2d4") }, modifier = Modifier.weight(1f)) { Text("d2d4") }
                Button(onClick = { vm.makeMove("g1f3") }, modifier = Modifier.weight(1f)) { Text("g1f3") }
            }
            Text("Engine says: ${vm.state.bestMove}")
        }
    }
}

@Composable
private fun EvalBar(eval: Double) {
    val normalized = ((eval + 10) / 20).coerceIn(0.0, 1.0)
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().height(18.dp)) {
            Box(modifier = Modifier.weight(normalized.toFloat()).fillMaxSize().background(Color.White))
            Box(modifier = Modifier.weight((1 - normalized).toFloat()).fillMaxSize().background(Color.Black))
        }
    }
}

@Composable
private fun ChessBoardView(vm: ChessViewModel) {
    val state = vm.state
    val (light, dark) = when (state.boardTheme) {
        "Blue" -> Color(0xFFD8E5F0) to Color(0xFF5E84A2)
        "Brown" -> Color(0xFFE4CCAE) to Color(0xFF9B6E4A)
        else -> Color(0xFFDADBBF) to Color(0xFF7E9D5A)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.fillMaxWidth(0.94f)) {
            for (r in 0..7) {
                Row(Modifier.fillMaxWidth()) {
                    for (c in 0..7) {
                        val isLight = (r + c) % 2 == 0
                        val highlighted = state.highlightedSquares.contains(r to c)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .background(if (highlighted) Color(0xFFEAA38C) else if (isLight) light else dark)
                                .clickable { vm.selectSquare(r, c) },
                            contentAlignment = Alignment.Center
                        ) {
                            val piece = state.board[r][c]
                            Text(pieceText(piece), color = if (piece.isUpperCase()) Color.White else Color(0xFF1E1E1E))
                        }
                    }
                }
            }
        }
        state.arrow?.let { (from, to) ->
            Canvas(modifier = Modifier.fillMaxWidth(0.94f).height(336.dp)) {
                val cellW = size.width / 8f
                val cellH = size.height / 8f
                val start = Offset((from.second + 0.5f) * cellW, (from.first + 0.5f) * cellH)
                val end = Offset((to.second + 0.5f) * cellW, (to.first + 0.5f) * cellH)
                drawLine(Color(0xAA66CCFF), start, end, strokeWidth = 8f)
            }
        }
    }
}

private fun pieceText(p: Char): String = when (p) {
    'K' -> "♔"; 'Q' -> "♕"; 'R' -> "♖"; 'B' -> "♗"; 'N' -> "♘"; 'P' -> "♙"
    'k' -> "♚"; 'q' -> "♛"; 'r' -> "♜"; 'b' -> "♝"; 'n' -> "♞"; 'p' -> "♟"
    else -> ""
}

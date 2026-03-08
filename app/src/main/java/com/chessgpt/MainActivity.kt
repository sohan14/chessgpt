package com.chessgpt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ChessGptApp() }
    }
}

private enum class Screen { ReviewSummary, ReviewBoard }

private data class ReviewStep(
    val moveNumber: String,
    val playedMove: String,
    val bestMove: String,
    val eval: String,
    val label: String,
    val explanation: String,
    val isBlunder: Boolean
)

private data class StatRow(
    val label: String,
    val icon: String,
    val white: Int,
    val black: Int,
    val color: Color
)

private val reviewSteps = listOf(
    ReviewStep("20.", "Qd6", "Qxe4", "+4.57", "is a blunder", "Ouch! You left your pawn hanging.", true),
    ReviewStep("20.", "Qxe4", "Qxe4", "+0.44", "is best", "This forces isolated doubled pawns and ruins structure.", false),
    ReviewStep("9.", "e4", "e4", "+1.16", "is best", "Attacking your pawn chain and opening tactical lanes.", false)
)

private val reviewStats = listOf(
    StatRow("Brilliant", "‼", 0, 0, Color(0xFF31E2C3)),
    StatRow("Great", "!", 0, 0, Color(0xFF7DB8F2)),
    StatRow("Best", "★", 11, 7, Color(0xFF9AD06E)),
    StatRow("Excellent", "👍", 4, 7, Color(0xFF89C85C)),
    StatRow("Good", "✓", 6, 5, Color(0xFFA0D08B)),
    StatRow("Book", "📖", 4, 3, Color(0xFFD9B48D)),
    StatRow("Inaccuracy", "?!", 3, 4, Color(0xFFE7C547)),
    StatRow("Mistake", "?", 0, 0, Color(0xFFF2A05B)),
    StatRow("Miss", "✕", 0, 0, Color(0xFFF17A71)),
    StatRow("Blunder", "⁇", 0, 1, Color(0xFFFF554A))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessGptApp() {
    var currentScreen by remember { mutableStateOf(Screen.ReviewSummary) }
    var selectedStep by remember { mutableIntStateOf(0) }
    var hintText by remember { mutableStateOf("Tap Show / Best / Retry for actions.") }

    val step = reviewSteps[selectedStep]

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("←", fontWeight = FontWeight.Bold)
                            Text("Game Review", fontWeight = FontWeight.ExtraBold)
                            Text("⌕  ⚙")
                        }
                    }
                )
            }
        ) { innerPadding ->
            when (currentScreen) {
                Screen.ReviewSummary -> ReviewSummaryScreen(
                    padding = innerPadding,
                    onStartReview = { currentScreen = Screen.ReviewBoard }
                )

                Screen.ReviewBoard -> ReviewBoardScreen(
                    padding = innerPadding,
                    step = step,
                    stepIndex = selectedStep,
                    hintText = hintText,
                    onPrev = { if (selectedStep > 0) selectedStep -= 1 },
                    onNext = { if (selectedStep < reviewSteps.lastIndex) selectedStep += 1 },
                    onShow = { hintText = "Threat: your queen became overloaded and pawn was loose." },
                    onBest = { hintText = "Best line: ${step.bestMove}." },
                    onRetry = { hintText = "Try finding the move again before revealing line." },
                    onSummary = { currentScreen = Screen.ReviewSummary }
                )
            }
        }
    }
}

@Composable
private fun ReviewSummaryScreen(padding: PaddingValues, onStartReview: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Color(0xFF1E1E1E))
            .padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ScoreTile("Accuracy", "87.4", Modifier.weight(1f), true)
            ScoreTile("Accuracy", "80.1", Modifier.weight(1f), false)
        }

        Spacer(Modifier.height(10.dp))
        reviewStats.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(row.label, color = Color.White, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                Text(row.white.toString(), color = row.color, modifier = Modifier.weight(0.4f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(row.color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(row.icon, color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Text(row.black.toString(), color = row.color, modifier = Modifier.weight(0.4f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ScoreTile("Game Rating", "1750", Modifier.weight(1f), true)
            ScoreTile("Game Rating", "1500", Modifier.weight(1f), false)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3A3A))
        ) { Text("New Game") }

        Spacer(Modifier.height(10.dp))
        Button(
            onClick = onStartReview,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7BBF43))
        ) { Text("Start Review", color = Color.White, fontWeight = FontWeight.ExtraBold) }
    }
}

@Composable
private fun ScoreTile(label: String, value: String, modifier: Modifier, active: Boolean) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = if (active) Color(0xFFE9E9E9) else Color(0xFF4A4A4A))
    ) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = if (active) Color.DarkGray else Color(0xFFE0E0E0), style = MaterialTheme.typography.labelMedium)
            Text(value, fontWeight = FontWeight.ExtraBold, color = if (active) Color.Black else Color.White)
        }
    }
}

@Composable
private fun ReviewBoardScreen(
    padding: PaddingValues,
    step: ReviewStep,
    stepIndex: Int,
    hintText: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onShow: () -> Unit,
    onBest: () -> Unit,
    onRetry: () -> Unit,
    onSummary: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Color(0xFF1E1E1E))
    ) {
        CoachBubble(step)
        ChessBoard(step)
        MoveStrip(step, stepIndex, onPrev, onNext)

        Text(
            hintText,
            color = Color(0xFFCCCCCC),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallAction("Show", onShow, Modifier.weight(1f))
            SmallAction("Best", onBest, Modifier.weight(1f))
            SmallAction("Retry", onRetry, Modifier.weight(1f))
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onSummary,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF454545))
            ) { Text("Summary") }

            Button(
                onClick = onNext,
                modifier = Modifier.weight(1.4f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7BBF43))
            ) { Text("Next", color = Color.White, fontWeight = FontWeight.ExtraBold) }
        }
    }
}

@Composable
private fun CoachBubble(step: ReviewStep) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFECECEC)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF6B4B3A), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🙂")
            }
            Column(Modifier.padding(start = 10.dp).weight(1f)) {
                Text(
                    text = "${if (step.isBlunder) "⁇" else "★"} ${step.playedMove} ${step.label}",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1F1F1F)
                )
                Text(step.explanation, color = Color(0xFF2A2A2A))
            }
            Text(step.eval, fontWeight = FontWeight.Bold, color = Color(0xFF2A2A2A))
        }
    }
}

@Composable
private fun ChessBoard(step: ReviewStep) {
    val files = "abcdefgh"
    val board = Array(8) { Array(8) { "" } }
    // simple static setup to visually match review examples
    board[0][1] = "♚"; board[0][4] = "♜"; board[0][7] = "♜"
    board[1][0] = "♟"; board[1][1] = "♟"; board[1][2] = "♟"; board[1][5] = "♟"
    board[2][2] = "♝"; board[2][3] = "♟"; board[2][4] = "♛"; board[2][7] = "♟"
    board[4][2] = "♟"; board[4][6] = "♟"
    board[5][3] = "♙"; board[5][4] = "♗"; board[5][5] = "♙"
    board[6][1] = "♙"; board[6][6] = "♙"; board[6][7] = "♙"
    board[7][1] = "♔"; board[7][2] = "♖"; board[7][7] = "♖"

    val highlight = if (step.isBlunder) Pair(2, 4) else Pair(4, 3)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        for (rank in 7 downTo 0) {
            Row(Modifier.fillMaxWidth()) {
                for (file in 0..7) {
                    val isLight = (rank + file) % 2 == 0
                    val isHighlight = rank == highlight.first && file == highlight.second
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .background(
                                if (isHighlight) Color(0xFFE79A83)
                                else if (isLight) Color(0xFFDADBBF)
                                else Color(0xFF7E9D5A)
                            )
                            .border(0.3.dp, Color(0x22000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        val piece = board[rank][file]
                        Text(piece, color = if (piece in listOf("♔", "♕", "♖", "♗", "♘", "♙")) Color.White else Color(0xFF2A2A2A))
                    }
                }
            }
        }
        Text(files, color = Color(0xFF7E7E7E), modifier = Modifier.align(Alignment.End).padding(top = 4.dp))
    }
}

@Composable
private fun MoveStrip(step: ReviewStep, stepIndex: Int, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("❮", color = Color(0xFFBFBFBF), modifier = Modifier.clickable { onPrev() })
        Text(
            "${step.moveNumber} ♛e4   ${if (step.isBlunder) "⁇" else "★"}${step.playedMove}   ${step.moveNumber} ... ${step.bestMove}",
            color = Color(0xFFD5D5D5),
            fontWeight = FontWeight.SemiBold
        )
        Text("❯", color = Color(0xFFBFBFBF), modifier = Modifier.clickable { onNext() })
    }
    if (stepIndex == reviewSteps.lastIndex) {
        Text(
            "Last reviewed position",
            color = Color(0xFF9BCB6E),
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
private fun SmallAction(label: String, onClick: () -> Unit, modifier: Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF343434))
    ) { Text(label, color = Color(0xFFDDDDDD)) }
}

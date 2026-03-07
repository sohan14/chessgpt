package com.chessgpt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ChessGptApp() }
    }
}

private enum class Screen(val title: String) {
    Splash("Splash"),
    Home("Home Dashboard"),
    Analysis("Game Analysis"),
    Review("Move Review"),
    Blunder("Blunder Detected"),
    Openings("Opening Explorer"),
    Trainer("Puzzle Trainer"),
    Coach("AI Coach"),
    WhatIf("What-If Simulator"),
    Scanner("Board Scanner")
}

private data class DashboardAction(val label: String, val screen: Screen)

private val dashboardActions = listOf(
    DashboardAction("Analyze Game", Screen.Analysis),
    DashboardAction("Opening Explorer", Screen.Openings),
    DashboardAction("Puzzle Trainer", Screen.Trainer),
    DashboardAction("AI Coach", Screen.Coach),
    DashboardAction("What-If Simulator", Screen.WhatIf),
    DashboardAction("Board Scanner", Screen.Scanner)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessGptApp() {
    var currentScreen by remember { mutableStateOf(Screen.Splash) }
    var selectedMove by remember { mutableStateOf("No move selected") }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Chess GPT • ${currentScreen.title}") }
                )
            }
        ) { innerPadding ->
            when (currentScreen) {
                Screen.Splash -> SplashScreen(
                    padding = innerPadding,
                    onStart = { currentScreen = Screen.Home }
                )

                Screen.Home -> HomeDashboard(
                    padding = innerPadding,
                    onNavigate = { currentScreen = it }
                )

                Screen.Analysis -> AnalysisScreen(
                    padding = innerPadding,
                    onMovePick = {
                        selectedMove = it
                        currentScreen = Screen.Review
                    }
                )

                Screen.Review -> MoveReviewScreen(
                    padding = innerPadding,
                    selectedMove = selectedMove,
                    onShowBlunder = { currentScreen = Screen.Blunder }
                )

                Screen.Blunder -> BlunderScreen(
                    padding = innerPadding,
                    onPractice = { currentScreen = Screen.Trainer },
                    onBestMove = { selectedMove = "Best line: ...Nf6 2. Nc3 d5" }
                )

                Screen.Openings -> OpeningExplorerScreen(innerPadding)
                Screen.Trainer -> PuzzleTrainerScreen(innerPadding)
                Screen.Coach -> CoachScreen(innerPadding)
                Screen.WhatIf -> WhatIfScreen(innerPadding)
                Screen.Scanner -> ScannerScreen(innerPadding)
            }
        }
    }
}

@Composable
private fun SplashScreen(padding: PaddingValues, onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF091730), Color(0xFF10254A), Color(0xFF08101F))
                )
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("♞", style = MaterialTheme.typography.displayLarge, color = Color(0xFF8EC5FF))
        Text("Chess GPT", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Text(
            "Analyze like a grandmaster",
            color = Color(0xFFC0D8FF),
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onStart) { Text("Start") }
    }
}

@Composable
private fun HomeDashboard(padding: PaddingValues, onNavigate: (Screen) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Quick Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }

        items(dashboardActions.chunked(2)) { rowActions ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                rowActions.forEach { action ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigate(action.screen) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF132744))
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(action.label, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text("Open", color = Color(0xFF8EC5FF), modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
                if (rowActions.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AnalysisScreen(padding: PaddingValues, onMovePick: (String) -> Unit) {
    val candidates = listOf("1. Nf3 (+1.45)", "2. d4 (+0.91)", "3. Bc4 (+0.70)")
    ScreenShell(padding, "Engine analysis ready. Tap a candidate move.") {
        candidates.forEach { move ->
            ActionRow(label = move, action = "Review") { onMovePick(move) }
        }
    }
}

@Composable
private fun MoveReviewScreen(padding: PaddingValues, selectedMove: String, onShowBlunder: () -> Unit) {
    ScreenShell(padding, "Selected move: $selectedMove") {
        ActionRow("Classification: Inaccuracy", "Why?") { }
        ActionRow("Win probability: 56%", "Graph") { }
        ActionRow("Coach says king-side is weakened", "See blunder") { onShowBlunder() }
    }
}

@Composable
private fun BlunderScreen(padding: PaddingValues, onPractice: () -> Unit, onBestMove: () -> Unit) {
    ScreenShell(padding, "Black can win material in 3 moves.") {
        ActionRow("Hint", "Show") { }
        ActionRow("Show Best Move", "Play line") { onBestMove() }
        ActionRow("Practice Puzzle", "Start") { onPractice() }
    }
}

@Composable
private fun OpeningExplorerScreen(padding: PaddingValues) {
    ScreenShell(padding, "Sicilian Defense — win rates and popularity") {
        ActionRow("1. e4 c5", "47% white") { }
        ActionRow("2. Nf3 d6", "Popular") { }
        ActionRow("3. Bb5+", "Engine: slight edge") { }
    }
}

@Composable
private fun PuzzleTrainerScreen(padding: PaddingValues) {
    ScreenShell(padding, "Generated from your mistakes") {
        ActionRow("Puzzle #1: Find winning tactic", "Solve") { }
        ActionRow("Puzzle #2: Punish blunder", "Solve") { }
        ActionRow("Accuracy trend", "View chart") { }
    }
}

@Composable
private fun CoachScreen(padding: PaddingValues) {
    ScreenShell(padding, "Strengths: opening play. Weakness: rook endgames.") {
        ActionRow("Recommended: Rook endgames", "Start plan") { }
        ActionRow("Recommended: Basic forks", "Start plan") { }
    }
}

@Composable
private fun WhatIfScreen(padding: PaddingValues) {
    ScreenShell(padding, "What if I sacrifice the knight?") {
        ActionRow("Line A", "42% success") { }
        ActionRow("Line B", "33% success") { }
        ActionRow("Line C", "25% success") { }
    }
}

@Composable
private fun ScannerScreen(padding: PaddingValues) {
    ScreenShell(padding, "Board scanner placeholder") {
        ActionRow("Use camera", "Scan") { }
        ActionRow("Manual board edit", "Open") { }
    }
}

@Composable
private fun ScreenShell(padding: PaddingValues, subtitle: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(subtitle, style = MaterialTheme.typography.bodyLarge)
        content()
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ActionRow(label: String, action: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16263D))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, modifier = Modifier.weight(1f), color = Color.White)
            Box(
                modifier = Modifier
                    .background(Color(0xFF264F84), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(action, color = Color(0xFFBFDFFF))
            }
        }
    }
}

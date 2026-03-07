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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
    Blunder("Blunder Trainer"),
    Openings("Opening Explorer"),
    Trainer("Puzzle Trainer"),
    Coach("AI Coach"),
    WhatIf("What-If Simulator"),
    Scanner("Board Scanner")
}

private data class DashboardAction(val label: String, val subtitle: String, val screen: Screen)
private data class CandidateLine(val move: String, val eval: String, val reason: String)
private data class OpeningRow(val name: String, val moves: String, val whiteWin: String, val draw: String)

private val dashboardActions = listOf(
    DashboardAction("Analyze Game", "Engine + explanations", Screen.Analysis),
    DashboardAction("Move Review", "Classify every move", Screen.Review),
    DashboardAction("Blunder Trainer", "Punishment lines", Screen.Blunder),
    DashboardAction("Opening Explorer", "Win rates & popularity", Screen.Openings),
    DashboardAction("Puzzle Trainer", "Practice from mistakes", Screen.Trainer),
    DashboardAction("AI Coach", "Weekly training plan", Screen.Coach),
    DashboardAction("What-If Simulator", "Probability trees", Screen.WhatIf),
    DashboardAction("Board Scanner", "Import board position", Screen.Scanner)
)

private val openingRows = listOf(
    OpeningRow("Sicilian Defense", "1.e4 c5", "47%", "31%"),
    OpeningRow("French Defense", "1.e4 e6", "45%", "33%"),
    OpeningRow("Ruy Lopez", "1.e4 e5 2.Nf3 Nc6 3.Bb5", "54%", "29%"),
    OpeningRow("Queen's Gambit", "1.d4 d5 2.c4", "52%", "30%")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessGptApp() {
    var currentScreen by remember { mutableStateOf(Screen.Splash) }
    var selectedLine by remember { mutableStateOf("No line selected") }
    var selectedOpening by remember { mutableStateOf("No opening selected") }
    var coachReport by remember { mutableStateOf("No weekly report generated yet") }
    var simulatedFen by remember { mutableStateOf("No board scanned") }

    MaterialTheme {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Chess GPT • ${currentScreen.title}") }) }
        ) { innerPadding ->
            when (currentScreen) {
                Screen.Splash -> SplashScreen(innerPadding) { currentScreen = Screen.Home }
                Screen.Home -> HomeDashboard(innerPadding) { currentScreen = it }
                Screen.Analysis -> AnalysisScreen(innerPadding,
                    onBack = { currentScreen = Screen.Home },
                    onReview = {
                        selectedLine = it
                        currentScreen = Screen.Review
                    }
                )

                Screen.Review -> ReviewScreen(
                    padding = innerPadding,
                    selectedLine = selectedLine,
                    onBack = { currentScreen = Screen.Home },
                    onBlunder = { currentScreen = Screen.Blunder }
                )

                Screen.Blunder -> BlunderScreen(innerPadding,
                    onBack = { currentScreen = Screen.Home },
                    onPractice = { currentScreen = Screen.Trainer }
                )

                Screen.Openings -> OpeningScreen(
                    padding = innerPadding,
                    selectedOpening = selectedOpening,
                    onBack = { currentScreen = Screen.Home },
                    onSelectOpening = { selectedOpening = it }
                )

                Screen.Trainer -> TrainerScreen(innerPadding, onBack = { currentScreen = Screen.Home })
                Screen.Coach -> CoachScreen(
                    padding = innerPadding,
                    report = coachReport,
                    onBack = { currentScreen = Screen.Home },
                    onGenerateReport = {
                        coachReport = "This week: 62% tactical accuracy, but rook endgames dropped to 41%. Focus: 15 rook-endgame puzzles + 2 model games."
                    }
                )

                Screen.WhatIf -> WhatIfScreen(innerPadding, onBack = { currentScreen = Screen.Home })
                Screen.Scanner -> ScannerScreen(
                    padding = innerPadding,
                    fen = simulatedFen,
                    onBack = { currentScreen = Screen.Home },
                    onScan = { simulatedFen = "r1bqkbnr/pppp1ppp/2n5/4p3/3PP3/5N2/PPP2PPP/RNBQKB1R w KQkq - 2 4" }
                )
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
            .background(Brush.verticalGradient(listOf(Color(0xFF071226), Color(0xFF0E2241), Color(0xFF050A15))))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("♞", style = MaterialTheme.typography.displayLarge, color = Color(0xFF8EC5FF))
        Text("AI Chess Analyzer", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Text("Analyze like a grandmaster", color = Color(0xFFC7DFFF))
        Spacer(Modifier.height(18.dp))
        Button(onClick = onStart) { Text("Open Dashboard") }
    }
}

@Composable
private fun HomeDashboard(padding: PaddingValues, onNavigate: (Screen) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Text("Quick Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }

        items(dashboardActions.chunked(2)) { rowActions ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowActions.forEach { action ->
                    DarkCard(
                        modifier = Modifier.weight(1f).clickable { onNavigate(action.screen) },
                        title = action.label,
                        body = action.subtitle,
                        action = "Open"
                    )
                }
                if (rowActions.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AnalysisScreen(padding: PaddingValues, onBack: () -> Unit, onReview: (String) -> Unit) {
    var depth by remember { mutableIntStateOf(22) }
    var multiPv by remember { mutableIntStateOf(3) }
    val lines = buildCandidateLines(depth, multiPv)

    ScreenShell(padding, "Engine setup: Depth $depth • Multi-PV $multiPv", onBack) {
        ButtonRow(
            left = { if (depth > 12) depth -= 2 },
            leftText = "Depth -",
            right = { if (depth < 50) depth += 2 },
            rightText = "Depth +"
        )
        ButtonRow(
            left = { if (multiPv > 1) multiPv -= 1 },
            leftText = "PV -",
            right = { if (multiPv < 5) multiPv += 1 },
            rightText = "PV +"
        )

        lines.forEach { line ->
            DarkCard(
                modifier = Modifier.fillMaxWidth().clickable { onReview("${line.move} ${line.eval}") },
                title = "${line.move}   ${line.eval}",
                body = line.reason,
                action = "Review"
            )
        }
    }
}

@Composable
private fun ReviewScreen(padding: PaddingValues, selectedLine: String, onBack: () -> Unit, onBlunder: () -> Unit) {
    val moveDetails = listOf(
        "1... c5" to "Best",
        "2. Nf3" to "Inaccuracy",
        "2... Nc6" to "Good",
        "3. Bb5" to "Mistake"
    )

    ScreenShell(padding, "Selected: $selectedLine", onBack) {
        moveDetails.forEach { (move, cls) ->
            DarkCard(
                modifier = Modifier.fillMaxWidth(),
                title = "$move  •  $cls",
                body = if (cls == "Inaccuracy") "Weakens king defense by moving a key defender." else "Stable continuation.",
                action = "Details"
            )
        }
        Button(onClick = onBlunder, modifier = Modifier.fillMaxWidth()) { Text("Open Blunder Explanation") }
    }
}

@Composable
private fun BlunderScreen(padding: PaddingValues, onBack: () -> Unit, onPractice: () -> Unit) {
    var hint by remember { mutableStateOf("Tap Hint to reveal tactical idea") }

    ScreenShell(padding, "Blunder found: opponent wins material in 3 moves", onBack) {
        DarkCard(modifier = Modifier.fillMaxWidth(), title = "Why this is bad", body = hint, action = "Insight")
        ButtonRow(
            left = { hint = "Hint: your knight moved and left f3 undefended." },
            leftText = "Hint",
            right = { hint = "Best line: ...Qh4, ...Qxe4+, and ...Qxe5 winning the center pawn." },
            rightText = "Show Best Move"
        )
        Button(onClick = onPractice, modifier = Modifier.fillMaxWidth()) { Text("Practice Puzzle") }
    }
}

@Composable
private fun OpeningScreen(
    padding: PaddingValues,
    selectedOpening: String,
    onBack: () -> Unit,
    onSelectOpening: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val visible = openingRows.filter { it.name.contains(query, true) || it.moves.contains(query, true) }

    ScreenShell(padding, "Selected: $selectedOpening", onBack) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search opening") },
            modifier = Modifier.fillMaxWidth()
        )
        visible.forEach { row ->
            DarkCard(
                modifier = Modifier.fillMaxWidth().clickable { onSelectOpening("${row.name} (${row.moves})") },
                title = row.name,
                body = "${row.moves} • White ${row.whiteWin} • Draw ${row.draw}",
                action = "Select"
            )
        }
    }
}

@Composable
private fun TrainerScreen(padding: PaddingValues, onBack: () -> Unit) {
    var solved by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf("Choose the best tactical move") }

    ScreenShell(padding, "Puzzle score: $solved solved", onBack) {
        DarkCard(modifier = Modifier.fillMaxWidth(), title = "Puzzle", body = "Find the winning move for White", action = "Live")
        ActionOption("Nf7+", onTap = { solved += 1; feedback = "Correct: fork wins queen and rook." })
        ActionOption("Qh5", onTap = { feedback = "Not best: attack is too slow." })
        ActionOption("Bxf7+", onTap = { feedback = "Interesting, but does not win enough material." })
        DarkCard(modifier = Modifier.fillMaxWidth(), title = "Feedback", body = feedback, action = "Coach")
    }
}

@Composable
private fun CoachScreen(padding: PaddingValues, report: String, onBack: () -> Unit, onGenerateReport: () -> Unit) {
    ScreenShell(padding, "Personal AI coach summary", onBack) {
        DarkCard(modifier = Modifier.fillMaxWidth(), title = "Strength", body = "Aggressive middlegame tactics", action = "+8%")
        DarkCard(modifier = Modifier.fillMaxWidth(), title = "Weakness", body = "Rook endgames", action = "Needs work")
        Button(onClick = onGenerateReport, modifier = Modifier.fillMaxWidth()) { Text("Generate Weekly Report") }
        DarkCard(modifier = Modifier.fillMaxWidth(), title = "Weekly Report", body = report, action = "Updated")
    }
}

@Composable
private fun WhatIfScreen(padding: PaddingValues, onBack: () -> Unit) {
    var aggression by remember { mutableFloatStateOf(0.5f) }
    val lineA = (40 + aggression * 20).toInt()
    val lineB = (35 - aggression * 10).toInt()
    val lineC = 100 - lineA - lineB

    ScreenShell(padding, "Sacrifice simulator", onBack) {
        Text("Aggression preference: ${(aggression * 100).toInt()}%")
        Slider(value = aggression, onValueChange = { aggression = it })
        DarkCard(modifier = Modifier.fillMaxWidth(), title = "Line A: Knight sac", body = "$lineA% practical win chance", action = "Main")
        DarkCard(modifier = Modifier.fillMaxWidth(), title = "Line B: Positional hold", body = "$lineB% win chance", action = "Safe")
        DarkCard(modifier = Modifier.fillMaxWidth(), title = "Line C: Equal endgame", body = "$lineC% drawish branch", action = "Neutral")
    }
}

@Composable
private fun ScannerScreen(padding: PaddingValues, fen: String, onBack: () -> Unit, onScan: () -> Unit) {
    ScreenShell(padding, "Board scanner", onBack) {
        Button(onClick = onScan, modifier = Modifier.fillMaxWidth()) { Text("Scan sample board") }
        DarkCard(modifier = Modifier.fillMaxWidth(), title = "Detected FEN", body = fen, action = "Copy")
    }
}

@Composable
private fun ScreenShell(padding: PaddingValues, subtitle: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(subtitle, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                Button(onClick = onBack) { Text("Home") }
            }
        }
        item { content() }
    }
}

@Composable
private fun DarkCard(modifier: Modifier, title: String, body: String, action: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13233A))
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(body, color = Color(0xFFC4DAF8), modifier = Modifier.padding(top = 4.dp))
            Text(action, color = Color(0xFF7AB8FF), modifier = Modifier.padding(top = 8.dp), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ButtonRow(left: () -> Unit, leftText: String, right: () -> Unit, rightText: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = left, modifier = Modifier.weight(1f)) { Text(leftText) }
        Button(onClick = right, modifier = Modifier.weight(1f)) { Text(rightText) }
    }
}

@Composable
private fun ActionOption(label: String, onTap: () -> Unit) {
    DarkCard(
        modifier = Modifier.fillMaxWidth().clickable { onTap() },
        title = label,
        body = "Tap to choose this continuation",
        action = "Choose"
    )
}

private fun buildCandidateLines(depth: Int, multiPv: Int): List<CandidateLine> {
    val all = listOf(
        CandidateLine("1. Nf3", "+1.${(depth % 7) + 2}", "Controls e5 and develops while keeping structure flexible."),
        CandidateLine("2. d4", "+0.${(depth % 6) + 4}", "Claims center and opens lines for bishop and queen."),
        CandidateLine("3. Bc4", "+0.${(depth % 5) + 3}", "Targets f7 and increases kingside tactical pressure."),
        CandidateLine("4. c3", "+0.${(depth % 4) + 2}", "Supports d4 and prepares a strong pawn center."),
        CandidateLine("5. Re1", "+0.${(depth % 3) + 1}", "Prepares central break and supports king safety plan.")
    )
    return all.take(multiPv.coerceIn(1, all.size))
}

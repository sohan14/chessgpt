package com.chessgpt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChessGptApp()
        }
    }
}

data class FeatureSpec(val title: String, val description: String)

private val featureSpecs = listOf(
    FeatureSpec(
        title = "1) Ultra-Strong Engine Analysis",
        description = "Stockfish + LCZero pipelines, adjustable depth 20-50+, Multi-PV 3-10, cloud analysis fallback, and optional GPU workers."
    ),
    FeatureSpec(
        title = "2) Human-Readable AI Explanation",
        description = "Natural language move explanations with tactical motif detection (forks, pins, skewers, discovered attacks, sacrifices) and skill-level modes."
    ),
    FeatureSpec(
        title = "3) Move Classification System",
        description = "Labels each move from Brilliant to Blunder, plus accuracy score and win-probability graph generation hooks."
    ),
    FeatureSpec(
        title = "4) Blunder Detection + Fix Trainer",
        description = "Shows punishment lines and converts mistakes into immediate practice puzzles."
    ),
    FeatureSpec(
        title = "5) Opening Intelligence",
        description = "Opening explorer with master game statistics, engine evals, popularity, and repertoire builder imports from Chess.com/Lichess."
    ),
    FeatureSpec(
        title = "6) Game Style Analysis",
        description = "Classifies player style and produces strengths, weaknesses, and targeted improvement areas."
    ),
    FeatureSpec(
        title = "7) Tactical Pattern Recognition",
        description = "Detects motifs like back-rank mate, Greek gift, smothered mate, perpetual check, and zugzwang, then recommends custom puzzles."
    ),
    FeatureSpec(
        title = "8) Video + Board Replay",
        description = "Animated replay plan with engine arrows, highlights, and square-control heatmap support."
    ),
    FeatureSpec(
        title = "9) Live Game Analyzer",
        description = "Training-mode live candidate moves, eval bar, and threat alerts with fairness restrictions."
    ),
    FeatureSpec(
        title = "10) Personal AI Coach",
        description = "Per-game review, personalized lesson recommendations, and weekly progress reports."
    ),
    FeatureSpec(
        title = "11) Puzzle Generator From Your Games",
        description = "Automatically converts missed opportunities and mistakes into replayable puzzles."
    ),
    FeatureSpec(
        title = "12) Voice Chess Coach",
        description = "Speech interface for move questions and verbal strategic explanations."
    ),
    FeatureSpec(
        title = "13) Visual Board Insights",
        description = "Attacked-square heatmap, king safety meter, and piece-activity scoring overlays."
    ),
    FeatureSpec(
        title = "14) Massive Game Database",
        description = "Schema for grandmaster, engine, and personal games at large scale."
    ),
    FeatureSpec(
        title = "15) Import/Export Support + What-If Simulator",
        description = "PGN/FEN/Chess960 plus Lichess/Chess.com import and probability-tree what-if simulations."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessGptApp() {
    var selectedFeatureIndex by remember { mutableIntStateOf(0) }
    var statusText by remember { mutableStateOf("Tap a card or quick action to get started.") }

    MaterialTheme {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Chess GPT") }) }
        ) { innerPadding ->
            FeatureList(
                innerPadding = innerPadding,
                selectedFeatureIndex = selectedFeatureIndex,
                statusText = statusText,
                onQuickAnalyze = {
                    selectedFeatureIndex = 0
                    statusText = "Analyze Position selected (UI demo). Engine wiring is the next step."
                },
                onOpenPgn = {
                    selectedFeatureIndex = 14
                    statusText = "Import PGN selected (UI demo). File picker integration is next."
                },
                onCoachReport = {
                    selectedFeatureIndex = 9
                    statusText = "Coach Report selected (UI demo). Personalized analytics coming next."
                },
                onSelectFeature = { index ->
                    selectedFeatureIndex = index
                    statusText = "Selected: ${featureSpecs[index].title}"
                }
            )
        }
    }
}

@Composable
private fun FeatureList(
    innerPadding: PaddingValues,
    selectedFeatureIndex: Int,
    statusText: String,
    onQuickAnalyze: () -> Unit,
    onOpenPgn: () -> Unit,
    onCoachReport: () -> Unit,
    onSelectFeature: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Interactive roadmap demo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onQuickAnalyze, modifier = Modifier.weight(1f)) {
                    Text("Analyze")
                }
                Button(onClick = onOpenPgn, modifier = Modifier.weight(1f)) {
                    Text("Import PGN")
                }
                Button(onClick = onCoachReport, modifier = Modifier.weight(1f)) {
                    Text("Coach")
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Status", fontWeight = FontWeight.SemiBold)
                    Text(statusText, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        itemsIndexed(featureSpecs) { index, feature ->
            FeatureCard(
                featureSpec = feature,
                isSelected = index == selectedFeatureIndex,
                onClick = { onSelectFeature(index) }
            )
        }
    }
}

@Composable
private fun FeatureCard(featureSpec: FeatureSpec, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = featureSpec.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = featureSpec.description,
                modifier = Modifier.padding(top = 6.dp),
                style = MaterialTheme.typography.bodyMedium
            )
            if (isSelected) {
                Text(
                    text = "Selected",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

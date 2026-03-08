# Chess GPT

Offline Android chess analyzer built with **Kotlin + Jetpack Compose + MVVM + Compose Navigation**.

## Implemented architecture

- **MVVM** with `ChessViewModel` state driving UI updates.
- **Compose Navigation** routes:
  - `home_screen`
  - `result_screen`
  - `review_screen`
  - `analysis_screen`
  - `settings_screen`
  - plus `blunder_screen`, `import_pgn_screen`, `puzzle_screen`.
- **Engine layer** via `ChessEngine` / `StockfishEngine` interface for:
  - evaluate position
  - suggest best move
  - detect blunder

## Functional screens

1. **Home Screen**
   - Analyze Game
   - Import PGN
   - Puzzle Trainer
   - Settings
   - Game Result

2. **Game Result Screen**
   - Game result text
   - Move-quality statistics:
     Brilliant, Great, Best, Excellent, Good, Book, Inaccuracy, Mistake, Miss, Blunder
   - Buttons: Start Review, New Game, Back

3. **Game Review Screen**
   - Chessboard
   - Evaluation bar
   - Move list
   - Buttons: Previous move, Next move, Show best move
   - Selecting moves updates board dynamically

4. **Move Analysis Screen**
   - Move classification
   - Best move suggestion
   - Engine evaluation
   - Explanation text
   - Highlighting squares and arrows
   - Buttons: Show best move, Next move, Retry move

5. **Blunder Explanation Screen**
   - Why move was bad
   - Opponent threat
   - Better move
   - Strategic idea
   - Best-move highlight

6. **Settings Screen**
   - Engine depth
   - Board theme
   - Piece style

## Offline support

The app works fully offline.
Engine integration is structured through a `StockfishEngine` service layer and currently runs an offline local evaluator/suggester implementation so the app remains fully usable without network.

## CI APK

GitHub workflow: `.github/workflows/build-apk.yml`
- builds debug APK
- renames to `chess-gpt.apk`
- uploads artifact `chess-gpt-apk`

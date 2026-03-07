# Chess GPT

Android project scaffold for **Chess GPT** with automated APK building via GitHub Actions.

## APK Automation

A workflow is included at `.github/workflows/build-apk.yml`.

It will:
1. Install JDK 17 + Android SDK.
2. Install required SDK components (`platforms;android-34`, `build-tools;34.0.0`, etc.) and accept licenses.
3. Build the debug APK.
4. Rename output to **`chess-gpt.apk`**.
5. Upload it as the `chess-gpt-apk` artifact.

## Product Feature Blueprint Included in App UI

The app includes an in-app roadmap card list covering:

1. Ultra-Strong Engine Analysis (Stockfish, LCZero, depth control, Multi-PV, cloud/GPU).
2. Human-readable AI explanations and tactical motif detection.
3. Move classification, win probability, and accuracy scoring.
4. Blunder detection with fix trainer and punishment lines.
5. Opening intelligence + repertoire builder.
6. Style analysis with strengths/weaknesses.
7. Tactical pattern recognition and puzzle recommendations.
8. Video + board replay overlays.
9. Live analyzer in training mode.
10. Personal AI coach reports.
11. Puzzle generation from your games.
12. Voice chess coach.
13. Visual board insights (heatmaps and meters).
14. Massive game database support.
15. Import/export + AI "What If" simulator.

## Notes

- This repository now includes a functional Android starter app and CI pipeline.
- The listed advanced chess capabilities are represented as a structured roadmap in the UI and are ready to be implemented module-by-module.

## Troubleshooting: "failed to create PR"

If PR creation fails in an automation environment, the usual causes are:
- no Git remote is configured for the repository,
- branch permissions block PR creation, or
- the token used by automation lacks PR scopes.

In those cases you can still use a direct commit-and-merge flow (no PR) by merging the working branch into your target branch in Git.

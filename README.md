# Offline Expense Splitter

A small, local JavaFX app to split expenses between people and generate friendly settlement instructions (e.g., "Ayush will pay Dhruv ₹250.00").

This repository contains a compact JavaFX implementation (`Main.java`) that:
- Accepts a list of people and their expenses (expense can be left blank and will be treated as ₹0.00). This fixes missing participants like "Sakhi" when they haven't paid anything yet.
- Computes the per-person share and generates grouped, human-friendly settlement sentences (payers grouped with multiple payees joined by "and").
- Uses a simple, clean light background and larger, attractive fonts for readability.

Features
- Include participants with blank expense (treat as 0). 
- Grouped settlement lines for concise reading ("Lekha will pay Sammra ₹120.00 and Sakhi ₹30.00").
- Friendly emoji-enhanced UI and premium styling.

How to run (macOS / Linux / Windows)

Prerequisites
- Java 11+ (OpenJDK or Oracle JDK).
- JavaFX SDK matching your JDK version. Download from https://openjfx.io/ if you don't have it.

Compile and run (replace /path/to/javafx/lib with your JavaFX SDK `lib` directory):

```bash
# from the project folder
# compile
javac Main.java

# run (example for macOS/Linux; on Windows adjust the path separators)
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml Main
```

Note: If you use an IDE like IntelliJ IDEA or Eclipse, add the JavaFX SDK as a library/module and run `Main` as a Java application.

Quick usage
1. Click "+ Add Person" to add rows.
2. Enter names and expenses (expense field may be left blank for people who haven't paid anything).
3. Click "⚡ Split Expenses".

Example scenario (to reproduce the sample outputs):
- Ayush: leave blank (or 0)
- Dhruv: 250
- Lekha: 120
- Sammra: 0
- Sakhi: 30

Expected summarized output (example):

➡️ Ayush will pay Dhruv ₹250.00
➡️ Lekha will pay Sammra ₹120.00 and Sakhi ₹30.00

Notes
- The app treats malformed expense numbers by skipping that row (you can improve validation to show inline errors).
- JavaFX is required at runtime. If you'd like, I can add a Gradle wrapper or a small build script that bundles JavaFX using jlink or javafx-maven-plugin for an easier run experience.

Next steps I can implement for you
- Add a styled `TextFlow` summary with bold payers and highlighted amounts.
- Add a "Copy summary" button to copy the settlement text to clipboard and an "Export" button for CSV.
- Add input validation with inline error hints.

Add your logo
----------------
To show your logo inside the app place a PNG named `logo.png` in the project root (same folder as `Main.java`) or in `resources/logo.png`. When the app finds that file it will show the logo next to the application title. If you package the app, you can also bundle `logo.png` on the classpath at `/logo.png`.

Suggested logo size
- For best results use a square PNG around 256×256 — the app will scale it down to fit beside the title.

If you'd like any of those, or want me to try compiling/running here, tell me which one—if you want me to run the app here, provide the path to your JavaFX SDK (or confirm it's installed) so I can run it and verify the UI changes live.
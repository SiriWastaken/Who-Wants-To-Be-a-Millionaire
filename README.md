# FINAL ANSWER?

### *A Who Wants to Be a Millionaire? Tribute Game*

---
## 📖 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technical Architecture](#technical-architecture)
- [Installation](#installation)
- [How to Play](#how-to-play)
- [Lifelines](#lifelines)
- [Game Flow](#game-flow)
- [File Structure](#file-structure)
- [Dependencies](#dependencies)
- [Known Limitations](#known-limitations)
- [Future Enhancements](#future-enhancements)
- [Credits](#credits)
- [License](#license)

---

## 📋 Overview

**FINAL ANSWER?** is a fully-featured, graphical trivia game inspired by the iconic television game show *Who Wants to Be a Millionaire?*. Built from the ground up using **Java Swing**, this project recreates the tension, excitement, and strategic decision-making of the original format while adding modern visual flair and original features.

The game challenges players to answer 16 increasingly difficult questions, with prize money scaling from £100 to £1,000,000. Players must balance knowledge, intuition, and risk management as they climb the money ladder, with lifelines available to aid in moments of uncertainty.

**Key Distinctions:**
- **Original Lifelines** - Each lifeline has been reimagined for a unique gameplay experience
- **Cinematic Presentation** - Animated intro sequence with rotating beams and pulsing logos
- **Dynamic Money Ladder** - Smooth, animated transitions between prize levels
- **Custom Rendering Engine** - All graphics are drawn using Java2D for a cohesive visual experience
- **No External Dependencies** - Runs entirely on standard Java with no third-party libraries

---

## ✨ Features

### Core Gameplay
- **16 Progressive Questions** - Difficulty scales with prize value
- **Timer System** - Each question has a time limit (increasing with difficulty)
- **Money Ladder** - Real-time visual progress tracking with smooth animations
- **Safe Points** - Guaranteed payouts at £1,000, £32,000, and £1,000,000
- **Play or Walk Away** - Option to leave with winnings before each question past £32,000

### Lifelines
| Lifeline | Description | Visual Feedback |
|----------|-------------|-----------------|
| **Swap** | Replace current question with an unused question of similar difficulty | Question refreshes instantly |
| **Audience Poll** | Simulated audience vote showing percentages for each answer | Percentage bars appear under each answer option |
| **25/75** | Eliminates one incorrect answer | Two wrong answers remain locked |
| **Phone a Friend** | Random friend gives their opinion on the correct answer | Message appears under the suggested answer |

### Visual & Audio Features
- **Cinematic Intro** - 12-second animated sequence with rotating studio beams
- **Custom UI Components** - Every visual element is hand-drawn using Java2D
- **Hardware-Antialiased Graphics** - Smooth, professional appearance
- **Responsive Layout** - Adapts to window resizing with dynamic repositioning
- **Animated Components** - Pulsing timer, flashing answer confirmation, smooth transitions

### Menus & Navigation
- **Main Menu** - Play, Credits, and Quit options
- **Credits Screen** - Developer attribution and game inspiration
- **Game Over Screen** - Displays winnings with options to replay or return to menu
- **Win Screen** - Celebratory confetti and fireworks for £1,000,000 victory

---

## 🏗️ Technical Architecture

### Design Pattern: Model-View-Controller (MVC) Inspired

The game follows a clean separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                      GameScreenPanel                        │
│                    (Controller + View)                      │
├─────────────────────────────────────────────────────────────┤
│                        GameSession                         │
│                        (Model)                             │
├─────────────────────────────────────────────────────────────┤
│                      GameScreenRenderer                    │
│                        (View)                              │
└─────────────────────────────────────────────────────────────┘
```

### Component Breakdown

| Component | Responsibility | Key Classes |
|-----------|---------------|-------------|
| **Model** | Game state, question management, scoring | `GameSession`, `Question`, `QuestionBank` |
| **View** | Rendering, visual feedback, UI layout | `GameScreenRenderer`, `MoneyLadder`, `IntroCutscene` |
| **Controller** | Input handling, game logic coordination | `GameScreenPanel`, `MainMenu` |
| **Data** | Question storage, CSV parsing | `QuestionParser`, `Lifelines` |

### Rendering Pipeline

1. **`paintComponent()`** - Triggered by Swing's repaint cycle
2. **`GameScreenRenderer.paint()`** - Draws all game elements in Z-order:
   - Background gradient + glow effects
   - Header (title, question counter, timer, money display)
   - Question card (category, question text, status)
   - Answer buttons (with hover/disabled states)
   - Lifeline buttons (with usage tracking)
   - Lifeline information overlays (Audience Poll, Phone a Friend)
   - Footer (instruction text)

3. **`MoneyLadder`** - Separate panel rendered independently with its own paint cycle

### Animation Framework

The game uses `javax.swing.Timer` for all animations:

- **Countdown Timer** - 1-second intervals, updates timer ring
- **Intro Sequence** - 16ms intervals (60 FPS), 750 frames total
- **Answer Animation** - 250ms intervals, 8 flashes after 2-second delay
- **Money Ladder** - 0.08 progress increments per frame for smooth transitions
- **Lifeline Display** - 8-second auto-dismiss timers

### Event Handling

All user input is handled through Swing's event system:

```
MouseEvent → MouseAdapter → handleClick() → 
  ├── Answer Click → beginAnswerAnimation() → submitAnswer()
  ├── Lifeline Click → useLifeline() → GameSession
  ├── Back Button → returnToMenu()
  └── Play/Walk Panel → Panel dispatches to callbacks
```

---

## 💻 Installation

### Prerequisites
- **Java Development Kit (JDK) 17 or higher**
- **Git** (optional, for cloning)

### Method 1: From Source

```bash
# Clone the repository
git clone https://github.com/SiriWasTaken/Who-Wants-To-Be-a-Millionaire.git

# Navigate to the project directory
cd FinalAnswer

# Compile all source files
javac -d bin src/*.java

# Run the game
java -cp bin Main
```

### Method 2: Using an IDE

1. Open your preferred IDE (IntelliJ IDEA, Eclipse, VS Code)
2. Import the project as a Java project
3. Ensure the source folder (`src/`) is on the classpath
4. Run `Main.java`

### Method 3: Running the JAR (if available)

```bash
java -jar FinalAnswer.jar
```

### File Placement

Ensure the following assets are in the correct locations:

```
src/
├── assets/
│   ├── logoImage.png          # Intro logo (optional)
│   ├── GameSoundtrack.mp3     # Audio file (optional)
│   └── QuestionsList.csv      # Question database (required)
```

---

## 🎮 How to Play

### Objective

Answer all 16 questions correctly to win £1,000,000. Each correct answer advances you up the money ladder. Wrong answers end the game, but you keep your last safe money.

### Controls

| Action | Method |
|--------|--------|
| **Select Answer** | Click on A, B, C, or D button |
| **Activate Lifeline** | Click on SWAP, AUDIENCE, 25/75, or PHONE |
| **Return to Menu** | Click MENU button (top-left) |
| **Skip Intro** | Click anywhere during cutscene |
| **Play/Walk Decision** | Click CONTINUE or WALK AWAY |

### Game Flow

1. **Intro Cutscene** - 12-second cinematic opening (click to skip)
2. **Question Display** - Category and question appear in the card
3. **Timer** - Counts down from the question's time limit
4. **Answer Selection** - Click an answer to lock it in
5. **Confirmation** - 2-second orange highlight, then 2 seconds of flashing
6. **Result** - Green flash for correct, Red flash for incorrect
7. **Advance** - Correct answers move to the next question
8. **Safe Points** - At £1,000, £32,000, and £1,000,000
9. **Play or Walk** - After £32,000, choose to continue or walk away

### Winning Conditions

| Outcome | Result |
|---------|--------|
| **All 16 correct** | Win £1,000,000 + Epic Victory Screen |
| **Wrong answer** | Game Over with last safe money |
| **Timer expires** | Game Over with last safe money |
| **Walk away** | Keep your current winnings |
| **Quit to menu** | No winnings kept |

### Money Ladder

```
16  £1,000,000  ← Grand Prize
15  £750,000
14  £500,000
13  £250,000
12  £125,000
11  £64,000
10  £32,000     ← Safe Point (Play or Walk activates here)
9   £16,000
8   £8,000
7   £4,000
6   £2,000
5   £1,000      ← Safe Point
4   £500
3   £300
2   £200
1   £100
```

---

## 🎯 Lifelines

### SWAP
- **Effect**: Replaces the current question with an unused question of the same difficulty
- **Limitations**: Once per game
- **Strategy**: Use when you're completely stumped on a question
- **Visual**: Question card updates immediately with new content

### AUDIENCE POLL
- **Effect**: Simulates audience voting with realistic percentage distribution
- **Mechanics**: 
  - Correct answer: 40-55% of votes
  - Incorrect answers: 15-25% each
  - Percentages sum to 100%
- **Limitations**: Once per game
- **Strategy**: Trust the majority when unsure
- **Visual**: Percentage labels appear under each answer option for 8 seconds

### 25/75
- **Effect**: Eliminates one incorrect answer, leaving 3 options
- **Limitations**: Once per game
- **Strategy**: Use when you can eliminate one answer yourself
- **Visual**: One incorrect answer becomes disabled/locked

### PHONE A FRIEND
- **Effect**: A random friend gives advice on the correct answer
- **Friend Names**: Alex, Jordan, Taylor, Morgan, Casey, Riley, Avery, Quinn
- **Reliability**: Always gives the correct answer (simulated friend confidence)
- **Limitations**: Once per game
- **Strategy**: Use as a "you're sure this is right" confirmation
- **Visual**: "Friend thinks this is the answer!" appears under the correct option

---

## 🔄 Game Flow Diagram

```
┌─────────────┐
│  Main Menu  │
│  ┌─────────┐│
│  │  PLAY   ││
│  │ CREDITS ││
│  │  QUIT   ││
│  └─────────┘│
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Intro     │  ← 12-second cinematic sequence
│  Cutscene   │  ← Click anywhere to skip
└──────┬──────┘
       │
       ▼
┌─────────────┐      ┌─────────────┐
│ Question N  │─────▶│  Timer      │
│  (1-16)     │      │  Countdown  │
└──────┬──────┘      └──────┬──────┘
       │                     │
       ▼                     │
┌─────────────┐              │
│ Answer      │              │
│ Selection   │              │
└──────┬──────┘              │
       │                     │
       ▼                     │
┌─────────────┐              │
│ 2-Second    │              │
│ Delay       │              │
└──────┬──────┘              │
       │                     │
       ▼                     │
┌─────────────┐              │
│ Flashing    │              │
│ (2 seconds) │              │
└──────┬──────┘              │
       │                     │
       ▼                     ▼
┌─────────────┐      ┌─────────────┐
│  Correct    │      │  Incorrect  │
│  Answer     │      │  Answer     │
└──────┬──────┘      └──────┬──────┘
       │                     │
       ▼                     ▼
┌─────────────┐      ┌─────────────┐
│ Advance to  │      │  Game Over  │
│ Next Q      │      │  (Safe $$)  │
└──────┬──────┘      └──────┬──────┘
       │                     │
       ▼                     ▼
┌─────────────┐      ┌─────────────┐
│ Question 10 │      │  Play Again │
│  (£32,000)  │      │  or Menu    │
│  ┌────────┐ │      └─────────────┘
│  │PLAY or  │ │
│  │WALK?    │ │
│  └────────┘ │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Question   │
│  11-15      │
│  (Repeats   │
│   until     │
│  16 or lose)│
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Question 16 │
│  (£1,000,000)│
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  WIN SCREEN │
│  Confetti   │
│  Fireworks  │
└─────────────┘
```

---

## 📁 File Structure

```
FinalAnswer/
├── src/
│   ├── Main.java                  # Entry point
│   ├── MainMenu.java              # Main menu with custom rendering
│   ├── IntroCutscene.java         # Cinematic opening sequence
│   ├── GameScreen.java            # Game window container
│   ├── GameScreenPanel.java       # Game controller + input handler
│   ├── GameScreenRenderer.java    # Game rendering engine
│   ├── GameSession.java           # Game state management
│   ├── GameOverPanel.java         # Game over screen
│   ├── WinScreenPanel.java        # Victory screen with particles
│   ├── PlayOrWalkPanel.java       # Play or Walk decision overlay
│   ├── MoneyLadder.java           # Money ladder component
│   ├── Question.java              # Question data model
│   ├── QuestionBank.java          # Question deck builder
│   ├── QuestionParser.java        # CSV question loader
│   ├── Lifelines.java             # Lifeline utility methods
│   ├── Credits.java               # Credits screen
│   ├── Settings.java              # Settings placeholder
│   └── assets/
│       ├── logoImage.png          # Intro logo
│       ├── GameSoundtrack.mp3     # Audio file (not implemented)
│       └── QuestionsList.csv      # Question database
└── bin/                           # Compiled .class files
```

### File Descriptions

| File | Lines | Purpose |
|------|-------|---------|
| `GameScreenPanel.java` | ~800 | Core game logic, input handling, animation control |
| `GameScreenRenderer.java` | ~600 | All visual rendering, button drawing, UI layout |
| `GameSession.java` | ~350 | Game state, scoring, timer, lifeline tracking |
| `MoneyLadder.java` | ~280 | Animated money ladder visualization |
| `QuestionBank.java` | ~250 | Question deck construction and difficulty selection |
| `MainMenu.java` | ~220 | Menu UI with custom buttons and animations |
| `IntroCutscene.java` | ~180 | Cinematic intro with rotating beams and logo |
| `GameOverPanel.java` | ~170 | Game over screen with winnings display |
| `WinScreenPanel.java` | ~350 | Victory screen with particle system |
| `QuestionParser.java` | ~150 | CSV parsing with quoted value support |
| `PlayOrWalkPanel.java` | ~320 | Play/Walk decision overlay |
| `Lifelines.java` | ~70 | Lifeline utility methods |

---

## 📦 Dependencies

### Runtime Dependencies
- **Java Runtime Environment (JRE) 17+** - Required to run the game
- **Java Swing** - Built-in GUI toolkit (included with Java)
- **Java2D** - Built-in graphics library (included with Java)

### Optional Dependencies
- **AFPlay (macOS)** - System audio player (optional, for audio)
- **FFPlay (macOS)** - Alternative audio player with seeking support

### Build Dependencies
- **Java Development Kit (JDK) 17+** - Required for compilation
- **No external libraries** - Everything is pure Java

---

## ⚠️ Known Limitations

### Audio
- **Not Implemented** - Audio playback is partially implemented but not functional in the current version
- **No Sound Effects** - Correct/incorrect sounds, lifeline sounds, and background music are placeholders
- **Future** - Audio will be implemented using Java's built-in audio support

### Performance
- **Windows Compatibility** - The game is optimized for macOS; Windows users may experience rendering issues
- **High Memory Usage** - Particle system on win screen uses significant memory (limited to 600 particles)

### Data
- **Hardcoded Paths** - Asset paths are relative and may break if files are moved
- **CSV Format** - Requires specific column ordering (ID,Question,AnswerA,AnswerB,AnswerC,AnswerD,CorrectAnswer,Difficulty,Time,Category)
- **Limited Questions** - Requires at least 16 questions across 16 difficulty levels

### UI/UX
- **No Resolution Scaling** - Fixed resolution of 1100x760
- **No Fullscreen Support** - Windowed mode only
- **No Keyboard Shortcuts** - Mouse-only controls

---

## 🚀 Future Enhancements

### V4 Planned Features
- [ ] **Audio Implementation** - Full soundtrack and sound effects
- [ ] **Settings Menu** - Volume control, difficulty selection, theme options
- [ ] **High Score Tracking** - Persistent storage of top scores
- [ ] **Multiple Question Packs** - Choose from different question categories
- [ ] **Fullscreen Mode** - Toggle between windowed and fullscreen
- [ ] **Keyboard Shortcuts** - 1-4 for answers, L for lifelines

### V5 Stretch Goals
- [ ] **Network Multiplayer** - Compete against friends online
- [ ] **Mobile Port** - Android/iOS version using a different framework
- [ ] **Custom Question Creator** - In-game tool to add new questions
- [ ] **Statistics Tracker** - Track accuracy, lifeline usage, and patterns
- [ ] **Leaderboards** - Global rankings for high scores

---

## 👏 Credits

### Development
- **Me!** - (with some help from Copilot), this game was developed exclusively by me.
### Inspiration
- **Who Wants to Be a Millionaire?** - Original game concept by Celador, the BBC, David Briggs, Mike Whitehill, and Steven Knight
- **Kaun Banega Crorepati** - Indian adaptation that inspired the time-limited format and simplified lifelines

### Tools
- **VS Code** - Primary development environment
- **Git** - Version control
- **OpenJDK 17** - Java development kit

### Special Thanks
- To the countless hours of testing and feedback from classmates
- To the original game show for providing such a compelling format to recreate
---

## 📄 License

This project was created for educational purposes as part of the ICS3U course. 

### Third-Party Assets
- **Game format** - Inspired by "Who Wants to Be a Millionaire?" which is owned by Sony Pictures Television
- **Question content** - Original questions created by the developer and/or sourced from public domain trivia
- **Logo image** - Custom asset; replace with your own or leave as placeholder


## 🎯 Final Thoughts

This project represents over 3,000 lines of Java code, dozens of hours of development, and a passion for recreating one of television's most iconic game shows. From the hand-drawn UI components to the particle-based victory screen, every aspect of this game has been crafted with attention to detail and a commitment to quality.

**Enjoy the game, and may your knowledge serve you well!**

---

*"Is that your final answer?"* 🤔

---

**Version 3.0 | Final Project | ICS3U - Summer 2026**

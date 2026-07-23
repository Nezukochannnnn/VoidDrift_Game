# ✦ VOIDDRIFT ✦

**VOIDDRIFT** is a production-grade 2D procedural roguelike dungeon crawler built natively for Android using Kotlin, Jetpack Compose, and MVVM Clean Architecture.

---

## 🌟 Features & Highlights

- **Procedural BSP Dungeon Generation**: Infinite seed-based dungeon rooms generated via Binary Space Partitioning (BSP) with guaranteed exit reachability via BFS pathfinding.
- **Pure Canvas Rendering**: 5-layer hardware-accelerated Compose Canvas renderer (Dungeon, Enemies, Particles, Player, Torch Lighting Mask).
- **AGSL RuntimeShaders (API 33+)**: Custom AGSL shaders for void swirl backgrounds, creeping void corruption, signature smoothstep torch lighting, and chromatic aberration glitch effects (with API 29-32 RadialGradient and color-split fallbacks).
- **Clean Architecture & MVVM**: Strictly decoupled Presentation, Domain, Data, and Core layers.
- **Reactive Persistence**: Room DB storing run records and cumulative player profiles using Kotlin `Flow`.
- **Dependency Injection**: Dagger Hilt powering all database, repository, and use case singletons.

---

## 🏗️ Architecture Diagram

```
                       +-----------------------------------+
                       |         UI Layer (Compose)        |
                       |  SplashScreen, HomeScreen,        |
                       |  GameScreen, GameOverScreen,      |
                       |  RunHistoryScreen, SettingsScreen |
                       +-----------------+-----------------+
                                         |
                                         v
                       +-----------------+-----------------+
                       |         Presentation            |
                       |         ViewModels &            |
                       |       Canvas Renderers          |
                       +-----------------+-----------------+
                                         |
                                         v
                       +-----------------+-----------------+
                       |           Domain                |
                       |       Use Cases & Models        |
                       |  GenerateDungeon, SaveRunRecord,|
                       |  StartNewRun, GetRunHistory     |
                       +--------+----------------+---------+
                                |                |
                                v                v
            +-------------------+----+      +----+------------------+
            |          Data          |      |         Core          |
            |   Room DB & DataStore  |      |   GameLoop, Physics   |
            |   Repositories Impl    |      |   Particle & Shaders  |
            +------------------------+      +-----------------------+
```

---

## 💻 Tech Stack

- **Language**: Kotlin 2.0+
- **UI**: Jetpack Compose (100% Compose, zero XML layouts)
- **Architecture**: MVVM + Clean Architecture (Data, Domain, Presentation, Core)
- **DI**: Dagger Hilt
- **Database**: Room Database with Kotlin Flow
- **Preferences**: DataStore Preferences
- **Shaders**: AGSL `RuntimeShader` (API 33+) with API 29-32 fallbacks
- **Audio**: SoundPool & MediaPlayer lightweight controller

---

## 🖼️ Screenshot Placeholders

```
+---------------------+  +---------------------+  +---------------------+
|                     |  |                     |  |                     |
|     SPLASH SCREEN   |  |     HOME SCREEN     |  |     GAME SCREEN     |
|   [ Void Swirl ]    |  |   [ Neon Title ]    |  |   [ Torch Lighting] |
|                     |  |                     |  |                     |
+---------------------+  +---------------------+  +---------------------+

+---------------------+  +---------------------+  +---------------------+
|                     |  |                     |  |                     |
|   GAME OVER SCREEN  |  |  RUN HISTORY SCREEN |  |   SETTINGS SCREEN   |
|   [ Glitch Title ]  |  |  [ Ranked Cards ]   |  |  [ Controls & Quality]
|                     |  |                     |  |                     |
+---------------------+  +---------------------+  +---------------------+
```

---

## 🛠️ Build & Run Instructions

### Prerequisites
- **Android Studio**: Ladybug / Jellyfish or newer
- **JDK**: 17+
- **Minimum SDK**: API 29 (Android 10)
- **Target SDK**: API 35 (Android 15)

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/Nezukochannnnn/VoidDrift_Game.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle dependencies.
4. Run on an Android Emulator or physical device (API 29+). For full AGSL shader support, test on an API 33+ device.

---

## 🔊 Sound Assets Setup

Drop custom `.mp3` or `.ogg` sound files into `app/src/main/res/raw/`:
- `ambient_void_hum.mp3` — Low background hum
- `crystal_pickup.mp3` — Crystal chime
- `enemy_hit.mp3` — Enemy hit thud
- `torch_warning.mp3` — Low torch warning pulse
- `game_over_sting.mp3` — Game over sting

---

## 📜 License
MIT License — VoidDrift Game

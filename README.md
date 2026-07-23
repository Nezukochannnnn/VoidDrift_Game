<<<<<<< HEAD
# VoidDrift_Game
A cool game to explore and play.
=======
# VOIDDRIFT 🚀

**VOIDDRIFT** is a high-octane 2D space arcade game built natively for Android using modern, production-grade Android architectural principles.

---

## 🌟 Architectural Features (Phase 1)
- **Language**: Kotlin 2.0+
- **Architecture**: MVVM + Clean Architecture (Data, Domain, Presentation)
- **Dependency Injection**: Dagger Hilt
- **Local Persistence**: Room Database with Kotlin `Flow` for High Scores
- **Preferences**: AndroidX `DataStore` for Game Settings
- **Asynchronous Logic**: Kotlin Coroutines + `StateFlow`
- **Game Engine**: Fixed & Delta-time 60-120 FPS game tick loop decoupled from rendering
- **Graphics Shader Support**: AGSL (`RuntimeShader`) for API 33+ with legacy canvas fallbacks for API 29-32
- **Physics Engine**: Custom 2D vector kinematics, drift momentum, screen wrap, circle & AABB bounding box collision detection

---

## 📁 Package Structure
```
com.voiddrift.game
├── VoidDriftApp.kt
├── di (Hilt Modules: Database, Repository, Dispatchers, Audio)
├── domain
│   ├── model (GameState, PlayerShip, Enemy, Obstacle, Projectile, PowerUp, ScoreRecord, Vector2D)
│   ├── repository (ScoreRepository, SettingsRepository, AudioRepository)
│   └── usecase (UpdatePhysicsUseCase, CheckCollisionsUseCase, SpawnEntitiesUseCase, SaveHighScoreUseCase, GetHighScoresUseCase, ManageSettingsUseCase)
├── data
│   ├── local (Room DB, ScoreDao, ScoreEntity, UserPreferencesDataStore)
│   └── repository (ScoreRepositoryImpl, SettingsRepositoryImpl, AudioRepositoryImpl)
└── presentation
    ├── engine (GameLoopEngine, PhysicsConstants)
    ├── model (GameUiState, AudioEvent, InputCommand)
    ├── shader (AgslShaderManager, VoidDriftShaders)
    ├── navigation (NavRoutes)
    └── viewmodel (GameViewModel, LeaderboardViewModel, SettingsViewModel)
```

---

## 🛠️ Requirements
- Minimum SDK: API 29 (Android 10)
- Target SDK: API 35 (Android 15)
- JDK: 17+
- AGSL RuntimeShader support on Android 13+ (API 33+) with fallback shaders on lower versions.

---

## 📜 License
MIT License - VoidDrift Game
>>>>>>> d47503e (feat: Phase 1 Clean Architecture & Engine Setup for VoidDrift)

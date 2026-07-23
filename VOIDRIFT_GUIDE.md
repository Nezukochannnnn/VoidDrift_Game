# ✦ VOIDRIFT — Technical Interview & System Architecture Guide ✦

Welcome to the official developer guide for **VOIDRIFT**. This document is designed to give you an extreme, production-grade technical breakdown of the entire VOIDRIFT codebase. Use this to prepare for your interviews, explain architectural decisions, and dive deep into the technical specifications.

---

## 🛠️ Section 1: Executive Summary & Project Description

### What is VOIDRIFT?
VOIDRIFT is a native Android 2D procedural dungeon crawler game built with **Kotlin**, **Jetpack Compose (pure Canvas rendering)**, and **MVVM Clean Architecture**. The core gameplay revolves around a player navigating a procedurally generated dungeon grid while managing a decaying torch radius and avoiding creeping void corruption and spawning enemies.

### Core Visual & Technical USP:
1. **Zero Layout Engine Overhead**: The gameplay field runs entirely on a single hardware-accelerated Compose `Canvas`. It bypasses the Compose layout/measure pass entirely, achieving a steady **60–120 FPS**.
2. **AGSL Shader Pipelines (API 33+)**: Custom runtime shaders run directly on the GPU for:
   - Creeping void corruption (Perlin/FBM noise dissolve).
   - Dynamic player-centric torch mask (flickering penumbra smoothstep attenuation).
   - Chromatic Aberration glitch shaders for game-over screens.
3. **BSP Procedural Dungeon Generator**: Dungeon layouts are generated using a Binary Space Partitioning (BSP) tree, guaranteeing solvable layouts via Breadth-First Search (BFS) pathfinding checks.

---

## 📐 Section 2: Architecture Blueprint (Clean Architecture + MVVM)

The project is structured strictly according to Clean Architecture guidelines divided into 3 main layers: **Domain**, **Data**, and **Presentation**, complemented by a **Core Engine** module.

```
+-----------------------------------------------------------------+
|                       PRESENTATION LAYER                        |
|   Screens (Compose) <--> ViewModels (StateFlow) <--> Renderers  |
+--------------------------------+--------------------------------+
                                 |
                                 v
+--------------------------------+--------------------------------+
|                          DOMAIN LAYER                           |
|      Entities (Models)  <-->  Use Cases (Business Rules)        |
+--------------------------------+--------------------------------+
                                 |
                                 v
+--------------------------------+--------------------------------+
|                           DATA LAYER                           |
|       Room DB (DAOs)  <-->  Repositories  <-->  DataStore       |
+-----------------------------------------------------------------+
```

### Dependency Rules:
- **Domain Layer** has no outer dependencies. It defines pure Kotlin models, repository interfaces, and use cases.
- **Data Layer** implements domain repositories, wrapping Room DAOs and DataStore preferences.
- **Presentation Layer** contains Jetpack Compose UI, Canvas renderers, ViewModels, and UI state flows.
- **Core Engine Layer** handles Vector Math, Collision calculation, Shaders, and Particle logic.

---

## 🌲 Section 3: Procedural Dungeon Generation (BSP Tree & BFS Solve)

The dungeon generator in [`GenerateDungeonUseCase.kt`](file:///c:/Users/saksh/OneDrive/Desktop/VoidDrift/app/src/main/java/com/voidrift/game/domain/usecase/GenerateDungeonUseCase.kt) is one of the most technically impressive parts of the project.

### Binary Space Partitioning (BSP) Algorithm:
1. **Node Subdivision**: Starts with a single root bounding block (grid dimensions ranging from 8x8 to 16x16). It recursively splits nodes horizontally or vertically based on a random seed until a minimum room size is met.
2. **Room Carving**: Rooms are carved inside the leaf nodes, leaving a wall buffer.
3. **Corridor Connectivity**: Corridors are carved to connect sister rooms at each level of the tree recursively.
4. **Item & Spawning**: Places crystals (3–6), enemies (2–5), a player starting point, and an exit tile.

### Solvability Check (BFS):
To guarantee the exit is reachable from the player's start position, the algorithm runs a Breadth-First Search (BFS) over the generated path grid. If no path connects player and exit, the dungeon generation is discarded and regenerated using a mutated seed, ensuring a **100% solvable layout** before the room starts.

---

## 🔄 Section 4: Decoupled Game Loop & Tick Pipeline

A robust game loop is mandatory for high-performance physics and synchronization. VOIDRIFT implements a delta-time Choreographer loop in [`GameLoop.kt`](file:///c:/Users/saksh/OneDrive/Desktop/VoidDrift/app/src/main/java/com/voidrift/game/core/engine/GameLoop.kt):

```kotlin
private suspend fun runGameLoop() {
    var lastFrameNanos = 0L
    while (_uiState.value is GameUiState.Playing) {
        withFrameNanos { frameNanos ->
            val deltaTime = if (lastFrameNanos == 0L) 0f
            else (frameNanos - lastFrameNanos) / 1_000_000_000f
            lastFrameNanos = frameNanos
            
            // Limit delta time to prevent physics tunneling
            val dt = deltaTime.coerceIn(0f, 0.05f)
            
            _world.update { world ->
                world
                    .tickVoidCorruption(dt)
                    .tickTorchDecay(dt)
                    .tickEnemyAI(dt)
                    .let { collisionSystem.resolve(it) }
                    .let { particleSystem.tick(it, dt) }
                    .checkWinLoss()
            }
        }
    }
}
```

### Key Tick Functions:
1. **`tickVoidCorruption`**: Simulates cellular automata-like void pressure spreading from void tiles to adjacent floors.
2. **`tickTorchDecay`**: Linearly reduces the player's torch radius over time, shrinking the visible viewport.
3. **`tickEnemyAI`**: Updates enemy velocities using vector targeting (chasing player when within detection radius, attacking when overlapping bounds).
4. **`CollisionSystem`**: Computes Axis-Aligned Bounding Box (AABB) intersection and uses Minimum Translation Vector (MTV) to push the player out of walls.

---

## 🎨 Section 5: GPU Rendering & AGSL Shader Systems

To render advanced graphic effects on Android Compose without dropping frames, VOIDRIFT leverages **AGSL (Android Graphics Shading Language)** on API 33+, with math-based canvas gradient fallables on legacy APIs.

### 🕯️ 1. Signature Torch Lighting Mask
The signature lighting mask creates a dark vignetted overlay with a bright warm circle centered on the player.
- **AGSL Implementation ([`TorchShader.kt`](file:///c:/Users/saksh/OneDrive/Desktop/VoidDrift/app/src/main/java/com/voidrift/game/core/shaders/TorchShader.kt))**:
  ```glsl
  uniform float2 playerPos;
  uniform float  radius;
  uniform float  time;
  half4 main(float2 coord) {
      float dist = distance(coord, playerPos);
      float flicker = 0.93 + 0.07 * sin(time * 19.3);
      float alpha = smoothstep(radius * flicker, radius * 0.25 * flicker, dist);
      return half4(1.0, 0.87, 0.55, alpha);
  }
  ```
- **Fallback (API 29-32)**: Uses a radial gradient brush with `BlendMode.DstIn` to simulate the lighting mask cleanly.

### 😈 2. Creeping Void Corruption
Void tiles are animated with purple-black color lerping based on the progress of the void. On API 33+, we pass a custom noise dissolve shader (`VoidCorruptShader`) that evaluates a warped fractal brownian motion (FBM) field to draw "dissolving" purple borders at the edge of the void.

### 📺 3. Chromatic Aberration Screen Glitch
On the game-over screen, the text "VOID CLAIMS ALL" undergoes chromatic aberration:
```glsl
uniform shader contents;
uniform float  iTime;
uniform float  iOffset;
half4 main(in vec2 fragCoord) {
    float shift = iOffset * sin(iTime * 25.0);
    half4 r = contents.eval(fragCoord + vec2(shift, 0.0));
    half4 g = contents.eval(fragCoord);
    half4 b = contents.eval(fragCoord - vec2(shift, 0.0));
    return half4(r.r, g.g, b.b, max(max(r.a, g.a), b.a));
}
```
It splits the red, green, and blue color channels dynamically on the GPU to simulate an old CRT monitor distortion.

---

## 🤝 Section 6: DI Architecture & Local Persistence

- **Hilt DI**:
  - `DatabaseModule`: Binds Room Database instance and DAOs (`RunDao`, `PlayerDao`).
  - `RepositoryModule`: Binds implementation repos to domain repository interfaces.
  - `UseCaseModule`: Restricts use-case scopes to ViewModels (`@ViewModelScoped`).
- **Room Database**:
  - Automatically records every run score, death cause, depth reached, and timestamp.
  - Uses Kotlin Flows to update Home screen best stats reactively whenever a player finishes a run.
- **Preferences DataStore**:
  - Fast, asynchronous settings read/write for Haptics, Volume, and SFX preferences.

---

## 🎯 Section 7: Key Interview Q&As for Your Panel

### Q1: Why did you choose Compose Canvas instead of standard layouts or XML?
**Answer**: XML layouts or Compose layouts (like columns, boxes) trigger layout, measurement, and draw phases for every single item. For a 16x16 grid with 256 tiles, 5 enemies, and 100 particles, rendering them as individual composable nodes would trigger excessive recombination and object allocations, leading to jank. Using a pure hardware-accelerated Compose `Canvas` with native `DrawScope` reduces the operation to simple lightweight canvas draws, keeping the GPU path highly optimized.

### Q2: How did you implement fallback rendering for devices running below API 33?
**Answer**: AGSL (`RuntimeShader`) was introduced in Android 13 (API 33). To support API 29-32 devices safely, we check `Build.VERSION.SDK_INT`. If below 33, we gracefully swap the GPU-drawn shader paint with optimized standard canvas gradients (e.g. radial brushes in `TorchMaskRenderer.kt` or animated alpha overlays in `DungeonRenderer.kt`), ensuring the game looks visually consistent without crashing on older platforms.

### Q3: What is "physics tunneling" and how did you prevent it in your game loop?
**Answer**: Physics tunneling happens when a game's frame rate drops, causing a large delta-time value. If the step is too large, a fast-moving player can pass right through a thin wall boundary between two frames. We prevented this in `GameLoop.kt` by clamping the delta time `dt = deltaTime.coerceIn(0f, 0.05f)`. This guarantees that even if a frame spikes, the physics simulation treats the step with a maximum threshold, preventing the player from tunneling out of bounds.

### Q4: How is Dagger Hilt beneficial for Clean Architecture?
**Answer**: Hilt helps enforce the dependency inversion principle. The domain layer defines abstractions (`IRunRepository`, `IPlayerRepository`) but doesn't know about implementation details. The data layer implements these repositories. Hilt wires them together at compile time in `@Module` files, allowing us to keep domain code completely decoupled from databases, networking, or local files.

---

## 📄 How to Export This Guide to PDF
1. **VS Code**: Install the **Markdown PDF** extension, right-click this file, and select *Markdown PDF: Export (pdf)*.
2. **Web Browser**: Open this markdown file in any previewer, right-click -> Print -> *Save as PDF*.
3. **Online**: Upload this markdown text to [dillinger.io](https://dillinger.io/) or any Markdown-to-PDF compiler.

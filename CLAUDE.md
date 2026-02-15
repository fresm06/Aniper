# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Project: 애니펄 (Aniper)

## Overview
사용자가 직접 제작한 반려동물 캐릭터를 휴대폰 배경화면(Live Wallpaper)에 풀어두고 교감하며, 마켓을 통해 캐릭터를 거래할 수 있는 인터랙티브 커스터마이징 플랫폼.
## Common Commands

### Building
**IMPORTANT: Claude가 빌드를 직접 실행하고 오류를 해결한다.**
```bash
# Clean build
./gradlew clean build

# Build without tests
./gradlew build -x test

# Run debug APK
./gradlew installDebug

# Run tests
./gradlew test
```

### Linting and Code Quality
```bash
# Run Android Lint
./gradlew lint

# View lint reports
# Reports are generated at: app/build/reports/lint-results-debug.html
```

## Architecture Overview

### Navigation Structure
The app uses **Android Navigation Component** with a single navigation graph (`res/navigation/nav_graph.xml`):
- **AuthFragment**: Login/authentication screen (hidden navigation bar)
- **HomeFragment**: Main home screen (start destination when authenticated)
- **CreatorFragment**: Pet creation/customization UI
- **MarketFragment**: Item marketplace for buying/trading
- **AdminFragment**: Admin/settings functionality

Navigation is managed by `MainActivity` with a `BottomNavigationView` that syncs with the `NavController`. The AuthFragment hides the bottom nav, and authentication transitions use `popUpTo` to clear the back stack.

### Overlay System (`overlay/` package)
The app's core feature is rendering pets as system overlays:

**PetOverlayService**: A foreground service that manages pet overlay windows
- Runs as `FOREGROUND_SERVICE_TYPE_SPECIAL_USE` (Android 12+ compliance)
- Manages WindowManager operations for adding/removing PetView instances to the window
- Creates persistent notification with stop action
- Tracks screen dimensions for coordinate calculations
- Can manage multiple pets simultaneously via `petViews` list

**PetView**: Custom FrameLayout that renders individual pets
- Handles rendering via ImageView
- Implements touch handling for grabbing/dragging pets
- Integrates PhysicsEngine for realistic movement (gravity, velocity)
- Uses AnimationHelper for state-specific animations
- Manages pet lifecycle and window manager parameters (gravity, type, flags)
- States: IDLE, WALKING_LEFT, WALKING_RIGHT, TAP_REACTION, GRABBED, FALLING, LANDING

**PhysicsEngine**: Calculates pet physics
- Applies gravity and velocity
- Handles collision with screen boundaries
- Used by PetView for movement calculations

### Model Layer
- **Pet**: Core pet data class with id, name, assetId, position, state, and active flag
- **PetAsset**: Metadata for pet sprites and animations (width, height, animation frames)
- **MarketItem**: Represents purchasable items in the marketplace

### Data Layer
**LocalPetData**: Singleton-like utility providing sample data
- `samplePets`: Pre-defined Pet instances for demo/testing
- `getAssetById()`: Asset lookup for pet rendering

### UI Utilities
**AnimationHelper**: Manages pet animations based on state
- Handles different animation sequences for each PetState
- Used by PetView to update visuals during state transitions

## Key Technical Decisions

### Permissions
- **SYSTEM_ALERT_WINDOW**: Required for drawing overlays on top of other apps
- **FOREGROUND_SERVICE**: Required for keeping the overlay service active
- **FOREGROUND_SERVICE_SPECIAL_USE**: Android 12+ requirement for special use foreground services
- **POST_NOTIFICATIONS**: Required for the persistent service notification

Request workflow in MainActivity:
1. Request POST_NOTIFICATIONS permission (Android 13+)
2. Check for SYSTEM_ALERT_WINDOW permission
3. If missing, launch Settings.ACTION_MANAGE_OVERLAY_PERMISSION intent
4. Overlay service auto-starts after permission is granted

### ViewBinding
The project uses ViewBinding for type-safe view access. Enable this in fragment/activity layouts and inflate programmatically rather than using findViewById.

### Foreground Service
The PetOverlayService uses a persistent notification to comply with Android foreground service requirements. The notification includes a stop action button for user control.

## Project Structure
```
app/
├── src/main/
│   ├── java/com/aniper/
│   │   ├── MainActivity.kt              # Main activity, handles permissions & navigation
│   │   ├── AniperApp.kt                 # Application class
│   │   ├── overlay/                     # Overlay rendering system
│   │   │   ├── PetOverlayService.kt     # Foreground service managing overlays
│   │   │   ├── PetView.kt               # Individual pet view (FrameLayout)
│   │   │   └── PhysicsEngine.kt         # Physics calculations
│   │   ├── ui/                          # Fragment UI screens
│   │   │   ├── auth/AuthFragment.kt
│   │   │   ├── home/HomeFragment.kt
│   │   │   ├── market/MarketFragment.kt
│   │   │   ├── creator/CreatorFragment.kt
│   │   │   └── admin/AdminFragment.kt
│   │   ├── model/                       # Data models
│   │   │   ├── Pet.kt
│   │   │   ├── PetAsset.kt
│   │   │   └── MarketItem.kt
│   │   ├── data/                        # Data layer
│   │   │   └── LocalPetData.kt
│   │   └── util/                        # Utilities
│   │       └── AnimationHelper.kt
│   ├── res/
│   │   ├── layout/                      # XML layouts
│   │   ├── navigation/nav_graph.xml     # Navigation graph
│   │   ├── values/                      # Resources (strings, themes, colors)
│   │   ├── drawable/                    # Vector drawables and images
│   │   └── menu/                        # Menu resources
│   └── AndroidManifest.xml
└── build.gradle.kts                     # App module build configuration
```

## Dependencies Summary
- **AndroidX Core**: Core functionality (KTX extensions for modern APIs)
- **Navigation**: Fragment navigation with Safe Args
- **Lifecycle**: ViewModel and LiveData
- **Coroutines**: Async operations (android dispatcher)
- **Material Design 3**: UI components and theming
- **ConstraintLayout**: Flexible XML layouts
- **RecyclerView & CardView**: List views and card components
- **Testing**: JUnit 4, Espresso for instrumented tests

## Development Notes

### Adding New Pets
1. Create Pet instance in `LocalPetData.samplePets`
2. Create corresponding PetAsset with sprite dimensions
3. Ensure drawable resources exist for pet sprites
4. Test overlay rendering in PetOverlayService

### Adding New Screens
1. Create Fragment subclass in appropriate `ui/` subdirectory
2. Add layout XML file
3. Add fragment entry and actions to `res/navigation/nav_graph.xml`
4. Update bottom navigation menu if adding main navigation tab
5. Use SafeArgs for type-safe argument passing between fragments

### Window Overlay Considerations
- PetView manages WindowManager.LayoutParams for positioning and layering
- Use `TYPE_APPLICATION_OVERLAY` for app overlay compatibility
- Coordinate system: top-left is (0,0), positive Y is downward
- Screen height calculation accounts for navigation bar height (80px)

## Testing Strategy
- Unit tests for model classes and utilities (in `src/test/`)
- Instrumented tests for UI components and service interactions (in `src/androidTest/`)
- Test overlay rendering by launching PetOverlayService on emulator with overlay permission granted

## 🛠 Tech Stack
- **Language:** Kotlin (Android Native)
- **UI Framework:** Jetpack Compose (Material 3)
- **Core Feature:** Android WallpaperService (Live Wallpaper), System Overlay (Interaction Layer)
- **Animation:** Rive or PNG Sequence (Frame-by-frame)


## ⚡ Efficiency Rules for AI (Token Saving)
1. **No Full Code:** 전체 파일을 다시 작성하지 마라. 수정되거나 추가된 함수/클래스 단위로만 출력하라.
2. **Code Omission:** 변경되지 않은 부분은 `// ... 기존 코드 유지`라고 표기하라.
3. **Architecture:** UI는 `Screen`, `Component`, `ViewModel`로 분리하여 모듈화된 코드를 제공하라.

## 📦 Git Workflow (IMPORTANT)
**모든 수정이 완료된 후에는 반드시 GitHub에 업로드해야 한다:**
1. 변경사항을 정리한 후 `git add` 로 스테이징
2. 변경사항을 요약하는 명확한 커밋 메시지 작성
3. `git commit` 으로 커밋 생성
4. `git push` 로 원격 저장소에 업로드

**커밋 메시지 규칙:**
- 형식: `[타입] 간단한 설명`
- 타입: `feat` (기능 추가), `fix` (버그 수정), `style` (UI/스타일 개선), `refactor` (리팩토링), `docs` (문서)
- 예시: `[feat] 캐릭터 랜덤 동작 및 착지 애니메이션 개선`

## 🎮 Core Logic Requirements
- **Interaction:** 가만히 있을 때, 움직일 때, 터치할 때, 위로 던질 때(Fling)의 4가지 상태를 관리하는 State Machine 구조를 사용할 것.
- **Battery:** 배경화면 실행 시 CPU 소모를 최소화하기 위해 프레임 제한(30~60fps) 로직을 포함할 것.
# Turnly UI Architecture Cleanup + DhikrCounter Parity Refactor

This plan outlines the complete refactor of Turnly's UI architecture to achieve 1:1 visual parity with the DhikrCounter reference project. We will consolidate settings, unify the motion system, and migrate to the authoritative Prismal rendering architecture.

## User Review Required

> [!IMPORTANT]
> **Prismal Migration**: We are moving from `com.kyant.backdrop` to the `Prismal` rendering system used in DhikrCounter.
> **Crash Fix**: A crash (`IllegalArgumentException: Cannot coerce value to an empty range`) was identified. This is likely due to incorrect defaults (Blur Radius 28 instead of 2.8) and missing clamping on parameters passed to the library.

> [!WARNING]
> **Min SDK Update**: DhikrCounter uses `minSdk 31`. Turnly uses `26`. If the Prismal library requires API 31 for some effects (like certain RenderEffects), we may need to increase Turnly's `minSdk`.

## Proposed Changes

---

### [Architecture] Settings & State Refactor

#### [MODIFY] [UserPreferencesRepository.kt](file:///D:/Turnly/app/src/main/java/com/crescentapps/turnly/data/preferences/UserPreferencesRepository.kt)
- Correct `DEFAULT_GLASS_BLUR_RADIUS` from `28f` to `2.8f`.
- Ensure all glass defaults match DhikrCounter's `SettingsManager.java`.
- Standardize all glass and motion keys.

---

### [Visuals] Glass & Rendering Migration (Stability Pass)

#### [MODIFY] [GlassComponents.kt](file:///D:/Turnly/app/src/main/java/com/crescentapps/turnly/presentation/components/GlassComponents.kt)
- **Safety Clamping**: Add explicit clamping to all values in `applyCalibratedSettings` to prevent library internal `coerceIn` failures.
- **IOR Fix**: Ensure IOR remains within `1.0..2.5` (standard physical range) to avoid rendering artifacts.
- **Density Awareness**: Verify that `blurRadius` and `thickness` are scaled correctly by density and checked for minimum values (e.g., `coerceAtLeast(density)` if required by library).

---

### [Infrastructure] Build Configuration

#### [MODIFY] [build.gradle.kts](file:///D:/Turnly/app/build.gradle.kts)
- Consider increasing `minSdk` to `31` to match DhikrCounter and ensure full compatibility with Prismal's rendering pipeline.

---

## Verification Plan

### Automated Tests
- Build and run on a physical device or emulator with density matching the reported crash (e.g., 3.2x).
- Verify that Logcat no longer shows `IllegalArgumentException` on the GL thread.

### Manual Verification
- **Settings Stress Test**: Move all sliders to their minimum and maximum values to ensure no edge cases trigger crashes.
- **Theme Switching**: Rapidly switch between Light, Dark, and AMOLED themes.

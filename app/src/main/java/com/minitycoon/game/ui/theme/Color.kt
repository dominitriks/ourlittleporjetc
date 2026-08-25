package com.minitycoon.game.ui.theme

import androidx.compose.ui.graphics.Color

// Light theme (system Material chrome — app bars, dialogs, default surfaces)
val TycoonGreenLight = Color(0xFF2E7D32)
val TycoonGoldLight = Color(0xFFF9A825)
val BackgroundLight = Color(0xFFF5F7F3)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF1B1F1C)

// Dark theme
val TycoonGreenDark = Color(0xFF66BB6A)
val TycoonGoldDark = Color(0xFFFFCA28)
val BackgroundDark = Color(0xFF121412)
val SurfaceDark = Color(0xFF1D211D)
val OnSurfaceDark = Color(0xFFE7ECE7)

// =====================================================================
// ART BIBLE — scene & game-UI illustration palette.
//
// This is the single source of truth for the "world" (factory + backdrop)
// and for the game-chrome (HUD panel, buttons) look. It is intentionally
// theme-independent: the illustrated world keeps the same lighting and
// colors regardless of the device's light/dark system setting, the same
// way a painted mobile game scene doesn't re-tint itself for dark mode.
//
// Conventions any future business (restaurant, farm, bank, ...) should
// follow to stay visually consistent with the factory:
//   - Light source: top-left, warm (drives every highlight placement).
//   - Every solid surface is a 2-stop vertical gradient, light->dark,
//     never a flat fill — that's what reads as "premium" instead of
//     "flat clip-art".
//   - Ambient shadow under every grounded object: a soft dark radial
//     ellipse (SoftShadow), never a hard-edged flat oval.
//   - Metal parts (pipes, tanks, vents, chimneys) get a diagonal
//     specular highlight stripe (MetalHighlight) at ~35% width.
//   - Corner radius: large shapes ~6% of their own width; small props
//     ~12%. Nothing is a hard 90° corner.
//   - Outline: a single 1.5-2dp darker-shade stroke (never black) on
//     primary silhouettes (building body, roof) to keep shapes readable
//     at small size — see *Outline colors below.
// =====================================================================

// --- Feedback colors (money/upgrade signal colors used across UI) ---
val MoneyGreen = Color(0xFF3DDC84)
val MoneyGreenDeep = Color(0xFF1B8B4C)
val UpgradeOrange = Color(0xFFFFA726)
val UpgradeOrangeDeep = Color(0xFFE65100)
val GlowYellow = Color(0xFFFFEE58)
val CoinGold = Color(0xFFFFD54F)
val CoinGoldDeep = Color(0xFFF9A825)
val CoinGoldOutline = Color(0xFFB8860B)

// --- Sky & atmosphere ---
val SkyTop = Color(0xFF5AA9E6)
val SkyMid = Color(0xFF8FCBEF)
val SkyBottom = Color(0xFFF3E9D2)
val SunGlow = Color(0xFFFFF3B0)

// --- Distant parallax hills (muted, desaturated for depth) ---
val HillFar = Color(0xFFA9C4D8)
val HillNear = Color(0xFF8FB3A8)

// --- Ground / road / vegetation ---
val GroundGreen = Color(0xFF7BB661)
val GroundGreenDark = Color(0xFF5C9448)
val FactoryLotDirt = Color(0xFFC9B78C)
val RoadGray = Color(0xFF6B6E75)
val RoadGrayLight = Color(0xFF83868D)
val RoadLineWhite = Color(0xFFF2F0E6)
val CloudWhite = Color(0xFFFFFFFF)
val TreeCanopyGreen = Color(0xFF4C8C3F)
val TreeCanopyGreenLight = Color(0xFF6BAE52)
val TreeTrunkBrown = Color(0xFF6B4A30)
val BushGreen = Color(0xFF5A9950)
val FenceWood = Color(0xFF9C7A52)
val FenceWoodDark = Color(0xFF7A5C3D)

// --- Factory materials: brick/tile stages (1-3) ---
val FactoryWallLight = Color(0xFFF0DFC0)
val FactoryWallDark = Color(0xFFD8B98C)
val FactoryWallOutline = Color(0xFF8A6A45)
val FactoryRoofLight = Color(0xFFE0574A)
val FactoryRoofDark = Color(0xFFA82F26)
val FactoryWindowSky = Color(0xFF2F6FA8)
val FactoryWindowGlass = Color(0xFF9FDCF5)
val FactoryWindowGlow = Color(0xFFFFE9A8)
val FactoryDoorLight = Color(0xFF8A5A3C)
val FactoryDoorDark = Color(0xFF5C3A24)

// --- Factory materials: modern stages (4-5, cooler glass/steel palette) ---
val FactoryWallModernLight = Color(0xFFE8EDF3)
val FactoryWallModernDark = Color(0xFFB7C3D1)
val FactoryWallModernOutline = Color(0xFF5A6B7D)
val FactoryRoofModernLight = Color(0xFF6C8494)
val FactoryRoofModernDark = Color(0xFF3E5464)
val FactoryWindowModernGlass = Color(0xFF7FE3E8)
val FactoryAccentTeal = Color(0xFF26C6DA)

// --- Metal / machinery (chimneys, pipes, tanks, vents — shared across stages) ---
val MetalLight = Color(0xFFC7CDD4)
val MetalMid = Color(0xFF9AA3AC)
val MetalDark = Color(0xFF6B747C)
val MetalHighlight = Color(0x99FFFFFF)
val RustAccent = Color(0xFFB0603A)
val TankOrange = Color(0xFFD9713C)
val SmokeGray = Color(0xFFD8D8D8)

// --- Small props ---
val CrateWood = Color(0xFFB98A54)
val CrateWoodDark = Color(0xFF8C6539)
val BarrelBlue = Color(0xFF3E7CB1)
val BarrelBlueDark = Color(0xFF2A5A85)
val PalletTan = Color(0xFFC7A46B)
val SignBoardBrown = Color(0xFF6E4A32)
val SignBoardText = Color(0xFFFFF6E3)
val TruckRed = Color(0xFFD64545)
val TruckRedDark = Color(0xFF9E2E2E)
val TruckGlass = Color(0xFFBFE6F5)

val SoftShadow = Color(0x40142014)

// --- Game-chrome (HUD & buttons) — dark "premium mobile game" panel style ---
val PanelNavyTop = Color(0xFF2B3550)
val PanelNavyBottom = Color(0xFF1A2138)
val PanelBorderHighlight = Color(0x33FFFFFF)
val PanelShadow = Color(0x552B3550)

val ButtonUpgradeTop = Color(0xFFFFC259)
val ButtonUpgradeBottom = Color(0xFFE8862B)
val ButtonUpgradeDisabledTop = Color(0xFF8A8578)
val ButtonUpgradeDisabledBottom = Color(0xFF6B675D)

val ButtonCollectTop = Color(0xFF63E08A)
val ButtonCollectBottom = Color(0xFF1FA35C)

val ButtonTextLight = Color(0xFFFFFDF7)
val ButtonTextShadow = Color(0x66000000)

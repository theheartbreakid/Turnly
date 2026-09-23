package com.crescentapps.turnly.presentation

import androidx.compose.runtime.Composable
import com.crescentapps.turnly.TurnlyApplication
import com.crescentapps.turnly.core.model.UiMode
import com.crescentapps.turnly.data.preferences.UserPreferences
import com.crescentapps.turnly.presentation.liquid.LiquidApp
import com.crescentapps.turnly.presentation.m3.Material3App

/**
 * Top-Level Turnly App Root.
 * Switches cleanly between two completely separate visual presentations:
 * - Material 3 (DEFAULT, Stable/Recommended): Standard Compose M3 components, 0 Liquid shader overhead.
 * - Liquid UI (Experimental): DhikrCounter-derived Prismal glass & shader canvas.
 */
@Composable
fun TurnlyApp(
    app: TurnlyApplication,
    prefs: UserPreferences,
    deepLinkCode: String? = null
) {
    when (prefs.uiMode) {
        UiMode.MATERIAL_3 -> {
            Material3App(
                app = app,
                prefs = prefs,
                deepLinkCode = deepLinkCode
            )
        }
        UiMode.LIQUID -> {
            LiquidApp(
                app = app,
                prefs = prefs,
                deepLinkCode = deepLinkCode
            )
        }
    }
}

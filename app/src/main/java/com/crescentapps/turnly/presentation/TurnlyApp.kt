package com.crescentapps.turnly.presentation

import androidx.compose.runtime.Composable
import com.crescentapps.turnly.TurnlyApplication
import com.crescentapps.turnly.data.preferences.UserPreferences
import com.crescentapps.turnly.presentation.liquid.LiquidApp

/**
 * Top-Level Turnly App Root.
 * Liquid UI is the only visual presentation.
 */
@Composable
fun TurnlyApp(
    app: TurnlyApplication,
    prefs: UserPreferences,
    deepLinkCode: String? = null
) {
    LiquidApp(
        app = app,
        prefs = prefs,
        deepLinkCode = deepLinkCode
    )
}

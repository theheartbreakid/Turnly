package com.crescentapps.turnly.presentation

import androidx.compose.runtime.Composable
import com.crescentapps.turnly.TurnlyApplication
import com.crescentapps.turnly.core.model.UiSystemMode
import com.crescentapps.turnly.data.preferences.UserPreferences
import com.crescentapps.turnly.presentation.gaussianblur.GaussianBlurApp
import com.crescentapps.turnly.presentation.liquid.LiquidApp

/**
 * Top-Level Turnly App Root Router.
 * Routes seamlessly between GaussianBlur UI and Liquid UI based on user preference.
 */
@Composable
fun TurnlyApp(
    app: TurnlyApplication,
    prefs: UserPreferences,
    deepLinkCode: String? = null
) {
    when (prefs.uiSystemMode) {
        UiSystemMode.GAUSSIAN_BLUR -> {
            GaussianBlurApp(
                app = app,
                prefs = prefs,
                deepLinkCode = deepLinkCode
            )
        }
        UiSystemMode.LIQUID -> {
            LiquidApp(
                app = app,
                prefs = prefs,
                deepLinkCode = deepLinkCode
            )
        }
    }
}

import re

with open('D:/Turnly/app/src/main/java/com/crescentapps/turnly/presentation/gaussianblur/screens/GaussianSettingsScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

imports = '''
import com.crescentapps.turnly.presentation.gaussianblur.theme.GaussianOptics
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianOptics
import androidx.compose.runtime.CompositionLocalProvider
'''
content = content.replace('import java.util.Locale', 'import java.util.Locale\n' + imports)

local_vars = '''
    var localBlurRadius by remember(prefs.gaussianBlurRadius) { mutableFloatStateOf(prefs.gaussianBlurRadius) }
    var localSurfaceOpacity by remember(prefs.gaussianSurfaceOpacity) { mutableFloatStateOf(prefs.gaussianSurfaceOpacity) }
    var localRefractionStrength by remember(prefs.gaussianRefractionStrength) { mutableFloatStateOf(prefs.gaussianRefractionStrength) }
    var localDynamicHighlightsIntensity by remember(prefs.gaussianDynamicHighlightsIntensity) { mutableFloatStateOf(prefs.gaussianDynamicHighlightsIntensity) }
    var localSpecularIntensity by remember(prefs.gaussianSpecularIntensity) { mutableFloatStateOf(prefs.gaussianSpecularIntensity) }
    var localCondensedLightRadius by remember(prefs.gaussianCondensedLightRadius) { mutableFloatStateOf(prefs.gaussianCondensedLightRadius) }

    val liveOptics = GaussianOptics(
        blurRadiusDp = localBlurRadius,
        surfaceOpacity = localSurfaceOpacity,
        refractionEnabled = prefs.gaussianRefractionEnabled,
        refractionHeightDp = localRefractionStrength * 24f,
        refractionAmountDp = localRefractionStrength * 48f,
        specularEnabled = prefs.gaussianSpecularEnabled,
        specularIntensity = localSpecularIntensity,
        dynamicHighlightsEnabled = prefs.gaussianDynamicHighlightsEnabled,
        dynamicHighlightsIntensity = localDynamicHighlightsIntensity,
        condensedLightEnabled = prefs.gaussianCondensedLightEnabled,
        condensedLightRadius = localCondensedLightRadius,
        reduceMotion = prefs.isReduceMotion
    )

    CompositionLocalProvider(LocalGaussianOptics provides liveOptics) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
'''
content = content.replace('    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {', local_vars)

content = content.replace('    // UI System Switcher Dialog', '    }\n\n    // UI System Switcher Dialog')

optics_sliders = '''
                        GaussianOpticsSliderRow(
                            title = "Backdrop Blur Radius",
                            value = localBlurRadius,
                            range = 0f..80f,
                            unit = "dp",
                            onValueChange = { localBlurRadius = it },
                            onValueChangeFinished = { viewModel.setGaussianBlurRadius(localBlurRadius) }
                        )

                        HorizontalDivider(color = colors.divider)

                        GaussianOpticsSliderRow(
                            title = "Surface Opacity",
                            value = localSurfaceOpacity * 100f,
                            range = 5f..95f,
                            unit = "%",
                            onValueChange = { localSurfaceOpacity = it / 100f },
                            onValueChangeFinished = { viewModel.setGaussianSurfaceOpacity(localSurfaceOpacity) }
                        )

                        HorizontalDivider(color = colors.divider)

                        GaussianOpticsSliderRow(
                            title = "Lens Refraction",
                            value = localRefractionStrength * 100f,
                            range = 0f..200f,
                            unit = "%",
                            onValueChange = { localRefractionStrength = it / 100f },
                            onValueChangeFinished = { viewModel.setGaussianRefractionStrength(localRefractionStrength) }
                        )

                        HorizontalDivider(color = colors.divider)

                        GaussianOpticsSliderRow(
                            title = "Dynamic Highlights",
                            value = localDynamicHighlightsIntensity * 100f,
                            range = 0f..200f,
                            unit = "%",
                            onValueChange = { localDynamicHighlightsIntensity = it / 100f },
                            onValueChangeFinished = { viewModel.setGaussianDynamicHighlightsIntensity(localDynamicHighlightsIntensity) }
                        )

                        HorizontalDivider(color = colors.divider)

                        GaussianOpticsSliderRow(
                            title = "Specular Reflection Rim",
                            value = localSpecularIntensity * 100f,
                            range = 0f..200f,
                            unit = "%",
                            onValueChange = { localSpecularIntensity = it / 100f },
                            onValueChangeFinished = { viewModel.setGaussianSpecularIntensity(localSpecularIntensity) }
                        )

                        HorizontalDivider(color = colors.divider)

                        GaussianOpticsSliderRow(
                            title = "Condensed Interaction Light",
                            value = localCondensedLightRadius,
                            range = 10f..200f,
                            unit = "dp",
                            onValueChange = { localCondensedLightRadius = it },
                            onValueChangeFinished = { viewModel.setGaussianCondensedLightRadius(localCondensedLightRadius) }
                        )
'''

content = re.sub(
    r'GaussianOpticsSliderRow\(\s*title = "Backdrop Blur Radius".*?onCheckedChange = \{ viewModel\.setGaussianCondensedLightEnabled\(it\) \}\n\s*\)',
    optics_sliders.strip(),
    content,
    flags=re.DOTALL
)

slider_row_sig_old = '''fun GaussianOpticsSliderRow(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    unit: String,
    onValueChange: (Float) -> Unit
) {'''
slider_row_sig_new = '''fun GaussianOpticsSliderRow(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    unit: String,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null
) {'''
content = content.replace(slider_row_sig_old, slider_row_sig_new)

slider_call_old = '''        GaussianSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range
        )'''
slider_call_new = '''        GaussianSlider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = range
        )'''
content = content.replace(slider_call_old, slider_call_new)

with open('D:/Turnly/app/src/main/java/com/crescentapps/turnly/presentation/gaussianblur/screens/GaussianSettingsScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)

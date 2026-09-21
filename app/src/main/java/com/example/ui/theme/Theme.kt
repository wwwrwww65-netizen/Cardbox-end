package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PosIndigoPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = PosIndigoContainerDark,
    onPrimaryContainer = PosIndigoOnContainerDark,
    inversePrimary = PosIndigoPrimary,
    
    secondary = PosTealSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = PosTealContainerDark,
    onSecondaryContainer = PosTealOnContainerDark,
    
    tertiary = PosVioletTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = PosVioletContainerDark,
    onTertiaryContainer = PosVioletOnContainerDark,
    
    background = PosDarkBackground,
    onBackground = PosDarkTextPrimary,
    
    surface = PosDarkSurface,
    onSurface = PosDarkTextPrimary,
    surfaceVariant = PosDarkSurfaceVariant,
    onSurfaceVariant = PosDarkTextSecondary,
    
    surfaceContainerLowest = PosDarkBackground,
    surfaceContainerLow = PosDarkSurfaceContainerLow,
    surfaceContainer = PosDarkSurfaceContainer,
    surfaceContainerHigh = PosDarkSurfaceContainerHigh,
    surfaceContainerHighest = Color(0xFF32405D),
    
    surfaceBright = Color(0xFF1E293B),
    surfaceDim = PosDarkBackground,
    
    outline = PosDarkOutline,
    outlineVariant = PosDarkOutlineVariant,
    
    error = PosRedErrorLight,
    onError = Color.White,
    errorContainer = PosRedContainerDark,
    onErrorContainer = PosRedOnContainerDark,
    
    inverseSurface = PosBackground,
    inverseOnSurface = PosTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = PosIndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = PosIndigoContainerLight,
    onPrimaryContainer = PosIndigoOnContainerLight,
    inversePrimary = PosIndigoPrimaryLight,
    
    secondary = PosTealSecondary,
    onSecondary = Color.White,
    secondaryContainer = PosTealContainerLight,
    onSecondaryContainer = PosTealOnContainerLight,
    
    tertiary = PosVioletTertiary,
    onTertiary = Color.White,
    tertiaryContainer = PosVioletContainerLight,
    onTertiaryContainer = PosVioletOnContainerLight,
    
    background = PosBackground,
    onBackground = PosTextPrimary,
    
    surface = PosSurface,
    onSurface = PosTextPrimary,
    surfaceVariant = PosSurfaceVariant,
    onSurfaceVariant = PosTextSecondary,
    
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = PosSurfaceContainerLow,
    surfaceContainer = PosSurfaceContainer,
    surfaceContainerHigh = PosSurfaceContainerHigh,
    surfaceContainerHighest = Color(0xFFCBD5E1),
    
    surfaceBright = Color.White,
    surfaceDim = Color(0xFFEDEAE6),
    
    outline = PosOutline,
    outlineVariant = PosOutlineVariant,
    
    error = PosRedError,
    onError = Color.White,
    errorContainer = PosRedContainerLight,
    onErrorContainer = PosRedOnContainerLight,
    
    inverseSurface = PosDarkSurface,
    inverseOnSurface = PosDarkTextPrimary
)

@Composable
fun MikroTikPosTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    darkTheme: Boolean = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    },
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography
        ) {
            ProvideTextStyle(value = TextStyle(fontFamily = ArabicFontFamily)) {
                content()
            }
        }
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = MikroTikPosTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)



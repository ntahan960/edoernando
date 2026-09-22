package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = CoralRose,
    onPrimary = Color.White,
    primaryContainer = CoralRoseDark,
    onPrimaryContainer = SoftPink,
    secondary = WarmWaffle,
    onSecondary = Color.White,
    secondaryContainer = DarkSurfaceSubtle,
    onSecondaryContainer = SoftWaffle,
    tertiary = MintMatcha,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceSubtle,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    outline = Color(0xFF3F3D45)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CoralRose,
    onPrimary = Color.White,
    primaryContainer = SoftPink,
    onPrimaryContainer = CoralRoseDark,
    secondary = WarmWaffle,
    onSecondary = Color.White,
    secondaryContainer = SoftWaffle,
    onSecondaryContainer = WarmWaffle,
    tertiary = MintMatcha,
    background = CreamBackground,
    surface = CreamSurface,
    surfaceVariant = SurfaceSubtle,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = BorderLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}


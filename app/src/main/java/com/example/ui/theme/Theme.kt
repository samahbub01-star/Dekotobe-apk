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
    primary = SleekRed,
    onPrimary = Color.White,
    primaryContainer = SleekRedAccent,
    onPrimaryContainer = Color.White,
    secondary = SleekBlue,
    onSecondary = Color.White,
    tertiary = SleekGreen,
    onTertiary = Color.Black,
    background = ZincBg,
    onBackground = ZincTextPrimary,
    surface = ZincSurface,
    onSurface = ZincTextPrimary,
    surfaceVariant = ZincCard,
    onSurfaceVariant = ZincTextSecondary,
    surfaceContainer = ZincSurface,
    surfaceContainerHigh = ZincElevated,
    outline = ZincBorder,
    outlineVariant = ZincBorderLight
  )

private val LightColorScheme = DarkColorScheme

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

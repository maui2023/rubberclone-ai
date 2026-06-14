package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = RisdaGold,
    onPrimary = RisdaTextOnPrimary,
    secondary = RisdaEmerald,
    onSecondary = RisdaTextOnPrimary,
    background = RisdaDeepBackground,
    onBackground = RisdaTextOnSurface,
    surface = RisdaSurface,
    onSurface = RisdaTextOnSurface,
    surfaceVariant = RisdaSurfaceVariant,
    onSurfaceVariant = RisdaTextMuted,
    tertiary = RisdaForest
  )

private val LightColorScheme = DarkColorScheme // Mengutamakan tema gelap tersendiri RISDA Malaysia mengikut kehendak pengguna

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

package org.sprachcafe.member.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SprachCafeRed,
    secondary = SprachCafeGold,
    tertiary = SprachCafeDarkRed,
    background = SprachCafeCream,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFF1D1B1A),
    onSurface = Color(0xFF1D1B1A)
)

@Composable
fun SprachCafeMemberTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}

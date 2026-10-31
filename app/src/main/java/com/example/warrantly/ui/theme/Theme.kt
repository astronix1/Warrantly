package com.example.warrantly.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.warrantly.data.model.WarrantyStatus

private val LightColorScheme = lightColorScheme(
    primary = WarrantlyGreen40,
    onPrimary = Color.White,
    primaryContainer = WarrantlyGreen90,
    onPrimaryContainer = WarrantlyGreen10,
    background = WarrantlyNeutral99,
    surface = WarrantlyNeutral99,
    onBackground = WarrantlyNeutral10,
    onSurface = WarrantlyNeutral10,
    surfaceVariant = WarrantlyNeutral90,
)

private val DarkColorScheme = darkColorScheme(
    primary = WarrantlyGreen80,
    onPrimary = WarrantlyGreen20,
    primaryContainer = WarrantlyGreen30,
    onPrimaryContainer = WarrantlyGreen90,
    background = WarrantlyNeutral10,
    surface = WarrantlyNeutral10,
    onBackground = WarrantlyNeutral90,
    onSurface = WarrantlyNeutral90,
    surfaceVariant = WarrantlyNeutral20,
)

@Composable
fun WarrantlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

object WarrantlyColors {
    @Composable
    fun statusColor(status: WarrantyStatus): Color {
        val dark = isSystemInDarkTheme()
        return when (status) {
            WarrantyStatus.ACTIVE -> if (dark) StatusActiveDark else StatusActive
            WarrantyStatus.EXPIRING_SOON -> if (dark) StatusExpiringSoonDark else StatusExpiringSoon
            WarrantyStatus.EXPIRED -> if (dark) StatusExpiredDark else StatusExpired
            WarrantyStatus.UNKNOWN -> if (dark) StatusUnknownDark else StatusUnknown
        }
    }

    @Composable
    fun statusContainerColor(status: WarrantyStatus): Color {
        val dark = isSystemInDarkTheme()
        return when (status) {
            WarrantyStatus.ACTIVE -> if (dark) StatusActiveContainerDark else StatusActiveContainer
            WarrantyStatus.EXPIRING_SOON -> if (dark) StatusExpiringSoonContainerDark else StatusExpiringSoonContainer
            WarrantyStatus.EXPIRED -> if (dark) StatusExpiredContainerDark else StatusExpiredContainer
            WarrantyStatus.UNKNOWN -> if (dark) StatusUnknownContainerDark else StatusUnknownContainer
        }
    }

    @Composable
    fun statusOnContainerColor(status: WarrantyStatus): Color {
        // On-container text uses the status color itself for contrast against container
        return statusColor(status)
    }
}

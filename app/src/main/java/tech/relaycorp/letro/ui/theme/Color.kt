package tech.relaycorp.letro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Primary1 = Color(0xFF250070)
val Primary2 = Color(0xFF7058E2)
val Primary3 = Color(0xFFA596EF)
val Secondary1 = Color(0xFF00034F)
val Secondary2 = Color(0xFF64B5FF)
val Secondary5 = Color(0xFFD1E4FF)
val Error1 = Color(0xFFBA1A1A)
val Error2 = Color(0xFFFFB4B4)
val Neutral2 = Color(0xFF0C1B44)
val Neutral3 = Color(0xFF5A6688)
val Neutral4 = Color(0xFF6D7B9E)
val Neutral5 = Color(0xFF818DAF)
val Neutral6 = Color(0xFFE0E6FF)
val Neutral7 = Color(0xFFF2F5FF)
val Neutral8 = Color(0xFFFFFFFF)
val NeutralVariant1 = Color(0xFF1C1B1F)
val NeutralVariant2 = Color(0xFF2A2830)
val NeutralVariant3 = Color(0xFF2F2D35)
val NeutralVariant4 = Color(0xFF403D4A)
val NeutralVariant5 = Color(0xFFAFB3BE)
val NeutralVariant6 = Color(0xFFBDC2CF)
val NeutralVariant7 = Color(0xFFE6E8EE)
val NeutralVariant8 = Color(0xFFE6E8EE)

object LetroColor {

    val FoggingBackgroundColor = Color(0x52000000)

    val SurfaceContainerHigh: Color
        @Composable
        get() = if (isSystemInDarkTheme()) NeutralVariant2 else Primary2

    val OnSurfaceContainerHigh: Color
        @Composable
        get() = if (isSystemInDarkTheme()) NeutralVariant8 else Neutral8

    val SurfaceContainer: Color
        @Composable
        get() = if (isSystemInDarkTheme()) NeutralVariant4 else Neutral6

    val OnSurfaceContainer: Color
        @Composable
        get() = if (isSystemInDarkTheme()) NeutralVariant6 else Neutral4

    val OnSurfaceVariant: Color
        @Composable
        get() = if (isSystemInDarkTheme()) NeutralVariant5 else Neutral3

    val SurfaceContainerLow: Color
        @Composable
        get() = if (isSystemInDarkTheme()) NeutralVariant3 else Neutral8

    @Composable
    fun statusBarUnderDialogOverlay(): Color {
        return if (isSystemInDarkTheme()) Color(0xFF1C1B1F) else Color(0xFF4A3C99)
    }

    @Composable
    fun disabledButtonBackgroundColor(): Color {
        return if (isSystemInDarkTheme()) Color(0x1AEFF0F3) else Color(0x1A0C1B44)
    }

    @Composable
    fun disabledButtonTextColor(): Color {
        return if (isSystemInDarkTheme()) Color(0xFF706F73) else Color(0x5C0C1B44)
    }
}

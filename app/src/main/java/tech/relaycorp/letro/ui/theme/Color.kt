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
val Error2 = Color(0xFFFFC4C4)
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
val NeutralVariant8 = Color(0xFFEFF0F3)

object LetroColor {
    
    val SurfaceContainerHigh: Color
        @Composable
        get() = if (isSystemInDarkTheme()) NeutralVariant2 else Primary2

    val SurfaceContainerLow: Color
        @Composable
        get() = if (isSystemInDarkTheme()) NeutralVariant3 else Neutral8

    @Composable
    fun disabledButtonBackgroundColor(): Color {
        return if (isSystemInDarkTheme()) Color(0x1AEFF0F3) else Color(0x1A0C1B44)
    }

    @Composable
    fun disabledButtonTextColor(): Color {
        return if (isSystemInDarkTheme()) Color(0x1AEFF0F3) else Color(0x5C0C1B44)
    }

}

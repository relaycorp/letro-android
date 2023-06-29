package tech.realycorp.letro.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import tech.realycorp.letro.R

val Inter = FontFamily(
    Font(R.font.inter_regular),
    Font(R.font.inter_black, weight = FontWeight.Black),
    Font(R.font.inter_bold, weight = FontWeight.Bold),
    Font(R.font.inter_light, weight = FontWeight.Light),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_extra_light, FontWeight.ExtraLight),
    Font(R.font.inter_extra_bold, FontWeight.ExtraBold),
    Font(R.font.inter_semi_bold, FontWeight.SemiBold),
    Font(R.font.inter_thin, FontWeight.Thin),
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.5.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    // TODO
    labelSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

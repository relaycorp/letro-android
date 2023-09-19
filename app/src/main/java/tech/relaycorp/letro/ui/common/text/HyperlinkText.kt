package tech.relaycorp.letro.ui.common.text

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.sp

@Composable
fun HyperlinkText(
    fullText: String,
    hyperLinks: Map<String, String>,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    linkTextColor: Color = MaterialTheme.colorScheme.primary,
    linkTextFontWeight: FontWeight = FontWeight.Normal,
    linkTextDecoration: TextDecoration = TextDecoration.None,
    fontSize: TextUnit = TextUnit.Unspecified,
) {
    val annotatedString = buildAnnotatedString {
        withStyle(
            style = ParagraphStyle(
                lineHeight = 18.sp,
            ),
        ) {
            append(fullText)
            addStyle(
                style = SpanStyle(
                    fontSize = fontSize,
                    color = textColor,
                    letterSpacing = TextUnit(0.2f, TextUnitType.Sp),
                ),
                start = 0,
                end = fullText.length,
            )
            for ((key, value) in hyperLinks) {
                val startIndex = fullText.indexOf(key)
                val endIndex = startIndex + key.length
                addStyle(
                    style = SpanStyle(
                        color = linkTextColor,
                        fontSize = fontSize,
                        fontWeight = linkTextFontWeight,
                        textDecoration = linkTextDecoration,
                        letterSpacing = TextUnit(0.2f, TextUnitType.Sp),
                    ),
                    start = startIndex,
                    end = endIndex,
                )
                addStringAnnotation(
                    tag = "URL",
                    annotation = value,
                    start = startIndex,
                    end = endIndex,
                )
            }
        }
    }

    val uriHandler = LocalUriHandler.current

    ClickableText(
        modifier = modifier,
        text = annotatedString,
        onClick = {
            annotatedString
                .getStringAnnotations("URL", it, it)
                .firstOrNull()?.let { stringAnnotation ->
                    uriHandler.openUri(stringAnnotation.item)
                }
        },
    )
}

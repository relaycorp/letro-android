package tech.relaycorp.letro.ui.common.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit

@Composable
fun BoldText(
    fullText: String,
    boldParts: List<String>,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: TextUnit = TextUnit.Unspecified,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    textAlign: TextAlign? = null,
) {
    val annotatedString = buildAnnotatedString {
        append(fullText)
        addStyle(
            style = SpanStyle(
                fontSize = fontSize,
                color = textColor,
            ),
            start = 0,
            end = fullText.length,
        )
        for (part in boldParts) {
            val startIndex = fullText.indexOf(part)
            val endIndex = startIndex + part.length
            addStyle(
                style = SpanStyle(
                    color = textColor,
                    fontSize = fontSize,
                    fontWeight = FontWeight.SemiBold,
                ),
                start = startIndex,
                end = endIndex,
            )
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        style = textStyle,
        textAlign = textAlign,
    )
}

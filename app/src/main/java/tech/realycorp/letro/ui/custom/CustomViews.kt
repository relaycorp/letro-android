package tech.realycorp.letro.ui.custom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import tech.realycorp.letro.R
import tech.realycorp.letro.ui.theme.ItemPadding
import tech.realycorp.letro.ui.theme.LetroTheme
import tech.realycorp.letro.ui.theme.PrimaryMain
import tech.realycorp.letro.ui.theme.TextFieldCornerRadius

@Composable
fun LetroButton(
    text: String,
    modifier: Modifier = Modifier,
    buttonType: ButtonType = ButtonType.Filled,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier.fillMaxWidth(),
        shape = CircleShape,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = when (buttonType) {
                ButtonType.Filled -> PrimaryMain
                ButtonType.Outlined -> Color.Transparent
            },
            contentColor = when (buttonType) {
                ButtonType.Filled -> Color.White
                ButtonType.Outlined -> PrimaryMain
            },
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.White,
        ),
        border = if (buttonType == ButtonType.Outlined) {
            BorderStroke(
                color = PrimaryMain,
                width = 1.dp,
            )
        } else {
            null
        },
        onClick = onClick,
    ) {
        Text(
            text = text,
        )
    }
}

sealed interface ButtonType {
    object Filled : ButtonType
    object Outlined : ButtonType
}

@Composable
fun LetroTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeHolderText: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false,
    maxLines: Int = 1,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        shape = RoundedCornerShape(TextFieldCornerRadius),
        textStyle = MaterialTheme.typography.bodyLarge,
        placeholder = {
            Text(
                text = placeHolderText,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        keyboardOptions = keyboardOptions,
        maxLines = maxLines,
        singleLine = singleLine,
        isError = isError,
    )
}

@Composable
fun HyperlinkText(
    fullText: String,
    hyperLinks: Map<String, String>,
    modifier: Modifier = Modifier,
    linkTextColor: Color = PrimaryMain,
    linkTextFontWeight: FontWeight = FontWeight.Normal,
    linkTextDecoration: TextDecoration = TextDecoration.Underline,
    fontSize: TextUnit = TextUnit.Unspecified,
) {
    val annotatedString = buildAnnotatedString {
        append(fullText)

        for ((key, value) in hyperLinks) {
            val startIndex = fullText.indexOf(key)
            val endIndex = startIndex + key.length
            addStyle(
                style = SpanStyle(
                    color = linkTextColor,
                    fontSize = fontSize,
                    fontWeight = linkTextFontWeight,
                    textDecoration = linkTextDecoration,
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
        addStyle(
            style = SpanStyle(
                fontSize = fontSize,
            ),
            start = 0,
            end = fullText.length,
        )
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

@Preview
@Composable
fun CustomViewsPreview() {
    LetroTheme {
        Column {
            LetroButton(text = "Filled Button") {}
            Spacer(modifier = Modifier.height(ItemPadding))
            LetroButton(
                text = "Outlined Button",
                buttonType = ButtonType.Outlined,
            ) {}
            Spacer(modifier = Modifier.height(ItemPadding))
            LetroButton(
                text = "Disabled Button",
                enabled = false,
            ) {}
            Spacer(modifier = Modifier.height(ItemPadding))

            LetroTextField(value = "some value", onValueChange = {})
            Spacer(modifier = Modifier.height(ItemPadding))
            LetroTextField(value = "", onValueChange = {})
            Spacer(modifier = Modifier.height(ItemPadding))
            LetroTextField(value = "", onValueChange = {}, isError = true)
            Spacer(modifier = Modifier.height(ItemPadding))

            HyperlinkText(
                fullText = stringResource(id = R.string.onboarding_create_account_terms_and_services),
                hyperLinks = mapOf(
                    stringResource(id = R.string.onboarding_create_account_terms_and_services_link_text)
                        to "https://letro.app/en/terms",
                ),
            )
            Spacer(modifier = Modifier.height(ItemPadding))
        }
    }
}

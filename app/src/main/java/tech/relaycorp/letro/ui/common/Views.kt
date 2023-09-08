package tech.relaycorp.letro.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import tech.relaycorp.letro.ui.theme.LetroColor
import tech.relaycorp.letro.R
import tech.relaycorp.letro.ui.theme.LetroTheme

@Composable
fun LetroButton(
    text: String,
    modifier: Modifier = Modifier,
    buttonType: ButtonType = ButtonType.Filled,
    enabled: Boolean = true,
    leadingIconResId: Int? = null,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        shape = CircleShape,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = when (buttonType) {
                ButtonType.Filled -> MaterialTheme.colorScheme.primary
                ButtonType.Outlined -> MaterialTheme.colorScheme.surface
            },
            contentColor = when (buttonType) {
                ButtonType.Filled -> MaterialTheme.colorScheme.onPrimary
                ButtonType.Outlined -> MaterialTheme.colorScheme.primary
            },
            disabledContainerColor = LetroColor.disabledButtonBackgroundColor(),
            disabledContentColor = LetroColor.disabledButtonTextColor(),
        ),
        border = if (buttonType == ButtonType.Outlined) {
            BorderStroke(
                color = MaterialTheme.colorScheme.primary,
                width = 1.dp,
            )
        } else {
            null
        },
        contentPadding = PaddingValues(
            vertical = 14.dp
        ),
        onClick = onClick,
    ) {
        if (leadingIconResId != null) {
            Icon(
                painter = painterResource(id = leadingIconResId),
                contentDescription = null,
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
fun LetroButtonMaxWidthFilled(
    text: String,
    modifier: Modifier = Modifier,
    buttonType: ButtonType = ButtonType.Filled,
    isEnabled: Boolean = true,
    leadingIconResId: Int? = null,
    onClick: () -> Unit,
) {
    LetroButton(
        text = text,
        modifier = modifier
            .fillMaxWidth(),
        buttonType = buttonType,
        enabled = isEnabled,
        leadingIconResId = leadingIconResId,
        onClick = onClick,
    )
}

sealed interface ButtonType {
    object Filled : ButtonType
    object Outlined : ButtonType
}

@Composable
fun LetroTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeHolderText: String = "",
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    TextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        textStyle = MaterialTheme.typography.bodyLarge,
        enabled = enabled,
        singleLine = singleLine,
        placeholder = {
            Text(
                text = placeHolderText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        keyboardOptions = keyboardOptions,
    )
}

@Composable
fun LetroOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeHolderText: String = "",
    suffixText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false,
    maxLines: Int = 1,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        shape = RoundedCornerShape(4.dp),
        textStyle = MaterialTheme.typography.bodyLarge,
        placeholder = {
            Text(
                text = placeHolderText,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        suffix = {
            if (suffixText != null) {
                Text(
                    suffixText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
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
    linkTextColor: Color = MaterialTheme.colorScheme.primary,
    linkTextFontWeight: FontWeight = FontWeight.Normal,
    linkTextDecoration: TextDecoration = TextDecoration.None,
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

@Preview(showBackground = true)
@Composable
fun CustomViewsPreview() {
    LetroTheme {
        Column {
            LetroButtonMaxWidthFilled(text = "Filled Button") {}
            Spacer(modifier = Modifier.height(ItemPadding))
            LetroButtonMaxWidthFilled(
                text = "Outlined Button",
                buttonType = ButtonType.Outlined,
            ) {}
            Spacer(modifier = Modifier.height(ItemPadding))
            LetroButtonMaxWidthFilled(
                text = "Disabled Button",
                isEnabled = false,
            ) {}
            Spacer(modifier = Modifier.height(ItemPadding))

            LetroOutlinedTextField(value = "some value", onValueChange = {})
            Spacer(modifier = Modifier.height(ItemPadding))
            LetroOutlinedTextField(value = "marian", onValueChange = {}, suffixText = "@guarapo.cafe")
            Spacer(modifier = Modifier.height(ItemPadding))
            LetroOutlinedTextField(value = "", onValueChange = {})
            Spacer(modifier = Modifier.height(ItemPadding))
            LetroOutlinedTextField(value = "", onValueChange = {}, isError = true)
            Spacer(modifier = Modifier.height(ItemPadding))

            LetroTextField(value = "sender", onValueChange = {}, placeHolderText = "")
            Spacer(modifier = Modifier.height(ItemPadding))
            LetroTextField(value = "", onValueChange = {}, placeHolderText = "placeholder")
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

private val ItemPadding = 8.dp
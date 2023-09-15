package tech.relaycorp.letro.ui.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun LetroTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeHolderText: String = "",
    enabled: Boolean = true,
    singleLine: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    placeholderColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    TextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle,
        enabled = enabled,
        singleLine = singleLine,
        placeholder = {
            Text(
                text = placeHolderText,
                style = MaterialTheme.typography.bodyLarge,
                color = placeholderColor,
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
fun LetroTextField(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeHolderText: String = "",
    enabled: Boolean = true,
    singleLine: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    placeholderColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    TextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle,
        enabled = enabled,
        singleLine = singleLine,
        placeholder = {
            Text(
                text = placeHolderText,
                style = MaterialTheme.typography.bodyLarge,
                color = placeholderColor,
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
    @StringRes label: Int? = null,
    hintText: String = "",
    suffixText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false,
    maxLines: Int = 1,
    singleLine: Boolean = true,
    isEnabled: Boolean = true,
    content: (@Composable () -> Unit)? = null,
) {
    Column {
        if (label != null) {
            Column {
                Text(
                    text = stringResource(id = label),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(
                    modifier = Modifier.height(8.dp),
                )
            }
        }
        OutlinedTextField(
            modifier = modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            shape = RoundedCornerShape(4.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            placeholder = {
                Text(
                    text = hintText,
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
            enabled = isEnabled,
        )
        if (content != null) {
            content()
        }
    }
}

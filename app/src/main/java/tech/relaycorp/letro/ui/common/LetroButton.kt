package tech.relaycorp.letro.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import tech.relaycorp.letro.ui.theme.LabelLargeProminent
import tech.relaycorp.letro.ui.theme.LetroColor

@Composable
fun LetroButton(
    text: String,
    modifier: Modifier = Modifier,
    buttonType: ButtonType = ButtonType.Filled,
    enabled: Boolean = true,
    leadingIconResId: Int? = null,
    contentPadding: PaddingValues = PaddingValues(
        vertical = 14.dp,
    ),
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
        contentPadding = contentPadding,
        onClick = onClick,
    ) {
        val contentColor = if (enabled) MaterialTheme.colorScheme.onPrimary else LetroColor.disabledButtonTextColor()
        if (leadingIconResId != null) {
            Icon(
                painter = painterResource(id = leadingIconResId),
                tint = contentColor,
                contentDescription = null,
            )
            Spacer(
                modifier = Modifier.width(8.dp),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.LabelLargeProminent,
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

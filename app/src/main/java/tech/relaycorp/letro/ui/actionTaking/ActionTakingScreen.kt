package tech.relaycorp.letro.ui.actionTaking

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.relaycorp.letro.ui.common.ButtonType
import tech.relaycorp.letro.ui.common.LetroButtonMaxWidthFilled
import tech.relaycorp.letro.ui.common.text.BoldText
import tech.relaycorp.letro.ui.theme.HorizontalScreenPadding
import tech.relaycorp.letro.ui.theme.LetroTheme

@Composable
fun ActionTakingScreen(
    actionTakingScreenUIStateModel: ActionTakingScreenUIStateModel,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = HorizontalScreenPadding,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = actionTakingScreenUIStateModel.image),
            contentDescription = null,
        )
        if (actionTakingScreenUIStateModel.titleStringRes != null) {
            Spacer(
                modifier = Modifier.height(24.dp),
            )
            Text(
                text = stringResource(id = actionTakingScreenUIStateModel.titleStringRes),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
        if (actionTakingScreenUIStateModel.firstMessageStringRes != null) {
            Spacer(
                modifier = Modifier.height(24.dp),
            )
            val boldPartOfMessage = actionTakingScreenUIStateModel.boldPartOfMessageInFirstMessage
            if (boldPartOfMessage == null) {
                Text(
                    text = stringResource(id = actionTakingScreenUIStateModel.firstMessageStringRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
            } else {
                BoldText(
                    fullText = stringResource(
                        id = actionTakingScreenUIStateModel.firstMessageStringRes,
                        boldPartOfMessage,
                    ),
                    boldParts = listOf(boldPartOfMessage),
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (actionTakingScreenUIStateModel.secondMessageStringRes != null) {
            Spacer(
                modifier = Modifier.height(24.dp),
            )
            Text(
                text = stringResource(id = actionTakingScreenUIStateModel.secondMessageStringRes),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
        if (actionTakingScreenUIStateModel.buttonFilledStringRes != null) {
            Spacer(
                modifier = Modifier.height(24.dp),
            )
            LetroButtonMaxWidthFilled(
                text = stringResource(id = actionTakingScreenUIStateModel.buttonFilledStringRes),
                onClick = actionTakingScreenUIStateModel.onButtonFilledClicked,
            )
        }
        if (actionTakingScreenUIStateModel.buttonOutlinedStringRes != null) {
            Spacer(
                modifier = Modifier.height(24.dp),
            )
            LetroButtonMaxWidthFilled(
                buttonType = ButtonType.Outlined,
                text = stringResource(id = actionTakingScreenUIStateModel.buttonOutlinedStringRes),
                onClick = actionTakingScreenUIStateModel.onButtonOutlinedClicked,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WaitingScreenPreview() {
    LetroTheme {
        ActionTakingScreen(ActionTakingScreenUIStateModel.RegistrationWaiting)
    }
}

@Preview(showBackground = true)
@Composable
private fun PairingRequestSentScreenPreview() {
    LetroTheme {
        ActionTakingScreen(ActionTakingScreenUIStateModel.PairingRequestSent {})
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountCreationFailedPreview() {
    LetroTheme {
        ActionTakingScreen(ActionTakingScreenUIStateModel.AccountCreationFailed("domain"))
    }
}

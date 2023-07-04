package tech.relaycorp.letro.ui.onboarding.actionTaking

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.relaycorp.letro.ui.custom.ButtonType
import tech.relaycorp.letro.ui.custom.LetroButton
import tech.relaycorp.letro.ui.theme.HorizontalScreenPadding
import tech.relaycorp.letro.ui.theme.ItemPadding
import tech.relaycorp.letro.ui.theme.LetroTheme
import tech.relaycorp.letro.ui.theme.VerticalScreenPadding

@Composable
fun ActionTakingScreen(
    actionTakingScreenUIStateModel: ActionTakingScreenUIStateModel,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = HorizontalScreenPadding,
                vertical = VerticalScreenPadding,
            ),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(150.dp),
        )
        Spacer(modifier = Modifier.height(VerticalScreenPadding))
        Text(
            text = stringResource(id = actionTakingScreenUIStateModel.titleStringRes),
            style = MaterialTheme.typography.headlineSmall,
        )
        if (actionTakingScreenUIStateModel.messageStringRes != null) {
            Spacer(modifier = Modifier.height(ItemPadding))
            Text(
                text = stringResource(id = actionTakingScreenUIStateModel.messageStringRes),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        if (actionTakingScreenUIStateModel.buttonFilledStringRes != null) {
            LetroButton(
                text = stringResource(id = actionTakingScreenUIStateModel.buttonFilledStringRes),
                onClick = actionTakingScreenUIStateModel.buttonFilledOnClicked,
            )
        }
        if (actionTakingScreenUIStateModel.buttonOutlinedStringRes != null) {
            Spacer(modifier = Modifier.height(VerticalScreenPadding))
            LetroButton(
                buttonType = ButtonType.Outlined,
                text = stringResource(id = actionTakingScreenUIStateModel.buttonOutlinedStringRes),
                onClick = actionTakingScreenUIStateModel.buttonOutlinedOnClicked,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WaitingScreenPreview() {
    LetroTheme {
        ActionTakingScreen(ActionTakingScreenUIStateModel.Waiting)
    }
}

@Preview(showBackground = true)
@Composable
fun AccountConfirmationScreenPreview() {
    LetroTheme {
        ActionTakingScreen(ActionTakingScreenUIStateModel.AccountConfirmation({}, {}))
    }
}

@Preview(showBackground = true)
@Composable
fun PairingRequestSentScreenPreview() {
    LetroTheme {
        ActionTakingScreen(ActionTakingScreenUIStateModel.PairingRequestSent {})
    }
}
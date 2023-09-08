package tech.relaycorp.letro.onboarding.actionTaking

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.relaycorp.letro.ui.common.ButtonType
import tech.relaycorp.letro.ui.common.LetroButtonMaxWidthFilled
import tech.relaycorp.letro.ui.theme.HorizontalScreenPadding
import tech.relaycorp.letro.ui.theme.LetroTheme

@Composable
fun ActionTakingScreen(
    actionTakingScreenUIStateModel: ActionTakingScreenUIStateModel,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(
                horizontal = HorizontalScreenPadding,
                vertical = 72.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = actionTakingScreenUIStateModel.image),
            contentDescription = null,
        )
        if (actionTakingScreenUIStateModel.titleStringRes != null) {
            Spacer(
                modifier = Modifier.height(24.dp)
            )
            Text(
                text = stringResource(id = actionTakingScreenUIStateModel.titleStringRes),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        if (actionTakingScreenUIStateModel.messageStringRes != null) {
            Spacer(
                modifier = Modifier.height(24.dp)
            )
            Text(
                text = stringResource(id = actionTakingScreenUIStateModel.messageStringRes),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        if (actionTakingScreenUIStateModel.buttonFilledStringRes != null) {
            Spacer(
                modifier = Modifier.height(24.dp)
            )
            LetroButtonMaxWidthFilled(
                text = stringResource(id = actionTakingScreenUIStateModel.buttonFilledStringRes),
                onClick = actionTakingScreenUIStateModel.onButtonFilledClicked,
            )
        }
        if (actionTakingScreenUIStateModel.buttonOutlinedStringRes != null) {
            Spacer(
                modifier = Modifier.height(24.dp)
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
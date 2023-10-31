package tech.relaycorp.letro.contacts.ui

import androidx.compose.runtime.Composable
import tech.relaycorp.letro.R
import tech.relaycorp.letro.ui.actionTaking.ActionTakingScreen
import tech.relaycorp.letro.ui.actionTaking.ActionTakingScreenUIStateModel

@Composable
fun WelcomeToLetroScreen(
    withConfettiAnimation: Boolean,
    onPairWithOthersClick: () -> Unit,
    onShareIdClick: () -> Unit,
) {
    ActionTakingScreen(
        model = ActionTakingScreenUIStateModel.NoContacts(
            title = R.string.onboarding_account_confirmation,
            image = R.drawable.account_created,
            onPairWithOthersClick = onPairWithOthersClick,
            onShareIdClick = onShareIdClick,
            isCenteredVertically = false,
            fullScreenAnimation = if (withConfettiAnimation) R.raw.confetti else null,
        ),
    )
}

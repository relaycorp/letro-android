package tech.relaycorp.letro.ui.onboarding.actionTaking

import androidx.annotation.StringRes
import tech.relaycorp.letro.R

sealed class ActionTakingScreenUIStateModel(
    @StringRes val titleStringRes: Int,
    @StringRes val messageStringRes: Int? = null,
    @StringRes val buttonFilledStringRes: Int? = null,
    @StringRes val buttonOutlinedStringRes: Int? = null,
    val buttonFilledOnClicked: () -> Unit = {},
    val buttonOutlinedOnClicked: () -> Unit = {},
) {
    object Waiting : ActionTakingScreenUIStateModel(
        titleStringRes = R.string.onboarding_waiting_title,
        messageStringRes = R.string.onboarding_waiting_message,
    )

    class AccountConfirmation(
        onPairWithPeople: () -> Unit,
        onShareId: () -> Unit,
    ) : ActionTakingScreenUIStateModel(
        titleStringRes = R.string.onboarding_account_confirmation,
        buttonFilledStringRes = R.string.general_pair_with_others,
        buttonOutlinedStringRes = R.string.onboarding_account_confirmation_share_your_id,
        buttonFilledOnClicked = onPairWithPeople,
        buttonOutlinedOnClicked = onShareId,
    )

    class PairingRequestSent(
        onGotItClicked: () -> Unit,
    ) : ActionTakingScreenUIStateModel(
        titleStringRes = R.string.onboarding_pairing_request_sent_title,
        messageStringRes = R.string.onboarding_pairing_request_sent_message,
        buttonFilledStringRes = R.string.onboarding_pairing_request_sent_button,
        buttonFilledOnClicked = onGotItClicked,
    )
}

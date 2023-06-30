package tech.realycorp.letro.ui.onboarding.actionTaking

import androidx.annotation.StringRes
import tech.realycorp.letro.R

sealed class ActionTakingScreenUIStateModel(
    @StringRes val titleStringRes: Int,
    @StringRes val messageStringRes: Int? = null,
    @StringRes val buttonFilledStringRes: Int? = null,
    @StringRes val buttonOutlinedStringRes: Int? = null,
    val buttonFilledOnClick: () -> Unit = {},
    val buttonOutlinedOnClick: () -> Unit = {},
) {
    object Waiting : ActionTakingScreenUIStateModel(
        titleStringRes = R.string.onboarding_waiting_title,
        messageStringRes = R.string.onboarding_waiting_message,
    )
    object AccountConfirmation : ActionTakingScreenUIStateModel(
        titleStringRes = R.string.onboarding_account_confirmation,
        buttonFilledStringRes = R.string.onboarding_account_confirmation_pair_with_others,
        buttonOutlinedStringRes = R.string.onboarding_account_confirmation_share_your_id,
    )
    object PairingRequestSent : ActionTakingScreenUIStateModel(
        titleStringRes = R.string.onboarding_pairing_request_sent_title,
        messageStringRes = R.string.onboarding_pairing_request_sent_message,
        buttonFilledStringRes = R.string.onboarding_pairing_request_sent_button,
    )
}

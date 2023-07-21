package tech.relaycorp.letro.ui.onboarding.actionTaking

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tech.relaycorp.letro.R

sealed class ActionTakingScreenUIStateModel(
    @StringRes val titleStringRes: Int,
    @StringRes val messageStringRes: Int? = null,
    @StringRes val buttonFilledStringRes: Int? = null,
    @StringRes val buttonOutlinedStringRes: Int? = null,
    @DrawableRes val imageRes: Int,
    val onButtonFilledClicked: () -> Unit = {},
    val onButtonOutlinedClicked: () -> Unit = {},
) {

    class AccountConfirmation(
        onPairWithPeople: () -> Unit,
        onShareId: () -> Unit,
    ) : ActionTakingScreenUIStateModel(
        titleStringRes = R.string.onboarding_account_confirmation,
        buttonFilledStringRes = R.string.general_pair_with_others,
        buttonOutlinedStringRes = R.string.onboarding_account_confirmation_share_your_id,
        onButtonFilledClicked = onPairWithPeople,
        onButtonOutlinedClicked = onShareId,
        imageRes = R.drawable.image_all_setup,
    )

    object Loading : ActionTakingScreenUIStateModel(
        titleStringRes = R.string.onboarding_waiting_title,
        messageStringRes = R.string.onboarding_waiting_message,
        imageRes = R.drawable.image_hold_tight,
    )

    class PairingRequestSent(
        onGotItClicked: () -> Unit,
    ) : ActionTakingScreenUIStateModel(
        titleStringRes = R.string.onboarding_pairing_request_sent_title,
        messageStringRes = R.string.onboarding_pairing_request_sent_message,
        buttonFilledStringRes = R.string.onboarding_pairing_request_sent_button,
        onButtonFilledClicked = onGotItClicked,
        imageRes = R.drawable.image_paring_request_sent,
    )
}

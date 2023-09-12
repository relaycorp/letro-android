package tech.relaycorp.letro.onboarding.actionTaking

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tech.relaycorp.letro.R

sealed class ActionTakingScreenUIStateModel(
    @StringRes val titleStringRes: Int?,
    @DrawableRes val image: Int,
    @StringRes val messageStringRes: Int? = null,
    @StringRes val buttonFilledStringRes: Int? = null,
    @StringRes val buttonOutlinedStringRes: Int? = null,
    val onButtonFilledClicked: () -> Unit = {},
    val onButtonOutlinedClicked: () -> Unit = {},
) {

    class NoContacts(
        @DrawableRes image: Int,
        onPairWithOthersClick: () -> Unit,
        onShareId: () -> Unit,
        @StringRes title: Int? = null,
        @StringRes message: Int? = null,
    ) : ActionTakingScreenUIStateModel(
        titleStringRes = title,
        image = image,
        messageStringRes = message,
        buttonFilledStringRes = R.string.general_pair_with_others,
        buttonOutlinedStringRes = R.string.onboarding_account_confirmation_share_your_id,
        onButtonFilledClicked = onPairWithOthersClick,
        onButtonOutlinedClicked = onShareId,
    )

    object RegistrationWaiting : ActionTakingScreenUIStateModel(
        titleStringRes = R.string.onboarding_waiting_title,
        image = R.drawable.waiting_for_account_creation,
        messageStringRes = R.string.onboarding_waiting_message,
    )

    class PairingRequestSent(
        onGotItClicked: () -> Unit,
    ) : ActionTakingScreenUIStateModel(
        titleStringRes = R.string.onboarding_pairing_request_sent_title,
        image = R.drawable.pairing_request_sent,
        messageStringRes = R.string.onboarding_pairing_request_sent_message,
        buttonFilledStringRes = R.string.onboarding_pairing_request_sent_button,
        onButtonFilledClicked = onGotItClicked,
    )
}

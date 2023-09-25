package tech.relaycorp.letro.onboarding.actionTaking

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tech.relaycorp.letro.R

sealed class ActionTakingScreenUIStateModel(
    @StringRes val titleStringRes: Int?,
    @DrawableRes val image: Int,
    @StringRes val messageStringRes: Int? = null,
    val boldPartOfMessage: String? = null,
    @StringRes val buttonFilledStringRes: Int? = null,
    @StringRes val buttonOutlinedStringRes: Int? = null,
    val onButtonFilledClicked: () -> Unit = {},
    val onButtonOutlinedClicked: () -> Unit = {},
) {

    class NoContacts(
        @DrawableRes image: Int,
        onPairWithOthersClick: () -> Unit,
        onShareIdClick: () -> Unit,
        @StringRes title: Int? = null,
        @StringRes message: Int? = null,
    ) : ActionTakingScreenUIStateModel(
        titleStringRes = title,
        image = image,
        messageStringRes = message,
        buttonFilledStringRes = R.string.general_pair_with_others,
        buttonOutlinedStringRes = R.string.onboarding_account_confirmation_share_your_id,
        onButtonFilledClicked = onPairWithOthersClick,
        onButtonOutlinedClicked = onShareIdClick,
    )

    object RegistrationWaiting : ActionTakingScreenUIStateModel(
        titleStringRes = R.string.onboarding_waiting_title,
        image = R.drawable.waiting_for_account_creation,
        messageStringRes = R.string.onboarding_waiting_message,
    )

    class PairingRequestSent(
        boldPartOfMessage: String? = null,
        onGotItClicked: () -> Unit,
    ) : ActionTakingScreenUIStateModel(
        titleStringRes = R.string.onboarding_pairing_request_sent_title,
        image = R.drawable.pairing_request_sent,
        messageStringRes = R.string.onboarding_pairing_request_sent_message,
        boldPartOfMessage = boldPartOfMessage,
        buttonFilledStringRes = R.string.onboarding_pairing_request_sent_button,
        onButtonFilledClicked = onGotItClicked,
    )

    class PairingRequestSentWithPermissionRequest(
        onRequestPermissionClick: () -> Unit,
        onSkipClicked: () -> Unit,
    ) : ActionTakingScreenUIStateModel(
        titleStringRes = R.string.onboarding_pairing_request_sent_title,
        image = R.drawable.pairing_request_sent,
        messageStringRes = R.string.we_need_your_permission,
        buttonFilledStringRes = R.string.grant_permission,
        buttonOutlinedStringRes = R.string.skip,
        onButtonFilledClicked = onRequestPermissionClick,
        onButtonOutlinedClicked = onSkipClicked,
    )
}
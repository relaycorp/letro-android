package tech.relaycorp.letro.ui.actionTaking

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import tech.relaycorp.letro.R

sealed class ActionTakingScreenUIStateModel(
    @StringRes val titleStringRes: Int?,
    val image: ActionTakingScreenImage,
    @RawRes val fullScreenAnimation: Int? = null,
    val isCenteredVertically: Boolean = true,
    @StringRes val firstMessageStringRes: Int? = null,
    val boldPartOfMessageInFirstMessage: String? = null,
    @StringRes val secondMessageStringRes: Int? = null,
    @StringRes val buttonFilledStringRes: Int? = null,
    @StringRes val buttonOutlinedStringRes: Int? = null,
    val onButtonFilledClicked: () -> Unit = {},
    val onButtonOutlinedClicked: () -> Unit = {},
) {

    class NoContacts(
        @DrawableRes image: Int,
        onPairWithOthersClick: () -> Unit,
        onShareIdClick: () -> Unit,
        fullScreenAnimation: Int? = null,
        isCenteredVertically: Boolean = true,
        @StringRes title: Int? = null,
        @StringRes message: Int? = null,
    ) : ActionTakingScreenUIStateModel(
        titleStringRes = title,
        image = ActionTakingScreenImage.Static(image),
        firstMessageStringRes = message,
        buttonFilledStringRes = R.string.general_pair_with_others,
        buttonOutlinedStringRes = R.string.onboarding_account_confirmation_share_your_id,
        onButtonFilledClicked = onPairWithOthersClick,
        onButtonOutlinedClicked = onShareIdClick,
        isCenteredVertically = isCenteredVertically,
        fullScreenAnimation = fullScreenAnimation,
    )

    object AccountCreation : ActionTakingScreenUIStateModel(
        titleStringRes = R.string.onboarding_waiting_title,
        image = ActionTakingScreenImage.Animated(
            animation = R.raw.account_waiting_animation,
            underlyingAnimation = R.raw.clouds,
        ),
        firstMessageStringRes = R.string.onboarding_waiting_message,
        isCenteredVertically = false,
    )

    object AccountLinking : ActionTakingScreenUIStateModel(
        titleStringRes = R.string.onboarding_waiting_title,
        image = ActionTakingScreenImage.Animated(
            animation = R.raw.account_waiting_animation,
            underlyingAnimation = R.raw.clouds,
        ),
        firstMessageStringRes = R.string.account_linking_message,
        isCenteredVertically = false,
    )

    class PairingRequestSent(
        boldPartOfMessage: String? = null,
        onGotItClicked: () -> Unit,
    ) : ActionTakingScreenUIStateModel(
        titleStringRes = R.string.onboarding_pairing_request_sent_title,
        image = ActionTakingScreenImage.Static(R.drawable.pairing_request_sent),
        firstMessageStringRes = R.string.onboarding_pairing_request_sent_message,
        boldPartOfMessageInFirstMessage = boldPartOfMessage,
        buttonFilledStringRes = R.string.onboarding_pairing_request_sent_button,
        onButtonFilledClicked = onGotItClicked,
    )

    class PairingRequestSentWithPermissionRequest(
        boldPartOfMessage: String? = null,
        onRequestPermissionClick: () -> Unit,
        onSkipClicked: () -> Unit,
    ) : ActionTakingScreenUIStateModel(
        titleStringRes = R.string.onboarding_pairing_request_sent_title,
        image = ActionTakingScreenImage.Static(R.drawable.pairing_request_sent),
        firstMessageStringRes = R.string.onboarding_pairing_request_sent_message,
        boldPartOfMessageInFirstMessage = boldPartOfMessage,
        secondMessageStringRes = R.string.we_need_your_permission,
        buttonFilledStringRes = R.string.grant_permission,
        buttonOutlinedStringRes = R.string.skip,
        onButtonFilledClicked = onRequestPermissionClick,
        onButtonOutlinedClicked = onSkipClicked,
    )

    class AccountCreationFailed(
        domain: String,
    ) : ActionTakingScreenUIStateModel(
        titleStringRes = R.string.we_could_not_claim_your_account,
        image = ActionTakingScreenImage.Static(R.drawable.awala_initialization_error),
        firstMessageStringRes = R.string.you_need_to_contact_your_it_team_at,
        boldPartOfMessageInFirstMessage = domain,
    )
}

sealed class ActionTakingScreenImage {

    data class Static(@DrawableRes val image: Int) : ActionTakingScreenImage()
    data class Animated(
        @RawRes val animation: Int,
        @RawRes val underlyingAnimation: Int,
    ) : ActionTakingScreenImage()
}

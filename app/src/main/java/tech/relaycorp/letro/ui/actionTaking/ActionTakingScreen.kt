package tech.relaycorp.letro.ui.actionTaking

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import tech.relaycorp.letro.ui.common.ButtonType
import tech.relaycorp.letro.ui.common.LetroButtonMaxWidthFilled
import tech.relaycorp.letro.ui.common.text.BoldText
import tech.relaycorp.letro.ui.theme.HorizontalScreenPadding
import tech.relaycorp.letro.ui.theme.LetroTheme

@Composable
fun ActionTakingScreen(
    model: ActionTakingScreenUIStateModel,
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        if (model.fullScreenAnimation != null) {
            val animationComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(model.fullScreenAnimation))
            LottieAnimation(
                modifier = Modifier.wrapContentSize(),
                alignment = Alignment.TopCenter,
                composition = animationComposition,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    vertical = if (model.isCenteredVertically) 0.dp else 40.dp,
                ),
            contentAlignment = if (model.isCenteredVertically) Alignment.Center else Alignment.TopCenter,
        ) {
            when (model.image) {
                is ActionTakingScreenImage.Static -> {
                    StaticContent(model = model, image = model.image.image)
                }
                is ActionTakingScreenImage.Animated -> {
                    AnimatedContent(
                        model = model,
                        animation = model.image.animation,
                        underlyingAnimation = model.image.underlyingAnimation,
                    )
                }
            }
        }
    }
}

@Composable
private fun StaticContent(
    model: ActionTakingScreenUIStateModel,
    @DrawableRes image: Int,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (model.isCenteredVertically) Arrangement.Center else Arrangement.Top,
    ) {
        Image(
            painter = painterResource(id = image),
            contentDescription = null,
        )
        CommonContentWithButtons(model = model)
    }
}

@Composable
private fun AnimatedContent(
    model: ActionTakingScreenUIStateModel,
    @RawRes animation: Int,
    @RawRes underlyingAnimation: Int,
) {
    val animationComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(animation))
    val animationProgress = animateLottieCompositionAsState(composition = animationComposition)

    val underlyingAnimationComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(underlyingAnimation))
    val underlyingAnimationProgress = animateLottieCompositionAsState(composition = animationComposition)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (model.isCenteredVertically) Arrangement.Center else Arrangement.Top,
    ) {
        if (animationProgress.progress > 0 && underlyingAnimationProgress.progress > 0) {
            Box(
                contentAlignment = Alignment.Center,
            ) {
                LottieAnimation(
                    modifier = Modifier.fillMaxWidth(),
                    composition = underlyingAnimationComposition,
                    iterations = LottieConstants.IterateForever,
                )
                LottieAnimation(
                    composition = animationComposition,
                    iterations = LottieConstants.IterateForever,
                )
            }
            CommonContentWithButtons(model = model)
        }
    }
}

@Composable
private fun CommonContentWithButtons(
    model: ActionTakingScreenUIStateModel,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(
            horizontal = HorizontalScreenPadding,
        ),
    ) {
        if (model.titleStringRes != null) {
            Spacer(
                modifier = Modifier.height(24.dp),
            )
            Text(
                text = stringResource(id = model.titleStringRes),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
        if (model.firstMessageStringRes != null) {
            Spacer(
                modifier = Modifier.height(24.dp),
            )
            val boldPartOfMessage = model.boldPartOfMessageInFirstMessage
            if (boldPartOfMessage == null) {
                Text(
                    text = stringResource(id = model.firstMessageStringRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
            } else {
                BoldText(
                    fullText = stringResource(
                        id = model.firstMessageStringRes,
                        boldPartOfMessage,
                    ),
                    boldParts = listOf(boldPartOfMessage),
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (model.secondMessageStringRes != null) {
            Spacer(
                modifier = Modifier.height(24.dp),
            )
            Text(
                text = stringResource(id = model.secondMessageStringRes),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
        if (model.buttonFilledStringRes != null) {
            Spacer(
                modifier = Modifier.height(24.dp),
            )
            LetroButtonMaxWidthFilled(
                text = stringResource(id = model.buttonFilledStringRes),
                onClick = model.onButtonFilledClicked,
            )
        }
        if (model.buttonOutlinedStringRes != null) {
            Spacer(
                modifier = Modifier.height(24.dp),
            )
            LetroButtonMaxWidthFilled(
                buttonType = ButtonType.Outlined,
                text = stringResource(id = model.buttonOutlinedStringRes),
                onClick = model.onButtonOutlinedClicked,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WaitingScreenPreview() {
    LetroTheme {
        ActionTakingScreen(ActionTakingScreenUIStateModel.AccountCreation)
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
        ActionTakingScreen(ActionTakingScreenUIStateModel.AccountLinkingFailed("domain"))
    }
}

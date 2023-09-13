package tech.relaycorp.letro.contacts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tech.relaycorp.letro.R
import tech.relaycorp.letro.home.HomeViewModel
import tech.relaycorp.letro.ui.common.LetroButton
import tech.relaycorp.letro.ui.theme.FloatingActionButtonPadding
import tech.relaycorp.letro.ui.theme.LetroColor

@Composable
fun ContactsScreenOverlayFloatingMenu(
    homeViewModel: HomeViewModel,
    onShareIdClick: () -> Unit,
    onPairWithOthersClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LetroColor.FoggingBackgroundColor),
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(FloatingActionButtonPadding),
        ) {
            LetroButton(
                text = stringResource(id = R.string.onboarding_account_confirmation_share_your_id),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 24.dp,
                ),
                leadingIconResId = R.drawable.ic_share,
                onClick = { onShareIdClick() },
            )
            Spacer(
                modifier = Modifier.height(16.dp),
            )
            LetroButton(
                text = stringResource(id = R.string.general_pair_with_others),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 24.dp,
                ),
                leadingIconResId = R.drawable.ic_pair,
                onClick = { onPairWithOthersClick() },
            )
            Spacer(
                modifier = Modifier.height(16.dp),
            )
            FloatingActionButton(
                onClick = { homeViewModel.onFloatingActionButtonClick() },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = stringResource(
                        id = R.string.floating_action_button_close_content_description,
                    ),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

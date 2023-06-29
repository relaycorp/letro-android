package tech.realycorp.letro.ui.onboarding.accountCreation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import tech.realycorp.letro.R
import tech.realycorp.letro.ui.custom.ButtonType
import tech.realycorp.letro.ui.custom.HyperlinkText
import tech.realycorp.letro.ui.custom.LetroButton
import tech.realycorp.letro.ui.custom.LetroTextField
import tech.realycorp.letro.ui.theme.BoxCornerRadius
import tech.realycorp.letro.ui.theme.HorizontalScreenPadding
import tech.realycorp.letro.ui.theme.ItemPadding
import tech.realycorp.letro.ui.theme.LargePadding
import tech.realycorp.letro.ui.theme.LetroTheme
import tech.realycorp.letro.ui.theme.VerticalScreenPadding

@Composable
fun AccountCreationScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = HorizontalScreenPadding,
                vertical = VerticalScreenPadding,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.letro_logo),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.height(LargePadding))
        Text(text = stringResource(id = R.string.onboarding_create_account_id))
        Spacer(modifier = Modifier.height(ItemPadding))
        LetroTextField(
            value = "",
            onValueChange = { /*TODO*/ },
            placeHolderText = stringResource(id = R.string.onboarding_create_account_id_placeholder),
        )
        // Make Text read a string resource with a link
        HyperlinkText(
            fullText = stringResource(id = R.string.onboarding_create_account_terms_and_services),
            hyperLinks = mapOf(
                stringResource(id = R.string.onboarding_create_account_terms_and_services_link_text)
                    to "https://letro.app/en/terms",
            ),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ItemPadding)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(BoxCornerRadius),
                ),
        ) {
            Image(
                painter = painterResource(id = R.drawable.info),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(ItemPadding))
            Text(text = stringResource(id = R.string.onboarding_create_account_no_internet_connection))
        }
        LetroButton(
            text = stringResource(id = R.string.onboarding_create_account_button),
            onClick = {
                // TODO
            },
        )
        Spacer(modifier = Modifier.height(LargePadding))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Divider(modifier = Modifier.fillMaxWidth())
            Text(
                text = stringResource(id = R.string.onboarding_create_account_or),
                modifier = Modifier
                    .padding(ItemPadding)
                    .background(MaterialTheme.colorScheme.surface),
            )
        }
        Spacer(modifier = Modifier.height(LargePadding))
        LetroButton(
            text = stringResource(id = R.string.onboarding_create_account_use_existing_account),
            buttonType = ButtonType.Outlined,
            onClick = {
                // TODO
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AccountCreationPreview() {
    LetroTheme {
        AccountCreationScreen()
    }
}

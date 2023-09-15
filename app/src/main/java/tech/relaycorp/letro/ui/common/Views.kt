package tech.relaycorp.letro.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.relaycorp.letro.R
import tech.relaycorp.letro.ui.common.text.HyperlinkText
import tech.relaycorp.letro.ui.theme.LetroTheme

@Preview(showBackground = true)
@Composable
fun CustomViewsPreview() {
    LetroTheme {
        Column {
            LetroButtonMaxWidthFilled(text = "Filled Button") {}
            Spacer(modifier = Modifier.height(ItemPadding))
            LetroButtonMaxWidthFilled(
                text = "Outlined Button",
                buttonType = ButtonType.Outlined,
            ) {}
            Spacer(modifier = Modifier.height(ItemPadding))
            LetroButtonMaxWidthFilled(
                text = "Disabled Button",
                isEnabled = false,
            ) {}
            Spacer(modifier = Modifier.height(ItemPadding))

            LetroOutlinedTextField(value = "some value", onValueChange = {})
            Spacer(modifier = Modifier.height(ItemPadding))
            LetroOutlinedTextField(value = "marian", onValueChange = {}, suffixText = "@guarapo.cafe")
            Spacer(modifier = Modifier.height(ItemPadding))
            LetroOutlinedTextField(value = "", onValueChange = {})
            Spacer(modifier = Modifier.height(ItemPadding))
            LetroOutlinedTextField(value = "", onValueChange = {}, isError = true)
            Spacer(modifier = Modifier.height(ItemPadding))

            LetroTextField(value = "sender", onValueChange = {}, placeHolderText = "")
            Spacer(modifier = Modifier.height(ItemPadding))
            LetroTextField(value = "", onValueChange = {}, placeHolderText = "placeholder")
            Spacer(modifier = Modifier.height(ItemPadding))

            HyperlinkText(
                fullText = stringResource(id = R.string.onboarding_create_account_terms_and_services),
                hyperLinks = mapOf(
                    stringResource(id = R.string.onboarding_create_account_terms_and_services_link_text)
                        to "https://letro.app/en/terms",
                ),
            )
            Spacer(modifier = Modifier.height(ItemPadding))
        }
    }
}

private val ItemPadding = 8.dp

package tech.relaycorp.letro.contacts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tech.relaycorp.letro.R
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.ui.theme.TitleMediumProminent
import tech.relaycorp.letro.utils.ext.applyIf

@Composable
fun ContactView(
    contact: Contact,
    onActionsButtonClick: (() -> Unit)? = null,
    onContactClick: (() -> Unit)? = null,
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .height(if (contact.alias != null) 64.dp else 56.dp)
            .applyIf(onContactClick != null) {
                clickable { onContactClick?.invoke() }
            }
            .padding(
                horizontal = 16.dp,
            )
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
        ) {
            if (contact.alias != null) {
                Text(
                    text = contact.alias,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.TitleMediumProminent,
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            Text(
                text = contact.contactVeraId,
                color = MaterialTheme.colorScheme.onSurface,
                style = if (contact.alias == null) MaterialTheme.typography.TitleMediumProminent else MaterialTheme.typography.bodyMedium,
            )
        }
        if (onActionsButtonClick != null) {
            Icon(
                painter = painterResource(id = R.drawable.ic_more),
                contentDescription = stringResource(id = R.string.icon_more_content_description),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable { onActionsButtonClick() },
            )
        }
    }
}

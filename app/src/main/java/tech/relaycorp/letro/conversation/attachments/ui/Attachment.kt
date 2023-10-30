package tech.relaycorp.letro.conversation.attachments.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.relaycorp.letro.R
import tech.relaycorp.letro.ui.theme.LetroColor
import tech.relaycorp.letro.utils.ext.applyIf
import java.util.UUID

data class AttachmentInfo(
    val fileId: UUID,
    val name: String,
    val size: String,
    @DrawableRes val icon: Int,
)

@Composable
fun Attachment(
    modifier: Modifier = Modifier,
    attachment: AttachmentInfo,
    onAttachmentClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(52.dp)
            .background(LetroColor.SurfaceContainer, RoundedCornerShape(6.dp))
            .applyIf(onAttachmentClick != null) {
                clickable { onAttachmentClick?.invoke() }
            }
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
    ) {
        Icon(
            painter = painterResource(id = attachment.icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .then(Modifier)
                .applyIf(onDeleteClick != null) {
                    weight(1f)
                },
        ) {
            Text(
                text = attachment.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = attachment.size,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }
        if (onDeleteClick != null) {
            Icon(
                painter = painterResource(id = R.drawable.ic_delete_attachment),
                contentDescription = stringResource(id = R.string.content_description_delete_attachment),
                modifier = Modifier.clickable { onDeleteClick() },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Attachment_Preview() {
    Column {
        Attachment(
            attachment = AttachmentInfo(
                name = "Short_name.pdf",
                size = "126 KB",
                icon = R.drawable.attachment_pdf,
                fileId = UUID.randomUUID(),
            ),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(fraction = 0.87F),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Attachment(
            attachment = AttachmentInfo(
                name = "Very long name of the file, that it can barely fit on the screen.img",
                size = "126 KB",
                icon = R.drawable.attachment_image,
                fileId = UUID.randomUUID(),
            ),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(fraction = 0.87F),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Attachment(
            attachment = AttachmentInfo(
                name = "Deleteable attachment with very long name of the file, that it can barely fit on the screen.img",
                size = "126 KB",
                icon = R.drawable.attachment_image,
                fileId = UUID.randomUUID(),
            ),
            onDeleteClick = {},
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(fraction = 0.87F),
        )
    }
}

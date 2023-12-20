package tech.relaycorp.letro.ui.common

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import tech.relaycorp.letro.R

@Composable
fun LetroAvatar(
    modifier: Modifier,
    filePath: String?,
) {
    if (filePath != null) {
        AsyncImage(
            model = filePath,
            contentDescription = stringResource(id = R.string.profile_photo),
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.default_profile_picture),
            contentDescription = stringResource(id = R.string.profile_photo),
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    }
}

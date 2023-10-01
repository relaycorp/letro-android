package tech.relaycorp.letro.utils.intent

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import tech.relaycorp.letro.R
import tech.relaycorp.letro.conversation.attachments.filepicker.model.File

fun Activity.goToNotificationSettings() {
    val intent = Intent()
    intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.putExtra("app_package", packageName)
    intent.putExtra("app_uid", applicationInfo.uid)
    intent.putExtra("android.provider.extra.APP_PACKAGE", packageName)
    startActivity(intent)
}

fun Activity.openLink(link: String) {
    try {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(link),
            ),
        )
    } catch (a: ActivityNotFoundException) {
        Toast
            .makeText(this, R.string.no_browser, Toast.LENGTH_SHORT)
            .show()
    }
}

fun Activity.shareText(text: String) {
    try {
        startActivity(
            Intent(
                Intent.ACTION_SEND,
            ).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            },
        )
    } catch (a: ActivityNotFoundException) {
        Toast
            .makeText(this, R.string.no_app_to_share, Toast.LENGTH_SHORT)
            .show()
    }
}

fun Activity.openFile(file: File.FileWithoutContent) {
    try {
        startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                setDataAndType(
                    FileProvider.getUriForFile(this@openFile, AUTHORITY, file.toFile()),
                    file.extension.mimeType,
                )
            },
        )
    } catch (e: ActivityNotFoundException) {
        Toast
            .makeText(this, R.string.no_app_to_open_file, Toast.LENGTH_SHORT)
            .show()
    }
}

private const val AUTHORITY = "tech.relaycorp.letro.provider"

package tech.relaycorp.letro.utils.shortcut

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import tech.relaycorp.letro.R
import tech.relaycorp.letro.contacts.model.Contact

fun Contact.toShortcutInfo(
    context: Context,
    icon: IconCompat,
    intentBuilder: (Contact) -> Intent,
): ShortcutInfoCompat {
    return ShortcutInfoCompat.Builder(context, id.toString())
        .setShortLabel(alias ?: contactVeraId)
        .setCategories(setOf(CATEGORY_SEND))
        .setIcon(icon)
        .setIntent(intentBuilder(this))
        .addCapabilityBinding(SEND_MESSAGE_CAPABILITY_BINDING)
        .build()
}

fun List<Contact>.toShortcutInfo(
    context: Context,
    intentBuilder: (Contact) -> Intent,
): List<ShortcutInfoCompat> {
    val bitmap = ContextCompat.getDrawable(context, R.drawable.share_direct_target)?.toBitmap() ?: return emptyList()
    val icon = IconCompat.createWithAdaptiveBitmap(bitmap)
    return arrayListOf<ShortcutInfoCompat>().apply {
        this@toShortcutInfo.forEach { contact ->
            add(contact.toShortcutInfo(context, icon, intentBuilder))
        }
    }
}

private const val SEND_MESSAGE_CAPABILITY_BINDING = "actions.intent.SEND_MESSAGE"
private const val CATEGORY_SEND = "tech.relaycorp.letro.CATEGORY_SEND"
